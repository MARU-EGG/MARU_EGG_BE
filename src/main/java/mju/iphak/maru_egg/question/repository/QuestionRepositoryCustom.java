package mju.iphak.maru_egg.question.repository;

import java.util.List;
import java.util.Optional;

import mju.iphak.maru_egg.common.dto.pagination.SliceQuestionResponse;
import mju.iphak.maru_egg.question.api.dto.response.SearchedQuestionsResponse;
import mju.iphak.maru_egg.question.repository.dto.request.QuestionCoreRequest;
import mju.iphak.maru_egg.question.repository.dto.request.SelectQuestionsRequest;
import mju.iphak.maru_egg.question.repository.dto.response.QuestionCoreResponse;

public interface QuestionRepositoryCustom {
	Optional<List<QuestionCoreResponse>> searchQuestions(
		final QuestionCoreRequest questionCoreRequest);

	SliceQuestionResponse<SearchedQuestionsResponse> searchQuestionsOfCursorPaging(
		final SelectQuestionsRequest selectQuestionsRequest);
}