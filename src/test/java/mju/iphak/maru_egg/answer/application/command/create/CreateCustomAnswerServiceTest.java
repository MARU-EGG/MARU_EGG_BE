package mju.iphak.maru_egg.answer.application.command.create;

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
import mju.iphak.maru_egg.answer.domain.Answer;
import mju.iphak.maru_egg.answer.api.dto.request.CreateAnswerRequest;
import mju.iphak.maru_egg.answer.repository.AnswerRepository;
import mju.iphak.maru_egg.common.MockTest;
import mju.iphak.maru_egg.question.domain.Question;
import mju.iphak.maru_egg.question.api.dto.request.CreateQuestionRequest;

class CreateCustomAnswerServiceTest extends MockTest {

	@Mock
	private AnswerRepository answerRepository;

	@InjectMocks
	private CreateCustomAnswerService createCustomAnswer;

	private Question question;
	private Answer answer;

	@BeforeEach
	void setUp() {
		MockitoAnnotations.openMocks(this);
		question = mock(Question.class);
		answer = mock(Answer.class);
	}

	@DisplayName("[성공] 답변 생성 요청")
	@Test
	void 답변_생성_성공() {
		// given
		CreateAnswerRequest answerRequest = new CreateAnswerRequest("example answer content", 2024);
		CreateQuestionRequest questionRequest = new CreateQuestionRequest("example content", AdmissionType.SUSI,
			AdmissionCategory.ADMISSION_GUIDELINE, answerRequest);
		Question question = questionRequest.toEntity();
		Answer answer = answerRequest.toEntity(question);

		when(answerRepository.save(any(Answer.class))).thenReturn(answer);

		// when
		createCustomAnswer.invoke(question, answerRequest);

		// then
		verify(answerRepository, times(1)).save(any(Answer.class));
	}
}
