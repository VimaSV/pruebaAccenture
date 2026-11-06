package com.accenture.pruebaaccenture.franquicia.repository;

import com.accenture.pruebaaccenture.franquicia.domain.Franquicia;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;

public interface FranquiciaRepository extends ReactiveCrudRepository<Franquicia, Long> {
}
