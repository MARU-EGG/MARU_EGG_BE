package mju.iphak.maru_egg.admission.api.admin;

import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import lombok.RequiredArgsConstructor;
import mju.iphak.maru_egg.admission.api.swagger.AdminAdmissionTypeDetailControllerDocs;
import mju.iphak.maru_egg.admission.application.detail.command.create.CreateAdmissionTypeDetail;
import mju.iphak.maru_egg.admission.application.detail.command.delete.DeleteAdmissionTypeDetail;
import mju.iphak.maru_egg.admission.application.detail.command.update.UpdateAdmissionTypeDetail;
import mju.iphak.maru_egg.admission.api.dto.request.CreateAdmissionTypeDetailRequest;
import mju.iphak.maru_egg.admission.api.dto.request.UpdateAdmissionTypeDetailRequest;

@RestController
@RequestMapping("/api/admin/admissions")
@RequiredArgsConstructor
public class AdminAdmissionTypeDetailController implements AdminAdmissionTypeDetailControllerDocs {

	private final DeleteAdmissionTypeDetail deleteAdmissionTypeDetail;
	private final CreateAdmissionTypeDetail createAdmissionTypeDetail;
	private final UpdateAdmissionTypeDetail updateAdmissionTypeDetail;

	@PostMapping("/detail")
	public void create(@RequestBody CreateAdmissionTypeDetailRequest request) {
		createAdmissionTypeDetail.invoke(request.detail(), request.type());
	}

	@PutMapping("/{id}")
	public void update(@PathVariable("id") Long admissionTypeDetailId,
		@RequestBody UpdateAdmissionTypeDetailRequest request) {
		updateAdmissionTypeDetail.invoke(admissionTypeDetailId, request.name());
	}

	@DeleteMapping("/{id}")
	public void delete(@PathVariable("id") Long id) {
		deleteAdmissionTypeDetail.invoke(id);
	}
}
