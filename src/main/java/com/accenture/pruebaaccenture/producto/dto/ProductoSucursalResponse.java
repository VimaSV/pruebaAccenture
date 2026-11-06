package com.accenture.pruebaaccenture.producto.dto;

public record ProductoSucursalResponse(
        Long id,
        String nombre,
        Integer stock,
        Long idSucursal,
        String nombreSucursal
) {
}
