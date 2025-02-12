package mju.iphak.maru_egg.campus.application.department.command.delete;

import static org.mockito.Mockito.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import mju.iphak.maru_egg.campus.repository.DepartmentRepository;
import mju.iphak.maru_egg.common.MockTest;

class DeleteDepartmentServiceTest extends MockTest {

	@Mock
	private DepartmentRepository departmentRepository;

	@InjectMocks
	private DeleteDepartmentService deleteDepartmentService;

	@BeforeEach
	void setUp() {
		MockitoAnnotations.openMocks(this);
	}

	@DisplayName("[성공] 학과 삭제 요청")
	@Test
	void 학과_삭제_성공() {
		// given
		Long departmentId = 1L;

		// when
		deleteDepartmentService.invoke(departmentId);

		// then
		verify(departmentRepository, times(1)).deleteById(departmentId);
	}
}
