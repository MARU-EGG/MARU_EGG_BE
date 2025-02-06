package mju.iphak.maru_egg.question.api.dto.request;

import org.springdoc.core.annotations.ParameterObject;

import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.PositiveOrZero;
import mju.iphak.maru_egg.admission.domain.AdmissionCategory;
import mju.iphak.maru_egg.admission.domain.AdmissionType;

@Schema(description = "질문 자동완성 요청 DTO")
@ParameterObject
public record SearchQuestionsRequest(

	@Schema(description = "입학 전형 타입(수시, 정시, 편입학)", allowableValues = {"SUSI", "JEONGSI", "PYEONIP"})
	AdmissionType type,

	@Schema(description = "입학 전형 카테고리(모집요강, 입시결과, 기출 문제)", allowableValues = {"ADMISSION_GUIDELINE", "PASSING_RESULT",
		"PAST_QUESTIONS", "INTERVIEW_PRACTICAL_TEST"})
	AdmissionCategory category,

	@Schema(description = "질문 내용", example = "수시")
	String content,

	@Parameter(description = "질문 수", example = "10")
	@PositiveOrZero
	Integer size,

	@Parameter(description = "질문 조회 수 cursor 식별값(초기 요청의 경우 0으로 입력, 이후엔 response 값의 nextCursorViewCount 사용)", example = "0")
	@PositiveOrZero
	Integer cursorViewCount,

	@Parameter(description = "질문 ID(초기 요청의 경우 0으로 입력, 이후엔 response 값의 nextQuestionId 사용)", example = "0")
	@PositiveOrZero
	Long questionId
) {
}
