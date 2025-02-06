package mju.iphak.maru_egg.answer.application.query.rag;

import mju.iphak.maru_egg.answer.api.dto.request.LLMAskQuestionRequest;
import mju.iphak.maru_egg.answer.api.dto.response.LLMAnswerResponse;
import reactor.core.publisher.Mono;

public interface RagAnswer {

	Mono<LLMAnswerResponse> invoke(LLMAskQuestionRequest request);
}
