package mju.iphak.maru_egg.campus.application.college.query.find;

import java.util.List;

import mju.iphak.maru_egg.campus.api.dto.response.CollegeResponse;

public interface FindAllCollege {

	List<CollegeResponse> invoke();
}
