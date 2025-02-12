package mju.iphak.maru_egg.question.repository;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import mju.iphak.maru_egg.admission.domain.AdmissionCategory;
import mju.iphak.maru_egg.admission.domain.AdmissionType;
import mju.iphak.maru_egg.answer.domain.Answer;
import mju.iphak.maru_egg.answer.repository.AnswerRepository;
import mju.iphak.maru_egg.common.RepositoryTest;
import mju.iphak.maru_egg.question.domain.Question;

class QuestionRepositoryTest extends RepositoryTest {

	@Autowired
	private QuestionRepository questionRepository;

	@Autowired
	private AnswerRepository answerRepository;

	private Question question;
	private Answer answer;

	@BeforeEach
	public void setUp() throws Exception {
		question = Question.of("테스트 질문 예시 예시입니다.", "테스트 질문 예시", AdmissionType.SUSI,
			AdmissionCategory.ADMISSION_GUIDELINE);
		questionRepository.save(question);
		questionRepository.saveAll(List.of(
			Question.of("추가1 테스트 질문 예시입니다.", "추가 테스트 질문 예시", AdmissionType.SUSI, AdmissionCategory.ADMISSION_GUIDELINE),
			Question.of("추가2 테스트 질문 예시입니다.", "추가 테스트 질문 예시", AdmissionType.SUSI, AdmissionCategory.ADMISSION_GUIDELINE),
			Question.of("추가3 테스트 질문 예시입니다.", "추가 테스트 질문 예시", AdmissionType.SUSI, AdmissionCategory.ADMISSION_GUIDELINE),
			Question.of("추가4 테스트 질문 예시입니다.", "추가 테스트 질문 예시", AdmissionType.SUSI, null)
		));
		answer = Answer.of(question, "테스트 답변입니다.");
		answerRepository.save(answer);
	}

	@DisplayName("[성공] 타입과 카테고리로 질문을 조회한다.")
	@Test
	void 타입과_카테고리로_질문_조회_성공() {
		// given
		AdmissionType type = AdmissionType.SUSI;
		AdmissionCategory category = AdmissionCategory.ADMISSION_GUIDELINE;

		// when
		List<Question> questions = executeFindByTypeAndCategory(type, category);

		// then
		assertThat(questions).isNotEmpty();
		assertThat(questions.size()).isEqualTo(4);
		assertThat(questions.get(0)).isEqualTo(question);
	}

	@DisplayName("[실패] 타입과 카테고리로 질문을 조회할 수 없다.")
	@Test
	void 타입과_카테고리로_질문_조회_실패() {
		// given
		AdmissionType invalidType = AdmissionType.JEONGSI;
		AdmissionCategory category = AdmissionCategory.ADMISSION_GUIDELINE;

		// when
		List<Question> questions = executeFindByTypeAndCategory(invalidType, category);

		// then
		assertThat(questions.isEmpty()).isTrue();
	}

	@DisplayName("[성공] 카테고리 없이 타입으로 질문을 조회한다.")
	@Test
	void 카테고리_없이_타입으로_질문_조회_성공() {
		// given
		AdmissionType type = AdmissionType.SUSI;

		// when
		List<Question> questions = executeFindByTypeOnly(type);

		// then
		assertThat(questions).isNotEmpty();
		assertThat(questions.size()).isEqualTo(5);
		assertThat(questions.get(0)).isEqualTo(question);
	}

	@DisplayName("[실패] 타입으로만 질문을 조회할 수 없다.")
	@Test
	void 타입으로만_질문_조회_실패() {
		// given
		AdmissionType invalidType = AdmissionType.JEONGSI;

		// when
		List<Question> questions = executeFindByTypeOnly(invalidType);

		// then
		assertThat(questions.isEmpty()).isTrue();
	}

	private List<Question> executeFindByTypeAndCategory(AdmissionType type, AdmissionCategory category) {
		return questionRepository.findAllByAdmissionTypeAndAdmissionCategoryAndRenewalYearAfterOrderByViewCountDesc(
			type, category, 2025);
	}

	private List<Question> executeFindByTypeOnly(AdmissionType type) {
		return questionRepository.findAllByAdmissionTypeAndRenewalYearOrderByViewCountDesc(type, 2025);
	}
}
