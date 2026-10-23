package com.agro_venta.products.interfaces.rest;

import com.agro_venta.products.domain.exception.BusinessException;
import com.agro_venta.products.domain.exception.InsufficientStockException;
import com.agro_venta.products.domain.exception.ResourceNotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.bind.support.WebExchangeBindException;
import reactor.core.publisher.Mono;

import java.time.Instant;
import java.util.Map;
import java.util.stream.Collectors;

@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    record ErrorResponse(long timestamp, int status, String error, String message,
                         Map<String, String> validationErrors) {}

    private static ErrorResponse of(int status, String error, String message) {
        return new ErrorResponse(Instant.now().toEpochMilli(), status, error, message, null);
    }

    @ExceptionHandler(ResourceNotFoundException.class)
    public Mono<ResponseEntity<ErrorResponse>> handleNotFound(ResourceNotFoundException ex) {
        return Mono.just(ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(of(404, "Not Found", ex.getMessage())));
    }

    @ExceptionHandler(BusinessException.class)
    public Mono<ResponseEntity<ErrorResponse>> handleBusiness(BusinessException ex) {
        return Mono.just(ResponseEntity.status(HttpStatus.UNPROCESSABLE_ENTITY)
                .body(of(422, "Unprocessable Entity", ex.getMessage())));
    }

    @ExceptionHandler(InsufficientStockException.class)
    public Mono<ResponseEntity<ErrorResponse>> handleStock(InsufficientStockException ex) {
        return Mono.just(ResponseEntity.status(HttpStatus.CONFLICT)
                .body(of(409, "Conflict", ex.getMessage())));
    }

    @ExceptionHandler(WebExchangeBindException.class)
    public Mono<ResponseEntity<ErrorResponse>> handleValidation(WebExchangeBindException ex) {
        Map<String, String> errors = ex.getFieldErrors().stream()
                .collect(Collectors.toMap(
                        fe -> fe.getField(),
                        fe -> fe.getDefaultMessage() != null ? fe.getDefaultMessage() : "Valor inválido",
                        (a, b) -> a));
        return Mono.just(ResponseEntity.badRequest().body(
                new ErrorResponse(Instant.now().toEpochMilli(), 400, "Bad Request",
                        "Error de validación", errors)));
    }

    @ExceptionHandler(Exception.class)
    public Mono<ResponseEntity<ErrorResponse>> handleUnexpected(Exception ex) {
        log.error("Unexpected error", ex);
        return Mono.just(ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(of(500, "Internal Server Error", "Error inesperado")));
    }
}
