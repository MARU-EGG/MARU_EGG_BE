package mju.iphak.maru_egg.question.repository;

import static mju.iphak.maru_egg.question.domain.QQuestion.*;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.Tuple;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.core.types.dsl.Expressions;
import com.querydsl.core.types.dsl.NumberTemplate;
import com.querydsl.jpa.impl.JPAQueryFactory;

import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import mju.iphak.maru_egg.admission.domain.AdmissionCategory;
import mju.iphak.maru_egg.admission.domain.AdmissionType;
import mju.iphak.maru_egg.common.dto.pagination.SliceQuestionResponse;
import mju.iphak.maru_egg.question.api.dto.response.SearchedQuestionsResponse;
import mju.iphak.maru_egg.question.domain.Question;
import mju.iphak.maru_egg.question.repository.dto.request.QuestionCoreRequest;
import mju.iphak.maru_egg.question.repository.dto.request.SelectQuestionsRequest;
import mju.iphak.maru_egg.question.repository.dto.response.QuestionCoreResponse;

@Repository
@RequiredArgsConstructor
public class QuestionRepositoryImpl implements QuestionRepositoryCustom {

	private static final double MIN_MATCHING_POINT = 0.005;

	private final JPAQueryFactory queryFactory;

	public Optional<List<QuestionCoreResponse>> searchQuestions(
		final QuestionCoreRequest questionCoreRequest) {
		return Optional.of(
			searchQuestionsByContentTokenAndType(
				questionCoreRequest.content(),
				questionCoreRequest.contentToken(),
				questionCoreRequest.type(),
				questionCoreRequest.category()
			).orElse(Collections.emptyList())
		);
	}

	@Override
	public SliceQuestionResponse<SearchedQuestionsResponse> searchQuestionsOfCursorPaging(
		final SelectQuestionsRequest selectQuestionsRequest) {
		SliceQuestionResponse<SearchedQuestionsResponse> response = searchQuestionsOfCursorPagingByContentWithLikeFunction(
			selectQuestionsRequest.type(),
			selectQuestionsRequest.category(),
			selectQuestionsRequest.content(),
			selectQuestionsRequest.pageable());
		if (response.data().isEmpty()) {
			response = searchQuestionsOfCursorPagingByContentWithFullTextSearch(
				selectQuestionsRequest.type(),
				selectQuestionsRequest.category(),
				selectQuestionsRequest.content(),
				selectQuestionsRequest.cursorViewCount(), selectQuestionsRequest.questionId(),
				selectQuestionsRequest.pageable());
		}
		return response;
	}

	private SliceQuestionResponse<SearchedQuestionsResponse> searchQuestionsOfCursorPagingByContentWithFullTextSearch(
		final AdmissionType type, final AdmissionCategory category, final String content,
		final Integer cursorViewCount, final Long questionId, final Pageable pageable) {

		int pageSize = pageable.getPageSize();
		Integer viewCountCursorKey = getValidCursorViewCount(cursorViewCount);
		Long questionIdCursorKey = getValidCursorQuestionId(questionId);

		NumberTemplate<Double> booleanTemplate = createBooleanTemplateByContentToken(content);
		BooleanExpression cursorPredicate = createCursorPredicate(viewCountCursorKey, questionIdCursorKey);

		List<Question> questions = fetchQuestions(type, category, booleanTemplate, cursorPredicate, pageSize);

		return buildSliceQuestionResponse(pageable, pageSize, questions);
	}

	private SliceQuestionResponse<SearchedQuestionsResponse> searchQuestionsOfCursorPagingByContentWithLikeFunction(
		final AdmissionType type, final AdmissionCategory category, final String content, final Pageable pageable) {

		int pageSize = pageable.getPageSize();

		BooleanBuilder whereClause = buildWhereClause(type, category, content);
		List<Question> questions = queryFactory.selectFrom(question)
			.where(whereClause)
			.limit(pageSize + 1)
			.fetch();

		return buildSliceQuestionResponse(pageable, pageSize, questions);
	}

	private Optional<List<QuestionCoreResponse>> searchQuestionsByContentTokenAndType(
		final String content, final String contentToken, final AdmissionType type, final AdmissionCategory category) {

		NumberTemplate<Double> booleanTemplate = createBooleanTemplateByContent(content);

		List<Tuple> tuples = fetchQuestionsByContentAndTypeAndCategory(booleanTemplate, type, category);

		if (tuples.isEmpty()) {
			booleanTemplate = createBooleanTemplateByContentToken(contentToken);
			tuples = fetchQuestionsByContentAndTypeAndCategory(booleanTemplate, type, category);
		}

		List<QuestionCoreResponse> result = tuples.stream()
			.map(tuple -> QuestionCoreResponse.of(tuple.get(question.id),
				tuple.get(question.contentToken)))
			.collect(Collectors.toList());

		return Optional.of(result);
	}

