package mju.iphak.maru_egg.campus.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import mju.iphak.maru_egg.campus.domain.Department;

public interface DepartmentRepository extends JpaRepository<Department, Long> {
	List<Department> findByCollegeId(Long collegeId);

	boolean existsByName(String name);
}