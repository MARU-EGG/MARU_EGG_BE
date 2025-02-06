package mju.iphak.maru_egg.answer.api.dto.response;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import mju.iphak.maru_egg.answer.domain.Answer;
import mju.iphak.maru_egg.answerreference.dto.response.AnswerReferenceResponse;

public record LLMAnswerResponse(

	@Schema(description = "입학 전형 타입(수시, 정시, 편입학)", allowableValues = {"SUSI", "JEONGSI", "PYEONIP"})
	String questionType,

	@Schema(description = "입학 전형 카테고리", allowableValues = {"ADMISSION_GUIDELINE", "PASSING_RESULT",
		"PAST_QUESTIONS", "INTERVIEW_PRACTICAL_TEST"})
	String questionCategory,

	@Schema(description = "답변 내용", example = "예시 답변입니다.")
	String answer,

	List<AnswerReferenceResponse> references
) {
	@Builder
	public LLMAnswerResponse {
		if (Objects.isNull(references)) {
			references = new ArrayList<>();
		}
	}

	public static LLMAnswerResponse of(String questionType, String questionCategory, Answer answer,
		List<AnswerReferenceResponse> references) {
		return LLMAnswerResponse.builder()
			.questionType(questionType)
			.questionCategory(questionCategory)
			.answer(answer.getContent())
			.references(references)
			.build();
	}
}