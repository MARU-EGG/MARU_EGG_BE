package mju.iphak.maru_egg.campus.application.department.command.update;

import mju.iphak.maru_egg.campus.api.dto.request.UpdateDepartmentRequest;

public interface UpdateDepartment {
	void invoke(Long departmentId, UpdateDepartmentRequest request);
}
