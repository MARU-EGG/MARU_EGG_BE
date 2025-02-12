package mju.iphak.maru_egg.campus.api.swagger;

import java.util.List;

import org.springframework.web.bind.annotation.PathVariable;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import mju.iphak.maru_egg.campus.api.dto.response.DepartmentResponse;

@Tag(name = "Department API", description = "학과 관련 API 입니다.")
public interface DepartmentControllerDocs {

	@Operation(summary = "모든 학과 조회",
		description = "전체 학과 목록을 조회합니다.",
		responses = {@ApiResponse(responseCode = "200", description = "모든 학과 조회 성공")})
	List<DepartmentResponse> getAll();

	@Operation(summary = "대학별 학과 조회",
		description = "특정 대학에 속한 학과 목록을 조회합니다.",
		responses = {@ApiResponse(responseCode = "200", description = "대학별 학과 조회 성공")})
	List<DepartmentResponse> getAllByCollege(@PathVariable("collegeId") Long collegeId);
}
