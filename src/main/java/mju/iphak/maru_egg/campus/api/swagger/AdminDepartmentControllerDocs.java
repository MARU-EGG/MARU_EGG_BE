package mju.iphak.maru_egg.campus.api.swagger;

import org.springframework.web.bind.annotation.PathVariable;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import mju.iphak.maru_egg.campus.api.dto.request.CreateDepartmentRequest;
import mju.iphak.maru_egg.campus.api.dto.request.UpdateDepartmentRequest;

@Tag(name = "Admin Department API", description = "관리자용 학과 관리 API 입니다.")
public interface AdminDepartmentControllerDocs {

	@Operation(
		summary = "학과 생성",
		description = "새로운 학과를 생성합니다.",
		responses = {
			@ApiResponse(responseCode = "200", description = "학과 생성 성공")
		}
	)
	void createDepartment(CreateDepartmentRequest request);

	@Operation(
		summary = "학과 수정",
		description = "기존 학과 정보를 수정합니다.",
		responses = {
			@ApiResponse(responseCode = "200", description = "학과 수정 성공")
		}
	)
	void updateDepartment(@PathVariable("departmentId") Long departmentId, UpdateDepartmentRequest request);

	@Operation(
		summary = "학과 삭제",
		description = "기존 학과 정보를 삭제합니다.",
		responses = {
			@ApiResponse(responseCode = "200", description = "학과 삭제 성공")
		}
	)
	void deleteDepartment(@PathVariable("departmentId") Long departmentId);
}
