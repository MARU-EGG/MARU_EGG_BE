package mju.iphak.maru_egg.question.repository;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import mju.iphak.maru_egg.TestcontainersConfiguration;
import mju.iphak.maru_egg.admission.domain.AdmissionCategory;
import mju.iphak.maru_egg.admission.domain.AdmissionType;
import mju.iphak.maru_egg.common.config.QueryDslConfig;
import mju.iphak.maru_egg.question.domain.Question;

@DataJpaTest
@ActiveProfiles("test")
@Import({TestcontainersConfiguration.class, QueryDslConfig.class})
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Transactional(propagation = Propagation.NOT_SUPPORTED)
class QuestionViewCountConcurrencyTest {

	@Autowired
	private QuestionRepository questionRepository;

	@Autowired
	private JdbcTemplate jdbcTemplate;

	@DisplayName("동시에 들어온 조회수 증가 요청을 유실하지 않는다")
	@Test
	void incrementsEveryConcurrentRequest() throws Exception {
		Question question = questionRepository.saveAndFlush(Question.of(
			"동시 조회수 테스트",
			"동시 조회수",
			AdmissionType.SUSI,
			AdmissionCategory.ADMISSION_GUIDELINE
		));
		int requests = 100;
		int workers = 20;
		CountDownLatch ready = new CountDownLatch(workers);
		CountDownLatch start = new CountDownLatch(1);
		ExecutorService executor = Executors.newFixedThreadPool(workers);

		try {
			List<Future<Integer>> results = new ArrayList<>();
			for (int i = 0; i < requests; i++) {
				results.add(executor.submit(() -> {
					ready.countDown();
					start.await();
					return questionRepository.incrementViewCount(question.getId());
				}));
			}
			assertThat(ready.await(5, TimeUnit.SECONDS)).isTrue();
			start.countDown();
			for (Future<Integer> result : results) {
				assertThat(result.get(10, TimeUnit.SECONDS)).isOne();
			}
		} finally {
			executor.shutdownNow();
		}

		Integer viewCount = jdbcTemplate.queryForObject(
			"select view_count from questions where id = ?",
			Integer.class,
			question.getId()
		);
		assertThat(viewCount).isEqualTo(requests);
	}
}
