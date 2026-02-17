package de.innologic.iamservice.api.error;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.MediaType;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.time.Instant;

@Component
public class ApiErrorWriter {

    private final ObjectMapper objectMapper;

    public ApiErrorWriter(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    public void write(HttpServletRequest request, HttpServletResponse response, HttpStatus status, String message) throws IOException {
        response.setStatus(status.value());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);

        ApiErrorResponse body = new ApiErrorResponse(
                Instant.now(),
                status.value(),
                status.name(),
                message,
                request.getRequestURI()
        );
        objectMapper.writeValue(response.getOutputStream(), body);
    }
}
