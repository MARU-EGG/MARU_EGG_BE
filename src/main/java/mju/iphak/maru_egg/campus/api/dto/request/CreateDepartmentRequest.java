package mju.iphak.maru_egg.campus.api.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import mju.iphak.maru_egg.campus.domain.College;
import mju.iphak.maru_egg.campus.domain.Department;

@Schema(description = "학과 생성 DTO")
public record CreateDepartmentRequest(

	@Schema(example = "컴퓨터공학과")
	String name,

	@Schema(example = "컴퓨터공학과 설명")
	String description,

	@Schema(example = "1")
	Long collegeId
) {
	public Department toEntity(College college) {
		return Department.builder()
			.name(this.name)
			.description(this.description)
			.college(college)
			.build();
	}
}
