package mju.iphak.maru_egg.campus.domain;

import java.util.List;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import mju.iphak.maru_egg.campus.api.dto.request.UpdateCollegeRequest;
import mju.iphak.maru_egg.common.entity.BaseEntity;

@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
@Table(name = "college")
public class College extends BaseEntity {

	@Column(nullable = false)
	@Enumerated(EnumType.STRING)
	private CampusType campus;

	@Column(nullable = false)
	private String name;

	private String description;

	@OneToMany(mappedBy = "college", cascade = CascadeType.ALL, orphanRemoval = true)
	private List<Department> departments;

	public void update(UpdateCollegeRequest request) {
		this.campus = request.campus() != null ? CampusType.convertToCampusType(request.campus()) : this.campus;
		this.name = request.name() != null ? request.name() : this.name;
		this.description = request.description() != null ? request.description() : this.description;
	}
}
