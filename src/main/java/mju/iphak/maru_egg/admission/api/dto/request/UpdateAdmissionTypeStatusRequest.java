package mju.iphak.maru_egg.admission.api.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import mju.iphak.maru_egg.admission.domain.AdmissionType;

@Schema(description = "입학 전형 상태 변경 DTO")
public record UpdateAdmissionTypeStatusRequest(

	@Schema(description = "입학 전형(수시, 정시, 편입학)", allowableValues = {"SUSI", "JEONGSI", "PYEONIP"})
	AdmissionType type
) {
}