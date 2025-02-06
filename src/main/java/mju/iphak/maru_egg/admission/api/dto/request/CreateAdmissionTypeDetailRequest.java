package mju.iphak.maru_egg.admission.api.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import mju.iphak.maru_egg.admission.domain.AdmissionType;

@Schema(description = "입학상세타입 생성 dto")
public record CreateAdmissionTypeDetailRequest(

	@Schema(examples = "학교장추천전형")
	String detail,
	AdmissionType type
) {
}
