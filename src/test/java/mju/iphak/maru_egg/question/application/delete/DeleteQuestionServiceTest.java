package mju.iphak.maru_egg.question.application.delete;

import static org.assertj.core.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.*;
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

import jakarta.persistence.EntityNotFoundException;
import mju.iphak.maru_egg.admission.domain.AdmissionCategory;
import mju.iphak.maru_egg.admission.domain.AdmissionType;
import mju.iphak.maru_egg.answer.application.query.find.FindAnswerByQuestionIdService;
import mju.iphak.maru_egg.answer.domain.Answer;
import mju.iphak.maru_egg.answer.repository.AnswerRepository;
import mju.iphak.maru_egg.common.MockTest;
import mju.iphak.maru_egg.question.application.command.delete.DeleteQuestionService;
import mju.iphak.maru_egg.question.domain.Question;
import mju.iphak.maru_egg.question.repository.QuestionRepository;
import mju.iphak.maru_egg.question.repository.dto.request.QuestionCoreRequest;
import mju.iphak.maru_egg.question.repository.dto.response.QuestionCoreResponse;

class DeleteQuestionServiceTest extends MockTest {

	@Mock
	private FindAnswerByQuestionIdService findAnswerByQuestionId;

	@Mock
	private QuestionRepository questionRepository;

	@Mock
	private AnswerRepository answerRepository;

	@InjectMocks
	private DeleteQuestionService deleteQuestion;

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

	@DisplayName("[성공] 질문 삭제 요청")
	@Test
	void 질문_삭제_성공() {
		// given
		Long id = 1L;
		Question question = Question.of("질문", "질문", AdmissionType.SUSI, AdmissionCategory.ADMISSION_GUIDELINE);
		when(questionRepository.findById(id)).thenReturn(Optional.ofNullable(question));

		// when
		deleteQuestion.invoke(id);

		// then
		verify(questionRepository, times(1)).findById(id);
		verify(questionRepository, times(1)).delete(any(Question.class));
	}

	@DisplayName("[실패] 질문 삭제 시 조회 실패")
	@Test
	void 질문_삭제_조회_실패_NOTFOUND() {
		// given
		Long id = 1L;
		when(questionRepository.findById(id)).thenReturn(Optional.empty());

		// when & then
		EntityNotFoundException exception = assertThrows(EntityNotFoundException.class, () -> {
			deleteQuestion.invoke(id);
		});

		// then
		assertThat(exception.getMessage()).isEqualTo("id: 1인 질문을 찾을 수 없습니다.");
	}
}
