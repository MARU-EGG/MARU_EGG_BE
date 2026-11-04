package mju.iphak.maru_egg.question.application.process;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Optional;
import java.util.concurrent.atomic.AtomicBoolean;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import org.springframework.transaction.support.AbstractPlatformTransactionManager;
import org.springframework.transaction.support.DefaultTransactionStatus;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import org.springframework.transaction.support.TransactionTemplate;

import mju.iphak.maru_egg.admission.domain.AdmissionCategory;
import mju.iphak.maru_egg.admission.domain.AdmissionType;
import mju.iphak.maru_egg.answer.api.dto.request.LLMAskQuestionRequest;
import mju.iphak.maru_egg.answer.api.dto.response.LLMAnswerResponse;
import mju.iphak.maru_egg.answer.application.command.create.CreateRAGAnswer;
import mju.iphak.maru_egg.answer.application.command.process.ProcessAnswer;
import mju.iphak.maru_egg.answer.application.command.process.ProcessAnswerService;
import mju.iphak.maru_egg.answer.application.query.rag.RagAnswer;
import mju.iphak.maru_egg.answer.domain.Answer;
import mju.iphak.maru_egg.question.api.dto.request.QuestionRequest;
import mju.iphak.maru_egg.question.api.dto.response.QuestionResponse;
import mju.iphak.maru_egg.question.application.query.find.FindMostSimilarQuestionId;
import mju.iphak.maru_egg.question.application.query.find.FindQuestion;
import mju.iphak.maru_egg.question.application.query.process.ProcessQuestion;
import mju.iphak.maru_egg.question.application.query.process.ProcessQuestionService;
import mju.iphak.maru_egg.question.domain.Question;
import mju.iphak.maru_egg.question.repository.QuestionRepository;
import reactor.core.publisher.Mono;

class TransactionBoundaryTest {

	private AnnotationConfigApplicationContext context;
	private QuestionRepository questionRepository;
	private ProcessAnswer processAnswer;
	private RagAnswer ragAnswer;
	private CreateRAGAnswer createRAGAnswer;

	@BeforeEach
	void setUp() {
		context = new AnnotationConfigApplicationContext();
		context.register(TransactionTestConfiguration.class);

		questionRepository = mock(QuestionRepository.class);
		processAnswer = mock(ProcessAnswer.class);
		ragAnswer = mock(RagAnswer.class);
		createRAGAnswer = mock(CreateRAGAnswer.class);

		context.registerBean(QuestionRepository.class, () -> questionRepository);
		context.registerBean(ProcessAnswer.class, () -> processAnswer);
		context.registerBean(RagAnswer.class, () -> ragAnswer);
		context.registerBean(CreateRAGAnswer.class, () -> createRAGAnswer);
		context.registerBean(FindQuestion.class, () -> mock(FindQuestion.class));
		context.registerBean(FindMostSimilarQuestionId.class, () -> mock(FindMostSimilarQuestionId.class));
		context.registerBean("processQuestionService", ProcessQuestionService.class);
		context.registerBean("processAnswerService", ProcessAnswerService.class);
		context.refresh();
	}

	@AfterEach
	void tearDown() {
		context.close();
	}

	@DisplayName("질문 후보 조회 트랜잭션을 끝낸 뒤 새 답변 처리를 호출한다")
	@Test
	void releasesReadTransactionBeforeCallingAnswerFlow() {
		AtomicBoolean repositoryTransactionActive = new AtomicBoolean();
		AtomicBoolean answerFlowTransactionActive = new AtomicBoolean();
		QuestionRequest request = request();

		when(questionRepository.searchQuestions(any())).thenAnswer(invocation -> {
			repositoryTransactionActive.set(TransactionSynchronizationManager.isActualTransactionActive());
			return Optional.empty();
		});
		when(processAnswer.invoke(any(), any())).thenAnswer(invocation -> {
			answerFlowTransactionActive.set(TransactionSynchronizationManager.isActualTransactionActive());
			return QuestionResponse.valueOfNotFoundRAG(request.content(), "답변 없음");
		});

		ProcessQuestion service = context.getBean("processQuestionService", ProcessQuestion.class);
		service.invoke(request);

		assertThat(repositoryTransactionActive).isTrue();
		assertThat(answerFlowTransactionActive).isFalse();
	}

	@DisplayName("RAG 응답을 기다릴 때는 트랜잭션이 없고 저장할 때만 쓰기 트랜잭션을 연다")
	@Test
	void keepsExternalCallOutsideTransactionAndSaveInsideTransaction() {
		AtomicBoolean ragSubscriptionTransactionActive = new AtomicBoolean();
		AtomicBoolean saveTransactionActive = new AtomicBoolean();
		QuestionRequest request = request();
		LLMAnswerResponse response = validAnswer(request);

		when(ragAnswer.invoke(any(LLMAskQuestionRequest.class))).thenReturn(Mono.defer(() -> {
			ragSubscriptionTransactionActive.set(TransactionSynchronizationManager.isActualTransactionActive());
			return Mono.just(response);
		}));
		doAnswer(invocation -> {
			saveTransactionActive.set(TransactionSynchronizationManager.isActualTransactionActive());
			return null;
		}).when(createRAGAnswer).invoke(any());

		ProcessAnswer service = context.getBean("processAnswerService", ProcessAnswer.class);
		service.invoke(request, "수시 일정");

		assertThat(ragSubscriptionTransactionActive).isFalse();
		assertThat(saveTransactionActive).isTrue();
	}

	private QuestionRequest request() {
		return new QuestionRequest(
			AdmissionType.SUSI,
			AdmissionCategory.ADMISSION_GUIDELINE,
			"수시 일정 알려주세요."
		);
	}

	private LLMAnswerResponse validAnswer(QuestionRequest request) {
		Question question = Question.of(
			request.content(),
			"수시 일정",
			request.type(),
			request.category()
		);
		Answer answer = Answer.of(question, "수시 일정 답변입니다.");
		return LLMAnswerResponse.of(
			request.type().getType(),
			request.category().getCategory(),
			answer,
			null
		);
	}

	@EnableTransactionManagement
	static class TransactionTestConfiguration {

		@org.springframework.context.annotation.Bean
		PlatformTransactionManager transactionManager() {
			return new InMemoryTransactionManager();
		}

		@org.springframework.context.annotation.Bean("readTransactionTemplate")
		TransactionTemplate readTransactionTemplate(PlatformTransactionManager transactionManager) {
			TransactionTemplate template = new TransactionTemplate(transactionManager);
			template.setReadOnly(true);
			return template;
		}

		@org.springframework.context.annotation.Bean("writeTransactionTemplate")
		TransactionTemplate writeTransactionTemplate(PlatformTransactionManager transactionManager) {
			return new TransactionTemplate(transactionManager);
		}
	}

	@SuppressWarnings("serial")
	private static final class InMemoryTransactionManager extends AbstractPlatformTransactionManager {

		@Override
		protected Object doGetTransaction() {
			return new Object();
		}

		@Override
		protected void doBegin(Object transaction, TransactionDefinition definition) {
			// AbstractPlatformTransactionManager manages synchronization state for this test double.
		}

		@Override
		protected void doCommit(DefaultTransactionStatus status) {
		}

		@Override
		protected void doRollback(DefaultTransactionStatus status) {
		}
	}
}
