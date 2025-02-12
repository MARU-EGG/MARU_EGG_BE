package mju.iphak.maru_egg.campus.application.college.query.find;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;
import mju.iphak.maru_egg.campus.api.dto.response.CollegeResponse;
import mju.iphak.maru_egg.campus.repository.CollegeRepository;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class FindAllCollegeService implements FindAllCollege {

	private final CollegeRepository collegeRepository;

	public List<CollegeResponse> invoke() {
		return collegeRepository.findAll().stream()
			.map(CollegeResponse::from)
			.toList();
	}
}
