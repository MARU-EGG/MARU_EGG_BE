package mju.iphak.maru_egg.question.api.dto.request;

import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "질문 목록 요청 DTO")
public record CheckQuestionRequest(

	@Parameter(description = "질문 id", example = "1")
	Long questionId
) {
}
