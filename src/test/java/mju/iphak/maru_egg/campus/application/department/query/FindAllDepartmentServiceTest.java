package mju.iphak.maru_egg.campus.application.department.query;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import mju.iphak.maru_egg.campus.api.dto.response.DepartmentResponse;
import mju.iphak.maru_egg.campus.domain.CampusType;
import mju.iphak.maru_egg.campus.domain.College;
import mju.iphak.maru_egg.campus.domain.Department;
import mju.iphak.maru_egg.campus.repository.DepartmentRepository;
import mju.iphak.maru_egg.common.MockTest;

class FindAllDepartmentServiceTest extends MockTest {

	@Mock
	private DepartmentRepository departmentRepository;

	@InjectMocks
	private FindAllDepartmentService findAllDepartmentService;

	private Department department1;
	private Department department2;

	@BeforeEach
	void setUp() {
		MockitoAnnotations.openMocks(this);

		College college = College.builder()
			.campus(CampusType.NATURAL)
			.name("자연과학대학")
			.description("자연과학 관련 학과들")
			.build();

		department1 = Department.builder()
			.name("컴퓨터공학과")
			.description("소프트웨어 및 컴퓨터 과학 연구")
			.college(college)
			.build();

		department2 = Department.builder()
			.name("수학과")
			.description("수리과학 연구")
			.college(college)
			.build();
	}

	@DisplayName("[성공] 모든 학과 목록 조회")
	@Test
	void 모든_학과_목록_조회_성공() {
		// given
		when(departmentRepository.findAll()).thenReturn(List.of(department1, department2));

		// when
		List<DepartmentResponse> result = findAllDepartmentService.invoke();

		// then
		assertThat(result).hasSize(2);
		assertThat(result.get(0).name()).isEqualTo("컴퓨터공학과");
		assertThat(result.get(1).name()).isEqualTo("수학과");
	}
}