	private List<Tuple> fetchQuestionsByContentAndTypeAndCategory(
		NumberTemplate<Double> booleanTemplate, AdmissionType type, AdmissionCategory category) {

		BooleanBuilder whereClause = new BooleanBuilder();
		whereClause.and(booleanTemplate.gt(0));
		whereClause.and(question.admissionType.eq(type));

		if (category != null) {
			whereClause.and(question.admissionCategory.eq(category));
		}

		return queryFactory.select(question.id, question.contentToken)
			.from(question)
			.where(whereClause)
			.fetch();
	}

	private NumberTemplate<Double> createBooleanTemplateByContentToken(String contentToken) {
		return Expressions.numberTemplate(Double.class, "function('match', {0}, {1})", question.content, contentToken);
	}

	private NumberTemplate<Double> createBooleanTemplateByContent(String content) {
		return Expressions.numberTemplate(Double.class, "function('match', {0}, {1})", question.content, content);
	}

	private BooleanExpression createCursorPredicate(Integer cursorViewCount, Long questionId) {
		if (cursorViewCount != null && questionId != null) {
			return question.viewCount.lt(cursorViewCount)
				.or(question.viewCount.eq(cursorViewCount).and(question.id.lt(questionId)));
		} else if (cursorViewCount != null) {
			return question.viewCount.lt(cursorViewCount);
		} else if (questionId != null) {
			return question.id.lt(questionId);
		}
		return null;
	}

	private Integer getValidCursorViewCount(Integer cursorViewCount) {
		return (cursorViewCount != null && cursorViewCount > 0) ? cursorViewCount : Integer.MAX_VALUE;
	}

	private Long getValidCursorQuestionId(Long questionId) {
		return (questionId != null && questionId > 0) ? questionId : Long.MAX_VALUE;
	}

	private List<Question> fetchQuestions(
		final AdmissionType type, final AdmissionCategory category,
		NumberTemplate<Double> booleanTemplate, BooleanExpression cursorPredicate, int pageSize) {

		BooleanBuilder whereClause = new BooleanBuilder();
		whereClause.and(question.isChecked.eq(true));
		whereClause.and(booleanTemplate.gt(MIN_MATCHING_POINT));
		if (cursorPredicate != null) {
			whereClause.and(cursorPredicate);
		}

		if (type != null) {
			whereClause.and(question.admissionType.eq(type));
		}

		if (category != null) {
			whereClause.and(question.admissionCategory.eq(category));
		}

		return queryFactory.selectFrom(question)
			.where(whereClause)
			.orderBy(question.viewCount.desc(), question.id.desc())
			.limit(pageSize + 1)
			.fetch();
	}

	private BooleanBuilder buildWhereClause(final AdmissionType type, final AdmissionCategory category,
		final String content) {
		BooleanBuilder whereClause = new BooleanBuilder();
		whereClause.and(question.isChecked.eq(true));
		if (content != null && !content.isEmpty()) {
			whereClause.and(question.content.contains(content));
		}
		if (type != null) {
			whereClause.and(question.admissionType.eq(type));
		}
		if (category != null) {
			whereClause.and(question.admissionCategory.eq(category));
		}
		return whereClause;
	}

	@NotNull
	private SliceQuestionResponse<SearchedQuestionsResponse> buildSliceQuestionResponse(
		final Pageable pageable, final int pageSize, final List<Question> questions) {

		boolean hasNext = questions.size() > pageSize;
		if (hasNext) {
			questions.remove(pageSize);
		}

		List<SearchedQuestionsResponse> questionResponses = questions.stream()
			.map(q -> SearchedQuestionsResponse.of(q.getId(), q.getContent(), q.isChecked()))
			.collect(Collectors.toList());

		Integer nextCursorViewCount = null;
		Long nextQuestionId = null;
		if (!questions.isEmpty()) {
			Question lastQuestion = questions.get(questions.size() - 1);
			nextCursorViewCount = lastQuestion.getViewCount();
			nextQuestionId = lastQuestion.getId();
		}

		return new SliceQuestionResponse<>(questionResponses, pageable.getPageNumber(), pageSize, hasNext,
			nextCursorViewCount, nextQuestionId);
	}
}