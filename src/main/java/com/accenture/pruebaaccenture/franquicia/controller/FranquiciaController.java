package com.accenture.pruebaaccenture.franquicia.controller;

import com.accenture.pruebaaccenture.franquicia.dto.CrearFranquiciaRequest;
import com.accenture.pruebaaccenture.franquicia.dto.FranquiciaResponse;
import com.accenture.pruebaaccenture.franquicia.service.FranquiciaService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/api/franquicias")
public class FranquiciaController {

    private final FranquiciaService franquiciaService;

    public FranquiciaController(FranquiciaService franquiciaService) {
        this.franquiciaService = franquiciaService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Mono<FranquiciaResponse> crear(@Valid @RequestBody CrearFranquiciaRequest request) {
        return franquiciaService.crearFranquicia(request);
    }
}
