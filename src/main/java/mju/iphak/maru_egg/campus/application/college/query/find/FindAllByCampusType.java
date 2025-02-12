package mju.iphak.maru_egg.campus.application.college.query.find;

import java.util.List;

import mju.iphak.maru_egg.campus.api.dto.response.CollegeResponse;

public interface FindAllByCampusType {

	List<CollegeResponse> invoke(String campusType);
}
