package mju.iphak.maru_egg.question.domain;

import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import mju.iphak.maru_egg.common.entity.BaseEntity;

@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
@Table(name = "questions")
public class Question extends BaseEntity {

	private String content;
	private QuestionType questionType;
	private QuestionCategory questionCategory;
}
