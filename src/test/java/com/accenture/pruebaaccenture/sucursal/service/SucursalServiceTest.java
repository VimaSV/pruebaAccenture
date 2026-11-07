package com.accenture.pruebaaccenture.sucursal.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.accenture.pruebaaccenture.franquicia.repository.FranquiciaRepository;
import com.accenture.pruebaaccenture.shared.exception.FranquiciaNotFoundException;
import com.accenture.pruebaaccenture.shared.exception.SucursalNotFoundException;
import com.accenture.pruebaaccenture.sucursal.domain.Sucursal;
import com.accenture.pruebaaccenture.sucursal.dto.ActualizarNombreRequest;
import com.accenture.pruebaaccenture.sucursal.dto.CrearSucursalRequest;
import com.accenture.pruebaaccenture.sucursal.dto.SucursalResponse;
import com.accenture.pruebaaccenture.sucursal.repository.SucursalRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

@ExtendWith(MockitoExtension.class)
class SucursalServiceTest {

    private static final Long ID_FRANQUICIA = 7L;

    @Mock
    private FranquiciaRepository franquiciaRepository;

    @Mock
    private SucursalRepository sucursalRepository;

    private SucursalService sucursalService;

    @BeforeEach
    void setUp() {
        sucursalService = new SucursalService(franquiciaRepository, sucursalRepository);
    }

    @Test
    void crearSucursalValidaFranquiciaYAsociaIdFranquicia() {
        when(franquiciaRepository.existsById(ID_FRANQUICIA)).thenReturn(Mono.just(true));
        when(sucursalRepository.save(any(Sucursal.class))).thenAnswer(invocacion -> {
            Sucursal guardada = invocacion.getArgument(0, Sucursal.class);
            guardada.setId(21L);
            return Mono.just(guardada);
        });

        StepVerifier.create(sucursalService.crearSucursal(ID_FRANQUICIA, new CrearSucursalRequest("Sucursal Norte")))
                .assertNext(respuesta -> {
                    assertThat(respuesta.id()).isEqualTo(21L);
                    assertThat(respuesta.nombre()).isEqualTo("Sucursal Norte");
                    assertThat(respuesta.idFranquicia()).isEqualTo(ID_FRANQUICIA);
                })
                .verifyComplete();

        verify(sucursalRepository).save(any(Sucursal.class));
    }

    @Test
    void crearSucursalConFranquiciaInexistenteLanzaExcepcion() {
        when(franquiciaRepository.existsById(ID_FRANQUICIA)).thenReturn(Mono.just(false));

        StepVerifier.create(sucursalService.crearSucursal(ID_FRANQUICIA, new CrearSucursalRequest("Sucursal Norte")))
                .expectError(FranquiciaNotFoundException.class)
                .verify();

        verify(sucursalRepository, never()).save(any(Sucursal.class));
    }

    @Test
    void crearSucursalConPublisherVacioDeExistenciaLanzaExcepcion() {
        when(franquiciaRepository.existsById(ID_FRANQUICIA)).thenReturn(Mono.empty());

        StepVerifier.create(sucursalService.crearSucursal(ID_FRANQUICIA, new CrearSucursalRequest("Sucursal Norte")))
                .expectError(FranquiciaNotFoundException.class)
                .verify();

        verify(sucursalRepository, never()).save(any(Sucursal.class));
    }

    @Test
    void renombrarSucursalActualizaYRetornaNombreNuevo() {
        when(sucursalRepository.actualizarNombre(21L, "Sucursal Renombrada")).thenReturn(Mono.just(1L));
        when(sucursalRepository.findById(21L)).thenReturn(Mono.just(sucursal(21L, "Sucursal Renombrada")));

        StepVerifier.create(sucursalService.renombrarSucursal(21L, new ActualizarNombreRequest("Sucursal Renombrada")))
                .assertNext(respuesta -> {
                    assertThat(respuesta.id()).isEqualTo(21L);
                    assertThat(respuesta.nombre()).isEqualTo("Sucursal Renombrada");
                    assertThat(respuesta.idFranquicia()).isEqualTo(ID_FRANQUICIA);
                })
                .verifyComplete();
    }

    @Test
    void renombrarSucursalConIdInexistenteLanzaExcepcion() {
        when(sucursalRepository.actualizarNombre(999L, "Sucursal Renombrada")).thenReturn(Mono.just(0L));

        StepVerifier.create(sucursalService.renombrarSucursal(999L, new ActualizarNombreRequest("Sucursal Renombrada")))
                .expectError(SucursalNotFoundException.class)
                .verify();

        verify(sucursalRepository, never()).findById(999L);
    }

    private Sucursal sucursal(Long id, String nombre) {
        Sucursal sucursal = new Sucursal(ID_FRANQUICIA, nombre);
        sucursal.setId(id);
        return sucursal;
    }
}
