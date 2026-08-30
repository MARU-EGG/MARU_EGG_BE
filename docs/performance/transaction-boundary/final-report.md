# RAG 외부 호출 트랜잭션 분리 및 부하 테스트 최종 리포트

## 1. 결론

외부 RAG 호출을 DB 트랜잭션 밖으로 분리하는 작업은 실제로 HikariCP 커넥션 고갈을 크게 줄였다.

- 10 VU 스모크에서 외부 응답 대기 중 Hikari active peak가 `10 → 1`로 감소했다.
- Tomcat 한도보다 낮은 100 VU 대조 실험에서 Hikari connection timeout이 `7,627 → 8`건으로 99.90% 감소했다.
- 같은 100 VU에서 요청 성공률은 `5.29% → 97.60%`, 성공 처리량은 약 `3.83 → 15.16 req/s`로 개선됐다.
- 3,000 VU에서는 Hikari timeout이 `58,579 → 481`건으로 99.18% 감소했지만, 양쪽 모두 정상 처리 기준을 충족하지 못했다.
- 리팩터링 후 3,000 VU의 다음 병목은 Tomcat 200개 플랫폼 스레드와 짧은 DB 트랜잭션의 순간 폭주였다. 따라서 이 결과를 “3,000 동시 사용자를 안정적으로 처리했다”라고 해석할 수는 없다.

핵심은 OSIV 비활성화 자체가 아니라, DB 접근 구간만 `TransactionTemplate`로 감싸 외부 I/O 전에 트랜잭션을 끝낸 것이다.

## 2. 기존 구조의 문제

기존 호출 흐름은 다음과 같았다.

```text
ProcessQuestionService.invoke() @Transactional
  → 유사 질문 검색
  → ProcessAnswerService.invoke() @Transactional(readOnly = true)
  → RagAnswerService.invoke() NOT_SUPPORTED
  → 외부 RAG 응답을 5~10초 동안 대기
  → CreateRAGAnswerService.invoke() REQUIRES_NEW
  → 질문·답변 저장
```

`NOT_SUPPORTED`가 실행 중인 트랜잭션을 중단해도, 중단된 트랜잭션의 `EntityManager`와 이미 획득한 JDBC 커넥션은 외부 호출이 끝날 때까지 보존될 수 있다. 실제 스모크 테스트에서 신규 요청 10개가 외부 응답을 기다리는 동안 Hikari active가 10까지 올라가 이 동작을 확인했다.

또한 저장 단계의 `REQUIRES_NEW`는 중단된 외부 트랜잭션과 별도로 새 커넥션을 요구하므로, 부하 상황에서는 풀 고갈을 더 빠르게 만들 수 있었다.

OSIV는 요청 종료까지 영속성 컨텍스트를 열어 두는 별개의 경계다. 테스트 프로필에서는 `spring.jpa.open-in-view=false`로 껐지만, 서비스 진입점의 명시적 `@Transactional`이 외부 호출을 감싸는 문제는 OSIV 설정만으로 해결되지 않는다.

## 3. 적용한 코드 구조

### 3.1 트랜잭션 템플릿

읽기와 쓰기의 의도를 분리한 템플릿을 등록했다.

```java
@Bean("readTransactionTemplate")
TransactionTemplate readTransactionTemplate(PlatformTransactionManager transactionManager) {
    TransactionTemplate template = new TransactionTemplate(transactionManager);
    template.setReadOnly(true);
    return template;
}

@Bean("writeTransactionTemplate")
TransactionTemplate writeTransactionTemplate(PlatformTransactionManager transactionManager) {
    return new TransactionTemplate(transactionManager);
}
```

### 3.2 신규 질문 경로

```text
TX 1: 유사 질문 검색
  → commit
  → HikariCP에 커넥션 반납

트랜잭션 없음: 외부 RAG 호출과 5~10초 응답 대기

TX 2: 질문·답변·참고 자료 저장
  → commit
  → HikariCP에 커넥션 반납
```

구현은 다음 책임으로 나눴다.

- `ProcessQuestionService`: `readTransactionTemplate.execute` 안에서 유사 질문 검색만 수행한다.
- `ProcessAnswerService`: 트랜잭션 없이 RAG 응답을 기다린 뒤 `writeTransactionTemplate.executeWithoutResult` 안에서 저장만 수행한다.
- `RagAnswerService`: 더 이상 `NOT_SUPPORTED`에 의존하지 않는다.
- `CreateRAGAnswerService`: `REQUIRES_NEW`를 제거하고 호출자가 만든 쓰기 트랜잭션에 참여한다.

