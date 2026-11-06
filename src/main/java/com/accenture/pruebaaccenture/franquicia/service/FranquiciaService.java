package com.accenture.pruebaaccenture.franquicia.service;

import com.accenture.pruebaaccenture.franquicia.domain.Franquicia;
import com.accenture.pruebaaccenture.franquicia.dto.CrearFranquiciaRequest;
import com.accenture.pruebaaccenture.franquicia.dto.FranquiciaResponse;
import com.accenture.pruebaaccenture.franquicia.repository.FranquiciaRepository;
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
}
