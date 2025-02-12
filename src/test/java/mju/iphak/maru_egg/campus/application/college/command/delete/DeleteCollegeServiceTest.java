package mju.iphak.maru_egg.campus.application.college.command.delete;

import static org.mockito.Mockito.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import mju.iphak.maru_egg.campus.repository.CollegeRepository;
import mju.iphak.maru_egg.common.MockTest;

class DeleteCollegeServiceTest extends MockTest {

	@Mock
	private CollegeRepository collegeRepository;

	@InjectMocks
	private DeleteCollegeService deleteCollegeService;

	@BeforeEach
	void setUp() {
		MockitoAnnotations.openMocks(this);
	}

	@DisplayName("[성공] 대학 삭제 요청")
	@Test
	void 대학_삭제_성공() {
		// given
		Long collegeId = 1L;

		// when
		deleteCollegeService.invoke(collegeId);

		// then
		verify(collegeRepository, times(1)).deleteById(collegeId);
	}
}
