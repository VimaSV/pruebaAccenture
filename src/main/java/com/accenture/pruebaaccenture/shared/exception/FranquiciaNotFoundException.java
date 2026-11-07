package com.accenture.pruebaaccenture.shared.exception;

public class FranquiciaNotFoundException extends RuntimeException {

    public FranquiciaNotFoundException(Long id) {
        super("No existe la franquicia con id: " + id);
    }
}
