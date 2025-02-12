package mju.iphak.maru_egg.campus.application.college.command.update;

import static mju.iphak.maru_egg.common.exception.ErrorCode.*;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import mju.iphak.maru_egg.campus.api.dto.request.UpdateCollegeRequest;
import mju.iphak.maru_egg.campus.domain.College;
import mju.iphak.maru_egg.campus.repository.CollegeRepository;

@Service
@Transactional
@RequiredArgsConstructor
public class UpdateCollegeService implements UpdateCollege {

	private final CollegeRepository collegeRepository;

	public void invoke(Long collegeId, UpdateCollegeRequest request) {
		College college = collegeRepository.findById(collegeId)
			.orElseThrow(() -> new EntityNotFoundException(
				String.format(NOT_FOUND_COLLEGE.getMessage(), request.campus())));
		college.update(request);
	}
}