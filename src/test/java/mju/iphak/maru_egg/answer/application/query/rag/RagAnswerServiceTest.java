package mju.iphak.maru_egg.answer.application.query.rag;

import static org.assertj.core.api.Assertions.*;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.web.reactive.function.client.WebClient;

import com.google.gson.Gson;

import mju.iphak.maru_egg.admission.domain.AdmissionCategory;
import mju.iphak.maru_egg.admission.domain.AdmissionType;
import mju.iphak.maru_egg.answer.domain.Answer;
import mju.iphak.maru_egg.answer.api.dto.request.LLMAskQuestionRequest;
import mju.iphak.maru_egg.answer.api.dto.response.LLMAnswerResponse;
import mju.iphak.maru_egg.common.MockTest;
import mju.iphak.maru_egg.question.domain.Question;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;

class RagAnswerServiceTest extends MockTest {

	@InjectMocks
	private RagAnswerService ragAnswerService;

	@Value("${LLM_BASE_URL}")
	private String llmBaseUrl;

	private MockWebServer mockWebServer;
	private WebClient webClient;
	private Question question;
	private Answer answer;

	@BeforeEach
	void setUp() throws Exception {
		this.mockWebServer = new MockWebServer();
		this.mockWebServer.start();

		webClient = WebClient.builder()
			.baseUrl(this.mockWebServer.url("/").toString())
			.clientConnector(new ReactorClientHttpConnector())
			.build();

		ragAnswerService = new RagAnswerService(webClient);
		question = createTestQuestion();
		answer = createTestAnswer(question);
	}

	@AfterEach
	void tearDown() throws Exception {
		if (mockWebServer != null) {
			mockWebServer.shutdown();
		}
	}

	@DisplayName("[성공] LLM 서버에 질문 요청 성공")
	@Test
	void LLM_질문_요청_성공() {
		// given
		LLMAskQuestionRequest request = LLMAskQuestionRequest.of(
			AdmissionType.SUSI.getType(),
			AdmissionCategory.ADMISSION_GUIDELINE.getCategory(),
			question.getContent()
		);

		LLMAnswerResponse expectedResponse = createExpectedResponse();
		mockWebServer.enqueue(new MockResponse()
			.setResponseCode(200)
			.setHeader("Content-Type", MediaType.APPLICATION_JSON_VALUE)
			.setBody(new Gson().toJson(expectedResponse)));

		// when
		LLMAnswerResponse result = ragAnswerService.invoke(request).block();

		// then
		assertThat(result).isNotNull();
		assertThat(result.questionType()).isEqualTo(expectedResponse.questionType());
		assertThat(result.questionCategory()).isEqualTo(expectedResponse.questionCategory());
		assertThat(result.answer()).isEqualTo(expectedResponse.answer());
	}

	private Question createTestQuestion() {
		return Question.of(
			"수시 일정 알려주세요.",
			"수시 일정",
			AdmissionType.SUSI,
			AdmissionCategory.ADMISSION_GUIDELINE
		);
	}

	private Answer createTestAnswer(Question question) {
		return Answer.of(
			question,
			"수시 일정은 2024년 12월 19일(목)부터 2024년 12월 26일(목) 18:00까지 최초합격자 발표가 있고, "
				+ "2025년 2월 10일(월) 10:00부터 2025년 2월 12일(수) 15:00까지 문서등록 및 등록금 납부가 진행됩니다. "
				+ "등록금 납부 기간은 2024년 12월 16일(월) 10:00부터 2024년 12월 18일(수) 15:00까지이며, "
				+ "방법은 입학처 홈페이지를 통한 문서등록 및 등록금 납부를 하시면 됩니다. 상세 안내는 추후 입학처 홈페이지를 통해 공지될 예정입니다."
		);
	}

	private LLMAnswerResponse createExpectedResponse() {
		return LLMAnswerResponse.of(
			AdmissionType.SUSI.getType(),
			AdmissionCategory.ADMISSION_GUIDELINE.getCategory(),
			answer,
			null
		);
	}
}
