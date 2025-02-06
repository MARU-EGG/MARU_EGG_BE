package mju.iphak.maru_egg.question.application.command.create;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import mju.iphak.maru_egg.answer.application.command.create.CreateCustomAnswer;
import mju.iphak.maru_egg.question.domain.Question;
import mju.iphak.maru_egg.question.api.dto.request.CreateQuestionRequest;
import mju.iphak.maru_egg.question.repository.QuestionRepository;

@Slf4j
@RequiredArgsConstructor
@Service
@Transactional
public class CreateCustomQuestionService implements CreateCustomQuestion {

	private final QuestionRepository questionRepository;
	private final CreateCustomAnswer createCustomAnswer;

	public void invoke(final CreateQuestionRequest request) {
		Question question = request.toEntity();
		questionRepository.save(question);
		createCustomAnswer.invoke(question, request.answer());
	}
}
