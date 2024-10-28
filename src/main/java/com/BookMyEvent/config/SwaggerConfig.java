package com.BookMyEvent.config;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeIn;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
import io.swagger.v3.oas.annotations.security.SecuritySchemes;
import io.swagger.v3.oas.annotations.servers.Server;
import org.springframework.context.annotation.Configuration;

@Configuration
@OpenAPIDefinition(
    info =
    @Info(
        title = "EventBook Backend API Documentation",
        description = "EventBook backend endpoints description",
        version = "1.0"
        ),
    servers = {
        @Server(url = "http://localhost:8080/api/v1", description = "Local dev ENV"),
        @Server(url = "https://rendereventapp.onrender.com/api/v1", description = "Prod ENV")

    },
    security = {@SecurityRequirement(name = "bearerAuth")})
@SecuritySchemes(
    value = {
        @SecurityScheme(
            name = "bearerAuth",
            description = "JWT auth description",
            scheme = "bearer",
            type = SecuritySchemeType.HTTP,
            bearerFormat = "JWT",
            in = SecuritySchemeIn.HEADER)
    })
public class SwaggerConfig {


}
