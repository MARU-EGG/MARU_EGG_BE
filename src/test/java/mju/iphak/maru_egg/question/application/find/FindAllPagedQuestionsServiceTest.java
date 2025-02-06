package mju.iphak.maru_egg.question.application.find;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import mju.iphak.maru_egg.admission.domain.AdmissionCategory;
import mju.iphak.maru_egg.admission.domain.AdmissionType;
import mju.iphak.maru_egg.common.MockTest;
import mju.iphak.maru_egg.common.dto.pagination.SliceQuestionResponse;
import mju.iphak.maru_egg.question.api.dto.request.SearchQuestionsRequest;
import mju.iphak.maru_egg.question.api.dto.response.SearchedQuestionsResponse;
import mju.iphak.maru_egg.question.application.query.find.FindAllPagedQuestionsService;
import mju.iphak.maru_egg.question.repository.QuestionRepository;
import mju.iphak.maru_egg.question.repository.dto.request.SelectQuestionsRequest;

class FindAllPagedQuestionsServiceTest extends MockTest {

	@Mock
	private QuestionRepository questionRepository;

	@InjectMocks
	private FindAllPagedQuestionsService findAllPagedQuestions;

	@BeforeEach
	void setUp() {
		MockitoAnnotations.openMocks(this);
	}

	@DisplayName("[성공] 질문 자동완성 조회 성공")
	@Test
	void 질문_자동완성_조회_성공() {
		// given
		String content = "example content";
		Integer cursorViewCount = 0;
		Long questionId = 0L;
		int size = 5;

		Pageable pageable = PageRequest.of(0, size);

		SearchQuestionsRequest request = new SearchQuestionsRequest(
			AdmissionType.SUSI,
			AdmissionCategory.ADMISSION_GUIDELINE,
			content,
			size,
			cursorViewCount,
			questionId
		);

		SelectQuestionsRequest selectQuestionsRequest = SelectQuestionsRequest.of(
			request.type(),
			request.category(),
			request.content(),
			request.cursorViewCount(),
			request.questionId(),
			pageable
		);

		SearchedQuestionsResponse searchedQuestionsResponse = new SearchedQuestionsResponse(
			1L, "example content", true
		);

		SliceQuestionResponse<SearchedQuestionsResponse> expectedResponse = new SliceQuestionResponse<>(
			List.of(searchedQuestionsResponse), 0, size, false, null, null
		);

		when(questionRepository.searchQuestionsOfCursorPaging(selectQuestionsRequest))
			.thenReturn(expectedResponse);

		// when
		SliceQuestionResponse<SearchedQuestionsResponse> result = findAllPagedQuestions.invoke(request);

		// then
		assertThat(result).isNotNull();
		assertThat(result.data()).isNotEmpty();
		assertThat(result.hasNext()).isFalse();
		assertThat(result.data().get(0).content()).isEqualTo("example content");

		verify(questionRepository, times(1)).searchQuestionsOfCursorPaging(selectQuestionsRequest);
	}

	@DisplayName("[성공] 질문 자동완성 - 빈 결과 반환")
	@Test
	void 질문_자동완성_빈결과() {
		// given
		String content = "no results";
		Integer cursorViewCount = 0;
		Long questionId = 0L;
		int size = 5;

		Pageable pageable = PageRequest.of(0, size);

		SearchQuestionsRequest request = new SearchQuestionsRequest(
			AdmissionType.SUSI,
			AdmissionCategory.ADMISSION_GUIDELINE,
			content,
			size,
			cursorViewCount,
			questionId
		);

		SelectQuestionsRequest selectQuestionsRequest = SelectQuestionsRequest.of(
			request.type(),
			request.category(),
			request.content(),
			request.cursorViewCount(),
			request.questionId(),
			pageable
		);

		SliceQuestionResponse<SearchedQuestionsResponse> emptyResponse = new SliceQuestionResponse<>(
			List.of(), 0, size, false, null, null
		);

		when(questionRepository.searchQuestionsOfCursorPaging(selectQuestionsRequest))
			.thenReturn(emptyResponse);

		// when
		SliceQuestionResponse<SearchedQuestionsResponse> result = findAllPagedQuestions.invoke(request);

		// then
		assertThat(result).isNotNull();
		assertThat(result.data()).isEmpty();
		assertThat(result.hasNext()).isFalse();

		verify(questionRepository, times(1)).searchQuestionsOfCursorPaging(selectQuestionsRequest);
	}
}
