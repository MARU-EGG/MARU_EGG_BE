package mju.iphak.maru_egg.question.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import mju.iphak.maru_egg.admission.domain.AdmissionCategory;
import mju.iphak.maru_egg.admission.domain.AdmissionType;
import mju.iphak.maru_egg.question.domain.Question;

public interface QuestionRepository extends JpaRepository<Question, Long>, QuestionRepositoryCustom {
	@Modifying(flushAutomatically = true)
	@Transactional
	@Query("update Question question set question.viewCount = question.viewCount + 1 where question.id = :questionId")
	int incrementViewCount(@Param("questionId") Long questionId);

	List<Question> findAllByAdmissionTypeAndAdmissionCategoryAndRenewalYearAfterOrderByViewCountDesc(AdmissionType type,
		AdmissionCategory category, int renewalYear);

	List<Question> findAllByAdmissionTypeAndRenewalYearOrderByViewCountDesc(AdmissionType type, int renewalYear);
}
