package com.accenture.pruebaaccenture.franquicia.service;

import com.accenture.pruebaaccenture.franquicia.domain.Franquicia;
import com.accenture.pruebaaccenture.franquicia.dto.ActualizarNombreRequest;
import com.accenture.pruebaaccenture.franquicia.dto.CrearFranquiciaRequest;
import com.accenture.pruebaaccenture.franquicia.dto.FranquiciaResponse;
import com.accenture.pruebaaccenture.franquicia.repository.FranquiciaRepository;
import com.accenture.pruebaaccenture.shared.exception.FranquiciaNotFoundException;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@Service
public class FranquiciaService {

    private final FranquiciaRepository franquiciaRepository;

    public FranquiciaService(FranquiciaRepository franquiciaRepository) {
        this.franquiciaRepository = franquiciaRepository;
    }

    public Mono<FranquiciaResponse> crearFranquicia(CrearFranquiciaRequest request) {
        return franquiciaRepository.save(new Franquicia(request.nombre()))
                .map(franquicia -> new FranquiciaResponse(franquicia.getId(), franquicia.getNombre()));
    }

    public Mono<FranquiciaResponse> renombrarFranquicia(Long id, ActualizarNombreRequest request) {
        return franquiciaRepository.actualizarNombre(id, request.nombre())
                .flatMap(filasAfectadas -> filasAfectadas > 0
                        ? franquiciaRepository.findById(id)
                              .map(franquicia -> new FranquiciaResponse(
                                      franquicia.getId(), franquicia.getNombre()))
                              .switchIfEmpty(Mono.error(new FranquiciaNotFoundException(id)))
                        : Mono.error(new FranquiciaNotFoundException(id)));
    }
}
