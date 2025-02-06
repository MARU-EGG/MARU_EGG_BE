package mju.iphak.maru_egg.question.application.query.process;

import mju.iphak.maru_egg.question.api.dto.request.QuestionRequest;
import mju.iphak.maru_egg.question.api.dto.response.QuestionResponse;

public interface ProcessQuestion {

	QuestionResponse invoke(final QuestionRequest request);
}
