package mju.iphak.maru_egg.answer.application.command.create;

import mju.iphak.maru_egg.question.api.dto.request.SaveRAGAnswerRequest;

public interface CreateRAGAnswer {

	void invoke(SaveRAGAnswerRequest request);
}
