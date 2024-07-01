package mju.iphak.maru_egg.answer.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import mju.iphak.maru_egg.answer.domain.Answer;

public interface AnswerRepository extends JpaRepository<Answer, Long> {
}
