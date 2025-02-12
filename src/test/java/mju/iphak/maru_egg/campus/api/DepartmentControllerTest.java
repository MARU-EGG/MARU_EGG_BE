package mju.iphak.maru_egg.campus.api;

import static org.assertj.core.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import mju.iphak.maru_egg.campus.api.dto.response.DepartmentResponse;
import mju.iphak.maru_egg.campus.domain.CampusType;
import mju.iphak.maru_egg.campus.domain.College;
import mju.iphak.maru_egg.campus.domain.Department;
import mju.iphak.maru_egg.campus.repository.CollegeRepository;
import mju.iphak.maru_egg.campus.repository.DepartmentRepository;
import mju.iphak.maru_egg.common.IntegrationTest;

class DepartmentControllerTest extends IntegrationTest {

	@Autowired
	private CollegeRepository collegeRepository;

	@Autowired
	private DepartmentRepository departmentRepository;

	private College college;

	private Department department;

	@BeforeEach
	public void setUp() throws Exception {
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

	@DisplayName("[성공] 모든 학과 조회 요청")
	@Test
	void 모든_학과_조회_성공() throws Exception {
		// when
		List<DepartmentResponse> result = getAllDepartments();

		// then
		assertThat(result).isNotEmpty();
	}

	@DisplayName("[성공] 특정 단과대의 학과 조회 요청")
	@Test
	void 특정_단과대_학과_조회_성공() throws Exception {
		// given
		Long collegeId = college.getId();

		// when
		List<DepartmentResponse> result = getAllByCollege(collegeId);

		// then
		assertThat(result).isNotEmpty();
	}

	private List<DepartmentResponse> getAllDepartments() throws Exception {
		String response = mvc.perform(get("/api/campuses/departments"))
			.andDo(print())
			.andExpect(status().isOk())
			.andReturn()
			.getResponse()
			.getContentAsString();
		return objectMapper.readValue(response,
			objectMapper.getTypeFactory().constructCollectionType(List.class, DepartmentResponse.class));
	}

	private List<DepartmentResponse> getAllByCollege(Long collegeId) throws Exception {
		String response = mvc.perform(get("/api/campuses/departments/college/{collegeId}", collegeId))
			.andDo(print())
			.andExpect(status().isOk())
			.andReturn()
			.getResponse()
			.getContentAsString();
		return objectMapper.readValue(response,
			objectMapper.getTypeFactory().constructCollectionType(List.class, DepartmentResponse.class));
	}
}
