# 1분 3,000건 수용량 튜닝 설계

## 목표

기존 답변 60%, 5~10초 Mock RAG를 호출하는 신규 질문 40% 조건에서 정확히 1분간 3,000건, 평균 50 RPS를 유입하고 단일 애플리케이션 인스턴스의 최소 안정 설정을 찾는다.

이 검증은 3,000 VU 동시 처리량 증명이 아니다. `3,000 VU` 한계 실험과 `1분간 3,000건` 수용량 실험의 결과와 표현을 분리한다.

## 확인된 출발점

- Tomcat 200, Hikari 20의 기존 3,000 VU 한계 실험은 Tomcat busy 200, Hikari pending 75, Hikari timeout 481로 실패했다.
- Tomcat만 1,000으로 늘린 추가 실험은 busy 997, JVM live threads 1,026, 시스템 CPU 약 100%, JVM memory-used 합계 약 2.1GB에 도달한 뒤 애플리케이션이 응답하지 않았다.
- 따라서 1,000 플랫폼 스레드는 후보에서 제외하고, 실제 요구량인 50 RPS에 맞는 작은 설정부터 계단식으로 검증한다.

## 부하 프로필

- k6 executor: `constant-arrival-rate`
- rate: 초당 50 iteration
- duration: 60초
- 총 예약 요청: 3,000건
- pre-allocated VU: 200
- maximum VU: 1,000
- 요청 구성: 기존 답변 60%, 신규 질문 40%
- 신규 질문 Mock RAG 지연: 결정적으로 5,000~10,000ms
- 개별 HTTP timeout: 30초

## 성공 기준

- selected request: 정확히 3,000건
- dropped iteration: 0건
- 기존/신규 실패: 각각 0건
- HTTP 실패: 0건
- Hikari acquisition timeout: 0건
- MySQL max-connection rejection: 0건
- Mock RAG 오류: 0건
- 기존 질문 p95: 1초 이하
- 신규 질문 p95: 15초 이하
- 애플리케이션 비정상 종료와 필수 지표 누락이 없어야 한다.

## 단계형 설정 탐색

동시에 여러 변수를 바꾸지 않는다.

1. Tomcat 200, Hikari 20
2. 1단계가 Tomcat 포화로 실패하면 Tomcat 250, Hikari 20
3. 2단계가 Tomcat 포화로 실패하면 Tomcat 300, Hikari 20
4. Tomcat은 여유가 있지만 Hikari pending/timeout이 발생하면 Tomcat 300, Hikari 30
5. Hikari 병목이 남으면 Tomcat 300, Hikari 40

가장 먼저 모든 성공 기준을 충족한 설정을 최소 안정 설정으로 선택하고 이후의 더 큰 설정은 실행하지 않는다. 300/40까지 실패하면 플랫폼 스레드와 커넥션 풀 증설을 중단하고 비동기 작업 큐, 202 응답, 상태 조회 구조를 대안으로 제시한다.

## 기록할 지표

- 요청 수, 성공률, 기존/신규 p95, dropped iterations
- Hikari active/idle/pending/timeout과 포화 지속 시간
- Tomcat busy/current/configured maximum
- JVM CPU, system CPU, memory-used 합계, live threads, GC pause
- MySQL Threads_connected, Threads_running, max connection 오류
- Mock RAG current/peak in-flight, 완료·오류 수, 지연 분포

로컬 단일 장비의 CPU·메모리는 프로덕션 용량 산정값이 아니라 설정 간 상대 비교와 실패 원인 판정에만 사용한다.

