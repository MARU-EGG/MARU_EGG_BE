package mju.iphak.maru_egg.answerreference.domain;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import mju.iphak.maru_egg.admission.domain.AdmissionCategory;
import mju.iphak.maru_egg.admission.domain.AdmissionType;
import mju.iphak.maru_egg.answer.domain.Answer;
import mju.iphak.maru_egg.answer.repository.AnswerRepository;
import mju.iphak.maru_egg.answerreference.repository.AnswerReferenceRepository;
import mju.iphak.maru_egg.common.RepositoryTest;
import mju.iphak.maru_egg.question.domain.Question;
import mju.iphak.maru_egg.question.repository.QuestionRepository;

class AnswerReferenceTest extends RepositoryTest {

	@Autowired
	private AnswerReferenceRepository answerReferenceRepository;

	@Autowired
	private QuestionRepository questionRepository;

	@Autowired
	private AnswerRepository answerRepository;

	@Test
	void test() {

		Question question = questionRepository.save(
			Question.of("content", "수시 일정", AdmissionType.SUSI, AdmissionCategory.ADMISSION_GUIDELINE));
		Answer answer = answerRepository.save(Answer.of(question, "수시 일정은 2024년 12월 19일(목)부터 ..."));
		AnswerReference save = answerReferenceRepository.save(AnswerReference.of("테스트 title", "테스트 link", answer));

		Long id = save.getAnswer().getId();

		AnswerReference answerReference = answerReferenceRepository.findById(save.getId()).get();
		Assertions.assertThat(answerReference.getAnswer().getId()).isEqualTo(id);
		Assertions.assertThat(answerReference.getAnswer().getId()).isNotNull();
	}
}
