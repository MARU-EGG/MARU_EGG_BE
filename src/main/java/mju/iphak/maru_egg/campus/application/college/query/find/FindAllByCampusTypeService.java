package mju.iphak.maru_egg.campus.application.college.query.find;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;
import mju.iphak.maru_egg.campus.api.dto.response.CollegeResponse;
import mju.iphak.maru_egg.campus.domain.CampusType;
import mju.iphak.maru_egg.campus.repository.CollegeRepository;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class FindAllByCampusTypeService implements FindAllByCampusType {

	private final CollegeRepository collegeRepository;

	public List<CollegeResponse> invoke(final String campusType) {
		return collegeRepository.findAll().stream()
			.filter(college -> college.getCampus().equals(CampusType.convertToCategory(campusType)))
			.map(CollegeResponse::from)
			.toList();
	}
}
