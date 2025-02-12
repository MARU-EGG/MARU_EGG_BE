package mju.iphak.maru_egg.campus.application.college.command.create;

import mju.iphak.maru_egg.campus.api.dto.request.CreateCollegeRequest;

public interface CreateCollege {
	void invoke(CreateCollegeRequest request);
}
