package auth.service.xflow_auth_service.utils.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.LockedException;
import org.springframework.security.authorization.AuthorizationDeniedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import jakarta.servlet.http.HttpServletRequest;
import auth.service.xflow_auth_service.dto.ErrorResponse;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<ErrorResponse> handleBadCredentials(BadCredentialsException ex) {
        String message = (ex.getMessage() != null && !ex.getMessage().isEmpty())
                        ? ex.getMessage() : "invalid-credentials";
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
            .body(new ErrorResponse(message, System.currentTimeMillis(), 401));
    }

    @ExceptionHandler(AuthorizationDeniedException.class)
    public ResponseEntity<ErrorResponse> handleAuthorizationDeniedException(AuthorizationDeniedException ex, HttpServletRequest request) {
        HttpStatus status = (request.getUserPrincipal() == null) ? HttpStatus.UNAUTHORIZED : HttpStatus.FORBIDDEN;
        String message = (status == HttpStatus.UNAUTHORIZED) ? "missing-or-invalid-token" : "access-denied";
        return ResponseEntity.status(status)
            .body(new ErrorResponse(message, System.currentTimeMillis(), status.value()));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidationExceptions(MethodArgumentNotValidException ex) {
        String message = (ex.getMessage() != null && !ex.getMessage().isEmpty())
                        ? ex.getMessage() : "bad-request";
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
            .body(new ErrorResponse(message, System.currentTimeMillis(), 400));
    }

    @ExceptionHandler(LockedException.class)
    public ResponseEntity<ErrorResponse> handleLockedException(LockedException ex) {
        String message = (ex.getMessage() != null && !ex.getMessage().isEmpty())
                        ? ex.getMessage() : "too-many-attempts";
        return ResponseEntity.status(HttpStatus.LOCKED)
            .body(new ErrorResponse(message, System.currentTimeMillis(), 423));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGlobalException(Exception ex) {
        String message = (ex.getMessage() != null && !ex.getMessage().isEmpty())
                        ? ex.getMessage() : "internal-server-error";
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
            .body(new ErrorResponse(message, System.currentTimeMillis(), 500));
    }

}
