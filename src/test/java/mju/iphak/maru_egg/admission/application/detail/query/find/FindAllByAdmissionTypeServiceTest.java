package mju.iphak.maru_egg.admission.application.detail.query.find;

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
import mju.iphak.maru_egg.admission.domain.AdmissionTypeDetail;
import mju.iphak.maru_egg.admission.domain.AdmissionTypeStatus;
import mju.iphak.maru_egg.admission.api.dto.response.AdmissionTypeDetailResponse;
import mju.iphak.maru_egg.admission.repository.AdmissionTypeDetailRepository;
import mju.iphak.maru_egg.common.MockTest;

class FindAllByAdmissionTypeServiceTest extends MockTest {

	@Mock
	private AdmissionTypeDetailRepository admissionTypeDetailRepository;

	@InjectMocks
	private FindAllByAdmissionTypeService findAllByAdmissionTypeService;

	@BeforeEach
	void setUp() {
		MockitoAnnotations.openMocks(this);
	}

	@DisplayName("[성공] 상세 정보 조회 요청")
	@Test
	void 상세정보_조회_성공() {
		// given
		AdmissionType type = AdmissionType.SUSI;
		AdmissionTypeStatus admissionTypeStatus = AdmissionTypeStatus.of(AdmissionType.SUSI);
		AdmissionTypeDetail admissionTypeDetail = AdmissionTypeDetail.of("학교장추천전형", admissionTypeStatus);
		when(admissionTypeDetailRepository.findAllByAdmissionType(type))
			.thenReturn(List.of(admissionTypeDetail));

		// when
		List<AdmissionTypeDetailResponse> result = findAllByAdmissionTypeService.invoke(type);

		// then
		assertThat(result).isNotEmpty()
			.hasSize(1)
			.extracting(AdmissionTypeDetailResponse::name)
			.contains("학교장추천전형");
	}
}
