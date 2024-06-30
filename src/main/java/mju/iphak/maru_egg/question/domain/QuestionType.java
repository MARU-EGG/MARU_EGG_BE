package mju.iphak.maru_egg.question.domain;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public enum QuestionType {
	SUSI("수시"),
	JEONGSI("정시"),
	PYEONIP("편입학"),
	JAEOEGUGMIN("재외국민");

	private final String questionType;
}
