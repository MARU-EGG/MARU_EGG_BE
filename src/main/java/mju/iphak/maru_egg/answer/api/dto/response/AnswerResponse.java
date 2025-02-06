package mju.iphak.maru_egg.answer.api.dto.response;

import java.time.LocalDate;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import mju.iphak.maru_egg.answer.domain.Answer;

@Builder
@Schema(description = "답변 DTO")
public record AnswerResponse(
	@Schema(description = "답변 id", example = "1")
	Long id,

	@Schema(description = "답변 내용", example = "예시 답변입니다.")
	String content,

	@Schema(description = "답변 갱신 년도", example = "2024")
	int renewalYear,

	@Schema(description = "답변 DB 생성 및 업데이트 날짜", example = "생성일자: 2024-07-24T15:49:33, 마지막 DB 갱신일자: 2024-07-24T15:52:49")
	String dateInformation
) {
	public static AnswerResponse from(Answer answer) {
		return AnswerResponse.builder()
			.id(answer.getId())
			.content(answer.getContent())
			.renewalYear(answer.getRenewalYear())
			.dateInformation(answer.getDateInformation())
			.build();
	}

	public static AnswerResponse valueOfRAG(String content) {
		LocalDate nowDate = LocalDate.now();
		return AnswerResponse.builder()
			.id(null)
			.content(content)
			.renewalYear(Integer.parseInt(String.valueOf(nowDate.getYear())))
			.dateInformation(null)
			.build();
	}
}
