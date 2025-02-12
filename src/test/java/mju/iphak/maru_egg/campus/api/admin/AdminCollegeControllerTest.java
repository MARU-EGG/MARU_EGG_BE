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
import mju.iphak.maru_egg.campus.application.college.command.create.CreateCollegeService;
import mju.iphak.maru_egg.campus.application.college.command.delete.DeleteCollegeService;
import mju.iphak.maru_egg.campus.application.college.command.update.UpdateCollegeService;
import mju.iphak.maru_egg.campus.domain.CampusType;
import mju.iphak.maru_egg.campus.domain.College;
import mju.iphak.maru_egg.campus.repository.CollegeRepository;
import mju.iphak.maru_egg.common.IntegrationTest;

@WithMockUser(roles = "ADMIN")
class AdminCollegeControllerTest extends IntegrationTest {

	@Autowired
	private CreateCollegeService createCollegeService;

	@Autowired
	private UpdateCollegeService updateCollegeService;

	@Autowired
	private DeleteCollegeService deleteCollegeService;

	@Autowired
	private CollegeRepository collegeRepository;

	private College college;

	@BeforeEach
	void setUp() {
		collegeRepository.deleteAll();
		College college = College.builder().name("자연과학대학").campus(CampusType.NATURAL).build();
		this.college = collegeRepository.save(college);
	}

	@DisplayName("[성공] 대학 생성 요청")
	@Test
	void 대학_생성_성공() throws Exception {
		// given
		CreateCollegeRequest request = new CreateCollegeRequest("공과대학", null, CampusType.NATURAL.getType());

		// when
		ResultActions result = PostCollege(request);

		// then
		result.andExpect(status().isCreated());
	}

	@DisplayName("[성공] 대학 수정 요청")
	@Test
	void 대학_수정_성공() throws Exception {
		// given
		Long collegeId = college.getId();
		UpdateCollegeRequest request = new UpdateCollegeRequest("변경된 대학명", null, null);

		// when
		ResultActions result = PutCollege(collegeId, request);

		// then
		result.andExpect(status().isOk());
	}

	@DisplayName("[성공] 대학 삭제 요청")
	@Test
	void 대학_삭제_성공() throws Exception {
		// given
		Long collegeId = college.getId();

		// when
		ResultActions result = DeleteCollege(collegeId);

		// then
		result.andExpect(status().isNoContent());
	}

	private ResultActions PostCollege(CreateCollegeRequest request) throws Exception {
		return mvc.perform(post("/api/admin/colleges")
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(request)))
			.andDo(print());
	}

	private ResultActions PutCollege(Long id, UpdateCollegeRequest request) throws Exception {
		return mvc.perform(put("/api/admin/colleges/{id}", id)
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(request)))
			.andDo(print());
	}

	private ResultActions DeleteCollege(Long id) throws Exception {
		return mvc.perform(delete("/api/admin/colleges/{id}", id)
				.contentType(MediaType.APPLICATION_JSON))
			.andDo(print());
	}
}
