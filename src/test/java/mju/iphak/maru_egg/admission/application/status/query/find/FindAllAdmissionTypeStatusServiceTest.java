package mju.iphak.maru_egg.admission.application.status.query.find;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import mju.iphak.maru_egg.admission.domain.AdmissionType;
import mju.iphak.maru_egg.admission.domain.AdmissionTypeStatus;
import mju.iphak.maru_egg.admission.api.dto.response.AdmissionTypeStatusResponse;
import mju.iphak.maru_egg.admission.repository.AdmissionTypeStatusRepository;
import mju.iphak.maru_egg.common.MockTest;

class FindAllAdmissionTypeStatusServiceTest extends MockTest {

	@Mock
	private AdmissionTypeStatusRepository admissionTypeStatusRepository;

	@InjectMocks
	private FindAllAdmissionTypeStatusService findAllAdmissionTypeStatus;

	@BeforeEach
	void setUp() {
		MockitoAnnotations.openMocks(this);
	}

	@DisplayName("[성공] 입학 전형 상태 목록 조회 요청")
	@Test
	void 입학_전형_상태_목록_조회_성공() {
		// given
		AdmissionTypeStatus status1 = new AdmissionTypeStatus(AdmissionType.SUSI, true);
		AdmissionTypeStatus status2 = new AdmissionTypeStatus(AdmissionType.JEONGSI, false);
		when(admissionTypeStatusRepository.findAll()).thenReturn(List.of(status1, status2));

		// when
		List<AdmissionTypeStatusResponse> result = findAllAdmissionTypeStatus.invoke();

		// then
		verify(admissionTypeStatusRepository, times(1)).findAll();
		assertThat(result).hasSize(2)
			.extracting("type", "activated")
			.containsExactlyInAnyOrder(
				tuple(AdmissionType.SUSI, true),
				tuple(AdmissionType.JEONGSI, false)
			);
	}
}
