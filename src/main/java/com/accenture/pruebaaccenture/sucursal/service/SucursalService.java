package com.accenture.pruebaaccenture.sucursal.service;

import com.accenture.pruebaaccenture.franquicia.repository.FranquiciaRepository;
import com.accenture.pruebaaccenture.shared.exception.FranquiciaNotFoundException;
import com.accenture.pruebaaccenture.sucursal.domain.Sucursal;
import com.accenture.pruebaaccenture.sucursal.dto.CrearSucursalRequest;
import com.accenture.pruebaaccenture.sucursal.dto.SucursalResponse;
import com.accenture.pruebaaccenture.sucursal.repository.SucursalRepository;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@Service
public class SucursalService {

    private final FranquiciaRepository franquiciaRepository;
    private final SucursalRepository sucursalRepository;

    public SucursalService(FranquiciaRepository franquiciaRepository,
                           SucursalRepository sucursalRepository) {
        this.franquiciaRepository = franquiciaRepository;
        this.sucursalRepository = sucursalRepository;
    }

    public Mono<SucursalResponse> crearSucursal(Long idFranquicia, CrearSucursalRequest request) {
        return franquiciaRepository.existsById(idFranquicia)
                .filter(Boolean::booleanValue)
                .switchIfEmpty(Mono.error(new FranquiciaNotFoundException(idFranquicia)))
                .flatMap(existe -> sucursalRepository.save(new Sucursal(idFranquicia, request.nombre())))
                .map(sucursal -> new SucursalResponse(
                        sucursal.getId(), sucursal.getNombre(), sucursal.getIdFranquicia()));
    }
}
