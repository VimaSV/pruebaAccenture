package com.accenture.pruebaaccenture.producto.repository;

import com.accenture.pruebaaccenture.producto.domain.Producto;
import java.util.List;
import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Flux;

public interface ProductoRepository extends ReactiveCrudRepository<Producto, Long> {

    Flux<Producto> findByIdSucursal(Long idSucursal);

    Flux<Producto> findByIdSucursalIn(List<Long> idsSucursal);

    @Query("""
            SELECT p.*
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
    Flux<Producto> findMaxStockPorSucursalDeFranquicia(Long idFranquicia);
}
