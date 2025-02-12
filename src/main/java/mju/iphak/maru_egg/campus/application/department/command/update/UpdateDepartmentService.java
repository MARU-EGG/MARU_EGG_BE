package mju.iphak.maru_egg.campus.application.department.command.update;

import static mju.iphak.maru_egg.common.exception.ErrorCode.*;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import mju.iphak.maru_egg.campus.api.dto.request.UpdateDepartmentRequest;
import mju.iphak.maru_egg.campus.domain.Department;
import mju.iphak.maru_egg.campus.repository.DepartmentRepository;

@Service
@Transactional
@RequiredArgsConstructor
public class UpdateDepartmentService implements UpdateDepartment {

	private final DepartmentRepository departmentRepository;

	public void invoke(final Long departmentId, final UpdateDepartmentRequest request) {
		Department department = departmentRepository.findById(departmentId)
			.orElseThrow(() -> new EntityNotFoundException(
				String.format(NOT_FOUND_DEPARTMENT_BY_ID.getMessage(), departmentId)));
		department.update(request);
	}
}
