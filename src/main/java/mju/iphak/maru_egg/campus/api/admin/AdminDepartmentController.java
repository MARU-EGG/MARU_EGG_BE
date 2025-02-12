package mju.iphak.maru_egg.campus.api.admin;

import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import lombok.RequiredArgsConstructor;
import mju.iphak.maru_egg.campus.api.dto.request.CreateDepartmentRequest;
import mju.iphak.maru_egg.campus.api.dto.request.UpdateDepartmentRequest;
import mju.iphak.maru_egg.campus.api.swagger.AdminDepartmentControllerDocs;
import mju.iphak.maru_egg.campus.application.department.command.create.CreateDepartment;
import mju.iphak.maru_egg.campus.application.department.command.delete.DeleteDepartment;
import mju.iphak.maru_egg.campus.application.department.command.update.UpdateDepartment;

@RestController
@RequestMapping("/api/admin/campuses/departments")
@RequiredArgsConstructor
public class AdminDepartmentController implements AdminDepartmentControllerDocs {

	private final CreateDepartment createDepartment;
	private final UpdateDepartment updateDepartment;
	private final DeleteDepartment deleteDepartment;

	@PostMapping
	public void createDepartment(@RequestBody CreateDepartmentRequest request) {
		createDepartment.invoke(request);
	}

	@PutMapping("/{departmentId}")
	public void updateDepartment(@PathVariable Long departmentId, @RequestBody UpdateDepartmentRequest request) {
		updateDepartment.invoke(departmentId, request);
	}

	@DeleteMapping("/{departmentId}")
	public void deleteDepartment(@PathVariable Long departmentId) {
		deleteDepartment.invoke(departmentId);
	}
}
