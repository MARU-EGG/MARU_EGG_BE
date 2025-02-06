package mju.iphak.maru_egg.admission.api.swagger;

import java.util.List;

import org.springframework.web.bind.annotation.PathVariable;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import mju.iphak.maru_egg.admission.domain.AdmissionType;
import mju.iphak.maru_egg.admission.api.dto.response.AdmissionTypeDetailResponse;

@Tag(name = "AdmissionTypeDetail API", description = "입학타입 상세 관련 API 입니다.")
public interface AdmissionTypeDetailControllerDocs {
	@Operation(summary = "모든 입학타입 상세 조회", description = "모든 입학타입 상세 리스트를 조회합니다.", responses = {
		@ApiResponse(responseCode = "200", description = "모든 입학타입 상세 조회 성공")
	})
	List<AdmissionTypeDetailResponse> getAll();

	@Operation(summary = "입학타입별 상세 조회", description = "특정 입학타입에 따라 입학타입 상세 리스트를 조회합니다.", responses = {
		@ApiResponse(responseCode = "200", description = "입학타입별 상세 조회 성공")
	})
	List<AdmissionTypeDetailResponse> getAllByType(@PathVariable("type") AdmissionType type);

}
