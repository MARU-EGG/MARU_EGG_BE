package mju.iphak.maru_egg.admission.api.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import mju.iphak.maru_egg.admission.domain.AdmissionType;

@Builder
public record AdmissionTypeStatusResponse(
	@Schema(description = "입학 전형(수시, 정시, 편입학)", allowableValues = {"SUSI", "JEONGSI", "PYEONIP"})
	AdmissionType type,

	@Schema(description = "활성화된 입학 전형 (true면 활성화, false면 비활성화)", example = "true")
	boolean isActivated
) {
	public static AdmissionTypeStatusResponse of(AdmissionType type, boolean isActivated) {
		return AdmissionTypeStatusResponse.builder()
			.type(type)
			.isActivated(isActivated)
			.build();
	}
}
