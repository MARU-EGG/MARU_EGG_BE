package mju.iphak.maru_egg.campus.application.college.command.update;

import mju.iphak.maru_egg.campus.api.dto.request.UpdateCollegeRequest;

public interface UpdateCollege {
	void invoke(Long collegeId, UpdateCollegeRequest request);
}
