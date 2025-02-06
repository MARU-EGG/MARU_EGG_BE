package mju.iphak.maru_egg.admission.api.swagger;

import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import mju.iphak.maru_egg.admission.api.dto.request.CreateAdmissionTypeDetailRequest;
import mju.iphak.maru_egg.admission.api.dto.request.UpdateAdmissionTypeDetailRequest;

@Tag(name = "Admin AdmissionTypeDetail API", description = "어드민 입학타입 상세 관련 API 입니다.")
public interface AdminAdmissionTypeDetailControllerDocs {

	@Operation(summary = "입학타입 상세 생성", description = "새로운 입학타입 상세를 생성합니다.", responses = {
		@ApiResponse(responseCode = "200", description = "입학타입 상세 생성 성공")
	})
	void create(@Valid @RequestBody CreateAdmissionTypeDetailRequest request);

	@Operation(summary = "입학타입 상세 업데이트", description = "특정 입학타입 상세를 업데이트합니다.", responses = {
		@ApiResponse(responseCode = "200", description = "입학타입 상세 업데이트 성공")
	})
	void update(@PathVariable("id") Long admissionTypeDetailId, @RequestBody UpdateAdmissionTypeDetailRequest request);

	@Operation(summary = "입학타입 상세 삭제", description = "특정 입학타입 상세를 삭제합니다.", responses = {
		@ApiResponse(responseCode = "200", description = "입학타입 상세 삭제 성공")
	})
	void delete(@PathVariable("id") Long id);
}