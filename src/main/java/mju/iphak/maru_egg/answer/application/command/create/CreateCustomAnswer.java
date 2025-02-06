package mju.iphak.maru_egg.answer.application.command.create;

import mju.iphak.maru_egg.answer.api.dto.request.CreateAnswerRequest;
import mju.iphak.maru_egg.question.domain.Question;

public interface CreateCustomAnswer {

	void invoke(final Question question, final CreateAnswerRequest request);
}
