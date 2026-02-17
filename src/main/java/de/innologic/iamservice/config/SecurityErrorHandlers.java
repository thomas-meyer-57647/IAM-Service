package de.innologic.iamservice.config;

import de.innologic.iamservice.api.error.ApiErrorWriter;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
public class SecurityErrorHandlers implements AuthenticationEntryPoint, AccessDeniedHandler {

    private final ApiErrorWriter apiErrorWriter;

    public SecurityErrorHandlers(ApiErrorWriter apiErrorWriter) {
        this.apiErrorWriter = apiErrorWriter;
    }

    @Override
    public void commence(HttpServletRequest request, HttpServletResponse response, AuthenticationException authException) throws IOException {
        apiErrorWriter.write(request, response, HttpStatus.UNAUTHORIZED, authException.getMessage());
    }

    @Override
    public void handle(HttpServletRequest request, HttpServletResponse response, AccessDeniedException accessDeniedException) throws IOException {
        apiErrorWriter.write(request, response, HttpStatus.FORBIDDEN, accessDeniedException.getMessage());
    }
}
