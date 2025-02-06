package mju.iphak.maru_egg.answer.application.command.create;

import static org.mockito.Mockito.*;

import java.util.Collections;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import mju.iphak.maru_egg.admission.domain.AdmissionCategory;
import mju.iphak.maru_egg.admission.domain.AdmissionType;
import mju.iphak.maru_egg.answer.domain.Answer;
import mju.iphak.maru_egg.answer.repository.AnswerRepository;
import mju.iphak.maru_egg.answerreference.application.create.BatchCreateAnswerReference;
import mju.iphak.maru_egg.answerreference.dto.response.AnswerReferenceResponse;
import mju.iphak.maru_egg.common.MockTest;
import mju.iphak.maru_egg.question.domain.Question;
import mju.iphak.maru_egg.question.api.dto.request.SaveRAGAnswerRequest;
import mju.iphak.maru_egg.question.repository.QuestionRepository;

class CreateRAGAnswerServiceTest extends MockTest {

	@Mock
	private QuestionRepository questionRepository;

	@Mock
	private AnswerRepository answerRepository;

	@Mock
	private BatchCreateAnswerReference createAnswerReference;

	@InjectMocks
	private CreateRAGAnswerService createRAGAnswerService;

	@BeforeEach
	void setUp() {
		MockitoAnnotations.openMocks(this);
	}

	@DisplayName("[성공] 질문과 답변 생성 요청")
	@Test
	void 질문_답변_생성_성공() {
		// given
		List<AnswerReferenceResponse> references = Collections.emptyList();
		SaveRAGAnswerRequest request = createRequest(references);
		Question mockQuestion = createMockQuestion(request);
		Answer mockAnswer = createMockAnswer(mockQuestion, request.answerContent());

		when(questionRepository.save(any(Question.class))).thenReturn(mockQuestion);
		when(answerRepository.save(any(Answer.class))).thenReturn(mockAnswer);

		// when
		createRAGAnswerService.invoke(request);

		// then
		verify(createAnswerReference, times(1)).invoke(mockAnswer, request.references());
		verify(questionRepository, times(1)).save(any(Question.class));
		verify(answerRepository, times(1)).save(any(Answer.class));
	}

	private SaveRAGAnswerRequest createRequest(List<AnswerReferenceResponse> references) {
		return SaveRAGAnswerRequest.builder()
			.type(AdmissionType.SUSI)
			.category(AdmissionCategory.ADMISSION_GUIDELINE)
			.content("수시 일정 알려주세요.")
			.contentToken("수시 일정")
			.answerContent("수시 일정은 2024년 12월 19일부터 시작됩니다.")
			.references(references)
			.build();
	}

	private Question createMockQuestion(SaveRAGAnswerRequest request) {
		return Question.of(
			request.content(),
			request.contentToken(),
			request.type(),
			request.category()
		);
	}

	private Answer createMockAnswer(Question question, String answerContent) {
		return Answer.of(question, answerContent);
	}
}
