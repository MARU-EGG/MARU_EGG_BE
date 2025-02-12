package mju.iphak.maru_egg.campus.application.department.command.create;

import mju.iphak.maru_egg.campus.api.dto.request.CreateDepartmentRequest;

public interface CreateDepartment {

	void invoke(CreateDepartmentRequest request);
}
