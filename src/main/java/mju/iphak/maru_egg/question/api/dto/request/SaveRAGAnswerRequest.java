package mju.iphak.maru_egg.question.api.dto.request;

import java.util.List;

import lombok.Builder;
import mju.iphak.maru_egg.admission.domain.AdmissionCategory;
import mju.iphak.maru_egg.admission.domain.AdmissionType;
import mju.iphak.maru_egg.answer.api.dto.response.LLMAnswerResponse;
import mju.iphak.maru_egg.answerreference.dto.response.AnswerReferenceResponse;
import mju.iphak.maru_egg.question.domain.Question;

@Builder
public record SaveRAGAnswerRequest(
	AdmissionType type,
	AdmissionCategory category,
	String content,
	String contentToken,
	String answerContent,
	List<AnswerReferenceResponse> references
) {

	public static SaveRAGAnswerRequest of(QuestionRequest request, String contentToken,
		LLMAnswerResponse llmAnswerResponse) {
		return SaveRAGAnswerRequest.builder()
			.type(request.type())
			.category(request.category())
			.content(request.content())
			.contentToken(contentToken)
			.answerContent(llmAnswerResponse.answer())
			.references(llmAnswerResponse.references())
			.build();
	}

	public Question toQuestionEntity() {
		return Question.of(content, contentToken, type, category);
	}
}
