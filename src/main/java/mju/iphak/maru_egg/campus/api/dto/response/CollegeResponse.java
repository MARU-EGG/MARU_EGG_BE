package mju.iphak.maru_egg.campus.api.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import mju.iphak.maru_egg.campus.domain.College;

@Builder
@Schema(description = "단과대학 응답 DTO")
public record CollegeResponse(

	@Schema(example = "1")
	Long collegeId,

	@Schema(example = "자연캠퍼스")
	String campus,

	@Schema(example = "ICT융합대학교")
	String name
) {
	public static CollegeResponse from(College college) {
		return CollegeResponse.builder()
			.collegeId(college.getId())
			.campus(college.getCampus().getType())
			.name(college.getName())
			.build();
	}
}
