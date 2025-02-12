package mju.iphak.maru_egg.campus.application.college.command.update;

import static org.assertj.core.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import jakarta.persistence.EntityNotFoundException;
import mju.iphak.maru_egg.campus.api.dto.request.UpdateCollegeRequest;
import mju.iphak.maru_egg.campus.domain.CampusType;
import mju.iphak.maru_egg.campus.domain.College;
import mju.iphak.maru_egg.campus.repository.CollegeRepository;
import mju.iphak.maru_egg.common.MockTest;

class UpdateCollegeServiceTest extends MockTest {

	@Mock
	private CollegeRepository collegeRepository;

	@InjectMocks
	private UpdateCollegeService updateCollegeService;

	private College college;

	@BeforeEach
	void setUp() {
		MockitoAnnotations.openMocks(this);
		college = College.builder()
			.campus(CampusType.NATURAL)
			.name("자연과학대학")
			.description("설명")
			.build();
	}

	@DisplayName("[성공] 대학 정보 수정 요청")
	@Test
	void 대학_수정_성공() {
		// given
		Long collegeId = 1L;
		UpdateCollegeRequest request = new UpdateCollegeRequest(CampusType.NATURAL.getType(), "변경된 단과대명", "변경된 설명");
		when(collegeRepository.findById(collegeId)).thenReturn(Optional.of(college));

		// when
		updateCollegeService.invoke(collegeId, request);

		// then
		assertThat("변경된 단과대명").isEqualTo(college.getName());
		assertThat("변경된 설명").isEqualTo(college.getDescription());
	}

	@DisplayName("[실패] 존재하지 않는 대학 수정 요청")
	@Test
	void 존재하지_않는_대학_수정_실패() {
		// given
		Long collegeId = 999L;
		UpdateCollegeRequest request = new UpdateCollegeRequest("변경된 대학명", "변경된 설명", null);
		when(collegeRepository.findById(collegeId)).thenReturn(Optional.empty());

		// when & then
		EntityNotFoundException exception = assertThrows(EntityNotFoundException.class,
			() -> updateCollegeService.invoke(collegeId, request));

		assertThat("캠퍼스가 변경된 대학명인 대학이 존재하지 않습니다.").isEqualTo(exception.getMessage());
	}
}
