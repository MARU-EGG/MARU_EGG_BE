package mju.iphak.maru_egg.common.exception;

import static org.springframework.http.HttpStatus.*;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {
	private static final String LOG_FORMAT = "Class : {}, ErrorMessage : {}";

	@ExceptionHandler(RuntimeException.class)
	public ResponseEntity<ErrorResponse> handleRuntimeException(RuntimeException e) {
		log.error(LOG_FORMAT, e.getClass().getSimpleName(), e.getMessage());

		return ResponseEntity.status(INTERNAL_SERVER_ERROR)
			.body(ErrorResponse.of(500, e.getMessage()));
	}
}
