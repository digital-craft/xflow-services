package map.service.xflow_map_service.config;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
// import org.springframework.security.access.AccessDeniedException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.bind.support.WebExchangeBindException;
import map.service.xflow_map_service.utils.exceptions.GraphNotConnectedException;
import map.service.xflow_map_service.utils.exceptions.MapDomainException;
import map.service.xflow_map_service.utils.exceptions.ResourceNotFoundException;
import map.service.xflow_map_service.dto.ErrorResponse;

import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidationErrors(MethodArgumentNotValidException ex) {
        Map<String, String> errors = new HashMap<>();
        ex.getBindingResult().getAllErrors().forEach(error -> {
            String fieldName = ((FieldError) error).getField();
            String errorMessage = error.getDefaultMessage();
            errors.put(fieldName, errorMessage);
        });

        ErrorResponse response = new ErrorResponse(
                "BAD_REQUEST",
                "Échec de validation des données d'entrée",
                HttpStatus.BAD_REQUEST.value(),
                errors
        );
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }

    @ExceptionHandler(WebExchangeBindException.class)
    public ResponseEntity<ErrorResponse> handleWebExchangeBindException(WebExchangeBindException ex) {
        Map<String, String> errors = new HashMap<>();
        ex.getBindingResult().getAllErrors().forEach(error -> {
            String fieldName = ((FieldError) error).getField();
            String errorMessage = error.getDefaultMessage();
            errors.put(fieldName, errorMessage);
        });

        ErrorResponse response = new ErrorResponse(
                "BAD_REQUEST",
                "Échec de validation des données d'entrée",
                HttpStatus.BAD_REQUEST.value(),
                errors
        );
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }

    // @ExceptionHandler(AccessDeniedException.class)
    // public ResponseEntity<ErrorResponse> handleAccessDenied(AccessDeniedException ex) {
    //     ErrorResponse response = new ErrorResponse(
    //             "FORBIDDEN",
    //             "Rôle insuffisant ou accès refusé à cette ressource cartographique.",
    //             HttpStatus.FORBIDDEN.value()
    //     );
    //     return ResponseEntity.status(HttpStatus.FORBIDDEN).body(response);
    // }

    @ExceptionHandler(GraphNotConnectedException.class)
    public ResponseEntity<ErrorResponse> handleGraphNotConnected(GraphNotConnectedException ex) {
        ErrorResponse response = new ErrorResponse(
                "GRAPH_NOT_CONNECTED",
                ex.getMessage(),
                HttpStatus.CONFLICT.value()
        );
        return ResponseEntity.status(HttpStatus.CONFLICT).body(response);
    }

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleNotFound(ResourceNotFoundException ex) {
        ErrorResponse response = new ErrorResponse(
                "NOT_FOUND",
                ex.getMessage(),
                HttpStatus.NOT_FOUND.value()
        );
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
    }

    @ExceptionHandler(MapDomainException.class)
    public ResponseEntity<ErrorResponse> handleDomainException(MapDomainException ex) {
        ErrorResponse response = new ErrorResponse(
                "BUSINESS_ERROR",
                ex.getMessage(),
                HttpStatus.UNPROCESSABLE_ENTITY.value()
        );
        return ResponseEntity.status(HttpStatus.UNPROCESSABLE_ENTITY).body(response);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGenericException(Exception ex) {
        ErrorResponse response = new ErrorResponse(
                "INTERNAL_SERVER_ERROR",
                "Une erreur inattendue est survenue.",
                HttpStatus.INTERNAL_SERVER_ERROR.value()
        );
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
    }
}