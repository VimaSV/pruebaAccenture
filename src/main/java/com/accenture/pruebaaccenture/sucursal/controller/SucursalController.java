package com.accenture.pruebaaccenture.sucursal.controller;

import com.accenture.pruebaaccenture.sucursal.dto.ActualizarNombreRequest;
import com.accenture.pruebaaccenture.sucursal.dto.CrearSucursalRequest;
import com.accenture.pruebaaccenture.sucursal.dto.SucursalResponse;
import com.accenture.pruebaaccenture.sucursal.service.SucursalService;
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
@RequestMapping("/api")
public class SucursalController {

    private final SucursalService sucursalService;

    public SucursalController(SucursalService sucursalService) {
        this.sucursalService = sucursalService;
    }

    @PostMapping("/franquicias/{idFranquicia}/sucursales")
    @ResponseStatus(HttpStatus.CREATED)
    public Mono<SucursalResponse> crear(
            @PathVariable Long idFranquicia,
            @Valid @RequestBody CrearSucursalRequest request) {
        return sucursalService.crearSucursal(idFranquicia, request);
    }

    @PatchMapping("/sucursales/{id}/nombre")
    public Mono<SucursalResponse> renombrar(
            @PathVariable Long id,
            @Valid @RequestBody ActualizarNombreRequest request) {
        return sucursalService.renombrarSucursal(id, request);
    }
}
