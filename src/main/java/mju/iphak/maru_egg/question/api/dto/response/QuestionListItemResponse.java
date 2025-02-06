package mju.iphak.maru_egg.question.api.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import mju.iphak.maru_egg.answer.api.dto.response.AnswerResponse;
import mju.iphak.maru_egg.question.domain.Question;

@Builder
@Schema(description = "질문 응답 DTO")
public record QuestionListItemResponse(
	@Schema(description = "질문 id", example = "1")
	Long id,

	@Schema(description = "질문", example = "수시 입학 요강 알려주세요.")
	String content,

	@Schema(description = "질문 조회수", example = "2")
	int viewCount,

	@Schema(description = "확인된 질문-답변인지 checked (true면 확인 완료, false면 확인 미완료)", example = "true")
	boolean isChecked,

	@Schema(description = "답변 DB 생성 및 업데이트 날짜", example = "생성일자: 2024-07-24T15:49:33, 마지막 DB 갱신일자: 2024-07-24T15:52:49")
	String dateInformation,

	@Schema(description = "답변 DTO")
	AnswerResponse answer
) {

	public static QuestionListItemResponse of(Question question, AnswerResponse answer) {
		return QuestionListItemResponse.builder()
			.id(question.getId())
			.content(question.getContent())
			.viewCount(question.getViewCount())
			.isChecked(question.isChecked())
			.dateInformation(question.getDateInformation())
			.answer(answer)
			.build();
	}
}