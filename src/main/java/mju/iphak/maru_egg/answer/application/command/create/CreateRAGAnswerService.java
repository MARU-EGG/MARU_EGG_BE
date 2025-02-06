package mju.iphak.maru_egg.answer.application.command.create;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import mju.iphak.maru_egg.answer.domain.Answer;
import mju.iphak.maru_egg.answer.repository.AnswerRepository;
import mju.iphak.maru_egg.answerreference.application.create.BatchCreateAnswerReference;
import mju.iphak.maru_egg.question.domain.Question;
import mju.iphak.maru_egg.question.api.dto.request.SaveRAGAnswerRequest;
import mju.iphak.maru_egg.question.repository.QuestionRepository;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(propagation = Propagation.REQUIRES_NEW)
public class CreateRAGAnswerService implements CreateRAGAnswer {

	private final AnswerRepository answerRepository;
	private final QuestionRepository questionRepository;
	private final BatchCreateAnswerReference createAnswerReference;

	public void invoke(SaveRAGAnswerRequest request) {
		Question question = questionRepository.save(request.toQuestionEntity());
		Answer answer = answerRepository.save(Answer.of(question, request.answerContent()));
		createAnswerReference.invoke(answer, request.references());
	}
}
