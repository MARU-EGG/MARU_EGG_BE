package mju.iphak.maru_egg.answer.api.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;

@Builder
public record LLMAskQuestionRequest(
	@Schema(description = "입학 전형 타입(수시, 정시, 편입학)", allowableValues = {"SUSI", "JEONGSI", "PYEONIP"})
	String questionType,

	@Schema(description = "입학 전형 카테고리", allowableValues = {"ADMISSION_GUIDELINE", "PASSING_RESULT",
		"PAST_QUESTIONS", "INTERVIEW_PRACTICAL_TEST"})
	String questionCategory,

	@Schema(description = "질문 내용", example = "예시 질문입니다.")
	String question
) {
	public static LLMAskQuestionRequest of(String questionType, String questionCategory, String question) {
		return LLMAskQuestionRequest.builder()
			.questionType(questionType)
			.questionCategory(questionCategory)
			.question(question)
			.build();
	}
}
