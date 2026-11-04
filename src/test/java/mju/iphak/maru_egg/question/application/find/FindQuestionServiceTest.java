package mju.iphak.maru_egg.question.application.find;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import mju.iphak.maru_egg.admission.domain.AdmissionCategory;
import mju.iphak.maru_egg.admission.domain.AdmissionType;
import mju.iphak.maru_egg.answer.application.query.find.FindAnswerByQuestionId;
import mju.iphak.maru_egg.answer.domain.Answer;
import mju.iphak.maru_egg.question.api.dto.response.QuestionResponse;
import mju.iphak.maru_egg.question.application.query.find.FindQuestionService;
import mju.iphak.maru_egg.question.domain.Question;
import mju.iphak.maru_egg.question.repository.QuestionRepository;

class FindQuestionServiceTest {

	@DisplayName("기존 질문을 반환하면서 조회수를 원자적으로 증가시킨다")
	@Test
	void incrementsViewCountWithAtomicRepositoryUpdate() {
		FindAnswerByQuestionId findAnswerByQuestionId = mock(FindAnswerByQuestionId.class);
		QuestionRepository questionRepository = mock(QuestionRepository.class);
		Question question = Question.of(
			"수시 일정 알려주세요.",
			"수시 일정",
			AdmissionType.SUSI,
			AdmissionCategory.ADMISSION_GUIDELINE
		);
		Answer answer = Answer.of(question, "수시 일정 답변입니다.");
		when(findAnswerByQuestionId.invoke(1L)).thenReturn(answer);

		FindQuestionService service = new FindQuestionService(findAnswerByQuestionId, questionRepository);
		QuestionResponse response = service.invoke(1L);

		verify(questionRepository).incrementViewCount(1L);
		assertThat(response.content()).isEqualTo(question.getContent());
		assertThat(question.getViewCount()).isZero();
	}
}
