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

import mju.iphak.maru_egg.admission.application.status.command.delete.DeleteAdmissionTypeStatusService;
import mju.iphak.maru_egg.admission.application.status.command.init.InitAdmissionTypeStatusService;
import mju.iphak.maru_egg.admission.application.status.query.find.FindAdmissionTypeStatusService;
import mju.iphak.maru_egg.admission.domain.AdmissionType;
import mju.iphak.maru_egg.admission.api.dto.request.UpdateAdmissionTypeStatusRequest;
import mju.iphak.maru_egg.common.IntegrationTest;

@WithMockUser(roles = "ADMIN")
class AdminAdmissionTypeStatusControllerTest extends IntegrationTest {

	@Autowired
	private FindAdmissionTypeStatusService findAdmissionTypeStatusService;

	@Autowired
	private DeleteAdmissionTypeStatusService deleteAdmissionTypeStatus;

	@Autowired
	private InitAdmissionTypeStatusService initAdmissionTypeStatus;

	@BeforeEach
	void setUp() {
		initAdmissionTypeStatus.invoke();
		deleteAdmissionTypeStatus.invoke(AdmissionType.JEONGSI);
	}

	@DisplayName("[성공] 질문 상태 초기화 요청")
	@Test
	void 질문_상태_초기화_성공() throws Exception {
		// given & when
		ResultActions resultActions = InitializeAdmissionTypeStatus();

		// then
		resultActions.andExpect(status().isOk());
	}

	@DisplayName("[성공] 질문 타입 상태 변경 요청")
	@Test
	void 질문_타입_상태_변경_성공() throws Exception {
		// given
		UpdateAdmissionTypeStatusRequest request = new UpdateAdmissionTypeStatusRequest(AdmissionType.SUSI);

		// when
		ResultActions resultActions = UpdateAdmissionTypeStatus(request);

		// then
		resultActions.andExpect(status().isOk());
	}

	@DisplayName("[실패] 존재하지 않는 질문 타입 상태 변경 요청")
	@Test
	void 질문_타입_상태_변경_실패_존재하지_않는_질문_타입() throws Exception {
		// given
		UpdateAdmissionTypeStatusRequest request = new UpdateAdmissionTypeStatusRequest(AdmissionType.JEONGSI);

		// when
		ResultActions resultActions = UpdateAdmissionTypeStatus(request);

		// then
		resultActions.andExpect(status().isNotFound());
	}

	private ResultActions InitializeAdmissionTypeStatus() throws Exception {
		return mvc.perform(post("/api/admin/questions/status/init")
				.contentType(MediaType.APPLICATION_JSON))
			.andDo(print());
	}

	private ResultActions UpdateAdmissionTypeStatus(UpdateAdmissionTypeStatusRequest request) throws Exception {
		return mvc.perform(put("/api/admin/questions/status")
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(request)))
			.andDo(print());
	}
}
