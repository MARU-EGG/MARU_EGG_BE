package mju.iphak.maru_egg.campus.application.college.query.find;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import mju.iphak.maru_egg.campus.api.dto.response.CollegeResponse;
import mju.iphak.maru_egg.campus.domain.CampusType;
import mju.iphak.maru_egg.campus.domain.College;
import mju.iphak.maru_egg.campus.repository.CollegeRepository;
import mju.iphak.maru_egg.common.MockTest;

class FindAllCollegeServiceTest extends MockTest {

	@Mock
	private CollegeRepository collegeRepository;

	@InjectMocks
	private FindAllCollegeService findAllCollegeService;

	private College naturalCollege;
	private College humanitiesCollege;

	@BeforeEach
	void setUp() {
		MockitoAnnotations.openMocks(this);
		naturalCollege = College.builder()
			.campus(CampusType.NATURAL)
			.name("자연과학대학")
			.description("자연과학 관련 학과들")
			.build();
		humanitiesCollege = College.builder()
			.campus(CampusType.HUMANITIES)
			.name("인문대학")
			.description("예시")
			.build();
	}

	@DisplayName("[성공] 모든 대학 목록 조회")
	@Test
	void 모든_대학_목록_조회_성공() {
		// given
		when(collegeRepository.findAll()).thenReturn(List.of(naturalCollege, humanitiesCollege));

		// when
		List<CollegeResponse> result = findAllCollegeService.invoke();

		// then
		assertThat(result).hasSize(2);
		assertThat(result.get(0).name()).isEqualTo("자연과학대학");
		assertThat(result.get(1).name()).isEqualTo("인문대학");
	}
}
