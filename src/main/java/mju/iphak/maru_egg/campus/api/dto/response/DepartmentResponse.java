package mju.iphak.maru_egg.campus.api.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import mju.iphak.maru_egg.campus.domain.Department;

@Builder
@Schema(description = "학과 응답 DTO")
public record DepartmentResponse(

	Long id,
	String name,
	Long collegeId
) {
	public static DepartmentResponse from(Department department) {
		return DepartmentResponse.builder()
			.id(department.getId())
			.name(department.getName())
			.collegeId(department.getCollege().getId())
			.build();
	}
}
