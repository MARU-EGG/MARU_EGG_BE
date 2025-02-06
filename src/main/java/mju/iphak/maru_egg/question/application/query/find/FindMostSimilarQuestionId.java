package mju.iphak.maru_egg.question.application.query.find;

import java.util.List;

import mju.iphak.maru_egg.question.repository.dto.response.QuestionCoreResponse;

public interface FindMostSimilarQuestionId {

	Long invoke(List<QuestionCoreResponse> questionCoreResponses, String contentToken);
}
