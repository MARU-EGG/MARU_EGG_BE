package mju.iphak.maru_egg.answer.api.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import mju.iphak.maru_egg.answer.domain.Answer;
import mju.iphak.maru_egg.question.domain.Question;

@Schema(description = "답변 생성 요청 DTO")
public record CreateAnswerRequest(
	@Schema(description = "질문 내용", example = "예시 답변입니다.")
	String content,

	@Schema(description = "파일 갱신 년도", example = "2024")
	int renewalYear
) {
	public Answer toEntity(Question question) {
		return Answer.builder()
			.question(question)
			.content(this.content)
			.renewalYear(this.renewalYear)
			.build();
	}
}
