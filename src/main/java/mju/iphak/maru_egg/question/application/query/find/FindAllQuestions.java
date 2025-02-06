package mju.iphak.maru_egg.question.application.query.find;

import java.util.List;

import mju.iphak.maru_egg.admission.domain.AdmissionCategory;
import mju.iphak.maru_egg.admission.domain.AdmissionType;
import mju.iphak.maru_egg.question.api.dto.response.QuestionListItemResponse;

public interface FindAllQuestions {

	List<QuestionListItemResponse> invoke(final AdmissionType type, final AdmissionCategory category);
}
