package mju.iphak.maru_egg.answer.application.query.rag;

import static mju.iphak.maru_egg.common.exception.ErrorCode.*;

import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;

import lombok.RequiredArgsConstructor;
import mju.iphak.maru_egg.answer.api.dto.request.LLMAskQuestionRequest;
import mju.iphak.maru_egg.answer.api.dto.response.LLMAnswerResponse;
import mju.iphak.maru_egg.common.exception.custom.webClient.BadRequestWebClientException;
import mju.iphak.maru_egg.common.exception.custom.webClient.InternalServerErrorWebClientException;
import mju.iphak.maru_egg.common.exception.custom.webClient.NotFoundWebClientException;
import reactor.core.publisher.Mono;

@Service
@RequiredArgsConstructor
@Transactional(propagation = Propagation.NOT_SUPPORTED)
public class RagAnswerService implements RagAnswer {

	private static final String DEFAULT_URL = "/maruegg/ask_question_api/";

	private final WebClient webClient;

	public Mono<LLMAnswerResponse> invoke(LLMAskQuestionRequest request) {
		MultiValueMap<String, String> formData = new LinkedMultiValueMap<>();
		formData.add("questionType", request.questionType());
		formData.add("questionCategory", request.questionCategory());
		formData.add("question", request.question());
		return webClient.post()
			.uri(DEFAULT_URL)
			.accept(MediaType.APPLICATION_JSON)
			.contentType(MediaType.APPLICATION_FORM_URLENCODED)
			.body(BodyInserters.fromFormData(formData))
			.retrieve()
			.onStatus(HttpStatusCode::isError, response -> {
				return switch (response.statusCode().value()) {
					case 400 -> Mono.error(new BadRequestWebClientException(
						String.format(BAD_REQUEST_WEBCLIENT.getMessage(), "LLM 서버", request.questionType(),
							request.questionCategory(), request.question())));
					case 404 -> Mono.error(new NotFoundWebClientException(
						String.format(NOT_FOUND_WEBCLIENT.getMessage(), "LLM 서버")));
					default -> Mono.error(new InternalServerErrorWebClientException(
						String.format(INTERNAL_ERROR_WEBCLIENT.getMessage(), "LLM 서버")));
				};
			})
			.bodyToMono(LLMAnswerResponse.class);
	}
}
