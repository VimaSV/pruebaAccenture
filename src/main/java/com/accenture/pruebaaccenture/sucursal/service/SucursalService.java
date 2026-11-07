package com.accenture.pruebaaccenture.sucursal.service;

import com.accenture.pruebaaccenture.franquicia.repository.FranquiciaRepository;
import com.accenture.pruebaaccenture.shared.exception.FranquiciaNotFoundException;
import com.accenture.pruebaaccenture.shared.exception.SucursalNotFoundException;
import com.accenture.pruebaaccenture.sucursal.domain.Sucursal;
import com.accenture.pruebaaccenture.sucursal.dto.ActualizarNombreRequest;
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

    public Mono<SucursalResponse> renombrarSucursal(Long id, ActualizarNombreRequest request) {
        return sucursalRepository.actualizarNombre(id, request.nombre())
                .flatMap(filasAfectadas -> filasAfectadas > 0
                        ? sucursalRepository.findById(id)
                              .map(sucursal -> new SucursalResponse(
                                      sucursal.getId(), sucursal.getNombre(), sucursal.getIdFranquicia()))
                              .switchIfEmpty(Mono.error(new SucursalNotFoundException(id)))
                        : Mono.error(new SucursalNotFoundException(id)));
    }
}
