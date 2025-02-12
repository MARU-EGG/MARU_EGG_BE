package mju.iphak.maru_egg.campus.api.swagger;

import org.springframework.web.bind.annotation.PathVariable;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import mju.iphak.maru_egg.campus.api.dto.request.CreateCollegeRequest;
import mju.iphak.maru_egg.campus.api.dto.request.UpdateCollegeRequest;

@Tag(name = "Admin College API", description = "관리자용 대학 관리 API 입니다.")
public interface AdminCollegeControllerDocs {

	@Operation(
		summary = "대학 생성",
		description = "새로운 대학을 생성합니다.",
		responses = {
			@ApiResponse(responseCode = "201", description = "대학 생성 성공")
		}
	)
	void createCollege(CreateCollegeRequest request);

	@Operation(
		summary = "대학 수정",
		description = "기존 대학 정보를 수정합니다.",
		responses = {
			@ApiResponse(responseCode = "200", description = "대학 수정 성공")
		}
	)
	void updateCollege(@PathVariable("collegeId") Long collegeId, UpdateCollegeRequest request);

	@Operation(
		summary = "대학 삭제",
		description = "기존 대학 정보를 삭제합니다.",
		responses = {
			@ApiResponse(responseCode = "204", description = "대학 삭제 성공")
		}
	)
	void deleteCollege(@PathVariable("collegeId") Long collegeId);
}
