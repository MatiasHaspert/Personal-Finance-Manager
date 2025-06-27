package com.example.personal_finance_manager.Exceptions;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;


import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

//  Mejora la claridad de las respuestas HTTP,
//  Evita duplicación de lógica y centraliza el control de excepciones.
//  Esta clase captura todas las excepciones en tiempo de ejecucion
@RestControllerAdvice
public class GlobalExceptionHandler extends ResponseEntityExceptionHandler {

    @ExceptionHandler(NotFoundException.class)
    public ResponseEntity<MensajeError> notFoundException(NotFoundException excepcion){
        MensajeError mensaje = new MensajeError(LocalDateTime.now(), HttpStatus.NOT_FOUND, excepcion.getMessage());
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(mensaje);
    }

    @ExceptionHandler(BadRequestException.class)
    public ResponseEntity<MensajeError> badRequestException(BadRequestException excepcion){
        MensajeError mensaje = new MensajeError(LocalDateTime.now(), HttpStatus.BAD_REQUEST, excepcion.getMessage());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(mensaje);
    }

    @Override
    protected ResponseEntity<Object> handleMethodArgumentNotValid(MethodArgumentNotValidException ex, HttpHeaders headers, HttpStatusCode status, WebRequest request) {
        Map<String, String> errores = new HashMap<>();
        ex.getBindingResult().getFieldErrors().forEach(error -> {
            errores.put(error.getField(), error.getDefaultMessage());
        });

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errores);
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<Map<String, String>> handleConstraintViolation(ConstraintViolationException ex) {
        Map<String, String> errors = new HashMap<>();
        Set<ConstraintViolation<?>> violations = ex.getConstraintViolations();
        for (ConstraintViolation<?> violation : violations) {
            String fieldName = "Message";
            String errorMessage = violation.getMessage();
            errors.put(fieldName, errorMessage);
        }
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errors);
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<String> handleTypeMismatch(MethodArgumentTypeMismatchException ex) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Formato de fecha inválido. Use yyyy/MM/dd");
    }

    @ExceptionHandler(org.springframework.security.access.AccessDeniedException.class)
    public ResponseEntity<MensajeError> handleAccessDeniedException(AccessDeniedException ex) {
        MensajeError mensaje = new MensajeError(
                LocalDateTime.now(),
                HttpStatus.FORBIDDEN,
                ex.getMessage()
        );
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(mensaje);
    }

}

