package mju.iphak.maru_egg.campus.application.college.command.delete;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;
import mju.iphak.maru_egg.campus.repository.CollegeRepository;

@Service
@Transactional
@RequiredArgsConstructor
public class DeleteCollegeService implements DeleteCollege {

	private final CollegeRepository collegeRepository;

	public void invoke(final Long collegeId) {
		collegeRepository.deleteById(collegeId);
	}
}
