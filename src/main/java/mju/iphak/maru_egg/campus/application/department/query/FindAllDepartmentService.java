package mju.iphak.maru_egg.campus.application.department.query;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;
import mju.iphak.maru_egg.campus.api.dto.response.DepartmentResponse;
import mju.iphak.maru_egg.campus.repository.DepartmentRepository;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class FindAllDepartmentService implements FindAllDepartment {

	private final DepartmentRepository departmentRepository;

	public List<DepartmentResponse> invoke() {
		return departmentRepository.findAll().stream()
			.map(DepartmentResponse::from)
			.toList();
	}
}
