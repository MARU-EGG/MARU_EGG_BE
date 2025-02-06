package mju.iphak.maru_egg.question.application.query.find;

import mju.iphak.maru_egg.common.dto.pagination.SliceQuestionResponse;
import mju.iphak.maru_egg.question.api.dto.request.SearchQuestionsRequest;
import mju.iphak.maru_egg.question.api.dto.response.SearchedQuestionsResponse;

public interface FindAllPagedQuestions {

	SliceQuestionResponse<SearchedQuestionsResponse> invoke(final SearchQuestionsRequest request);
}
