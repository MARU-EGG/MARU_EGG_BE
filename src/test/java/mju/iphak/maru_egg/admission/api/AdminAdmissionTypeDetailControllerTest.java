package mju.iphak.maru_egg.admission.api;

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

import mju.iphak.maru_egg.admission.application.detail.command.create.CreateAdmissionTypeDetailService;
import mju.iphak.maru_egg.admission.application.detail.query.find.FindAllAdmissionTypeDetailService;
import mju.iphak.maru_egg.admission.application.status.command.init.InitAdmissionTypeStatusService;
import mju.iphak.maru_egg.admission.application.status.query.find.FindAdmissionTypeStatusService;
import mju.iphak.maru_egg.admission.domain.AdmissionType;
import mju.iphak.maru_egg.admission.api.dto.request.CreateAdmissionTypeDetailRequest;
import mju.iphak.maru_egg.admission.api.dto.request.UpdateAdmissionTypeDetailRequest;
import mju.iphak.maru_egg.common.IntegrationTest;

@WithMockUser(roles = "ADMIN")
class AdminAdmissionTypeDetailControllerTest extends IntegrationTest {

	@Autowired
	private FindAdmissionTypeStatusService findAdmissionTypeStatusService;

	@Autowired
	private FindAllAdmissionTypeDetailService findAllAdmissionTypeDetail;

	@Autowired
	private CreateAdmissionTypeDetailService createAdmissionTypeDetail;

	@Autowired
	private InitAdmissionTypeStatusService initAdmissionTypeStatus;

	@BeforeEach
	void setUp() {
		initAdmissionTypeStatus.invoke();
		createAdmissionTypeDetail.invoke("학교장추천전형", AdmissionType.SUSI);
	}

	@DisplayName("[성공] 전형 유형 세부정보 생성 요청")
	@Test
	void 전형_유형_세부정보_생성_성공() throws Exception {
		// given
		CreateAdmissionTypeDetailRequest request = new CreateAdmissionTypeDetailRequest(
			"New Detail", AdmissionType.SUSI);

		// when
		ResultActions result = PostAdmissionTypeDetail(request);

		// then
		result.andExpect(status().isOk());
	}

	@DisplayName("[성공] 전형 유형 세부정보 수정 요청")
	@Test
	void 전형_유형_세부정보_수정_성공() throws Exception {
		// given
		Long admissionTypeDetailId = findAllAdmissionTypeDetail.invoke().get(0).id();
		String updatedName = "UpdatedName";
		UpdateAdmissionTypeDetailRequest updateAdmissionTypeDetailRequest = new UpdateAdmissionTypeDetailRequest(
			updatedName);

		// when
		ResultActions result = PutAdmissionTypeDetail(admissionTypeDetailId, updateAdmissionTypeDetailRequest);

		// then
		result.andExpect(status().isOk());
	}

	@DisplayName("[성공] 전형 유형 세부정보 삭제 요청")
	@Test
	void 전형_유형_세부정보_삭제_성공() throws Exception {
		// given
		Long admissionTypeDetailId = findAllAdmissionTypeDetail.invoke().get(0).id();

		// when
		ResultActions result = DeleteAdmissionTypeDetail(admissionTypeDetailId);

		// then
		result.andExpect(status().isOk());
	}

	private ResultActions PostAdmissionTypeDetail(CreateAdmissionTypeDetailRequest request) throws Exception {
		return mvc.perform(post("/api/admin/admissions/detail")
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(request)))
			.andDo(print());
	}

	private ResultActions PutAdmissionTypeDetail(Long id, UpdateAdmissionTypeDetailRequest request) throws Exception {
		return mvc.perform(put("/api/admin/admissions/{id}", id)
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(request)))
			.andDo(print());
	}

	private ResultActions DeleteAdmissionTypeDetail(Long id) throws Exception {
		return mvc.perform(delete("/api/admin/admissions/{id}", id)
				.contentType(MediaType.APPLICATION_JSON))
			.andDo(print());
	}
}
