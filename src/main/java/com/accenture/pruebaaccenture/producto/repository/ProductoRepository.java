package com.accenture.pruebaaccenture.producto.repository;

import com.accenture.pruebaaccenture.producto.domain.Producto;
import org.springframework.data.r2dbc.repository.Modifying;
import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface ProductoRepository extends ReactiveCrudRepository<Producto, Long> {

    @Modifying
    @Query("UPDATE producto SET stock = :stock WHERE id = :id")
    Mono<Long> actualizarStock(Long id, Integer stock);

    @Query("""
            SELECT p.id, p.nombre, p.stock,
                   s.id AS id_sucursal, s.nombre AS nombre_sucursal
            FROM producto p
            JOIN sucursal s ON p.id_sucursal = s.id
            WHERE s.id_franquicia = :idFranquicia
              AND p.stock = (
                    SELECT MAX(p2.stock)
                    FROM producto p2
                    WHERE p2.id_sucursal = p.id_sucursal
                  )
              AND p.id = (
                    SELECT MIN(p3.id)
                    FROM producto p3
                    WHERE p3.id_sucursal = p.id_sucursal
                      AND p3.stock = p.stock
                  )
            ORDER BY p.id_sucursal ASC
            """)
    Flux<ProductoSucursal> findMaxStockConSucursalDeFranquicia(Long idFranquicia);

    @Query("""
            SELECT p.id, p.nombre, p.stock,
                   s.id AS id_sucursal, s.nombre AS nombre_sucursal
            FROM producto p
            JOIN sucursal s ON p.id_sucursal = s.id
            WHERE s.id_franquicia = :idFranquicia
            ORDER BY s.id ASC, p.id ASC
            """)
    Flux<ProductoSucursal> listarConSucursalPorFranquicia(Long idFranquicia);
}
