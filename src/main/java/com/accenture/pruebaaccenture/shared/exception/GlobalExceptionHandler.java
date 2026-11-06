package com.accenture.pruebaaccenture.shared.exception;

import com.accenture.pruebaaccenture.shared.error.ErrorResponse;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.bind.support.WebExchangeBindException;
import reactor.core.publisher.Mono;

@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);
    private static final String MENSAJE_CONFLICTO = "La operación genera un conflicto con los datos existentes";
    private static final String MENSAJE_INTERNO = "Ocurrió un error interno en el servidor";

    @ExceptionHandler({
            FranquiciaNotFoundException.class,
            SucursalNotFoundException.class,
            ProductoNotFoundException.class
    })
    public Mono<ResponseEntity<ErrorResponse>> manejarRecursoNoEncontrado(RuntimeException ex) {
        return Mono.just(construir(HttpStatus.NOT_FOUND, ex.getMessage()));
    }

    @ExceptionHandler(WebExchangeBindException.class)
    public Mono<ResponseEntity<ErrorResponse>> manejarValidacion(WebExchangeBindException ex) {
        String mensaje = ex.getFieldErrors().stream()
                .map(campo -> campo.getField() + ": " + campo.getDefaultMessage())
                .collect(Collectors.joining("; "));
        return Mono.just(construir(HttpStatus.BAD_REQUEST, mensaje));
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public Mono<ResponseEntity<ErrorResponse>> manejarConflictoDeDatos(DataIntegrityViolationException ex) {
        return Mono.just(construir(HttpStatus.CONFLICT, MENSAJE_CONFLICTO));
    }

    @ExceptionHandler(Exception.class)
    public Mono<ResponseEntity<ErrorResponse>> manejarErrorInesperado(Exception ex) {
        log.error("Error interno no controlado", ex);
        return Mono.just(construir(HttpStatus.INTERNAL_SERVER_ERROR, MENSAJE_INTERNO));
    }

    private ResponseEntity<ErrorResponse> construir(HttpStatus status, String mensaje) {
        return ResponseEntity.status(status).body(ErrorResponse.of(status, mensaje));
    }
}
