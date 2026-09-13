package com.bankflow.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * OpenAPI 3 / Swagger Documentation Configuration.
 * 
 * Key Interview Concepts:
 * - OpenAPI / Swagger automates interactive API documentation.
 * - Configuring the SecurityScheme allows developers and QA engineers to authenticate
 *   with Bearer JWT directly from the Swagger UI interface ('Authorize' button).
 */
@Configuration
public class OpenApiConfig {

    private static final String SECURITY_SCHEME_NAME = "Bearer Authentication";

    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("BankFlow — Banking & Transaction Management System API")
                        .version("1.0.0")
                        .description("Production-style enterprise REST API for account management, deposits, "
                                + "atomic inter-account transfers with pessimistic concurrency locking, "
                                + "idempotency support, beneficiary management, PDF statements, and admin analytics.")
                        .contact(new Contact()
                                .name("BankFlow Engineering Team")
                                .email("dev@bankflow.com")
                                .url("https://bankflow.com"))
                        .license(new License()
                                .name("Apache 2.0")
                                .url("https://www.apache.org/licenses/LICENSE-2.0.html")))
                .addSecurityItem(new SecurityRequirement().addList(SECURITY_SCHEME_NAME))
                .components(new Components()
                        .addSecuritySchemes(SECURITY_SCHEME_NAME,
                                new SecurityScheme()
                                        .name(SECURITY_SCHEME_NAME)
                                        .type(SecurityScheme.Type.HTTP)
                                        .scheme("bearer")
                                        .bearerFormat("JWT")
                                        .description("Enter your JWT token obtained from /api/auth/login")));
    }
}
