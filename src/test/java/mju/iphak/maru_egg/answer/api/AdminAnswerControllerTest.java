package mju.iphak.maru_egg.answer.api;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.util.List;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.ResultMatcher;

import mju.iphak.maru_egg.admission.domain.AdmissionCategory;
import mju.iphak.maru_egg.admission.domain.AdmissionType;
import mju.iphak.maru_egg.answer.application.command.update.UpdateAnswerContentService;
import mju.iphak.maru_egg.answer.domain.Answer;
import mju.iphak.maru_egg.answer.api.dto.request.UpdateAnswerContentRequest;
import mju.iphak.maru_egg.answer.repository.AnswerRepository;
import mju.iphak.maru_egg.common.IntegrationTest;
import mju.iphak.maru_egg.question.domain.Question;
import mju.iphak.maru_egg.question.repository.QuestionRepository;

@WithMockUser(roles = "ADMIN")
class AdminAnswerControllerTest extends IntegrationTest {

	@Autowired
	private UpdateAnswerContentService updateAnswerContent;

	@Autowired
	private QuestionRepository questionRepository;

	@Autowired
	private AnswerRepository answerRepository;

	@BeforeEach
	void setUp() {
		initializeTestData();
	}

	@AfterEach
	void tearDown() {
		answerRepository.deleteAllInBatch();
		questionRepository.deleteAllInBatch();
	}

	@DisplayName("[성공] 답변 수정 요청")
	@Test
	void 답변_수정_성공() throws Exception {
		// given
		Long questionId = getFirstQuestionId();
		UpdateAnswerContentRequest request = new UpdateAnswerContentRequest(questionId, "새로운 답변 내용");

		// when & then
		UpdateAnswerContent(request, status().isOk());
	}

	@DisplayName("[실패] 답변 수정 요청 - 잘못된 JSON 형식")
	@Test
	void 답변_수정_실패_잘못된_JSON_형식() throws Exception {
		// given
		String invalidJson = "{\"id\": \"ㅇㅇ\", \"content\": \"새로운 답변 내용\"}";

		// when & then
		UpdateAnswerContent(invalidJson, status().isBadRequest());
	}

	@DisplayName("[실패] 답변 수정 요청 - 존재하지 않는 답변 ID")
	@Test
	void 답변_수정_실패_존재하지_않는_답변_ID() throws Exception {
		// given
		UpdateAnswerContentRequest request = new UpdateAnswerContentRequest(999L, "새로운 답변 내용");

		// when & then
		UpdateAnswerContent(request, status().isNotFound());
	}

	private void UpdateAnswerContent(Object content, ResultMatcher expectedStatus) throws Exception {
		mvc.perform(put("/api/admin/answers")
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(content)))
			.andExpect(expectedStatus)
			.andDo(print());
	}

	private void UpdateAnswerContent(String content, ResultMatcher expectedStatus) throws Exception {
		mvc.perform(put("/api/admin/answers")
				.contentType(MediaType.APPLICATION_JSON)
				.content(content))
			.andExpect(expectedStatus)
			.andDo(print());
	}

	private void initializeTestData() {
		Question question = Question.of("질문", "질문", AdmissionType.SUSI, AdmissionCategory.ADMISSION_GUIDELINE);
		Answer answer = Answer.of(question, "답변");

		questionRepository.saveAndFlush(question);
		answerRepository.saveAndFlush(answer);
	}

	private Long getFirstQuestionId() {
		List<Question> questions = questionRepository.findAll();
		return questions.get(0).getId();
	}
}
