package com.banking.config;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
@OpenAPIDefinition(info = @io.swagger.v3.oas.annotations.info.Info(title = "Banking API", version = "1.0"), security = @SecurityRequirement(name = "bearerAuth"))
@SecurityScheme(name = "bearerAuth", type = SecuritySchemeType.HTTP, scheme = "bearer", bearerFormat = "JWT")
public class OpenApiConfig {

        @Bean
        public OpenAPI bankingOpenAPI() {
                return new OpenAPI()
                                .info(new Info()
                                                .title("Banking App Apis")
                                                .version("1.0")
                                                .description("REST API documentation for Banking Application")
                                                .contact(new Contact()
                                                                .name("Banking Team")
                                                                .email("banking@examle.com"))
                                                .license(new License()
                                                                .name("Apache 2.0")))
                                .servers(
                                                List.of(
                                                                new Server()
                                                                                .url("http://localhost:8080")
                                                                                .description("Local Development Server")));
        }
}
