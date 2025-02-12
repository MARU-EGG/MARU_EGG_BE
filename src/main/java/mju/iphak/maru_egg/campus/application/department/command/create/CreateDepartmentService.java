package mju.iphak.maru_egg.campus.application.department.command.create;

import static mju.iphak.maru_egg.common.exception.ErrorCode.*;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import mju.iphak.maru_egg.campus.api.dto.request.CreateDepartmentRequest;
import mju.iphak.maru_egg.campus.domain.College;
import mju.iphak.maru_egg.campus.domain.Department;
import mju.iphak.maru_egg.campus.repository.CollegeRepository;
import mju.iphak.maru_egg.campus.repository.DepartmentRepository;

@Service
@Transactional
@RequiredArgsConstructor
public class CreateDepartmentService implements CreateDepartment {

	private final DepartmentRepository departmentRepository;
	private final CollegeRepository collegeRepository;

	public void invoke(final CreateDepartmentRequest request) {
		College college = collegeRepository.findById(request.collegeId())
			.orElseThrow(() -> new EntityNotFoundException(
				String.format(NOT_FOUND_COLLEGE_BY_ID.getMessage(), request.collegeId())));

		validateDuplicateDepartmentName(request);

		Department department = request.toEntity(college);
		departmentRepository.save(department);
	}

	private void validateDuplicateDepartmentName(final CreateDepartmentRequest request) {
		if (departmentRepository.existsByName(request.name())) {
			throw new IllegalArgumentException(String.format(DUPLICATE_DEPARTMENT_NAME.getMessage(), request.name()));
		}
	}
}
