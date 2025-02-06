package mju.iphak.maru_egg.question.application.create;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import mju.iphak.maru_egg.admission.domain.AdmissionCategory;
import mju.iphak.maru_egg.admission.domain.AdmissionType;
import mju.iphak.maru_egg.answer.api.dto.request.CreateAnswerRequest;
import mju.iphak.maru_egg.answer.application.command.create.CreateCustomAnswerService;
import mju.iphak.maru_egg.answer.application.query.find.FindAnswerByQuestionIdService;
import mju.iphak.maru_egg.answer.domain.Answer;
import mju.iphak.maru_egg.answer.repository.AnswerRepository;
import mju.iphak.maru_egg.common.MockTest;
import mju.iphak.maru_egg.question.api.dto.request.CreateQuestionRequest;
import mju.iphak.maru_egg.question.application.command.create.CreateCustomQuestionService;
import mju.iphak.maru_egg.question.domain.Question;
import mju.iphak.maru_egg.question.repository.QuestionRepository;
import mju.iphak.maru_egg.question.repository.dto.request.QuestionCoreRequest;
import mju.iphak.maru_egg.question.repository.dto.response.QuestionCoreResponse;

class CreateCustomQuestionServiceTest extends MockTest {

	@Mock
	private QuestionRepository questionRepository;

	@Mock
	private AnswerRepository answerRepository;

	@Mock
	private CreateCustomAnswerService createAnswer;

	@Mock
	private FindAnswerByQuestionIdService findAnswerByQuestionId;

	@InjectMocks
	private CreateCustomQuestionService createCustomQuestion;

	private Question question;
	private Answer answer;

	@BeforeEach
	void setUp() {
		MockitoAnnotations.openMocks(this);
		question = mock(Question.class);
		answer = mock(Answer.class);

		when(question.getId()).thenReturn(1L);
		when(answer.getId()).thenReturn(1L);
		when(findAnswerByQuestionId.invoke(1L)).thenReturn(answer);
		when(answerRepository.findByQuestionId(anyLong())).thenReturn(Optional.of(answer));
		when(questionRepository.searchQuestions(any(QuestionCoreRequest.class)))
			.thenReturn(Optional.of(List.of(
				QuestionCoreResponse.of(1L, "테스트 질문입니다."))));
		when(questionRepository.findById(1L)).thenReturn(Optional.of(question));
	}

	@DisplayName("[성공] 질문 생성 요청")
	@Test
	void 질문_생성_성공() {
		// given
		CreateAnswerRequest answerRequest = new CreateAnswerRequest("example answer content", 2024);
		CreateQuestionRequest request = new CreateQuestionRequest(
			"example content",
			AdmissionType.SUSI,
			AdmissionCategory.ADMISSION_GUIDELINE,
			answerRequest
		);
		Question question = request.toEntity();

		when(questionRepository.save(question)).thenReturn(question);

		// when
		createCustomQuestion.invoke(request);

		// then
		verify(questionRepository, times(1)).save(any(Question.class));
		verify(createAnswer, times(1)).invoke(any(Question.class), eq(answerRequest));
	}
}
