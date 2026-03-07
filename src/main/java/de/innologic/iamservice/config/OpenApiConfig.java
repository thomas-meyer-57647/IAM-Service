package de.innologic.iamservice.config;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.info.Contact;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import io.swagger.v3.oas.models.Components;

@Configuration
@ConditionalOnProperty(prefix = "iam.swagger", name = "enabled", havingValue = "true")
@OpenAPIDefinition(
        info = @Info(
                title = "${spring.application.name:iam-service}",
                version = "${iam.swagger.version:1.0}",
                description = "REST API for the IAM service as specified in Pflichtenheft V1.6",
                contact = @Contact(name = "Flowtrack Platform", email = "iam-service@innologic.de")
        ),
        security = @SecurityRequirement(name = "bearerAuth")
)
@SecurityScheme(
        name = "bearerAuth",
        type = SecuritySchemeType.HTTP,
        scheme = "bearer",
        bearerFormat = "JWT",
        description = "JWT Bearer token containing tenant and subject claims"
)
public class OpenApiConfig {

    @Bean
    public io.swagger.v3.oas.models.OpenAPI openAPI(@Value("${iam.swagger.version:1.0}") String version,
                                                     @Value("${spring.application.name:iam-service}") String appName) {
        return new io.swagger.v3.oas.models.OpenAPI()
                .components(new Components().addSecuritySchemes("bearerAuth",
                        new io.swagger.v3.oas.models.security.SecurityScheme()
                                .type(io.swagger.v3.oas.models.security.SecurityScheme.Type.HTTP)
                                .scheme("bearer")
                                .bearerFormat("JWT")))
                .info(new io.swagger.v3.oas.models.info.Info()
                        .title(appName + " API")
                        .version(version)
                        .description("OpenAPI definition for the iam-service"));
    }
}
