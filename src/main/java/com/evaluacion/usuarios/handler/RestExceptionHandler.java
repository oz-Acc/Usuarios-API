package com.evaluacion.usuarios.handler;

import com.evaluacion.usuarios.exception.ResourceNotFoundException;
import com.evaluacion.usuarios.dto.ApiError;
import org.springframework.http.converter.HttpMessageNotReadableException;

import java.util.stream.Collectors;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;

@org.springframework.web.bind.annotation.RestControllerAdvice
public class RestExceptionHandler {

    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(RestExceptionHandler.class);

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ApiError> handleResourceNotFoundException(ResourceNotFoundException ex) {
        ApiError error = new ApiError(ex.getMessage());
        return new ResponseEntity<>(error, HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiError> handleValidationErrors(MethodArgumentNotValidException ex) {

        String mensajeDeErrorDetallado = ex.getBindingResult().getAllErrors().stream()
            .map(error -> {
                String fieldName = (error instanceof FieldError) ? ((FieldError) error).getField()
                    : error.getObjectName();
                if ("contrasena".equals(fieldName)) {
                fieldName = "contraseña";
                }
                String defaultMessage = error.getDefaultMessage();
                return String.format("El campo '%s' es inválido. Detalle: %s", fieldName, defaultMessage);
            })
            .collect(Collectors.joining("; "));

        ApiError error = new ApiError(mensajeDeErrorDetallado);

        return new ResponseEntity<>(error, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(com.evaluacion.usuarios.exception.EmailAlreadyRegisteredException.class)
    public ResponseEntity<ApiError> handleEmailAlreadyRegistered(com.evaluacion.usuarios.exception.EmailAlreadyRegisteredException ex) {
        ApiError error = new ApiError(ex.getMessage());
        return new ResponseEntity<>(error, HttpStatus.CONFLICT);
    }

    @ExceptionHandler(com.evaluacion.usuarios.exception.PasswordInvalidException.class)
    public ResponseEntity<ApiError> handlePasswordInvalid(com.evaluacion.usuarios.exception.PasswordInvalidException ex) {
        ApiError error = new ApiError(ex.getMessage());
        return new ResponseEntity<>(error, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ApiError> handleMalformedJson(HttpMessageNotReadableException ex) {
        String msg = "JSON mal formado o contenido inválido";
        ApiError error = new ApiError(msg);
        return new ResponseEntity<>(error, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiError> handleGeneric(Exception ex) {
        log.error("Unhandled exception in request handling", ex);
        ApiError error = new ApiError("Ocurrió un error interno");
        return new ResponseEntity<>(error, HttpStatus.INTERNAL_SERVER_ERROR);
    }
}