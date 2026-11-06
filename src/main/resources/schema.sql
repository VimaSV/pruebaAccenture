-- Orden de creación: padre antes que hijo (franquicia -> sucursal -> producto).
-- Sin ON DELETE CASCADE: borrar un padre con hijos debe fallar por la FK.
CREATE TABLE IF NOT EXISTS franquicia (
    id     BIGINT AUTO_INCREMENT PRIMARY KEY,
    nombre VARCHAR(150) NOT NULL
);

CREATE TABLE IF NOT EXISTS sucursal (
    id           BIGINT AUTO_INCREMENT PRIMARY KEY,
    nombre       VARCHAR(150) NOT NULL,
    id_franquicia BIGINT NOT NULL,
    KEY idx_sucursal_franquicia (id_franquicia),
    CONSTRAINT fk_sucursal_franquicia
        FOREIGN KEY (id_franquicia) REFERENCES franquicia (id)
);

CREATE TABLE IF NOT EXISTS producto (
    id         BIGINT AUTO_INCREMENT PRIMARY KEY,
    nombre     VARCHAR(150) NOT NULL,
    stock      INT NOT NULL,
    id_sucursal BIGINT NOT NULL,
    KEY idx_producto_sucursal (id_sucursal),
    CONSTRAINT fk_producto_sucursal
        FOREIGN KEY (id_sucursal) REFERENCES sucursal (id),
    CONSTRAINT chk_producto_stock_no_negativo CHECK (stock >= 0)
);
