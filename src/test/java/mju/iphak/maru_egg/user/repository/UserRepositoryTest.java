package mju.iphak.maru_egg.user.repository;

import static org.assertj.core.api.Assertions.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import mju.iphak.maru_egg.common.RepositoryTest;
import mju.iphak.maru_egg.user.domain.Role;
import mju.iphak.maru_egg.user.domain.User;

public class UserRepositoryTest extends RepositoryTest {

	@Autowired
	private UserRepository userRepository;

	private User saveUser;
	private String email;

	@BeforeEach
	public void setUp() throws Exception {
		email = "maru@gmail.com";
		saveUser = userRepository.save(User.builder().name("maruegg").email(email).role(Role.GUEST).build());
	}

	@Test
	public void existsByEmail_존재하는경우_true() {
		final boolean existsByEmail = userRepository.findByEmail(email).isPresent();
		assertThat(existsByEmail).isTrue();
	}

	@Test
	public void existsByEmail_존재하지않은_경우_false() {
		final boolean existsByEmail = userRepository.findByEmail("invalid@asd.com").isPresent();
		assertThat(existsByEmail).isFalse();
	}
}