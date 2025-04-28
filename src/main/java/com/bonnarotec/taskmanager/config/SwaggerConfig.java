package com.bonnarotec.taskmanager.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.OAuthFlows;
import io.swagger.v3.oas.models.security.OAuthFlow;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.security.Scopes;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SwaggerConfig {

    @Bean
    public OpenAPI openAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Task Manager API")
                        .description("API documentation with authentication")
                        .version("1.0"))
                .components(new Components()
                        // JWT Bearer auth scheme
                        .addSecuritySchemes("bearerAuth", new SecurityScheme()
                                .type(SecurityScheme.Type.HTTP)
                                .scheme("bearer")
                                .bearerFormat("JWT"))
                        // OAuth2 scheme (using Authorization Code Flow)
                        .addSecuritySchemes("oauth2", new SecurityScheme()
                                .type(SecurityScheme.Type.OAUTH2)
                                .flows(new OAuthFlows().authorizationCode(new OAuthFlow()
                                        .authorizationUrl("http://localhost:8080/oauth2/authorization/google")
                                        .tokenUrl("http://localhost:8080/login/oauth2/code/google")
                                        .scopes(new Scopes()
                                                .addString("read", "read access")
                                                .addString("write", "write access"))))))
                // Apply security globally
                .addSecurityItem(new SecurityRequirement().addList("bearerAuth"))
                .addSecurityItem(new SecurityRequirement().addList("oauth2"));
    }
}
