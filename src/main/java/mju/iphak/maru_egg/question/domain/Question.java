package mju.iphak.maru_egg.question.domain;

import static mju.iphak.maru_egg.common.constant.RenewalYearConst.RENEWAL_YEAR;

import org.hibernate.annotations.ColumnDefault;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import mju.iphak.maru_egg.admission.domain.AdmissionCategory;
import mju.iphak.maru_egg.admission.domain.AdmissionType;
import mju.iphak.maru_egg.answer.domain.Answer;
import mju.iphak.maru_egg.common.entity.BaseEntity;

@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
@Table(name = "questions")
public class Question extends BaseEntity {

	@Column(columnDefinition = "TEXT", nullable = false)
	private String content;

	private String contentToken;

	@Enumerated(EnumType.STRING)
	private AdmissionType admissionType;

	@Enumerated(EnumType.STRING)
	private AdmissionCategory admissionCategory;

	@Column(name = "view_count", nullable = false)
	private int viewCount;

	@ColumnDefault("false")
	private boolean isChecked;

	@Column(name = "renewal_year", columnDefinition = "2024")
	private int renewalYear;

	@OneToOne(mappedBy = "question", orphanRemoval = true)
	private Answer answer;

	public String getDateInformation() {
		return "생성일자: %s, 마지막 DB 갱신일자: %s".formatted(this.getCreatedAt(), this.getUpdatedAt());
	}

	public void incrementViewCount() {
		this.viewCount++;
	}

	public void updateIsChecked() {
		this.isChecked = !this.isChecked;
	}

	public void updateContent(String content) {
		this.content = content != null ? content : this.content;
	}

	public static Question of(String content, String contentToken, AdmissionType admissionType,
		AdmissionCategory admissionCategory) {
		return Question.builder()
			.content(content)
			.contentToken(contentToken)
			.admissionType(admissionType)
			.admissionCategory(admissionCategory)
			.isChecked(false)
			.renewalYear(RENEWAL_YEAR)
			.build();
	}

	public static Question create(String content, String contentToken, AdmissionType admissionType,
		AdmissionCategory admissionCategory) {
		return Question.builder()
			.content(content)
			.contentToken(contentToken)
			.admissionType(admissionType)
			.admissionCategory(admissionCategory)
			.isChecked(true)
			.renewalYear(RENEWAL_YEAR)
			.build();
	}
}
