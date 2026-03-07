package de.innologic.iamservice.api.error;

import jakarta.validation.ConstraintViolationException;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.core.MethodParameter;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authorization.AuthorizationDeniedException;
import org.springframework.security.authorization.AuthorizationResult;
import org.springframework.security.authorization.AuthorizationResult;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingRequestHeaderException;
import org.springframework.validation.BeanPropertyBindingResult;

import java.io.IOException;
import java.lang.reflect.Method;
import java.util.Collections;

import static org.assertj.core.api.Assertions.assertThat;

class GlobalExceptionHandlerTest {

    private GlobalExceptionHandler handler;
    private MockHttpServletRequest request;
    private MethodParameter methodParameter;

    @BeforeEach
    void setUp() throws NoSuchMethodException {
        handler = new GlobalExceptionHandler();
        request = new MockHttpServletRequest("GET", "/api/v1/test");
        Method sample = SampleController.class.getDeclaredMethod("handle", String.class);
        methodParameter = new MethodParameter(sample, 0);
    }

    @Test
    void illegalArgumentReturnsBadRequest() {
        ResponseEntity<ApiErrorResponse> response = handler.badRequest(new IllegalArgumentException("tenantId must not be blank"), request);

        assertError(response, HttpStatus.BAD_REQUEST, "tenantId must not be blank");
    }

    @Test
    void accessDeniedReturnsForbidden() {
        ResponseEntity<ApiErrorResponse> response = handler.forbidden(new AccessDeniedException("forbidden"), request);

        assertError(response, HttpStatus.FORBIDDEN, "forbidden");
    }

    @Test
    void authorizationDeniedReturnsForbidden() {
        AuthorizationResult unauthorized = () -> false;
        ResponseEntity<ApiErrorResponse> response = handler.forbidden(new AuthorizationDeniedException("denied", unauthorized), request);

        assertError(response, HttpStatus.FORBIDDEN, "denied");
    }

    @Test
    void httpMessageNotReadableReturnsBadRequest() {
        HttpMessageNotReadableException ex = new HttpMessageNotReadableException("unreadable", new IOException(), null);
        ResponseEntity<ApiErrorResponse> response = handler.unreadable(ex, request);

        assertError(response, HttpStatus.BAD_REQUEST, "unreadable");
    }

    @Test
    void missingRequestHeaderReturnsBadRequest() {
        MissingRequestHeaderException ex = new MissingRequestHeaderException("X-Tenant-Id", methodParameter);
        ResponseEntity<ApiErrorResponse> response = handler.missingHeader(ex, request);

        assertError(response, HttpStatus.BAD_REQUEST, "X-Tenant-Id");
    }

    @Test
    void methodArgumentNotValidReturnsBadRequest() {
        MethodArgumentNotValidException ex = new MethodArgumentNotValidException(
                methodParameter,
                new BeanPropertyBindingResult(new Object(), "target")
        );
        ResponseEntity<ApiErrorResponse> response = handler.validation(ex, request);

        assertError(response, HttpStatus.BAD_REQUEST, "Validation failed");
    }

    @Test
    void constraintViolationReturnsBadRequest() {
        ConstraintViolationException ex = new ConstraintViolationException("constraint set", Collections.emptySet());
        ResponseEntity<ApiErrorResponse> response = handler.constraintViolation(ex, request);

        assertError(response, HttpStatus.BAD_REQUEST, "constraint set");
    }

    @Test
    void genericExceptionReturnsInternalServerError() {
        RuntimeException ex = new RuntimeException("boom");
        ResponseEntity<ApiErrorResponse> response = handler.generic(ex, request);

        assertError(response, HttpStatus.INTERNAL_SERVER_ERROR, "boom");
    }

    private void assertError(ResponseEntity<ApiErrorResponse> response, HttpStatus expected, String expectedMessage) {
        assertThat(response.getStatusCode()).isEqualTo(expected);
        ApiErrorResponse body = response.getBody();
        assertThat(body).isNotNull();
        assertThat(body.status()).isEqualTo(expected.value());
        assertThat(body.error()).isEqualTo(expected.name());
        assertThat(body.message()).contains(expectedMessage);
        assertThat(body.path()).isEqualTo("/api/v1/test");
        assertThat(body.timestamp()).isNotNull();
    }

    @SuppressWarnings("unused")
    private static class SampleController {
        void handle(String value) {
        }
    }
}