### 3.3 기존 질문 경로의 동시성 보완

기존 `question.increaseViewCount()` 방식은 동시에 읽은 요청들이 같은 값을 덮어쓸 수 있었다. 이를 DB의 단일 원자 연산으로 변경했다.

```sql
UPDATE questions
SET view_count = view_count + 1
WHERE question_id = ?
```

100개 동시 증가 통합 테스트에서 최종 값 100을 확인했다. 응답 DTO는 `viewCount`를 반환하지 않으므로 영속성 컨텍스트 안의 기존 엔티티 값이 즉시 갱신되지 않는 문제도 응답 계약에는 영향을 주지 않는다.

## 4. 검증한 논리적 경계

자동화 테스트로 다음을 확인했다.

- RAG publisher를 구독하는 시점에는 활성 트랜잭션이 없다.
- RAG 응답이 도착한 뒤 저장 시점에는 쓰기 트랜잭션이 활성화된다.
- 실제 MySQL과 HikariCP를 사용한 통합 테스트에서 RAG 응답을 보류하는 동안 active와 pending 커넥션은 0이다.

구조상 외부 호출 성공 후 저장이 실패하면 쓰기 트랜잭션의 DB 변경만 롤백된다. 검색 트랜잭션과 저장 트랜잭션 사이에는 원자성이 없다. 외부 호출 도중 다른 요청이 같은 질문을 먼저 저장할 수 있으므로 DB 유니크 제약이나 중복 병합 정책은 별도 과제다.

## 5. 부하 테스트 설계

### 5.1 비교 대상

- 베이스라인: `c7e1268`, 트랜잭션 코드는 수정하지 않았다.
- 베이스라인 실행 호환 패치: 잘못된 `renewal_year columnDefinition`만 DDL 생성이 가능하도록 수정했다. 트랜잭션 동작에는 영향을 주지 않는다.
- 리팩터링 코드: `76a8ba5`.
- 부하 테스트 하네스: `b278eaa`.

### 5.2 동일 조건

- Java 17, MySQL 8.0, 로컬 Docker.
- HikariCP: `minimumIdle=20`, `maximumPoolSize=20`, `connectionTimeout=1s`.
- Tomcat: 최대 스레드 200, accept queue 4,000, max connection 8,192.
- MySQL `max_connections=500`.
- k6 요청 timeout 30초.
- Mock RAG: 질문별 결정적 지연 5,000~10,000ms, 오류 0건.
- 요청 구성: 기존 질문 60%, 매번 다른 신규 질문 40%.
- 기존 질문 6개를 실제 API로 워밍업한 뒤 Mock 호출 수를 초기화했다.
- 측정 전 같은 6개를 다시 요청해 Mock 호출 0건을 확인했다.
- 측정 구간의 신규 질문 선택 수와 최종 Mock 호출 수가 같은지도 자동 검증했다. 정상 스모크에서는 `54 = 54`였다.

### 5.3 부하 프로필

| 프로필 | 단계 | 목적 |
|---|---:|---|
| 스모크 | 10 VU | 트랜잭션 중 커넥션 점유를 가장 단순하게 관찰 |
| 대조 | 100 VU, 90초 유지 | Tomcat 200 스레드 한도 아래에서 커넥션 분리 효과 비교 |
| 최대 부하 | 100→500→1,000→2,000→3,000 VU | 다음 병목과 장애 양상 확인 |

## 6. 결과

### 6.1 10 VU 스모크

| 지표 | 베이스라인 | 리팩터링 후 |
|---|---:|---:|
| 요청 실패율 | 0% | 0% |
| Hikari active peak | 10 | 1 |
| Hikari pending peak | 0 | 0 |
| Hikari timeout | 0 | 0 |
| Tomcat busy peak | 11 | 11 |
| MySQL running peak | 3 | 3 |

외부 호출 10개가 동시에 진행될 때 베이스라인은 커넥션 10개를 계속 점유했고, 리팩터링 후에는 외부 대기와 무관하게 peak가 1이었다. 트랜잭션 경계 변경의 직접적인 증거다.

### 6.2 100 VU 대조

