package com.accenture.pruebaaccenture.shared.exception;

public class SucursalNotFoundException extends RuntimeException {

    public SucursalNotFoundException(Long id) {
        super("No existe la sucursal con id: " + id);
    }
}
