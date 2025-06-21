package mju.iphak.maru_egg.question.application.query.find;

import static mju.iphak.maru_egg.common.constant.RenewalYearConst.RENEWAL_YEAR;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import mju.iphak.maru_egg.admission.domain.AdmissionCategory;
import mju.iphak.maru_egg.admission.domain.AdmissionType;
import mju.iphak.maru_egg.answer.api.dto.response.AnswerResponse;
import mju.iphak.maru_egg.question.api.dto.response.QuestionListItemResponse;
import mju.iphak.maru_egg.question.domain.Question;
import mju.iphak.maru_egg.question.repository.QuestionRepository;

@Slf4j
@RequiredArgsConstructor
@Service
@Transactional
public class FindAllQuestionsService implements FindAllQuestions {

	private final QuestionRepository questionRepository;

	public List<QuestionListItemResponse> invoke(final AdmissionType type, final AdmissionCategory category) {
		List<Question> questions = findQuestions(type, category);
		return questions.stream()
			.map(this::createQuestionResponse)
			.collect(Collectors.toList());
	}

	private List<Question> findQuestions(final AdmissionType type, final AdmissionCategory category) {
		if (category == null) {
			return questionRepository.findAllByAdmissionTypeAndRenewalYearOrderByViewCountDesc(type, RENEWAL_YEAR);
		}
		return questionRepository.findAllByAdmissionTypeAndAdmissionCategoryAndRenewalYearAfterOrderByViewCountDesc(
			type, category, RENEWAL_YEAR);
	}

	private QuestionListItemResponse createQuestionResponse(final Question question) {
		AnswerResponse answerResponse = AnswerResponse.from(question.getAnswer());
		return QuestionListItemResponse.of(question, answerResponse);
	}
}