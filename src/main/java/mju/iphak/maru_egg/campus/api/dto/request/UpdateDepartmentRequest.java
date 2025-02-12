package mju.iphak.maru_egg.campus.api.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "학과 수정 DTO")
public record UpdateDepartmentRequest(

	@Schema(example = "컴퓨터공학과")
	String name,

	@Schema(example = "설명")
	String description
) {
}
