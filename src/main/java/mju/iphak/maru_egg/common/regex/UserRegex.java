package mju.iphak.maru_egg.common.regex;

public class UserRegex {
	public static final String NAME_REGEXP = "^[가-힣a-zA-Z\\d\\[\\]<>-]{2,20}";
	public static final String EMAIL_REGEXP = "[a-z0-9]+@[a-z]+\\.[a-z]{2,8}$";
	public static final String PASSWORD_REGEXP = "^(?=.*[a-zA-Z])(?=.*[0-9]).{8,25}$";
}
