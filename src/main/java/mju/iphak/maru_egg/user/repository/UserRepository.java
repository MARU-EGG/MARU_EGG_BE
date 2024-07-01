package mju.iphak.maru_egg.user.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import mju.iphak.maru_egg.user.domain.User;

public interface UserRepository extends JpaRepository<User, Long> {
	Optional<User> findByEmail(String email);
}
