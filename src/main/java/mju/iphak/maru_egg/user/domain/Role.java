package mju.iphak.maru_egg.user.domain;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public enum Role {
	GUEST("ROLE_GUEST"),
	ADMIN("ROLE_ADMIN");

	private final String role;
}
