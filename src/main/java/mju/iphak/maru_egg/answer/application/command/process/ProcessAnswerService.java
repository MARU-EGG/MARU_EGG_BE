package mju.iphak.maru_egg.answer.application.command.process;

import static mju.iphak.maru_egg.answer.utils.AnswerGuideUtils.*;

import java.time.Duration;
import java.time.temporal.ChronoUnit;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import mju.iphak.maru_egg.answer.application.command.create.CreateRAGAnswer;
import mju.iphak.maru_egg.answer.application.query.rag.RagAnswer;
import mju.iphak.maru_egg.answer.api.dto.request.LLMAskQuestionRequest;
import mju.iphak.maru_egg.answer.api.dto.response.LLMAnswerResponse;
import mju.iphak.maru_egg.question.api.dto.request.QuestionRequest;
import mju.iphak.maru_egg.question.api.dto.request.SaveRAGAnswerRequest;
import mju.iphak.maru_egg.question.api.dto.response.QuestionResponse;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ProcessAnswerService implements ProcessAnswer {

	private final RagAnswer ragAnswer;
	private final CreateRAGAnswer createRAGAnswer;

	public QuestionResponse invoke(QuestionRequest request, String contentToken) {
		LLMAskQuestionRequest askQuestionRequest = getLlmAskQuestionRequest(request);

		LLMAnswerResponse llmAnswerResponse = ragAnswer.invoke(askQuestionRequest)
			.block(Duration.of(20L, ChronoUnit.SECONDS));

		if (isInvalidAnswer(llmAnswerResponse)) {
			log.info("[INVALID ANSWER] 전형 타입: \"{}\", 전형: \"{}\", 질문: \"{}\" 에 대해 답변하지 못했습니다.", request.type(),
				request.category(), request.content());
			return QuestionResponse.valueOfNotFoundRAG(request.content(),
				generateGuideAnswer(llmAnswerResponse.references()));
		}
		SaveRAGAnswerRequest saveRAGAnswerRequest = SaveRAGAnswerRequest.of(request, contentToken, llmAnswerResponse);

		createRAGAnswer.invoke(saveRAGAnswerRequest);
		log.info("[NEW ANSWER] 답변: \"{}\"", llmAnswerResponse.answer());
		log.info("[NEW QUESTION] 전형 타입: \"{}\", 전형: \"{}\" 질문: \"{}\"",
			llmAnswerResponse.questionType(),
			llmAnswerResponse.questionCategory(), request.content());
		return QuestionResponse.valueOfRAG(request.content(), llmAnswerResponse);
	}

	private LLMAskQuestionRequest getLlmAskQuestionRequest(final QuestionRequest request) {
		String questionCategory = request.category() == null ? "" : request.category().getCategory();
		return LLMAskQuestionRequest.of(
			request.type().getType(),
			questionCategory,
			request.content()
		);
	}
}
