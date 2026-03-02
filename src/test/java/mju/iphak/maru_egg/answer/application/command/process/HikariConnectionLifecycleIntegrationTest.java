package mju.iphak.maru_egg.answer.application.command.process;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import java.util.Collections;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;

import com.zaxxer.hikari.HikariDataSource;

import mju.iphak.maru_egg.TestcontainersConfiguration;
import mju.iphak.maru_egg.admission.domain.AdmissionCategory;
import mju.iphak.maru_egg.admission.domain.AdmissionType;
import mju.iphak.maru_egg.answer.api.dto.request.LLMAskQuestionRequest;
import mju.iphak.maru_egg.answer.api.dto.response.LLMAnswerResponse;
import mju.iphak.maru_egg.answer.application.query.rag.RagAnswer;
import mju.iphak.maru_egg.answer.domain.Answer;
import mju.iphak.maru_egg.question.api.dto.request.QuestionRequest;
import mju.iphak.maru_egg.question.api.dto.response.QuestionResponse;
import mju.iphak.maru_egg.question.domain.Question;
import reactor.core.publisher.Mono;
import reactor.core.publisher.Sinks;

@SpringBootTest(properties = {
	"spring.datasource.hikari.maximum-pool-size=2",
	"spring.datasource.hikari.minimum-idle=0",
	"spring.datasource.hikari.connection-timeout=1000"
})
@ActiveProfiles("test")
@Import(TestcontainersConfiguration.class)
class HikariConnectionLifecycleIntegrationTest {

	@Autowired
	private ProcessAnswer processAnswer;

	@Autowired
	private HikariDataSource dataSource;

	@MockBean
	private RagAnswer ragAnswer;

	@DisplayName("장시간 RAG 응답 대기 중에는 Hikari 커넥션을 점유하지 않는다")
	@Test
	void returnsConnectionBeforeWaitingForRagResponse() throws Exception {
		waitUntilPoolHasNoActiveConnections();
		CountDownLatch subscribed = new CountDownLatch(1);
		Sinks.One<LLMAnswerResponse> responseSink = Sinks.one();
		QuestionRequest request = request();

		when(ragAnswer.invoke(any(LLMAskQuestionRequest.class))).thenReturn(Mono.defer(() -> {
			subscribed.countDown();
			return responseSink.asMono();
		}));

		ExecutorService executor = Executors.newSingleThreadExecutor();
		try {
			CompletableFuture<QuestionResponse> result = CompletableFuture.supplyAsync(
				() -> processAnswer.invoke(request, "커넥션 테스트"),
				executor
			);

			assertThat(subscribed.await(5, TimeUnit.SECONDS)).isTrue();
			assertThat(dataSource.getHikariPoolMXBean().getActiveConnections()).isZero();
			assertThat(dataSource.getHikariPoolMXBean().getThreadsAwaitingConnection()).isZero();

			responseSink.tryEmitValue(validAnswer(request));
			assertThat(result.get(10, TimeUnit.SECONDS).answer().content()).isEqualTo("커넥션 테스트 답변입니다.");
			waitUntilPoolHasNoActiveConnections();
			assertThat(dataSource.getHikariPoolMXBean().getActiveConnections()).isZero();
		} finally {
			executor.shutdownNow();
		}
	}

	private void waitUntilPoolHasNoActiveConnections() throws InterruptedException {
		long deadline = System.nanoTime() + TimeUnit.SECONDS.toNanos(5);
		while (dataSource.getHikariPoolMXBean().getActiveConnections() != 0 && System.nanoTime() < deadline) {
			Thread.sleep(25);
		}
		assertThat(dataSource.getHikariPoolMXBean().getActiveConnections()).isZero();
	}

	private QuestionRequest request() {
		return new QuestionRequest(
			AdmissionType.SUSI,
			AdmissionCategory.ADMISSION_GUIDELINE,
			"커넥션 테스트 질문"
		);
	}

	private LLMAnswerResponse validAnswer(QuestionRequest request) {
		Question question = Question.of(
			request.content(),
			"커넥션 테스트",
			request.type(),
			request.category()
		);
		Answer answer = Answer.of(question, "커넥션 테스트 답변입니다.");
		return LLMAnswerResponse.of(
			request.type().getType(),
			request.category().getCategory(),
			answer,
			Collections.emptyList()
		);
	}
}
