package mju.iphak.maru_egg.common.exception;

import static org.springframework.http.HttpStatus.*;

import org.springframework.http.HttpStatus;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum ErrorCode {

	// 400 error
	BAD_REQUEST_WEBCLIENT(BAD_REQUEST, "%s에 잘못된 요청을 보냈습니다. (questionType: %s, questionCategory: %s, question: %s"),
	DUPLICATE_COLLEGE_NAME(BAD_REQUEST, "단과대 이름 '%s'은(는) 이미 존재합니다."),
	DUPLICATE_DEPARTMENT_NAME(BAD_REQUEST, "학과 이름 '%s'은(는) 이미 존재합니다."),

	// 401 error
	UNAUTHORIZED_REQUEST(UNAUTHORIZED, "로그인 후 다시 시도해주세요."),

	// 404 error
	NOT_FOUND_QUESTION(NOT_FOUND, "type: %s, category: %s, content: %s인 질문을 찾을 수 없습니다."),
	NOT_FOUND_QUESTION_BY_ID(NOT_FOUND, "id: %s인 질문을 찾을 수 없습니다."),
	NOT_FOUND_QUESTION_WITHOUT_CATEGORY(NOT_FOUND, "type: %s, content: %s인 질문을 찾을 수 없습니다."),
	NOT_FOUND_QUESTION_BY_TYPE_CATEGORY(NOT_FOUND, "type: %s, category: %s인 질문을 찾을 수 없습니다."),
	NOT_FOUND_ANSWER(NOT_FOUND, "답변 id가 %s인 답변을 찾을 수 없습니다."),
	NOT_FOUND_ANSWER_BY_QUESTION_ID(NOT_FOUND, "질문 id가 %s인 답변을 찾을 수 없습니다."),
	NOT_FOUND_USER(NOT_FOUND, "유저 이메일이 %s인 유저를 찾을 수 없습니다."),
	NOT_FOUND_WEBCLIENT(NOT_FOUND, "%s에서 값을 불러오지 못 했습니다."),
	NOT_FOUND_ADMISSION_TYPE_STATUS(NOT_FOUND, "입학 전형이 %s인 입학 전형 상태를 찾을 수 없습니다."),
	NOT_FOUND_ADMISSION_TYPE_DETAILS(NOT_FOUND, "id가 %s인 입학 전형 상세를 찾을 수 없습니다."),
	NOT_FOUND_COLLEGE(NOT_FOUND, "캠퍼스가 %s인 대학이 존재하지 않습니다."),
	NOT_FOUND_COLLEGE_BY_ID(NOT_FOUND, "ID가 %s인 단과대가 존재하지 않습니다."),
	NOT_FOUND_DEPARTMENT_BY_ID(NOT_FOUND, "ID가 %s인 학과이 존재하지 않습니다."),

	// 500 error
	INTERNAL_ERROR_SIMILARITY(INTERNAL_SERVER_ERROR, "contentToken: %s, question: %s인 질문의 유사도를 검사하는 도중 오류가 발생했습니다."),
	INTERNAL_ERROR_TEXT_SIMILARITY(INTERNAL_SERVER_ERROR, "질문의 유사도를 검사하는 도중 오류가 발생했습니다."),
	INTERNAL_ERROR_WEBCLIENT(INTERNAL_SERVER_ERROR, "%s에서 서버 오류가 발생했습니다.");

	private final HttpStatus status;
	private final String message;
}
