package mju.iphak.maru_egg.campus.api.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "대학 수정 DTO")
public record UpdateCollegeRequest(

	@Schema(example = "인문캠퍼스")
	String campus,

	@Schema(example = "ICT융합대학")
	String name,

	@Schema(example = "설명")
	String description
) {
}
