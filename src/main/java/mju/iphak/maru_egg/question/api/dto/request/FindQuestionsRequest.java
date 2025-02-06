package mju.iphak.maru_egg.question.api.dto.request;

import org.springdoc.core.annotations.ParameterObject;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import mju.iphak.maru_egg.admission.domain.AdmissionCategory;
import mju.iphak.maru_egg.admission.domain.AdmissionType;

@Schema(description = "질문 목록 요청 DTO")
@ParameterObject
public record FindQuestionsRequest(

	@Schema(description = "입학 전형 타입(수시, 정시, 편입학)", allowableValues = {"SUSI", "JEONGSI", "PYEONIP"})
	@NotNull(message = "입학 전형 타입은 비어있을 수 없습니다.")
	AdmissionType type,

	@Schema(description = "입학 전형 카테고리(모집요강, 입시결과, 기출 문제)", allowableValues = {"ADMISSION_GUIDELINE", "PASSING_RESULT",
		"PAST_QUESTIONS", "INTERVIEW_PRACTICAL_TEST"})
	AdmissionCategory category
) {
}