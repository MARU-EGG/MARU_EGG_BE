package mju.iphak.maru_egg.campus.application.department.command.delete;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;
import mju.iphak.maru_egg.campus.repository.DepartmentRepository;

@Service
@Transactional
@RequiredArgsConstructor
public class DeleteDepartmentService implements DeleteDepartment {

	private final DepartmentRepository departmentRepository;

	public void invoke(final Long departmentId) {
		departmentRepository.deleteById(departmentId);
	}
}
