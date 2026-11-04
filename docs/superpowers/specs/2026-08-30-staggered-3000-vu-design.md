# 1분 분산 3,000 VU 부하 테스트 설계

## 목표

k6에서 VU 3,000개를 생성하고 각 VU가 질문 API를 정확히 한 번 호출하게 한다. 요청 시작은 60초 동안 균등 분산해 실제 사용자 유입을 모사하고, 동기 Spring MVC API와 현재 트랜잭션 경계를 변경하지 않은 상태에서 커넥션 풀 고갈 없이 전체 요청을 완료하는지 검증한다.

## 시나리오

- executor: `per-vu-iterations`
- VU: 3,000
- iterations per VU: 1
- 총 요청: 3,000
- VU별 요청 지연: `(vuId - 1) × 60 / 3,000`초
- 요청 시작 속도: 초당 약 50명
- 기존 질문 60%, 신규 질문 40%
- 신규 질문 Mock RAG 지연: 5~10초
- 요청 timeout: 120초
- scenario maxDuration: 180초

이 테스트는 3,000 VU가 존재하는 부하 테스트지만 3,000개 요청이 한순간에 동시에 서버에 도달하는 테스트는 아니다.

## 안정성 기준

- selected request 3,000건
- successful request 3,000건
- failed request 0건
- dropped iteration 0건
- Hikari acquisition timeout 0건
- MySQL max-connection rejection 0건
- Mock RAG 오류 0건 및 신규 선택 수와 호출 수 일치
- 애플리케이션 비정상 종료 없음
- 전체 완료 시간, 기존/신규 p95, Hikari pending peak와 지속 시간은 결과에 별도 기록

## 설정 탐색

1. Tomcat 200, Hikari 20
2. 실패 원인이 Tomcat 포화일 때만 Tomcat 250, Hikari 20
3. Tomcat 포화가 지속될 때만 Tomcat 300, Hikari 20
4. Hikari pending 또는 timeout이 원인일 때만 Hikari 30, 40 순서로 증가

가장 먼저 안정성 기준을 만족한 구성을 최소 설정으로 선택한다. 스레드와 풀을 무조건 크게 설정하지 않는다.

## 면접 표현 조건

테스트가 실제로 통과한 뒤에만 다음과 같이 설명한다.

> k6에서 VU 3,000명을 생성해 각 사용자가 한 번씩 요청하도록 했고, 요청 시작은 1분 동안 초당 50명씩 분산했습니다. 기존 답변 60%, 5~10초 RAG 신규 질문 40% 조건에서 전체 요청과 Hikari timeout 0건을 확인했습니다.

