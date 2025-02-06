package mju.iphak.maru_egg.admission.application.detail.query.find;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;
import mju.iphak.maru_egg.admission.domain.AdmissionTypeDetail;
import mju.iphak.maru_egg.admission.api.dto.response.AdmissionTypeDetailResponse;
import mju.iphak.maru_egg.admission.repository.AdmissionTypeDetailRepository;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class FindAllAdmissionTypeDetailService implements FindAllAdmissionTypeDetail {

	private final AdmissionTypeDetailRepository admissionTypeDetailRepository;

	public List<AdmissionTypeDetailResponse> invoke() {
		List<AdmissionTypeDetail> admissionTypeDetails = admissionTypeDetailRepository.findAll();
		return admissionTypeDetails.stream()
			.map(AdmissionTypeDetailResponse::from)
			.toList();
	}
}
