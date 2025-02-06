package mju.iphak.maru_egg.question.api.dto.request;

import static mju.iphak.maru_egg.common.utils.PhraseExtractionUtils.*;

import io.swagger.v3.oas.annotations.media.Schema;
import mju.iphak.maru_egg.admission.domain.AdmissionCategory;
import mju.iphak.maru_egg.admission.domain.AdmissionType;
import mju.iphak.maru_egg.answer.api.dto.request.CreateAnswerRequest;
import mju.iphak.maru_egg.question.domain.Question;

@Schema(description = "질문 생성 요청 DTO")
public record CreateQuestionRequest(
	@Schema(description = "질문 내용", example = "예시 질문입니다.")
	String content,

	@Schema(description = "입학 전형 타입(수시, 정시, 편입학)", allowableValues = {"SUSI", "JEONGSI", "PYEONIP"})
	AdmissionType questionType,

	@Schema(description = "입학 전형 카테고리", allowableValues = {"ADMISSION_GUIDELINE", "PASSING_RESULT",
		"PAST_QUESTIONS", "INTERVIEW_PRACTICAL_TEST"})
	AdmissionCategory questionCategory,

	CreateAnswerRequest answer
) {
	public Question toEntity() {
		return Question.create(content, extractPhrases(content), questionType, questionCategory);
	}
}
