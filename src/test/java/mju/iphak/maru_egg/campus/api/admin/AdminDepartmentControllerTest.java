package mju.iphak.maru_egg.campus.api.admin;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.ResultActions;

import mju.iphak.maru_egg.campus.api.dto.request.CreateDepartmentRequest;
import mju.iphak.maru_egg.campus.api.dto.request.UpdateDepartmentRequest;
import mju.iphak.maru_egg.campus.domain.CampusType;
import mju.iphak.maru_egg.campus.domain.College;
import mju.iphak.maru_egg.campus.domain.Department;
import mju.iphak.maru_egg.campus.repository.CollegeRepository;
import mju.iphak.maru_egg.campus.repository.DepartmentRepository;
import mju.iphak.maru_egg.common.IntegrationTest;

@WithMockUser(roles = "ADMIN")
class AdminDepartmentControllerTest extends IntegrationTest {

	@Autowired
	private CollegeRepository collegeRepository;

	@Autowired
	private DepartmentRepository departmentRepository;

	private College college;

	private Department department;

	@BeforeEach
	void setUp() {
		college = College.builder()
			.campus(CampusType.NATURAL)
			.name("자연과학대학")
			.description("자연과학 관련 학과들")
			.build();
		collegeRepository.save(college);

		department = Department.builder()
			.name("컴퓨터공학과")
			.description("소프트웨어 및 컴퓨터 과학 연구")
			.college(college)
			.build();

		departmentRepository.save(department);
	}

	@DisplayName("[성공] 학과 생성 요청")
	@Test
	void 학과_생성_성공() throws Exception {
		// given
		CreateDepartmentRequest request = new CreateDepartmentRequest("융합소프트웨어학부", "소프트웨어 개발 및 연구", college.getId());

		// when
		ResultActions result = PostDepartment(request);

		// then
		result.andExpect(status().isOk());
	}

	@DisplayName("[성공] 학과 수정 요청")
	@Test
	void 학과_수정_성공() throws Exception {
		// given
		Long departmentId = department.getId();
		UpdateDepartmentRequest request = new UpdateDepartmentRequest("소프트웨어학과", "소프트웨어 공학 전공");

		// when
		ResultActions result = PutDepartment(departmentId, request);

		// then
		result.andExpect(status().isOk());
	}

	@DisplayName("[성공] 학과 삭제 요청")
	@Test
	void 학과_삭제_성공() throws Exception {
		// given
		Long departmentId = 1L;

		// when
		ResultActions result = DeleteDepartment(departmentId);

		// then
		result.andExpect(status().isOk());
	}

	private ResultActions PostDepartment(CreateDepartmentRequest request) throws Exception {
		return mvc.perform(post("/api/admin/campuses/departments")
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(request)))
			.andDo(print());
	}

	private ResultActions PutDepartment(Long id, UpdateDepartmentRequest request) throws Exception {
		return mvc.perform(put("/api/admin/campuses/departments/{id}", id)
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(request)))
			.andDo(print());
	}

	private ResultActions DeleteDepartment(Long id) throws Exception {
		return mvc.perform(delete("/api/admin/campuses/departments/{id}", id)
				.contentType(MediaType.APPLICATION_JSON))
			.andDo(print());
	}
}
