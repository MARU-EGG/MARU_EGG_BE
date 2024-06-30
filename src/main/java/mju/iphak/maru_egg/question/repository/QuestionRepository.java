package mju.iphak.maru_egg.question.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import mju.iphak.maru_egg.question.domain.Question;

public interface QuestionRepository extends JpaRepository<Question, Long> {
}
