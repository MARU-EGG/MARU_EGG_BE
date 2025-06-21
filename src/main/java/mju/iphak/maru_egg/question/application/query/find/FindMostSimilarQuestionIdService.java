package mju.iphak.maru_egg.question.application.query.find;

import static mju.iphak.maru_egg.common.exception.ErrorCode.INTERNAL_ERROR_TEXT_SIMILARITY;

import java.util.Comparator;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;
import mju.iphak.maru_egg.common.utils.NLP.TextSimilarityUtils;
import mju.iphak.maru_egg.question.api.dto.response.SimilarityResult;
import mju.iphak.maru_egg.question.repository.dto.response.QuestionCoreResponse;

@Service
@RequiredArgsConstructor
public class FindMostSimilarQuestionIdService implements FindMostSimilarQuestionId {

	private static final double STANDARD_SIMILARITY = 0.95;

	public Long invoke(List<QuestionCoreResponse> questionCoreResponses, String contentToken) {
		Map<CharSequence, Integer> inputQuestionTfIdf = computeTfIdf(questionCoreResponses, contentToken);

		return questionCoreResponses.stream()
			.map(core -> {
				Map<CharSequence, Integer> coreQuestionTfIdf = computeTfIdf(questionCoreResponses, core.contentToken());
				double similarity = TextSimilarityUtils.computeCosineSimilarity(inputQuestionTfIdf, coreQuestionTfIdf);
				return new SimilarityResult(core.id(), similarity);
			})
			.max(Comparator.comparingDouble(SimilarityResult::similarity))
			.filter(result -> result.similarity() > STANDARD_SIMILARITY)
			.map(SimilarityResult::id)
			.orElse(null);
	}

	private Map<CharSequence, Integer> computeTfIdf(List<QuestionCoreResponse> questionCoreResponses,
		String contentToken) {
		List<String> contentTokens = questionCoreResponses.stream()
			.map(QuestionCoreResponse::contentToken)
			.toList();

		try {
			return TextSimilarityUtils.computeTfIdf(contentTokens, contentToken);
		} catch (Exception e) {
			throw new RuntimeException(String.format(INTERNAL_ERROR_TEXT_SIMILARITY.getMessage()));
		}
	}
}
