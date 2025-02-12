package mju.iphak.maru_egg.question.application.find;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

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
import mju.iphak.maru_egg.answer.application.query.find.FindAnswerByQuestionIdService;
import mju.iphak.maru_egg.answer.domain.Answer;
import mju.iphak.maru_egg.common.MockTest;
import mju.iphak.maru_egg.question.api.dto.response.QuestionListItemResponse;
import mju.iphak.maru_egg.question.application.query.find.FindAllQuestionsService;
import mju.iphak.maru_egg.question.domain.Question;
import mju.iphak.maru_egg.question.repository.QuestionRepository;
import mju.iphak.maru_egg.question.repository.dto.request.QuestionCoreRequest;
import mju.iphak.maru_egg.question.repository.dto.response.QuestionCoreResponse;

class FindAllQuestionsServiceTest extends MockTest {

	@Mock
	private QuestionRepository questionRepository;

	@Mock
	private FindAnswerByQuestionIdService findAnswerByQuestionId;

	@InjectMocks
	private FindAllQuestionsService findAllQuestionsService;

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
		when(questionRepository.searchQuestions(any(QuestionCoreRequest.class)))
			.thenReturn(Optional.of(List.of(
				QuestionCoreResponse.of(1L, "테스트 질문입니다."))));
		when(questionRepository.findById(1L)).thenReturn(Optional.of(question));
	}

	@DisplayName("[성공] 질문 목록 조회")
	@Test
	void 질문_목록_조회_성공() {
		// given
		AdmissionType type = AdmissionType.SUSI;
		AdmissionCategory category = AdmissionCategory.ADMISSION_GUIDELINE;

		when(questionRepository.findAllByAdmissionTypeAndAdmissionCategoryAndRenewalYearAfterOrderByViewCountDesc(type,
			category, 2025))
			.thenReturn(List.of(question));
		when(findAnswerByQuestionId.invoke(question.getId())).thenReturn(answer);

		// when
		List<QuestionListItemResponse> result = findAllQuestionsService.invoke(type, category);

		// then
		assertThat(result).isNotNull();
		assertThat(result).isNotEmpty();
		assertThat(result.get(0).content()).isEqualTo(question.getContent());
		assertThat(result.get(0).answer().content()).isEqualTo(answer.getContent());
	}

	@DisplayName("[성공] 질문 목록 조회 - 카테고리 없이")
	@Test
	void 질문_목록_조회_성공_카테고리_없이() {
		// given
		AdmissionType type = AdmissionType.SUSI;

		when(questionRepository.findAllByAdmissionTypeAndRenewalYearOrderByViewCountDesc(type, 2025))
			.thenReturn(List.of(question));
		when(findAnswerByQuestionId.invoke(question.getId())).thenReturn(answer);

		// when
		List<QuestionListItemResponse> result = findAllQuestionsService.invoke(type, null);

		// then
		assertThat(result).isNotNull();
		assertThat(result).isNotEmpty();
		assertThat(result.get(0).content()).isEqualTo(question.getContent());
		assertThat(result.get(0).answer().content()).isEqualTo(answer.getContent());
	}
}