| 지표 | 베이스라인 | 리팩터링 후 |
|---|---:|---:|
| 전체 요청 | 7,961 | 1,709 |
| 성공 요청 | 421 | 1,668 |
| 실패 요청 | 7,540 | 41 |
| 성공률 | 5.29% | 97.60% |
| 성공 처리량(약) | 3.83 req/s | 15.16 req/s |
| Hikari active peak | 20 | 20 |
| active=20 관측 샘플 | 91/103 | 20/110 |
| Hikari pending peak | 86 | 17 |
| Hikari timeout | 7,627 | 8 |
| Tomcat busy peak | 101 | 101 |
| MySQL running peak | 3 | 22 |
| Mock in-flight peak | 20 | 97 |
| JVM process CPU peak | 14.65% | 42.19% |
| JVM memory-used 합계 peak | 523.4 MiB | 1,643.5 MiB |
| JVM live thread peak | 127 | 128 |
| GC pause 합계 증가 | 0.081s | 2.533s |

두 실행 모두 순간 active peak는 20이다. 중요한 차이는 최대값 자체가 아니라 풀을 점유한 시간과 대기·timeout이다. 베이스라인은 관측 샘플의 88.3%에서 active가 20이었고 7,627회 커넥션 획득에 실패했다. 리팩터링 후 active=20 관측은 18.2%, timeout은 8회였다.

베이스라인의 전체 요청 수가 더 많은 이유는 성능이 좋아서가 아니다. 커넥션을 1초 안에 얻지 못한 요청이 빠르게 실패하면서 closed workload의 VU가 다음 반복으로 넘어갔기 때문이다. 따라서 전체 iteration 수가 아니라 성공률과 성공 처리량을 비교해야 한다.

리팩터링 후 총 p95는 약 27.7초였다. 요청 성공은 크게 늘었지만 100개의 동기 요청 스레드가 5~10초 외부 I/O와 후속 저장을 수행하면서 대기열이 형성됐다. 커넥션 병목 제거와 응답 지연 개선은 같은 의미가 아니다.

### 6.3 최대 3,000 VU

| 지표 | 베이스라인 | 리팩터링 후 |
|---|---:|---:|
| 최대 생성 VU | 3,000 | 3,000 |
| 전체 요청 | 59,698 | 39,046 |
| 성공 요청 | 1,233 | 2,029 |
| 성공률 | 2.07% | 5.20% |
| Hikari active peak | 20 | 20 |
| Hikari pending peak | 186 | 75 |
| Hikari timeout | 58,579 | 481 |
| Tomcat busy peak | 200 | 200 |
| MySQL running peak | 3 | 22 |
| Mock in-flight peak | 20 | 172 |
| JVM process CPU peak | 9.12% | 58.28% |
| JVM memory-used 합계 peak | 841.4 MiB | 1,731.0 MiB |
| JVM live thread peak | 226 | 226 |
| GC pause 합계 증가 | 0.285s | 1.412s |

리팩터링 후 Hikari timeout은 99.18% 감소했고 실제 DB 작업과 외부 호출이 더 많이 진행됐다. 베이스라인의 낮은 CPU·MySQL running 수치는 여유 용량이 아니라 커넥션 획득 단계에서 대부분의 요청이 탈락한 결과다.

그러나 리팩터링 후에도 3,000 VU에서는 Tomcat busy가 설정 한도 200에 도달했고 k6 30초 timeout이 다수 발생했다. 또한 짧은 검색·조회수 증가·저장 트랜잭션이 한꺼번에 몰리며 Hikari active 20, pending 75, timeout 481을 기록했다. DB 커넥션을 외부 대기에서 해방한 뒤 동기 요청 스레드와 순간 DB 처리량이 다음 병목으로 이동한 것이다.

## 7. 주요 지표 해석

### HikariCP

- `active`: 현재 애플리케이션 코드가 빌려 쓴 커넥션 수.
- `idle`: 풀에서 즉시 빌릴 수 있는 커넥션 수.
- `pending`: 커넥션을 기다리는 애플리케이션 스레드 수.
- `timeout_total`: `connectionTimeout=1s` 안에 커넥션을 얻지 못한 누적 횟수.
- 이 실험의 핵심 판정 지표는 active peak 하나가 아니라 `active=maximum` 지속, pending, timeout의 조합이다.

### Tomcat

- 100 VU에서는 busy peak가 101이므로 설정 한도 200보다 낮았다.
- 3,000 VU에서는 busy peak 200으로 포화됐다.
- Actuator의 `tomcat_threads_config_max_threads`는 이 환경에서 `-1`을 반환했지만, 실행 환경 파일과 애플리케이션 설정은 200이며 실제 busy도 200에서 멈췄다.

### MySQL

