package mju.iphak.maru_egg.campus.application.department.query;

import java.util.List;

import mju.iphak.maru_egg.campus.api.dto.response.DepartmentResponse;

public interface FindAllByCollege {
	List<DepartmentResponse> invoke(Long collegeId);
}
