package mju.iphak.maru_egg.admission.api.dto.response;

import lombok.Builder;
import mju.iphak.maru_egg.admission.domain.AdmissionType;
import mju.iphak.maru_egg.admission.domain.AdmissionTypeDetail;

@Builder
public record AdmissionTypeDetailResponse(
	Long id,
	String name,
	AdmissionType type
) {
	public static AdmissionTypeDetailResponse from(AdmissionTypeDetail admissionTypeDetail) {
		return AdmissionTypeDetailResponse.builder()
			.id(admissionTypeDetail.getId())
			.name(admissionTypeDetail.getName())
			.type(admissionTypeDetail.getAdmissionTypeStatus().getAdmissionType())
			.build();
	}
}
