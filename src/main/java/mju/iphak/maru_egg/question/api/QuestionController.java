package mju.iphak.maru_egg.question.api;

import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.v3.oas.annotations.Parameter;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import mju.iphak.maru_egg.common.dto.pagination.SliceQuestionResponse;
import mju.iphak.maru_egg.question.api.swagger.QuestionControllerDocs;
import mju.iphak.maru_egg.question.application.query.find.FindAllPagedQuestions;
import mju.iphak.maru_egg.question.application.query.find.FindAllQuestionsService;
import mju.iphak.maru_egg.question.application.query.find.FindQuestion;
import mju.iphak.maru_egg.question.application.query.process.ProcessQuestion;
import mju.iphak.maru_egg.question.api.dto.request.FindQuestionsRequest;
import mju.iphak.maru_egg.question.api.dto.request.QuestionRequest;
import mju.iphak.maru_egg.question.api.dto.request.SearchQuestionsRequest;
import mju.iphak.maru_egg.question.api.dto.response.QuestionListItemResponse;
import mju.iphak.maru_egg.question.api.dto.response.QuestionResponse;
import mju.iphak.maru_egg.question.api.dto.response.SearchedQuestionsResponse;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api")
public class QuestionController implements QuestionControllerDocs {

	private final ProcessQuestion processQuestion;
	private final FindAllQuestionsService findAllQuestionsService;
	private final FindQuestion findQuestion;
	private final FindAllPagedQuestions findAllPagedQuestions;

	@PostMapping("/questions")
	public QuestionResponse question(@Valid @RequestBody QuestionRequest request) {
		return processQuestion.invoke(request);
	}

	@GetMapping("/questions")
	public List<QuestionListItemResponse> getQuestions(@Valid @ModelAttribute FindQuestionsRequest request) {
		return findAllQuestionsService.invoke(request.type(), request.category());
	}

	@GetMapping("/questions/search")
	public SliceQuestionResponse<SearchedQuestionsResponse> searchQuestions(
		@Valid @ModelAttribute SearchQuestionsRequest request) {
		return findAllPagedQuestions.invoke(request);
	}

	@GetMapping("/question")
	public QuestionResponse getQuestion(
		@Parameter(description = "조회할 질문 아이디", example = "1") @RequestParam("questionId") Long questionId) {
		return findQuestion.invoke(questionId);
	}
}