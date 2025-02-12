package mju.iphak.maru_egg.campus.application.department.command.update;

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
import mju.iphak.maru_egg.campus.api.dto.request.UpdateDepartmentRequest;
import mju.iphak.maru_egg.campus.domain.CampusType;
import mju.iphak.maru_egg.campus.domain.College;
import mju.iphak.maru_egg.campus.domain.Department;
import mju.iphak.maru_egg.campus.repository.DepartmentRepository;
import mju.iphak.maru_egg.common.MockTest;

class UpdateDepartmentServiceTest extends MockTest {

	@Mock
	private DepartmentRepository departmentRepository;

	@InjectMocks
	private UpdateDepartmentService updateDepartmentService;

	private Department department;

	@BeforeEach
	void setUp() {
		MockitoAnnotations.openMocks(this);

		College college = College.builder()
			.campus(CampusType.NATURAL)
			.name("자연과학대학")
			.description("자연과학 관련 학과들")
			.build();

		department = Department.builder()
			.name("컴퓨터공학과")
			.description("소프트웨어 및 컴퓨터 과학 연구")
			.college(college)
			.build();
	}

	@DisplayName("[성공] 학과 정보 수정 요청")
	@Test
	void 학과_수정_성공() {
		// given
		Long departmentId = 1L;
		UpdateDepartmentRequest request = new UpdateDepartmentRequest("소프트웨어학과", "소프트웨어 엔지니어링 전공");

		when(departmentRepository.findById(departmentId)).thenReturn(Optional.of(department));

		// when
		updateDepartmentService.invoke(departmentId, request);

		// then
		assertEquals("소프트웨어학과", department.getName());
		assertEquals("소프트웨어 엔지니어링 전공", department.getDescription());
	}

	@DisplayName("[실패] 존재하지 않는 학과 수정 요청")
	@Test
	void 존재하지_않는_학과_수정_실패() {
		// given
		Long departmentId = 999L;
		UpdateDepartmentRequest request = new UpdateDepartmentRequest("소프트웨어학과", "소프트웨어 엔지니어링 전공");

		when(departmentRepository.findById(departmentId)).thenReturn(Optional.empty());

		// when & then
		EntityNotFoundException exception = assertThrows(EntityNotFoundException.class,
			() -> updateDepartmentService.invoke(departmentId, request));

		assertEquals("ID가 999인 학과이 존재하지 않습니다.", exception.getMessage());
	}
}
