package mju.iphak.maru_egg.question.api.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import mju.iphak.maru_egg.admission.domain.AdmissionCategory;
import mju.iphak.maru_egg.admission.domain.AdmissionType;

@Schema(description = "질문 생성 요청 DTO")
public record QuestionRequest(

	@Schema(description = "입학 전형 타입(수시, 정시, 편입학)", allowableValues = {"SUSI", "JEONGSI", "PYEONIP"})
	@NotNull(message = "입학 전형은 비어있을 수 없습니다.")
	AdmissionType type,

	@Schema(description = "입학 전형 카테고리(모집요강, 입시결과, 기출 문제)", allowableValues = {"ADMISSION_GUIDELINE", "PASSING_RESULT",
		"PAST_QUESTIONS", "INTERVIEW_PRACTICAL_TEST"}, nullable = true)
	AdmissionCategory category,

	@Schema(description = "질문 내용", example = "수시 입학 요강에 대해 알려주세요.")
	@NotBlank(message = "질문은 비어있을 수 없습니다.")
	@Size(max = 1000, message = "크기가 0에서 1000 사이여야 합니다.")
	String content
) {
}