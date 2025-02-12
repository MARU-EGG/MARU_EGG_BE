package mju.iphak.maru_egg.campus.application.college.command.create;

import static org.assertj.core.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import mju.iphak.maru_egg.campus.api.dto.request.CreateCollegeRequest;
import mju.iphak.maru_egg.campus.repository.CollegeRepository;
import mju.iphak.maru_egg.common.MockTest;

class CreateCollegeServiceTest extends MockTest {

	@Mock
	private CollegeRepository collegeRepository;

	@InjectMocks
	private CreateCollegeService createCollegeService;

	@BeforeEach
	void setUp() {
		MockitoAnnotations.openMocks(this);
	}

	@DisplayName("[실패] 중복된 대학명으로 생성 요청")
	@Test
	void 중복된_대학명_생성_실패() {
		// given
		CreateCollegeRequest request = new CreateCollegeRequest("NATURAL", "자연과학대학", "자연과학 설명");
		when(collegeRepository.existsByName("자연과학대학")).thenReturn(true);

		// when & then
		IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
			() -> createCollegeService.invoke(request));

		// then
		assertThat("단과대 이름 '자연과학대학'은(는) 이미 존재합니다.").isEqualTo(exception.getMessage());
	}
}
