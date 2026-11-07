package com.accenture.pruebaaccenture.sucursal.repository;

import com.accenture.pruebaaccenture.sucursal.domain.Sucursal;
import org.springframework.data.r2dbc.repository.Modifying;
import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface SucursalRepository extends ReactiveCrudRepository<Sucursal, Long> {

    Flux<Sucursal> findByIdFranquicia(Long idFranquicia);

    @Modifying
    @Query("UPDATE sucursal SET nombre = :nombre WHERE id = :id")
    Mono<Long> actualizarNombre(Long id, String nombre);
}
