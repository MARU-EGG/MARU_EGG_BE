package mju.iphak.maru_egg.campus.api;

import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import lombok.RequiredArgsConstructor;
import mju.iphak.maru_egg.campus.api.dto.response.DepartmentResponse;
import mju.iphak.maru_egg.campus.api.swagger.DepartmentControllerDocs;
import mju.iphak.maru_egg.campus.application.department.query.FindAllByCollege;
import mju.iphak.maru_egg.campus.application.department.query.FindAllDepartment;

@RestController
@RequestMapping("/api/departments")
@RequiredArgsConstructor
public class DepartmentController implements DepartmentControllerDocs {

	private final FindAllDepartment findAllDepartment;
	private final FindAllByCollege findAllByCollege;

	@GetMapping
	public List<DepartmentResponse> getAll() {
		return findAllDepartment.invoke();
	}

	@GetMapping("/college/{collegeId}")
	public List<DepartmentResponse> getAllByCollege(@PathVariable("collegeId") Long collegeId) {
		return findAllByCollege.invoke(collegeId);
	}
}
