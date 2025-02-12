package mju.iphak.maru_egg.campus.api;

import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import lombok.RequiredArgsConstructor;
import mju.iphak.maru_egg.campus.api.dto.response.CollegeResponse;
import mju.iphak.maru_egg.campus.api.swagger.CollegeControllerDocs;
import mju.iphak.maru_egg.campus.application.college.query.find.FindAllByCampusType;
import mju.iphak.maru_egg.campus.application.college.query.find.FindAllCollege;
import mju.iphak.maru_egg.campus.domain.CampusType;

@RestController
@RequestMapping("/api/colleges")
@RequiredArgsConstructor
public class CollegeController implements CollegeControllerDocs {

	private final FindAllCollege findAllCollege;
	private final FindAllByCampusType findAllByCampusType;

	@GetMapping
	public List<CollegeResponse> getAll() {
		return findAllCollege.invoke();
	}

	@GetMapping("/campus/{campusType}")
	public List<CollegeResponse> getAllByCampusType(@PathVariable("campusType") CampusType campusType) {
		return findAllByCampusType.invoke(campusType);
	}
}
