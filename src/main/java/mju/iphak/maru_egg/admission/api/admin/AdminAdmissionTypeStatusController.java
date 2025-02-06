package mju.iphak.maru_egg.admission.api.admin;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import mju.iphak.maru_egg.admission.api.swagger.AdminAdmissionTypeStatusControllerDocs;
import mju.iphak.maru_egg.admission.application.status.command.init.InitAdmissionTypeStatus;
import mju.iphak.maru_egg.admission.application.status.command.update.UpdateAdmissionTypeStatus;
import mju.iphak.maru_egg.admission.api.dto.request.UpdateAdmissionTypeStatusRequest;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/admin/questions/status")
public class AdminAdmissionTypeStatusController implements AdminAdmissionTypeStatusControllerDocs {

	private final InitAdmissionTypeStatus initAdmissionTypeStatus;
	private final UpdateAdmissionTypeStatus updateAdmissionTypeStatus;

	@PostMapping("/init")
	public void initializeQuestionTypeStatus() {
		initAdmissionTypeStatus.invoke();
	}

	@PutMapping()
	public void updateQuestionTypeStatus(@Valid @RequestBody UpdateAdmissionTypeStatusRequest request) {
		updateAdmissionTypeStatus.invoke(request.type());
	}
}
