package mju.iphak.maru_egg.campus.domain;

import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import mju.iphak.maru_egg.campus.api.dto.request.UpdateDepartmentRequest;
import mju.iphak.maru_egg.common.entity.BaseEntity;

@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
@Table(name = "department")
public class Department extends BaseEntity {

	private String name;

	private String description;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "college_id")
	private College college;

	public void update(UpdateDepartmentRequest request) {
		this.name = request.name() != null ? request.name() : this.name;
		this.description = request.description() != null ? request.description() : this.description;
	}
}
