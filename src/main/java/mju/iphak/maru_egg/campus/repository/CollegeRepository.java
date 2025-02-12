package mju.iphak.maru_egg.campus.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import mju.iphak.maru_egg.campus.domain.College;

public interface CollegeRepository extends JpaRepository<College, Long> {
	boolean existsByName(String name);
}
