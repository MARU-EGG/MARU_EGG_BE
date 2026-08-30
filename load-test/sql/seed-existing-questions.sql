USE maru_egg_load_test;

ALTER TABLE questions ADD FULLTEXT INDEX ft_questions_content (content);
ALTER TABLE questions ADD FULLTEXT INDEX ft_questions_content_token (content_token);

INSERT INTO questions
  (id, created_at, updated_at, admission_category, admission_type, content, content_token, is_checked, renewal_year, view_count)
VALUES
  (1, NOW(), NOW(), 'ADMISSION_GUIDELINE', 'SUSI', '수시 모집 일정은 언제인가요?', '수시 모집 일정 언제', TRUE, 2025, 0),
  (2, NOW(), NOW(), 'ADMISSION_GUIDELINE', 'SUSI', '수시 지원 자격을 알려주세요.', '수시 지원 자격', TRUE, 2025, 0),
  (3, NOW(), NOW(), 'PASSING_RESULT', 'SUSI', '수시 입시 결과를 알려주세요.', '수시 입시 결과', TRUE, 2025, 0),
  (4, NOW(), NOW(), 'ADMISSION_GUIDELINE', 'JEONGSI', '정시 모집 일정을 알려주세요.', '정시 모집 일정', TRUE, 2025, 0),
  (5, NOW(), NOW(), 'PASSING_RESULT', 'JEONGSI', '정시 입시 결과를 알려주세요.', '정시 입시 결과', TRUE, 2025, 0),
  (6, NOW(), NOW(), 'ADMISSION_GUIDELINE', 'PYEONIP', '편입학 지원 자격을 알려주세요.', '편입학 지원 자격', TRUE, 2025, 0);

INSERT INTO answers (id, created_at, updated_at, content, renewal_year, question_id)
VALUES
  (1, NOW(), NOW(), '수시 모집 일정에 대한 기존 답변입니다.', 2025, 1),
  (2, NOW(), NOW(), '수시 지원 자격에 대한 기존 답변입니다.', 2025, 2),
  (3, NOW(), NOW(), '수시 입시 결과에 대한 기존 답변입니다.', 2025, 3),
  (4, NOW(), NOW(), '정시 모집 일정에 대한 기존 답변입니다.', 2025, 4),
  (5, NOW(), NOW(), '정시 입시 결과에 대한 기존 답변입니다.', 2025, 5),
  (6, NOW(), NOW(), '편입학 지원 자격에 대한 기존 답변입니다.', 2025, 6);
