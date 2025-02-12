package mju.iphak.maru_egg.campus.domain;

import com.fasterxml.jackson.annotation.JsonProperty;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Schema(description = "캠퍼스 종류", enumAsRef = true)
@Getter
@RequiredArgsConstructor
public enum CampusType {
	@JsonProperty("NATURAL") NATURAL("자연캠퍼스"),
	@JsonProperty("HUMANITIES") HUMANITIES("인문캠퍼스"),
	;

	private final String type;

	@Override
	public String toString() {
		return this.type;
	}

	public static CampusType convertToCategory(String category) {
		if (category.equals(NATURAL.getType())) {
			return NATURAL;
		}

		if (category.equals(HUMANITIES.getType())) {
			return HUMANITIES;
		}

		return null;
	}
}
