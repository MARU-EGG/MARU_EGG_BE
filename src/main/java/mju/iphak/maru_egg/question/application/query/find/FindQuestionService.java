package mju.iphak.maru_egg.question.application.query.find;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;
import mju.iphak.maru_egg.answer.application.query.find.FindAnswerByQuestionId;
import mju.iphak.maru_egg.answer.domain.Answer;
import mju.iphak.maru_egg.answer.api.dto.response.AnswerResponse;
import mju.iphak.maru_egg.answerreference.dto.response.AnswerReferenceResponse;
import mju.iphak.maru_egg.question.domain.Question;
import mju.iphak.maru_egg.question.api.dto.response.QuestionResponse;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class FindQuestionService implements FindQuestion {

	private final FindAnswerByQuestionId findAnswerByQuestionId;

	public QuestionResponse invoke(Long questionId) {

		Answer answer = findAnswerByQuestionId.invoke(questionId);
		Question question = answer.getQuestion();
		question.incrementViewCount();

		List<AnswerReferenceResponse> answerReferenceResponses = answer.getReferences().stream()
			.map(reference -> AnswerReferenceResponse.of(reference.getTitle(), reference.getLink()))
			.toList();
		AnswerResponse answerResponse = AnswerResponse.from(answer);
		return QuestionResponse.of(question, answerResponse, answerReferenceResponses);
	}
}
