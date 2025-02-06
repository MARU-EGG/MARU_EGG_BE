package mju.iphak.maru_egg.question.api.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;

@Builder
@Schema(description = "질문 응답 DTO")
public record SearchedQuestionsResponse(

	@Schema(description = "질문 ID", example = "1")
	Long id,

	@Schema(description = "질문 내용", example = "수시 일정 알려주세요.")
	String content,

	@Schema(description = "확인된 질문-답변인지 checked (true면 확인 완료, false면 확인 미완료)", example = "true")
	boolean isChecked
) {
	public static SearchedQuestionsResponse of(Long id, String content, boolean isChecked) {
		return SearchedQuestionsResponse.builder()
			.id(id)
			.content(content)
			.isChecked(isChecked)
			.build();
	}
}