- `Threads_connected` peak는 두 구조 모두 22였다. 앱 풀 20개와 측정용 연결이 포함된다.
- 베이스라인의 `Threads_running` peak는 3에 불과했다. DB가 바쁜 것이 아니라 커넥션이 외부 I/O를 기다리며 놀고 있었다.
- 리팩터링 후 peak 22는 커넥션이 실제 짧은 DB 작업에 사용됐음을 보여준다.
- MySQL max-connection 오류는 두 구조 모두 0건이었다. 병목은 MySQL의 500개 한도가 아니라 앱의 Hikari 20개 한도였다.

### JVM

- 리팩터링 후 CPU·메모리·GC 사용량이 늘었다. 이는 회귀라기보다 커넥션 획득 단계에서 즉시 실패하던 요청이 실제 RAG·DB 처리까지 진입한 결과가 포함된다.
- 메모리는 모든 memory pool의 `jvm_memory_used_bytes` 합계 peak이며 OS RSS가 아니다.
- 로컬 단일 실행 결과이므로 프로덕션 JVM 용량 산정 근거로 직접 사용할 수 없다.

## 8. 한계와 후속 개선

1. 3,000 VU는 로컬 장애 한계 실험이다. 프로덕션 수용량 증명이 아니다.
2. Spring MVC의 동기 요청 모델에서는 트랜잭션을 끝내도 외부 응답 동안 Tomcat 플랫폼 스레드는 점유된다.
3. 다음 단계는 전체 체인의 비동기화 여부를 검토하거나, 별도 작업 큐·상태 조회 방식으로 긴 RAG 작업을 요청 스레드에서 분리하는 것이다.
4. 동기 구조를 유지한다면 RAG bulkhead, 적절한 connect/read timeout, circuit breaker, 전체 요청 deadline, 제한된 재시도와 jitter backoff가 필요하다.
5. Hikari 풀 크기를 단순히 늘리기 전에 DB 쿼리 시간, DB CPU, lock wait, Little's Law 기반 동시 DB 작업 수를 측정해야 한다.
6. 동일 신규 질문의 동시 저장 경쟁은 이번 트랜잭션 분리와 별개의 정합성 문제다. 정규화된 질문 키 유니크 제약 또는 결과 병합 정책이 필요하다.
7. Actuator가 Tomcat 스레드와 경쟁해 최대 부하 구간의 1초 샘플 일부가 누락됐다. 누적 Hikari timeout과 k6 결과는 보존됐지만, 운영 환경에서는 별도 모니터링 경로가 바람직하다.
8. 리팩터링 후 100 VU에서도 실패 41건이 남았다. 3,000 VU 확장 전 25·50·75·100 VU 계단형 테스트로 무오류 운용점을 별도로 찾아야 한다.

## 9. 실행 및 검증 방법

```bash
# 스모크
SMOKE=true RUN_ID=smoke-refactored \
  bash load-test/scripts/run-load-test.sh

# 100 VU 대조
LOAD_PROFILE=comparison RUN_ID=comparison-refactored \
  bash load-test/scripts/run-load-test.sh

# 최대 3,000 VU
RUN_ID=full-refactored \
  bash load-test/scripts/run-load-test.sh
```

각 실행은 다음 산출물을 남긴다.

- `k6-summary.json`: 요청 수, 성공·실패, route별 지연, 최대 VU.
- `metrics.jsonl`: 초 단위 Hikari·Tomcat·JVM·MySQL·Mock 시계열.
- `metrics-summary.json`: peak와 counter delta 요약.
- `raw/`: Prometheus, MySQL status/process list, Mock, health 원본.
- `existing-path-verification.json`: 기존 질문이 Mock을 호출하지 않았다는 사전 검증.
- `metrics-validation.txt`, `run-validation.txt`: 지표 및 60:40 실행 계약 검증 결과.

대규모 실행이 실패 기준을 넘으면 스크립트는 의도적으로 non-zero로 종료하지만, 모든 측정 파일은 그대로 보존한다.

## 10. 최종 자동화 검증 상태

- 트랜잭션 경계·Hikari 실제 MySQL·조회수 동시성 관련 집중 Gradle 테스트: 9/9 통과.
- Mock 지연·60:40 선택·JVM/Hikari 요약·실행 계약 Node 테스트: 11/11 통과.
- 셸 스크립트 문법 검사와 k6 스크립트 inspect 통과.
- 기존 전체 테스트는 155개 중 102개 통과, 53개 실패 상태다. 남은 실패는 이 변경 전부터 존재한 controller property, Full-Text 초기화, 일부 unit fixture 문제이며 별도 상태 문서에 기록했다.
- 따라서 이 리포트는 변경 범위의 집중 검증은 통과했다고 말할 수 있지만 저장소 전체 테스트가 green이라고 주장하지 않는다.
