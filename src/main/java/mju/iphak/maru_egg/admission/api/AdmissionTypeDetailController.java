package mju.iphak.maru_egg.admission.api;

import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import lombok.RequiredArgsConstructor;
import mju.iphak.maru_egg.admission.api.swagger.AdmissionTypeDetailControllerDocs;
import mju.iphak.maru_egg.admission.application.detail.query.find.FindAllAdmissionTypeDetail;
import mju.iphak.maru_egg.admission.application.detail.query.find.FindAllByAdmissionType;
import mju.iphak.maru_egg.admission.domain.AdmissionType;
import mju.iphak.maru_egg.admission.api.dto.response.AdmissionTypeDetailResponse;

@RestController
@RequestMapping("/api/admissions")
@RequiredArgsConstructor
public class AdmissionTypeDetailController implements AdmissionTypeDetailControllerDocs {

	private final FindAllAdmissionTypeDetail findAllAdmissionTypeDetail;
	private final FindAllByAdmissionType findAllByAdmissionType;

	@GetMapping("/details")
	public List<AdmissionTypeDetailResponse> getAll() {
		return findAllAdmissionTypeDetail.invoke();
	}

	@GetMapping("/details/{type}")
	public List<AdmissionTypeDetailResponse> getAllByType(@PathVariable("type") AdmissionType type) {
		return findAllByAdmissionType.invoke(type);
	}
}
