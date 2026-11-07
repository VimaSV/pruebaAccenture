package com.accenture.pruebaaccenture.franquicia.controller;

import com.accenture.pruebaaccenture.franquicia.dto.ActualizarNombreRequest;
import com.accenture.pruebaaccenture.franquicia.dto.CrearFranquiciaRequest;
import com.accenture.pruebaaccenture.franquicia.dto.FranquiciaResponse;
import com.accenture.pruebaaccenture.franquicia.service.FranquiciaService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
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

    @PatchMapping("/{id}/nombre")
    public Mono<FranquiciaResponse> renombrar(
            @PathVariable Long id,
            @Valid @RequestBody ActualizarNombreRequest request) {
        return franquiciaService.renombrarFranquicia(id, request);
    }
}
