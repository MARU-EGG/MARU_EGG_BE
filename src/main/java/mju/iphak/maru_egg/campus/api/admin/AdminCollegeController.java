package mju.iphak.maru_egg.campus.api.admin;

import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import lombok.RequiredArgsConstructor;
import mju.iphak.maru_egg.campus.api.dto.request.CreateCollegeRequest;
import mju.iphak.maru_egg.campus.api.dto.request.UpdateCollegeRequest;
import mju.iphak.maru_egg.campus.api.swagger.AdminCollegeControllerDocs;
import mju.iphak.maru_egg.campus.application.college.command.create.CreateCollege;
import mju.iphak.maru_egg.campus.application.college.command.delete.DeleteCollege;
import mju.iphak.maru_egg.campus.application.college.command.update.UpdateCollege;

@RestController
@RequestMapping("/api/admin/campuses/colleges")
@RequiredArgsConstructor
public class AdminCollegeController implements AdminCollegeControllerDocs {

	private final CreateCollege createCollege;
	private final UpdateCollege updateCollege;
	private final DeleteCollege deleteCollege;

	@PostMapping
	public void createCollege(@RequestBody CreateCollegeRequest request) {
		createCollege.invoke(request);
	}

	@PutMapping("/{collegeId}")
	public void updateCollege(@PathVariable Long collegeId, @RequestBody UpdateCollegeRequest request) {
		updateCollege.invoke(collegeId, request);
	}

	@DeleteMapping("/{collegeId}")
	public void deleteCollege(@PathVariable Long collegeId) {
		deleteCollege.invoke(collegeId);
	}
}
