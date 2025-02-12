package mju.iphak.maru_egg.common.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.ProviderManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.factory.PasswordEncoderFactories;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.HttpStatusEntryPoint;
import org.springframework.security.web.authentication.logout.LogoutFilter;
import org.springframework.security.web.servlet.util.matcher.MvcRequestMatcher;
import org.springframework.web.servlet.handler.HandlerMappingIntrospector;

import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.RequiredArgsConstructor;
import mju.iphak.maru_egg.auth.jwt.filter.JwtAuthenticationProcessingFilter;
import mju.iphak.maru_egg.auth.jwt.service.JwtService;
import mju.iphak.maru_egg.auth.login.filter.CustomJsonUsernamePasswordAuthenticationFilter;
import mju.iphak.maru_egg.auth.login.handler.LoginFailureHandler;
import mju.iphak.maru_egg.auth.login.handler.LoginSuccessHandler;
import mju.iphak.maru_egg.auth.login.service.LoginService;
import mju.iphak.maru_egg.user.repository.UserRepository;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@RequiredArgsConstructor
public class SecurityConfig {

	@Value("${custom.swagger.key}")
	private String swaggerKey;

	private static final String API_PREFIX = "/api";
	private static final String ADMIN_API_PREFIX = "/api/admin";

	private final LoginService loginService;
	private final JwtService jwtService;
	private final UserRepository userRepository;
	private final ObjectMapper objectMapper;

	@Bean
	public SecurityFilterChain filterChain(HttpSecurity http, HandlerMappingIntrospector introspector) throws
		Exception {
		http
			.csrf(AbstractHttpConfigurer::disable)
			.formLogin(AbstractHttpConfigurer::disable)
			.httpBasic(AbstractHttpConfigurer::disable)
			.sessionManagement(sessionManagement ->
				sessionManagement.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
			.exceptionHandling(e -> e
				.authenticationEntryPoint(new HttpStatusEntryPoint(HttpStatus.UNAUTHORIZED)))
			.authorizeHttpRequests(authorizeHttpRequests ->
				authorizeHttpRequests
					.requestMatchers("/favicon.ico", "/css/**", "/js/**", "/img/**", "/lib/**")
					.permitAll()
					.requestMatchers(new MvcRequestMatcher(introspector, API_PREFIX + "/"))
					.permitAll()
					.requestMatchers(new MvcRequestMatcher(introspector, API_PREFIX + "/campuses/**"))
					.permitAll()
					.requestMatchers(new MvcRequestMatcher(introspector, API_PREFIX + "/auth/sign-up"))
					.permitAll()
					.requestMatchers(new MvcRequestMatcher(introspector, API_PREFIX + "/auth/sign-in"))
					.permitAll()
					.requestMatchers(new MvcRequestMatcher(introspector, API_PREFIX + "/questions/**"))
					.permitAll()
					.requestMatchers(new MvcRequestMatcher(introspector, API_PREFIX + "/question/**"))
					.permitAll()
					.requestMatchers(new MvcRequestMatcher(introspector, API_PREFIX + "/admissions/**"))
					.permitAll()
					.requestMatchers(new MvcRequestMatcher(introspector, ADMIN_API_PREFIX + "/**"))
					.hasRole("ADMIN")
					.requestMatchers(new MvcRequestMatcher(introspector, "/actuator/**"))
					.permitAll()
					.requestMatchers(new MvcRequestMatcher(introspector, "/grafana/**"))
					.permitAll()
					.requestMatchers(new MvcRequestMatcher(introspector, String.format("/%s/api-docs/**", swaggerKey)))
					.permitAll()
					.requestMatchers(
						new MvcRequestMatcher(introspector, String.format("/%s/swagger-ui/**", swaggerKey)))
					.permitAll()
					.anyRequest()
					.authenticated())

			// AuthenticationFilter 커스텀 적용
			.addFilterAfter(customJsonUsernamePasswordAuthenticationFilter(), LogoutFilter.class)

			// JWT Filter 후 사용자 인증
			.addFilterBefore(jwtAuthenticationProcessingFilter(), CustomJsonUsernamePasswordAuthenticationFilter.class);

		return http.build();
	}

	@Bean
	public PasswordEncoder passwordEncoder() {
		return PasswordEncoderFactories.createDelegatingPasswordEncoder();
	}

	// AuthenticationManager <-> AuthenticatorProvider <-> UserDetailsService <-> UserDetails
	@Bean
	public AuthenticationManager authenticationManager() {
		DaoAuthenticationProvider provider = new DaoAuthenticationProvider();
		provider.setPasswordEncoder(passwordEncoder());
		provider.setUserDetailsService(loginService);
		return new ProviderManager(provider);
	}

	@Bean
	public LoginSuccessHandler loginSuccessHandler() {
		return new LoginSuccessHandler(jwtService, userRepository);
	}

	@Bean
	public LoginFailureHandler loginFailureHandler() {
		return new LoginFailureHandler();
	}

	@Bean
	public CustomJsonUsernamePasswordAuthenticationFilter customJsonUsernamePasswordAuthenticationFilter() {
		CustomJsonUsernamePasswordAuthenticationFilter customFilter =
			new CustomJsonUsernamePasswordAuthenticationFilter(objectMapper);
		customFilter.setAuthenticationManager(authenticationManager());
		customFilter.setAuthenticationSuccessHandler(loginSuccessHandler());
		customFilter.setAuthenticationFailureHandler(loginFailureHandler());
		return customFilter;
	}

	@Bean
	public JwtAuthenticationProcessingFilter jwtAuthenticationProcessingFilter() {
		return new JwtAuthenticationProcessingFilter(jwtService, userRepository);
	}
}