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
public class FindAllByCollegeService implements FindAllByCollege {

	private final DepartmentRepository departmentRepository;

	public List<DepartmentResponse> invoke(Long collegeId) {
		return departmentRepository.findByCollegeId(collegeId).stream()
			.map(DepartmentResponse::from)
			.toList();
	}
}
