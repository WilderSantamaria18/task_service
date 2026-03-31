package com.idat.pe.task_service.exception;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;

/**
 * Manejador global de excepciones para el servicio de tareas
 * Sprint 2: T-203 - Manejo estandarizado de errores
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    /**
     * Maneja excepciones genéricas de runtime
     */
    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<ErrorResponse> handleRuntimeException(
            RuntimeException ex,
            HttpServletRequest request) {
        
        // Determinar status según el mensaje de la excepción
        HttpStatus status = HttpStatus.INTERNAL_SERVER_ERROR;
        
        if (ex.getMessage() != null) {
            if (ex.getMessage().contains("no encontrada") || 
                ex.getMessage().contains("No encontrado")) {
                status = HttpStatus.NOT_FOUND;
            } else if (ex.getMessage().contains("No autorizado")) {
                status = HttpStatus.FORBIDDEN;
            }
        }
        
        ErrorResponse error = ErrorResponse.builder()
                .statusCode(status.value())
                .timestamp(LocalDateTime.now())
                .message(ex.getMessage())
                .description("Error procesando la solicitud")
                .path(request.getRequestURI())
                .build();
        
        return ResponseEntity.status(status).body(error);
    }

    /**
     * Maneja errores de validación del DTO (anotaciones @Valid)
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidationException(
            MethodArgumentNotValidException ex,
            HttpServletRequest request) {
        
        String message = ex.getBindingResult()
                .getFieldErrors()
                .stream()
                .map(e -> e.getField() + ": " + e.getDefaultMessage())
                .findFirst()
                .orElse("Error de validación");
        
        ErrorResponse error = ErrorResponse.builder()
                .statusCode(HttpStatus.BAD_REQUEST.value())
                .timestamp(LocalDateTime.now())
                .message("Validación fallida")
                .description(message)
                .path(request.getRequestURI())
                .build();
        
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
    }

    /**
     * Maneja cualquier otra excepción no capturada
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGenericException(
            Exception ex,
            HttpServletRequest request) {
        
        ErrorResponse error = ErrorResponse.builder()
                .statusCode(HttpStatus.INTERNAL_SERVER_ERROR.value())
                .timestamp(LocalDateTime.now())
                .message("Error interno del servidor")
                .description(ex.getMessage())
                .path(request.getRequestURI())
                .build();
        
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
    }
}
