package mju.iphak.maru_egg.admission.application.status.query.find;

import java.util.List;

import mju.iphak.maru_egg.admission.api.dto.response.AdmissionTypeStatusResponse;

public interface FindAllAdmissionTypeStatus {

	List<AdmissionTypeStatusResponse> invoke();
}
