package mju.iphak.maru_egg.campus.application.college.command.create;

import static mju.iphak.maru_egg.common.exception.ErrorCode.*;

import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;
import mju.iphak.maru_egg.campus.api.dto.request.CreateCollegeRequest;
import mju.iphak.maru_egg.campus.domain.College;
import mju.iphak.maru_egg.campus.repository.CollegeRepository;

@Service
@RequiredArgsConstructor
public class CreateCollegeService implements CreateCollege {

	private final CollegeRepository collegeRepository;

	public void invoke(CreateCollegeRequest request) {
		validateDuplicateCollegeName(request);

		College college = request.toEntity();
		collegeRepository.save(college);
	}

	private void validateDuplicateCollegeName(final CreateCollegeRequest request) {
		if (collegeRepository.existsByName(request.name())) {
			throw new IllegalArgumentException(String.format(DUPLICATE_COLLEGE_NAME.getMessage(), request.name()));
		}
	}
}