package mju.iphak.maru_egg.answer.utils;

import java.util.List;
import java.util.stream.Collectors;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import mju.iphak.maru_egg.answer.api.dto.response.LLMAnswerResponse;
import mju.iphak.maru_egg.answerreference.dto.response.AnswerReferenceResponse;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class AnswerGuideUtils {

	private static final String INVALID_ANSWER_ONE = "해당 내용에 대한 정보는 존재하지 않습니다.";
	private static final String INVALID_ANSWER_TWO = "제공된 정보 내에서";
	private static final String INVALID_ANSWER_THREE = "구체적인 정보를 찾을 수 없습니다.";

	private static final String BASE_MESSAGE = "질문해주신 내용에 대한 적절한 정보을 발견하지 못 했습니다.\n\n대신 질문해주신 내용에 가장 적합한 자료들을 골라봤어요. 참고하셔서 다시 질문해주세요!\n\n\n\n";
	private static final String REFERENCE_TEXT_AND_LINK = "**참고자료 %d** : [%s **[바로가기]**](%s)\n\n";
	private static final String IPHAK_OFFICE_NUMBER_GUIDE = "\n\n입학처 상담 전화번호 : 02-300-1799, 1800";
	private static final String PAGE_SPLIT_REGEX = "page=";
	private static final String PAGE_ANNOUNCE = "페이지 ";

	public static boolean isInvalidAnswer(LLMAnswerResponse llmAnswerResponse) {
		return llmAnswerResponse.answer() == null || llmAnswerResponse.answer().contains(INVALID_ANSWER_ONE)
			|| llmAnswerResponse.answer().contains(INVALID_ANSWER_TWO)
			|| llmAnswerResponse.answer().contains(INVALID_ANSWER_THREE);
	}

	public static String generateGuideAnswer(final List<AnswerReferenceResponse> references) {
		return BASE_MESSAGE + references.stream()
			.map(reference -> {
				String page = extractPageFromLink(reference.link());
				String title = reference.title() + " " + page;
				return String.format(REFERENCE_TEXT_AND_LINK,
					references.indexOf(reference) + 1,
					title,
					reference.link());
			})
			.collect(Collectors.joining("\n")) + IPHAK_OFFICE_NUMBER_GUIDE;
	}

	private static String extractPageFromLink(String link) {
		try {
			return link.split(PAGE_SPLIT_REGEX)[1] + PAGE_ANNOUNCE;
		} catch (ArrayIndexOutOfBoundsException e) {
			return "알 수 없는 페이지 ";
		}
	}
}
