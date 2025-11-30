package com.evaluacion.usuarios.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
            .info(new Info()
                .title("Usuarios API")
                .version("1.0")
                .description("API REST para gestión de usuarios con autenticación JWT. Endpoints públicos: POST /api/usuarios (crear) y POST /auth/login. Resto requiere JWT."))
            .components(new Components()
                .addSecuritySchemes("bearerAuth",
                    new SecurityScheme()
                        .type(SecurityScheme.Type.HTTP)
                        .scheme("bearer")
                        .bearerFormat("JWT")
                        .description("Ingrese el token JWT obtenido desde POST /auth/login")))
            .addSecurityItem(new SecurityRequirement().addList("bearerAuth"));
    }
}
