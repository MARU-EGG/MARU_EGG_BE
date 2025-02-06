package mju.iphak.maru_egg.answer.api.swagger;

import org.springframework.web.bind.annotation.RequestBody;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import mju.iphak.maru_egg.answer.api.dto.request.UpdateAnswerContentRequest;
import mju.iphak.maru_egg.common.meta.CustomApiResponse;
import mju.iphak.maru_egg.common.meta.CustomApiResponses;

@Tag(name = "Admin Answer API", description = "어드민 답변 관련 API 입니다.")
public interface AdminAnswerControllerDocs {

	@Operation(summary = "답변 수정", description = "답변을 수정하는 API", responses = {
		@ApiResponse(responseCode = "200", description = "답변을 수정 성공"),
	})
	@CustomApiResponses({
		@CustomApiResponse(error = "HttpMessageNotReadableException", status = 400, message = "Invalid input format: JSON parse error: Cannot deserialize value of type `java.lang.Long` from String \"ㅇㅇ\": not a valid `java.lang.Long` value", description = "잘못된 요청 값을 보낸 경우"),
		@CustomApiResponse(error = "EntityNotFoundException", status = 404, message = "답변 id가 123131인 답변을 찾을 수 없습니다.", description = "답변을 찾지 못한 경우"),
		@CustomApiResponse(error = "InternalServerError", status = 500, message = "내부 서버 오류가 발생했습니다.", description = "내부 서버 오류")
	})
	void updateAnswerContent(@Valid @RequestBody UpdateAnswerContentRequest request);
}
