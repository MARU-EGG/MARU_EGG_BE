export const EXISTING_QUESTIONS = Object.freeze([
  { type: 'SUSI', category: 'ADMISSION_GUIDELINE', content: '수시 모집 일정은 언제인가요?' },
  { type: 'SUSI', category: 'ADMISSION_GUIDELINE', content: '수시 지원 자격을 알려주세요.' },
  { type: 'SUSI', category: 'PASSING_RESULT', content: '수시 입시 결과를 알려주세요.' },
  { type: 'JEONGSI', category: 'ADMISSION_GUIDELINE', content: '정시 모집 일정을 알려주세요.' },
  { type: 'JEONGSI', category: 'PASSING_RESULT', content: '정시 입시 결과를 알려주세요.' },
  { type: 'PYEONIP', category: 'ADMISSION_GUIDELINE', content: '편입학 지원 자격을 알려주세요.' },
]);

export function selectWorkload({ runId, vu, iteration }) {
  const cycleIndex = iteration % 10;
  if (cycleIndex < 6) {
    return {
      kind: 'existing',
      request: EXISTING_QUESTIONS[cycleIndex],
    };
  }
  const uniqueBase = 10_000_000 + (vu * 1_000_003) + (iteration * 97);
  const uniqueTerms = Array.from(
    { length: 8 },
    (_, offset) => uniqueBase + (offset * 99_991),
  ).join(' ');

  return {
    kind: 'new',
    request: {
      type: 'SUSI',
      category: 'ADMISSION_GUIDELINE',
      content: `부하테스트 신규 질문 ${runId} vu-${vu} iter-${iteration} 고유번호 ${uniqueTerms}`,
    },
  };
}
