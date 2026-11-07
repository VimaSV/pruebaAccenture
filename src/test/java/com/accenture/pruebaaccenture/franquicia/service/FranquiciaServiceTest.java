package com.accenture.pruebaaccenture.franquicia.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.accenture.pruebaaccenture.franquicia.domain.Franquicia;
import com.accenture.pruebaaccenture.franquicia.dto.ActualizarNombreRequest;
import com.accenture.pruebaaccenture.franquicia.dto.CrearFranquiciaRequest;
import com.accenture.pruebaaccenture.franquicia.dto.FranquiciaResponse;
import com.accenture.pruebaaccenture.franquicia.repository.FranquiciaRepository;
import com.accenture.pruebaaccenture.shared.exception.FranquiciaNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

@ExtendWith(MockitoExtension.class)
class FranquiciaServiceTest {

    @Mock
    private FranquiciaRepository franquiciaRepository;

    private FranquiciaService franquiciaService;

    @BeforeEach
    void setUp() {
        franquiciaService = new FranquiciaService(franquiciaRepository);
    }

    @Test
    void crearFranquiciaRetornaRespuestaConIdGenerado() {
        CrearFranquiciaRequest request = new CrearFranquiciaRequest("Franquicia Centro");

        when(franquiciaRepository.save(any(Franquicia.class))).thenAnswer(invocacion -> {
            Franquicia guardada = invocacion.getArgument(0, Franquicia.class);
            guardada.setId(1L);
            return Mono.just(guardada);
        });

        StepVerifier.create(franquiciaService.crearFranquicia(request))
                .assertNext(respuesta -> {
                    assertThat(respuesta.id()).isEqualTo(1L);
                    assertThat(respuesta.nombre()).isEqualTo("Franquicia Centro");
                })
                .verifyComplete();

        verify(franquiciaRepository).save(any(Franquicia.class));
    }

    @Test
    void crearFranquiciaPropagaErrorDePersistencia() {
        when(franquiciaRepository.save(any(Franquicia.class)))
                .thenReturn(Mono.error(new IllegalStateException("error de base de datos")));

        StepVerifier.create(franquiciaService.crearFranquicia(new CrearFranquiciaRequest("Franquicia Latina")))
                .expectError(IllegalStateException.class)
                .verify();
    }

    @Test
    void renombrarFranquiciaActualizaYRetornaNombreNuevo() {
        when(franquiciaRepository.actualizarNombre(5L, "Nombre Nuevo")).thenReturn(Mono.just(1L));
        when(franquiciaRepository.findById(5L)).thenReturn(Mono.just(franquicia(5L, "Nombre Nuevo")));

        StepVerifier.create(franquiciaService.renombrarFranquicia(5L, new ActualizarNombreRequest("Nombre Nuevo")))
                .assertNext(respuesta -> {
                    assertThat(respuesta.id()).isEqualTo(5L);
                    assertThat(respuesta.nombre()).isEqualTo("Nombre Nuevo");
                })
                .verifyComplete();
    }

    @Test
    void renombrarFranquiciaConIdInexistenteLanzaExcepcion() {
        when(franquiciaRepository.actualizarNombre(999L, "Nombre Nuevo")).thenReturn(Mono.just(0L));

        StepVerifier.create(franquiciaService.renombrarFranquicia(999L, new ActualizarNombreRequest("Nombre Nuevo")))
                .expectError(FranquiciaNotFoundException.class)
                .verify();

        verify(franquiciaRepository, never()).findById(999L);
    }

    @Test
    void renombrarFranquiciaConZeroFilasYFindIdVacioLanzaExcepcion() {
        when(franquiciaRepository.actualizarNombre(7L, "Nombre Nuevo")).thenReturn(Mono.just(1L));
        when(franquiciaRepository.findById(7L)).thenReturn(Mono.empty());

        StepVerifier.create(franquiciaService.renombrarFranquicia(7L, new ActualizarNombreRequest("Nombre Nuevo")))
                .expectError(FranquiciaNotFoundException.class)
                .verify();
    }

    private Franquicia franquicia(Long id, String nombre) {
        Franquicia franquicia = new Franquicia(nombre);
        franquicia.setId(id);
        return franquicia;
    }
}
