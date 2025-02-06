package mju.iphak.maru_egg.answer.application.command.create;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import mju.iphak.maru_egg.answer.domain.Answer;
import mju.iphak.maru_egg.answer.api.dto.request.CreateAnswerRequest;
import mju.iphak.maru_egg.answer.repository.AnswerRepository;
import mju.iphak.maru_egg.question.domain.Question;

@Slf4j
@RequiredArgsConstructor
@Service
@Transactional
public class CreateCustomAnswerService implements CreateCustomAnswer {

	private final AnswerRepository answerRepository;

	public void invoke(final Question question, final CreateAnswerRequest request) {
		Answer answer = request.toEntity(question);
		answerRepository.save(answer);
	}
}
