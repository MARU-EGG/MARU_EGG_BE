package mju.iphak.maru_egg.campus.application.college.query.find;

import java.util.List;

import mju.iphak.maru_egg.campus.api.dto.response.CollegeResponse;
import mju.iphak.maru_egg.campus.domain.CampusType;

public interface FindAllByCampusType {

	List<CollegeResponse> invoke(CampusType campusType);
}
