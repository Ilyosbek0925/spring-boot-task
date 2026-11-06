package epam.springboottask.exception;

import epam.springboottask.dto.ApiErrorResponse;
import java.util.LinkedHashMap;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.ErrorResponseException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.resource.NoResourceFoundException;

@Slf4j
@RestControllerAdvice
public class RestExceptionHandler {

    private static final String BAD_REQUEST_CODE = "BAD_REQUEST";
    private static final String METHOD_NOT_ALLOWED_CODE = "METHOD_NOT_ALLOWED";
    private static final String METHOD_NOT_ALLOWED_MESSAGE = "HTTP method is not supported for this endpoint";
    private static final String REQUEST_FAILED_CODE = "REQUEST_FAILED";
    private static final String REQUEST_FAILED_MESSAGE = "Request could not be completed";
    private static final String INVALID_FIELD_MESSAGE = "Invalid value";

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ApiErrorResponse> handleNotFound(ResourceNotFoundException exception) {
        log.info("Requested resource not found: {}", exception.getMessage());
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ApiErrorResponse.notFound());
    }

    @ExceptionHandler(DuplicateResourceException.class)
    public ResponseEntity<ApiErrorResponse> handleDuplicate(DuplicateResourceException exception) {
        log.info("Duplicate resource request: {}", exception.getMessage());
        return ResponseEntity.status(HttpStatus.CONFLICT).body(ApiErrorResponse.conflict());
    }

    @ExceptionHandler(NoResourceFoundException.class)
    public ResponseEntity<ApiErrorResponse> handleEndpointNotFound(NoResourceFoundException exception) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ApiErrorResponse.notFound());
    }

    @ExceptionHandler(ErrorResponseException.class)
    public ResponseEntity<ApiErrorResponse> handleFrameworkError(ErrorResponseException exception) {
        HttpStatus status = HttpStatus.valueOf(exception.getStatusCode().value());
        ApiErrorResponse response = switch (status) {
            case NOT_FOUND -> ApiErrorResponse.notFound();
            case CONFLICT -> ApiErrorResponse.conflict();
            case BAD_REQUEST -> ApiErrorResponse.badRequest(Map.of());
            default -> ApiErrorResponse.of(status, REQUEST_FAILED_CODE, REQUEST_FAILED_MESSAGE);
        };
        return ResponseEntity.status(status).body(response);
    }

    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<ApiErrorResponse> handleBadCredentials(BadCredentialsException exception) {
        log.info("Authentication failed");
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(ApiErrorResponse.invalidCredentials());
    }

    @ExceptionHandler(AuthenticationException.class)
    public ResponseEntity<ApiErrorResponse> handleAuthentication(AuthenticationException exception) {
        log.info("Authentication failed: {}", exception.getClass().getSimpleName());
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(ApiErrorResponse.unauthorized());
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ApiErrorResponse> handleAccessDenied(AccessDeniedException exception) {
        log.info("Access denied: {}", exception.getMessage());
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(ApiErrorResponse.forbidden());
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiErrorResponse> handleValidation(MethodArgumentNotValidException exception) {
        Map<String, String> fieldErrors = new LinkedHashMap<>();
        exception.getBindingResult().getFieldErrors()
                .forEach(error -> fieldErrors.putIfAbsent(
                        error.getField(),
                        error.getDefaultMessage() == null ? INVALID_FIELD_MESSAGE : error.getDefaultMessage()));
        log.debug("Request validation failed for fields: {}", fieldErrors.keySet());
        return ResponseEntity.badRequest().body(ApiErrorResponse.badRequest(fieldErrors));
    }

    @ExceptionHandler({HttpMessageNotReadableException.class, MethodArgumentTypeMismatchException.class})
    public ResponseEntity<ApiErrorResponse> handleMalformedRequest(Exception exception) {
        return ResponseEntity.badRequest()
                .body(ApiErrorResponse.of(HttpStatus.BAD_REQUEST, BAD_REQUEST_CODE, "Request is malformed"));
    }

    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public ResponseEntity<ApiErrorResponse> handleMethodNotAllowed(
            HttpRequestMethodNotSupportedException exception) {
        return ResponseEntity.status(HttpStatus.METHOD_NOT_ALLOWED)
                .body(ApiErrorResponse.of(
                        HttpStatus.METHOD_NOT_ALLOWED, METHOD_NOT_ALLOWED_CODE, METHOD_NOT_ALLOWED_MESSAGE));
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ApiErrorResponse> handleDataConflict(DataIntegrityViolationException exception) {
        log.warn("Database constraint rejected the request", exception);
        return ResponseEntity.status(HttpStatus.CONFLICT).body(ApiErrorResponse.conflict());
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiErrorResponse> handleUnexpected(Exception exception) {
        log.error("Unhandled request failure", exception);
        return ResponseEntity.internalServerError().body(ApiErrorResponse.internalError());
    }
}
