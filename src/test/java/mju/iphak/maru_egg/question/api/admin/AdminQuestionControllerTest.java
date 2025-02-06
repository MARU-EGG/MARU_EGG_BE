package mju.iphak.maru_egg.question.api.admin;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.ResultMatcher;

import mju.iphak.maru_egg.admission.domain.AdmissionCategory;
import mju.iphak.maru_egg.admission.domain.AdmissionType;
import mju.iphak.maru_egg.answer.domain.Answer;
import mju.iphak.maru_egg.answer.api.dto.request.CreateAnswerRequest;
import mju.iphak.maru_egg.answer.repository.AnswerRepository;
import mju.iphak.maru_egg.common.IntegrationTest;
import mju.iphak.maru_egg.question.domain.Question;
import mju.iphak.maru_egg.question.api.dto.request.CheckQuestionRequest;
import mju.iphak.maru_egg.question.api.dto.request.CreateQuestionRequest;
import mju.iphak.maru_egg.question.api.dto.request.UpdateQuestionContentRequest;
import mju.iphak.maru_egg.question.repository.QuestionRepository;

@WithMockUser(roles = "ADMIN")
class AdminQuestionControllerTest extends IntegrationTest {

	@Autowired
	private QuestionRepository questionRepository;

	@Autowired
	private AnswerRepository answerRepository;

	@BeforeEach
	void setUp() {
		initializeTestData();
	}

	@DisplayName("[성공] 질문 체크 요청")
	@Test
	void 질문_체크_성공() throws Exception {
		// given
		Long questionId = getFirstQuestionId();
		CheckQuestionRequest request = new CheckQuestionRequest(questionId);

		// when
		ResultActions resultActions = CheckQuestion(request);

		// then
		resultActions.andExpect(status().isOk());
	}

	@DisplayName("[실패] 질문 체크 요청 - 잘못된 JSON 형식")
	@Test
	void 질문_체크_실패_잘못된_JSON_형식() throws Exception {
		// given
		String invalidJson = "{\"questionId\": \"잘못된 아이디 형식\", \"check\": true}";

		// when
		ResultActions resultActions = CheckQuestion(invalidJson);

		// then
		resultActions.andExpect(status().isBadRequest());
	}

	@DisplayName("[실패] 질문 체크 요청 - 존재하지 않는 질문 ID")
	@Test
	void 질문_체크_실패_존재하지_않는_질문_ID() throws Exception {
		// given
		CheckQuestionRequest request = new CheckQuestionRequest(999L);

		// when
		ResultActions resultActions = CheckQuestion(request);

		// then
		resultActions.andExpect(status().isNotFound());
	}

	@DisplayName("[성공] 질문 생성 요청")
	@Test
	void 질문_생성_성공() throws Exception {
		// given
		CreateQuestionRequest request = createSampleCreateQuestionRequest();

		// when
		ResultActions resultActions = CreateQuestion(request);

		// then
		resultActions.andExpect(status().isOk());
	}

	@DisplayName("[성공] 질문 삭제 요청")
	@Test
	void 질문_삭제_성공() throws Exception {
		// given
		Long id = getFirstQuestionId();

		// when
		ResultActions resultActions = DeleteQuestion(id);

		// then
		resultActions.andExpect(status().isOk());
	}

	@DisplayName("[성공] 질문 수정 요청")
	@Test
	void 질문_수정_성공() throws Exception {
		// given
		Long questionId = getFirstQuestionId();
		UpdateQuestionContentRequest request = new UpdateQuestionContentRequest(questionId, "새로운 질문 내용");

		// when & then
		UpdateQuestion(request, status().isOk());
	}

	@DisplayName("[실패] 질문 수정 요청 - 잘못된 JSON 형식")
	@Test
	void 질문_수정_실패_잘못된_JSON_형식() throws Exception {
		// given
		String invalidJson = "{\"id\": \"ㅇㅇ\", \"content\": \"새로운 질문 내용\"}";

		// when & then
		UpdateQuestion(invalidJson, status().isBadRequest());
	}

	@DisplayName("[실패] 질문 수정 요청 - 존재하지 않는 질문 ID")
	@Test
	void 질문_수정_실패_존재하지_않는_질문_ID() throws Exception {
		// given
		UpdateQuestionContentRequest request = new UpdateQuestionContentRequest(999L, "새로운 질문 내용");

		// when & then
		UpdateQuestion(request, status().isNotFound());
	}

	private ResultActions CheckQuestion(CheckQuestionRequest request) throws Exception {
		return mvc.perform(put("/api/admin/questions/check")
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(request)))
			.andDo(print());
	}

	private ResultActions CheckQuestion(String invalidJson) throws Exception {
		return mvc.perform(put("/api/admin/questions/check")
				.contentType(MediaType.APPLICATION_JSON)
				.content(invalidJson))
			.andDo(print());
	}

	private ResultActions CreateQuestion(CreateQuestionRequest request) throws Exception {
		return mvc.perform(post("/api/admin/questions/new")
				.content(objectMapper.writeValueAsString(request))
				.contentType(MediaType.APPLICATION_JSON))
			.andDo(print());
	}

	private ResultActions DeleteQuestion(Long id) throws Exception {
		return mvc.perform(delete("/api/admin/questions/" + id)
				.contentType(MediaType.APPLICATION_JSON))
			.andDo(print());
	}

	private void UpdateQuestion(Object content, ResultMatcher expectedStatus) throws Exception {
		mvc.perform(put("/api/admin/questions")
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(content)))
			.andExpect(expectedStatus)
			.andDo(print());
	}

	private void UpdateQuestion(String content, ResultMatcher expectedStatus) throws Exception {
		mvc.perform(put("/api/admin/questions")
				.contentType(MediaType.APPLICATION_JSON)
				.content(content))
			.andExpect(expectedStatus)
			.andDo(print());
	}

	private Long getFirstQuestionId() {
		List<Question> questions = questionRepository.findAll();
		return questions.get(0).getId();
	}

	private CreateQuestionRequest createSampleCreateQuestionRequest() {
		return new CreateQuestionRequest("example content", AdmissionType.SUSI,
			AdmissionCategory.ADMISSION_GUIDELINE,
			new CreateAnswerRequest("example answer content", 2024));
	}

	private void initializeTestData() {
		Question question = Question.of("질문", "질문", AdmissionType.SUSI, AdmissionCategory.ADMISSION_GUIDELINE);
		Answer answer = Answer.of(question, "답변");

		questionRepository.saveAndFlush(question);
		answerRepository.saveAndFlush(answer);
	}
}
