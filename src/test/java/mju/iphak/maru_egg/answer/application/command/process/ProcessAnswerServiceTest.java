package mju.iphak.maru_egg.answer.application.command.process;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import mju.iphak.maru_egg.admission.domain.AdmissionCategory;
import mju.iphak.maru_egg.admission.domain.AdmissionType;
import mju.iphak.maru_egg.answer.application.command.create.CreateRAGAnswer;
import mju.iphak.maru_egg.answer.application.query.rag.RagAnswer;
import mju.iphak.maru_egg.answer.domain.Answer;
import mju.iphak.maru_egg.answer.api.dto.request.LLMAskQuestionRequest;
import mju.iphak.maru_egg.answer.api.dto.response.LLMAnswerResponse;
import mju.iphak.maru_egg.common.MockTest;
import mju.iphak.maru_egg.question.domain.Question;
import mju.iphak.maru_egg.question.api.dto.request.QuestionRequest;
import mju.iphak.maru_egg.question.api.dto.response.QuestionResponse;
import reactor.core.publisher.Mono;

class ProcessAnswerServiceTest extends MockTest {

	@Mock
	private RagAnswer ragAnswer;

	@Mock
	private CreateRAGAnswer createRAGAnswer;

	@InjectMocks
	private ProcessAnswerService processAnswerService;

	@BeforeEach
	void setUp() {
		MockitoAnnotations.openMocks(this);
	}

	@DisplayName("[성공] RAG 서버에서 유효한 답변을 받아 질문과 답변 저장 요청")
	@Test
	void RAG_답변_성공_질문_답변_저장() {
		// given
		AdmissionType type = AdmissionType.SUSI;
		AdmissionCategory category = AdmissionCategory.ADMISSION_GUIDELINE;
		String content = "수시 일정 알려주세요.";
		String contentToken = "수시 일정";

		QuestionRequest request = new QuestionRequest(type, category, content);
		Question question = Question.of(content, contentToken, type, category);

		Answer answer = Answer.of(
			question,
			"수시 일정은 2024년 12월 19일부터 12월 26일까지 최초합격자 발표가 있습니다. "
				+ "등록금 납부 기간은 12월 16일부터 12월 18일까지입니다."
		);

		LLMAnswerResponse mockRagResponse = LLMAnswerResponse.of(
			type.getType(),
			category.getCategory(),
			answer,
			null
		);

		when(ragAnswer.invoke(any(LLMAskQuestionRequest.class))).thenReturn(Mono.just(mockRagResponse));

		// when
		QuestionResponse result = processAnswerService.invoke(request, contentToken);

		// then
		assertThat(result).isNotNull();
		assertThat(result.content()).isEqualTo(content);
		assertThat(result.answer().content()).isEqualTo(answer.getContent());
	}
}
