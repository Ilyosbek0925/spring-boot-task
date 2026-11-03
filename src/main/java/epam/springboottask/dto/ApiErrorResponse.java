package epam.springboottask.dto;

import java.util.Map;
import org.springframework.http.HttpStatus;

public record ApiErrorResponse(int status, String code, String message, Map<String, String> fieldErrors) {

    private static final String BAD_REQUEST_CODE = "BAD_REQUEST";
    private static final String UNAUTHORIZED_CODE = "UNAUTHORIZED";
    private static final String FORBIDDEN_CODE = "FORBIDDEN";
    private static final String NOT_FOUND_CODE = "NOT_FOUND";
    private static final String CONFLICT_CODE = "CONFLICT";
    private static final String INTERNAL_ERROR_CODE = "INTERNAL_SERVER_ERROR";

    public ApiErrorResponse {
        fieldErrors = fieldErrors == null ? Map.of() : Map.copyOf(fieldErrors);
    }

    public static ApiErrorResponse badRequest(Map<String, String> fieldErrors) {
        return new ApiErrorResponse(
                HttpStatus.BAD_REQUEST.value(), BAD_REQUEST_CODE, "Request validation failed", fieldErrors);
    }

    public static ApiErrorResponse unauthorized() {
        return new ApiErrorResponse(
                HttpStatus.UNAUTHORIZED.value(), UNAUTHORIZED_CODE, "Authentication is required", Map.of());
    }

    public static ApiErrorResponse invalidCredentials() {
        return new ApiErrorResponse(
                HttpStatus.UNAUTHORIZED.value(), UNAUTHORIZED_CODE, "Invalid username or password", Map.of());
    }

    public static ApiErrorResponse forbidden() {
        return new ApiErrorResponse(
                HttpStatus.FORBIDDEN.value(), FORBIDDEN_CODE, "Access is denied", Map.of());
    }

    public static ApiErrorResponse notFound() {
        return new ApiErrorResponse(
                HttpStatus.NOT_FOUND.value(), NOT_FOUND_CODE, "Requested resource was not found", Map.of());
    }

    public static ApiErrorResponse conflict() {
        return new ApiErrorResponse(
                HttpStatus.CONFLICT.value(), CONFLICT_CODE, "Request conflicts with existing data", Map.of());
    }

    public static ApiErrorResponse internalError() {
        return new ApiErrorResponse(
                HttpStatus.INTERNAL_SERVER_ERROR.value(), INTERNAL_ERROR_CODE, "An unexpected error occurred", Map.of());
    }

    public static ApiErrorResponse of(HttpStatus status, String code, String message) {
        return new ApiErrorResponse(status.value(), code, message, Map.of());
    }
}
