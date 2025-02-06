package mju.iphak.maru_egg.question.repository.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;

@Builder
public record QuestionCoreResponse(

	@Schema(description = "질문 id", example = "1")
	Long id,

	@Schema(description = "질문 토큰", example = "수시 입학 모집요강")
	String contentToken
) {
	public static QuestionCoreResponse of(Long id, String contentToken) {
		return QuestionCoreResponse.builder()
			.id(id)
			.contentToken(contentToken)
			.build();
	}
}
