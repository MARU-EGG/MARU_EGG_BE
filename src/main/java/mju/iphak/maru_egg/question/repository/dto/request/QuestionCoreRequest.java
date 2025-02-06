package mju.iphak.maru_egg.question.repository.dto.request;

import lombok.Builder;
import mju.iphak.maru_egg.admission.domain.AdmissionCategory;
import mju.iphak.maru_egg.admission.domain.AdmissionType;
import mju.iphak.maru_egg.question.api.dto.request.QuestionRequest;

@Builder
public record QuestionCoreRequest(
	AdmissionType type,
	AdmissionCategory category,
	String content,
	String contentToken
) {

	public static QuestionCoreRequest of(final QuestionRequest request, final String contentToken) {
		return QuestionCoreRequest.builder()
			.type(request.type())
			.category(request.category())
			.content(request.content())
			.contentToken(contentToken)
			.build();
	}
}
