package com.accenture.pruebaaccenture.shared;

import static org.assertj.core.api.Assertions.assertThat;

import jakarta.validation.Validation;
import jakarta.validation.Validator;
import java.util.List;
import java.util.stream.Stream;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

class ValidacionActualizarNombreTest {

    private final Validator validator = Validation.buildDefaultValidatorFactory().getValidator();

    static Stream<Class<?>> dtosActualizarNombre() {
        return Stream.of(
                com.accenture.pruebaaccenture.franquicia.dto.ActualizarNombreRequest.class,
                com.accenture.pruebaaccenture.sucursal.dto.ActualizarNombreRequest.class,
                com.accenture.pruebaaccenture.producto.dto.ActualizarNombreRequest.class);
    }

    @ParameterizedTest
    @MethodSource("dtosActualizarNombre")
    void nombreValidoDeHasta150CaracteresPasaLaValidacion(Class<?> tipoDto) throws Exception {
        Object dto = crear(tipoDto, "Nombre Válido");
        assertThat(validator.validate(dto)).isEmpty();
    }

    @ParameterizedTest
    @MethodSource("dtosActualizarNombre")
    void mensajeDeHasta150CaracteresPasaLaValidacion(Class<?> tipoDto) throws Exception {
        Object dto = crear(tipoDto, "N".repeat(150));
        assertThat(validator.validate(dto)).isEmpty();
    }

    @ParameterizedTest
    @MethodSource({ "nombresInvalidos" })
    void nombreInvalidoNoPasaLaValidacion(String nombreInvalido) throws Exception {
        List<Class<?>> tipos = dtosActualizarNombre().toList();
        for (Class<?> tipoDto : tipos) {
            Object dto = crear(tipoDto, nombreInvalido);
            assertThat(validator.validate(dto)).isNotEmpty();
        }
    }

    static Stream<String> nombresInvalidos() {
        return Stream.of(
                null,
                "",
                "   ",
                "N".repeat(151));
    }

    private static Object crear(Class<?> tipoDto, String nombre) throws Exception {
        return tipoDto.getDeclaredConstructor(String.class).newInstance(nombre);
    }
}
