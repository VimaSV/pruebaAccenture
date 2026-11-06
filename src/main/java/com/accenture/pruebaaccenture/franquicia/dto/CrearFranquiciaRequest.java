package com.accenture.pruebaaccenture.franquicia.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CrearFranquiciaRequest(
        @NotBlank
        @Size(max = 150)
        String nombre
) {
}
