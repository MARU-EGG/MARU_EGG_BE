package mju.iphak.maru_egg.admission.application.status.query.find;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;
import mju.iphak.maru_egg.admission.domain.AdmissionTypeStatus;
import mju.iphak.maru_egg.admission.api.dto.response.AdmissionTypeStatusResponse;
import mju.iphak.maru_egg.admission.repository.AdmissionTypeStatusRepository;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class FindAllAdmissionTypeStatusService implements FindAllAdmissionTypeStatus {

	private final AdmissionTypeStatusRepository admissionTypeStatusRepository;

	public List<AdmissionTypeStatusResponse> invoke() {
		List<AdmissionTypeStatus> admissionTypeStatuses = admissionTypeStatusRepository.findAll();
		return admissionTypeStatuses.stream()
			.map(questionTypeStatus -> AdmissionTypeStatusResponse.of(questionTypeStatus.getAdmissionType(),
				questionTypeStatus.isActivated()))
			.toList();
	}
}
