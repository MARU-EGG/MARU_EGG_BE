package mju.iphak.maru_egg.question.application.command.create;

import mju.iphak.maru_egg.question.api.dto.request.CreateQuestionRequest;

public interface CreateCustomQuestion {

	void invoke(final CreateQuestionRequest request);
}
