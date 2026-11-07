package com.accenture.pruebaaccenture.franquicia.repository;

import com.accenture.pruebaaccenture.franquicia.domain.Franquicia;
import org.springframework.data.r2dbc.repository.Modifying;
import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Mono;

public interface FranquiciaRepository extends ReactiveCrudRepository<Franquicia, Long> {

    @Modifying
    @Query("UPDATE franquicia SET nombre = :nombre WHERE id = :id")
    Mono<Long> actualizarNombre(Long id, String nombre);
}
