package mju.iphak.maru_egg.campus.api.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import mju.iphak.maru_egg.campus.domain.CampusType;
import mju.iphak.maru_egg.campus.domain.College;

@Builder
@Schema(description = "대학 응답 DTO")
public record CollegeResponse(

	Long id,
	CampusType campus,
	String name
) {
	public static CollegeResponse from(College college) {
		return CollegeResponse.builder()
			.id(college.getId())
			.campus(college.getCampus())
			.name(college.getName())
			.build();
	}
}
