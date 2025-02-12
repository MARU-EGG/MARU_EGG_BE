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

import mju.iphak.maru_egg.campus.api.dto.request.CreateCollegeRequest;
import mju.iphak.maru_egg.campus.api.dto.request.UpdateCollegeRequest;
import mju.iphak.maru_egg.campus.domain.CampusType;
import mju.iphak.maru_egg.campus.domain.College;
import mju.iphak.maru_egg.campus.repository.CollegeRepository;
import mju.iphak.maru_egg.common.IntegrationTest;

@WithMockUser(roles = "ADMIN")
class AdminCollegeControllerTest extends IntegrationTest {

	@Autowired
	private CollegeRepository collegeRepository;

	private College college;

	@BeforeEach
	void setUp() {
		collegeRepository.deleteAll();
		college = collegeRepository.save(
			College.builder()
				.campus(CampusType.NATURAL)
				.name("자연과학대학")
				.description("자연과학 관련 학과들")
				.build()
		);
	}

	@DisplayName("[성공] 대학 생성 요청")
	@Test
	void 대학_생성_성공() throws Exception {
		// given
		CreateCollegeRequest request = new CreateCollegeRequest("자연캠퍼스", "공과대학", "공학 관련 학과들");

		// when
		ResultActions result = postCollege(request);

		// then
		result.andExpect(status().isOk());
	}

	@DisplayName("[성공] 대학 수정 요청")
	@Test
	void 대학_수정_성공() throws Exception {
		// given
		Long collegeId = college.getId();
		UpdateCollegeRequest request = new UpdateCollegeRequest("인문캠퍼스", null, "업데이트된 설명");

		// when
		ResultActions result = putCollege(collegeId, request);

		// then
		result.andExpect(status().isOk());
	}

	@DisplayName("[성공] 대학 삭제 요청")
	@Test
	void 대학_삭제_성공() throws Exception {
		// given
		Long collegeId = college.getId();

		// when
		ResultActions result = deleteCollege(collegeId);

		// then
		result.andExpect(status().isOk());
	}

	private ResultActions postCollege(CreateCollegeRequest request) throws Exception {
		return mvc.perform(post("/api/admin/campuses/colleges")
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(request)))
			.andDo(print());
	}

	private ResultActions putCollege(Long id, UpdateCollegeRequest request) throws Exception {
		return mvc.perform(put("/api/admin/campuses/colleges/{id}", id)
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(request)))
			.andDo(print());
	}

	private ResultActions deleteCollege(Long id) throws Exception {
		return mvc.perform(delete("/api/admin/campuses/colleges/{id}", id)
				.contentType(MediaType.APPLICATION_JSON))
			.andDo(print());
	}
}
