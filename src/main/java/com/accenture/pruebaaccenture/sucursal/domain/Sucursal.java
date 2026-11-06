package com.accenture.pruebaaccenture.sucursal.domain;

import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

@Table("sucursal")
public class Sucursal {

    private Long id;
    private String nombre;
    private Long idFranquicia;

    protected Sucursal() {
    }

    public Sucursal(Long idFranquicia, String nombre) {
        this.idFranquicia = idFranquicia;
        this.nombre = nombre;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public Long getIdFranquicia() {
        return idFranquicia;
    }

    public void setIdFranquicia(Long idFranquicia) {
        this.idFranquicia = idFranquicia;
    }
}
