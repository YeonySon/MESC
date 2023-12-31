package com.ksol.mes.global.config;

import java.util.Arrays;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;

@Configuration
public class SwaggerConfig {
	SecurityScheme securityScheme = new SecurityScheme()
		.type(SecurityScheme.Type.HTTP).scheme("Bearer").bearerFormat("JWT")
		.in(SecurityScheme.In.HEADER).name("Authorization");
	SecurityRequirement securityRequirement = new SecurityRequirement().addList("Bearer");

	@Bean
	public OpenAPI openAPI() {
		return new OpenAPI()
			.components(new Components().addSecuritySchemes("Bearer", securityScheme))
			.security(Arrays.asList(securityRequirement))
			.info(apiInfo());
	}

	private Info apiInfo() {
		return new Info()
			.title("MES doc")
			.description("mes for Swagger UI")
			.version("1.0.0");
	}
}
