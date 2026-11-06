package com.accenture.pruebaaccenture.producto.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;

public record CrearProductoRequest(
        @NotBlank
        @Size(max = 150)
        String nombre,

        @NotNull
        @PositiveOrZero
        Integer stock
) {
}
