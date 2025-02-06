package mju.iphak.maru_egg.question.application.query.find;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import mju.iphak.maru_egg.common.dto.pagination.SliceQuestionResponse;
import mju.iphak.maru_egg.question.api.dto.request.SearchQuestionsRequest;
import mju.iphak.maru_egg.question.api.dto.response.SearchedQuestionsResponse;
import mju.iphak.maru_egg.question.repository.QuestionRepository;
import mju.iphak.maru_egg.question.repository.dto.request.SelectQuestionsRequest;

@Slf4j
@RequiredArgsConstructor
@Service
@Transactional(readOnly = true)
public class FindAllPagedQuestionsService implements FindAllPagedQuestions {

	private final QuestionRepository questionRepository;

	public SliceQuestionResponse<SearchedQuestionsResponse> invoke(final SearchQuestionsRequest request) {
		Pageable pageable = PageRequest.of(0, request.size());
		SliceQuestionResponse<SearchedQuestionsResponse> response;

		SelectQuestionsRequest selectQuestionsRequest = SelectQuestionsRequest.of(request.type(), request.category(),
			request.content(),
			request.cursorViewCount(), request.questionId(),
			pageable);
		response = questionRepository.searchQuestionsOfCursorPaging(selectQuestionsRequest);
		return response;
	}

}
