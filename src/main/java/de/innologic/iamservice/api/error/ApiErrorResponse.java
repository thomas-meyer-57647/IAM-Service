package de.innologic.iamservice.api.error;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.Instant;

@Schema(description = "Standardized error response returned when requests fail")
public record ApiErrorResponse(
        @Schema(description = "UTC timestamp of the error", example = "2026-03-07T02:12:00.123Z") Instant timestamp,
        @Schema(description = "HTTP status code of the failure", example = "404") int status,
        @Schema(description = "Short HTTP status reason", example = "NOT_FOUND") String error,
        @Schema(description = "Detailed error message", example = "Subject not found") String message,
        @Schema(description = "Request path that caused the error", example = "/api/v1/access/subjects/USR-1/modules/user") String path
) {
}
