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
  const uniqueQuestionId = 1_000_000_000_000 + (vu * 1_000_003) + (iteration * 97);

  return {
    kind: 'new',
    request: {
      type: 'SUSI',
      category: 'ADMISSION_GUIDELINE',
      content: String(uniqueQuestionId),
    },
  };
}
