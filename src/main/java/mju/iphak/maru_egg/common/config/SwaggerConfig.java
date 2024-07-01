package mju.iphak.maru_egg.common.config;

import java.util.LinkedHashMap;
import java.util.Map;

import org.springdoc.core.customizers.OperationCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.media.Content;
import io.swagger.v3.oas.models.media.MediaType;
import io.swagger.v3.oas.models.media.Schema;
import io.swagger.v3.oas.models.responses.ApiResponse;
import io.swagger.v3.oas.models.responses.ApiResponses;
import mju.iphak.maru_egg.common.exception.ErrorResponse;

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
			.description("명지대 입학처 챗봇, MARU EGG REST API 문서.")
			.version("1.0.0");
	}

	/**
	 * Operation 의 기존 ApiResponse 에 공통 응답 추가
	 */
	@Bean
	public OperationCustomizer operationCustomizer() {
		return (operation, handlerMethod) -> {
			ApiResponses apiResponses = operation.getResponses();
			if (apiResponses == null) {
				apiResponses = new ApiResponses();
				operation.setResponses(apiResponses);
			}
			apiResponses.putAll(getCommonResponses());
			return operation;
		};
	}

	/**
	 * 공통 응답 정보를 생성하여 맵으로 리턴한다.
	 *
	 * @return LinkedHashMap<String, ApiResponse> ApiResponse Map
	 */
	private Map<String, ApiResponse> getCommonResponses() {
		LinkedHashMap<String, ApiResponse> responses = new LinkedHashMap<>();
		responses.put("404", notFoundResponse());
		responses.put("500", internalServerResponse());
		return responses;
	}

	/**
	 * 404 Response 를 생성하여 리턴
	 *
	 * @return ApiResponse 404 응답 객체
	 */
	private ApiResponse notFoundResponse() {
		ApiResponse apiResponse = new ApiResponse();
		apiResponse.setDescription("""
			Not Found
			- 요청한 URI 가 올바른지 확인한다.
			""");
		addContent(apiResponse, 404, "Not Found");
		return apiResponse;
	}

	/**
	 * 500 Response 를 생성하여 리턴
	 *
	 * @return ApiResponse 500 응답 객체
	 */
	private ApiResponse internalServerResponse() {
		ApiResponse apiResponse = new ApiResponse();
		apiResponse.setDescription("""
			Internal Server Error (Unchecked Exception)
			- API 담당자에게 오류 확인을 요청한다.
			""");
		addContent(apiResponse, 500, "Internal Server Error");
		return apiResponse;
	}

	/**
	 * ApiResponse 의 Content 정보를 추가
	 *
	 * @param apiResponse Api 응답 객체
	 * @param status      응답 상태 코드
	 * @param message     응답 메시지
	 */
	@SuppressWarnings("rawtypes")
	private void addContent(ApiResponse apiResponse, int status, String message) {
		Content content = new Content();
		MediaType mediaType = new MediaType();
		Schema schema = new Schema<>();
		schema.$ref("#/components/schemas/ErrorResponse");
		mediaType.schema(schema).example(ErrorResponse.of(status, message));
		content.addMediaType("application/json", mediaType);
		apiResponse.setContent(content);
	}
}
