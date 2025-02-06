package mju.iphak.maru_egg.admission.api.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "입학 전형 상세명 변경 DTO")
public record UpdateAdmissionTypeDetailRequest(

	@Schema(description = "입학 전형(수시, 정시, 편입학)", allowableValues = {"SUSI", "JEONGSI", "PYEONIP"})
	String name
) {
}