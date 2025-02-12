package mju.iphak.maru_egg.campus.api.swagger;

import java.util.List;

import org.springframework.web.bind.annotation.PathVariable;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import mju.iphak.maru_egg.campus.api.dto.response.CollegeResponse;
import mju.iphak.maru_egg.campus.domain.CampusType;

@Tag(name = "College API", description = "대학 관련 API 입니다.")
public interface CollegeControllerDocs {

	@Operation(summary = "모든 대학 조회",
		description = "전체 대학과 해당 학과 목록을 조회합니다.",
		responses = {@ApiResponse(responseCode = "200", description = "모든 대학 조회 성공")})
	List<CollegeResponse> getAll();

	@Operation(summary = "캠퍼스 타입별 대학 조회",
		description = "특정 캠퍼스 타입(자연캠퍼스, 인문캠퍼스)에 속한 대학을 조회합니다.",
		responses = {@ApiResponse(responseCode = "200", description = "캠퍼스 타입별 대학 조회 성공")})
	List<CollegeResponse> getAllByCampusType(@PathVariable("campusType") CampusType campusType);
}
