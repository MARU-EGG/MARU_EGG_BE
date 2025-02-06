package mju.iphak.maru_egg.question.api.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

@Schema(description = "질문 수정 요청 DTO")
public record UpdateQuestionContentRequest(

	@Schema(description = "질문 ID", example = "1")
	Long id,

	@Schema(description = "변경할 질문 내용", example = "변경된 질문입니다.")
	@NotBlank(message = "변경할 질문은 비어있을 수 없습니다.")
	String content
) {
}

