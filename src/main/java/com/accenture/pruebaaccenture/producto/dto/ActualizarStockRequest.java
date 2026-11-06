package com.accenture.pruebaaccenture.producto.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;

public record ActualizarStockRequest(
        @NotNull
        @PositiveOrZero
        Integer stock
) {
}
