package mju.iphak.maru_egg.admission.api;

import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import lombok.RequiredArgsConstructor;
import mju.iphak.maru_egg.admission.api.swagger.AdmissionTypeStatusControllerDocs;
import mju.iphak.maru_egg.admission.application.status.query.find.FindAllAdmissionTypeStatus;
import mju.iphak.maru_egg.admission.api.dto.response.AdmissionTypeStatusResponse;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/admissions/status")
public class AdmissionTypeStatusController implements AdmissionTypeStatusControllerDocs {

	private final FindAllAdmissionTypeStatus findAllAdmissionTypeStatus;

	@GetMapping()
	public List<AdmissionTypeStatusResponse> findAllAdmissionTypeStatus() {
		return findAllAdmissionTypeStatus.invoke();
	}
}
