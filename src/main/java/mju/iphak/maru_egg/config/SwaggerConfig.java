package mju.iphak.maru_egg.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;

@Configuration
public class SwaggerConfig {

	@Bean
	public OpenAPI openAPI() {
		return new OpenAPI()
			.components(new Components())
			.info(apiInfo());
	}

	private Info apiInfo() {
		return new Info()
			.title("MARU EGG REST API")
			.description("명지대 입학처 챗봇, MARU EGG REST API 문서입니다.")
			.version("1.0.0");
	}
}
