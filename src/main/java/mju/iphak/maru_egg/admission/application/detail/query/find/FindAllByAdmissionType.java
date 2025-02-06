package mju.iphak.maru_egg.admission.application.detail.query.find;

import java.util.List;

import mju.iphak.maru_egg.admission.domain.AdmissionType;
import mju.iphak.maru_egg.admission.api.dto.response.AdmissionTypeDetailResponse;

public interface FindAllByAdmissionType {

	List<AdmissionTypeDetailResponse> invoke(AdmissionType admissionType);
}
