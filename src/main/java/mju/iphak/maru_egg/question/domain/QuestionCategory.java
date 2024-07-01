package mju.iphak.maru_egg.question.domain;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public enum QuestionCategory {
	ADMISSIONS_TIMELINE("전형 일정"),
	QUALIFICATION("지원자격"),
	DOCUMENTATION("제출서류"),
	UNIV_LIFE("대학생활"),
	INTERVIEW_PRACTICAL_TEST("면접/실기"),
	ETC("기타");

	private final String questionCategory;
}
