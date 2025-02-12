package mju.iphak.maru_egg.campus.api.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import mju.iphak.maru_egg.campus.domain.CampusType;
import mju.iphak.maru_egg.campus.domain.College;

@Schema(description = "대학 생성 DTO")
public record CreateCollegeRequest(

	@Schema(example = "자연캠퍼스", description = "자연캠퍼스 / 인문캠퍼스 둘 중 하나")
	@NotNull
	String campus,

	@Schema(example = "ICT융합대학")
	@NotNull
	String name,

	@Schema(example = "설명")
	String description
) {
	public College toEntity() {
		return College.builder()
			.campus(CampusType.convertToCampusType(this.campus))
			.name(this.name)
			.description(this.description)
			.build();
	}
}
