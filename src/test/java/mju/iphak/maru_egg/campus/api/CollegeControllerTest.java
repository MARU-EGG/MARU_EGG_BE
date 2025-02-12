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

import mju.iphak.maru_egg.campus.api.dto.response.CollegeResponse;
import mju.iphak.maru_egg.campus.domain.CampusType;
import mju.iphak.maru_egg.campus.domain.College;
import mju.iphak.maru_egg.campus.domain.Department;
import mju.iphak.maru_egg.campus.repository.CollegeRepository;
import mju.iphak.maru_egg.campus.repository.DepartmentRepository;
import mju.iphak.maru_egg.common.IntegrationTest;

class CollegeControllerTest extends IntegrationTest {

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

	@DisplayName("[성공] 모든 단과대 조회 요청")
	@Test
	void 모든_단과대_조회_성공() throws Exception {
		// when
		List<CollegeResponse> result = GetAllColleges();

		// then
		assertThat(result).isNotEmpty();
	}

	@DisplayName("[성공] 특정 캠퍼스의 단과대 조회 요청")
	@Test
	void 특정_캠퍼스_단과대_조회_성공() throws Exception {
		// given
		String campusType = CampusType.NATURAL.getType();

		// when
		List<CollegeResponse> result = GetAllByCampusType(campusType);

		// then
		assertThat(result).isNotEmpty();
	}

	private List<CollegeResponse> GetAllColleges() throws Exception {
		String response = mvc.perform(get("/api/campuses/colleges"))
			.andDo(print())
			.andExpect(status().isOk())
			.andReturn()
			.getResponse()
			.getContentAsString();
		return objectMapper.readValue(response,
			objectMapper.getTypeFactory().constructCollectionType(List.class, CollegeResponse.class));
	}

	private List<CollegeResponse> GetAllByCampusType(String campusType) throws Exception {
		String response = mvc.perform(get("/api/campuses/colleges/campus/{campusType}", campusType))
			.andDo(print())
			.andExpect(status().isOk())
			.andReturn()
			.getResponse()
			.getContentAsString();
		return objectMapper.readValue(response,
			objectMapper.getTypeFactory().constructCollectionType(List.class, CollegeResponse.class));
	}
}
