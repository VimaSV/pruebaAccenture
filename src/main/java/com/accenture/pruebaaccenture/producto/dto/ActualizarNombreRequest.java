package com.accenture.pruebaaccenture.producto.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record ActualizarNombreRequest(
        @NotBlank
        @Size(max = 150)
        String nombre
) {
}
