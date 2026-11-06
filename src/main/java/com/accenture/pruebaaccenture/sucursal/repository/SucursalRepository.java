package com.accenture.pruebaaccenture.sucursal.repository;

import com.accenture.pruebaaccenture.sucursal.domain.Sucursal;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Flux;

public interface SucursalRepository extends ReactiveCrudRepository<Sucursal, Long> {

    Flux<Sucursal> findByIdFranquicia(Long idFranquicia);
}
