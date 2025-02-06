package mju.iphak.maru_egg.answer.application.command.process;

import mju.iphak.maru_egg.question.api.dto.request.QuestionRequest;
import mju.iphak.maru_egg.question.api.dto.response.QuestionResponse;

public interface ProcessAnswer {

	QuestionResponse invoke(QuestionRequest questionRequest, String contentToken);
}
