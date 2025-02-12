package mju.iphak.maru_egg.campus.application.department.command.create;

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
import mju.iphak.maru_egg.campus.api.dto.request.CreateDepartmentRequest;
import mju.iphak.maru_egg.campus.domain.CampusType;
import mju.iphak.maru_egg.campus.domain.College;
import mju.iphak.maru_egg.campus.domain.Department;
import mju.iphak.maru_egg.campus.repository.CollegeRepository;
import mju.iphak.maru_egg.campus.repository.DepartmentRepository;
import mju.iphak.maru_egg.common.MockTest;

class CreateDepartmentServiceTest extends MockTest {

	@Mock
	private CollegeRepository collegeRepository;

	@Mock
	private DepartmentRepository departmentRepository;

	@InjectMocks
	private CreateDepartmentService createDepartmentService;

	private College college;

	@BeforeEach
	void setUp() {
		MockitoAnnotations.openMocks(this);
		college = College.builder()
			.campus(CampusType.NATURAL)
			.name("자연과학대학")
			.description("자연과학 관련 학과들")
			.build();
	}

	@DisplayName("[성공] 학과 생성 요청")
	@Test
	void 학과_생성_성공() {
		// given
		CreateDepartmentRequest request = new CreateDepartmentRequest("컴퓨터공학과", "설명", 1L);
		when(collegeRepository.findById(1L)).thenReturn(Optional.of(college));
		when(departmentRepository.existsByName("컴퓨터공학과")).thenReturn(false);

		// when
		createDepartmentService.invoke(request);

		// then
		verify(departmentRepository, times(1)).save(any(Department.class));
	}

	@DisplayName("[실패] 존재하지 않는 단과대에 학과 생성 요청")
	@Test
	void 존재하지_않는_단과대에_학과_생성_실패() {
		// given
		CreateDepartmentRequest request = new CreateDepartmentRequest("컴퓨터공학과", "설명", 999L);
		when(collegeRepository.findById(999L)).thenReturn(Optional.empty());

		// when & then
		EntityNotFoundException exception = assertThrows(EntityNotFoundException.class,
			() -> createDepartmentService.invoke(request));

		assertEquals("ID가 999인 단과대가 존재하지 않습니다.", exception.getMessage());
	}

	@DisplayName("[실패] 중복 학과명 생성 요청")
	@Test
	void 중복_학과명_생성_실패() {
		// given
		CreateDepartmentRequest request = new CreateDepartmentRequest("컴퓨터공학과", "설명", 1L);
		when(collegeRepository.findById(1L)).thenReturn(Optional.of(college));
		when(departmentRepository.existsByName("컴퓨터공학과")).thenReturn(true);

		// when & then
		IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
			() -> createDepartmentService.invoke(request));

		assertEquals("학과 이름 '컴퓨터공학과'은(는) 이미 존재합니다.", exception.getMessage());
	}
}
