package mju.iphak.maru_egg.question.api.dto.response;

import java.util.List;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import mju.iphak.maru_egg.answer.api.dto.response.AnswerResponse;
import mju.iphak.maru_egg.answer.api.dto.response.LLMAnswerResponse;
import mju.iphak.maru_egg.answerreference.dto.response.AnswerReferenceResponse;
import mju.iphak.maru_egg.question.domain.Question;

@Builder
@Schema(description = "질문 응답 DTO")
public record QuestionResponse(
	@Schema(description = "질문 id", type = "string", examples = {"1", "null"})
	Long id,

	@Schema(description = "질문", example = "수시 일정 알려주세요.")
	String content,

	@Schema(description = "답변 DB 생성 및 업데이트 날짜", example = "생성일자: 2024-07-24T22:11:02, 마지막 DB 갱신일자: 2024-07-24T22:45:25.037855")
	String dateInformation,

	@Schema(description = "답변 DTO")
	AnswerResponse answer,

	@Schema(description = "참고 자료 리스트")
	List<AnswerReferenceResponse> references
) {
	public static QuestionResponse of(Question question, AnswerResponse answerResponse,
		List<AnswerReferenceResponse> answerReferenceResponses) {
		return QuestionResponse.builder()
			.id(question.getId())
			.content(question.getContent())
			.dateInformation(question.getDateInformation())
			.answer(answerResponse)
			.references(answerReferenceResponses)
			.build();
	}

	public static QuestionResponse valueOfNotFoundRAG(String content, String answer) {
		AnswerResponse answerResponse = AnswerResponse.valueOfRAG(answer);
		return QuestionResponse.builder()
			.id(null)
			.content(content)
			.dateInformation(null)
			.answer(answerResponse)
			.references(null)
			.build();
	}

	public static QuestionResponse valueOfRAG(String content, LLMAnswerResponse llmAnswerResponse) {
		AnswerResponse answerResponse = AnswerResponse.valueOfRAG(llmAnswerResponse.answer());
		return QuestionResponse.builder()
			.id(null)
			.content(content)
			.dateInformation(null)
			.answer(answerResponse)
			.references(llmAnswerResponse.references())
			.build();
	}
}