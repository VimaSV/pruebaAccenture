package com.accenture.pruebaaccenture.producto.service;

import com.accenture.pruebaaccenture.franquicia.repository.FranquiciaRepository;
import com.accenture.pruebaaccenture.producto.domain.Producto;
import com.accenture.pruebaaccenture.producto.dto.ActualizarStockRequest;
import com.accenture.pruebaaccenture.producto.dto.CrearProductoRequest;
import com.accenture.pruebaaccenture.producto.dto.ProductoResponse;
import com.accenture.pruebaaccenture.producto.dto.ProductoSucursalResponse;
import com.accenture.pruebaaccenture.producto.repository.ProductoRepository;
import com.accenture.pruebaaccenture.producto.repository.ProductoSucursal;
import com.accenture.pruebaaccenture.shared.exception.FranquiciaNotFoundException;
import com.accenture.pruebaaccenture.shared.exception.ProductoNotFoundException;
import com.accenture.pruebaaccenture.shared.exception.SucursalNotFoundException;
import com.accenture.pruebaaccenture.sucursal.repository.SucursalRepository;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Service
public class ProductoService {

    private final FranquiciaRepository franquiciaRepository;
    private final SucursalRepository sucursalRepository;
    private final ProductoRepository productoRepository;

    public ProductoService(FranquiciaRepository franquiciaRepository,
                           SucursalRepository sucursalRepository,
                           ProductoRepository productoRepository) {
        this.franquiciaRepository = franquiciaRepository;
        this.sucursalRepository = sucursalRepository;
        this.productoRepository = productoRepository;
    }

    public Mono<ProductoResponse> agregarProducto(Long idSucursal, CrearProductoRequest request) {
        return sucursalRepository.existsById(idSucursal)
                .filter(Boolean::booleanValue)
                .switchIfEmpty(Mono.error(new SucursalNotFoundException(idSucursal)))
                .flatMap(existe -> productoRepository.save(
                        new Producto(idSucursal, request.nombre(), request.stock())))
                .map(producto -> new ProductoResponse(
                        producto.getId(), producto.getNombre(), producto.getStock()));
    }

    public Mono<Void> eliminarProducto(Long id) {
        return productoRepository.existsById(id)
                .filter(Boolean::booleanValue)
                .switchIfEmpty(Mono.error(new ProductoNotFoundException(id)))
                .flatMap(existe -> productoRepository.deleteById(id))
                .then();
    }

    public Mono<ProductoResponse> actualizarStock(Long id, ActualizarStockRequest request) {
        return productoRepository.actualizarStock(id, request.stock())
                .flatMap(filasAfectadas -> filasAfectadas > 0
                        ? productoRepository.findById(id)
                              .map(producto -> new ProductoResponse(
                                      producto.getId(), producto.getNombre(), producto.getStock()))
                              .switchIfEmpty(Mono.error(new ProductoNotFoundException(id)))
                        : Mono.error(new ProductoNotFoundException(id)));
    }

    public Flux<ProductoSucursalResponse> consultarMaxStockPorFranquicia(Long idFranquicia) {
        return franquiciaRepository.existsById(idFranquicia)
                .filter(Boolean::booleanValue)
                .switchIfEmpty(Mono.error(new FranquiciaNotFoundException(idFranquicia)))
                .flatMapMany(existe -> productoRepository.findMaxStockConSucursalDeFranquicia(idFranquicia))
                .map(this::aRespuestaConSucursal);
    }

    public Flux<ProductoSucursalResponse> listarProductosDeFranquicia(Long idFranquicia) {
        return franquiciaRepository.existsById(idFranquicia)
                .filter(Boolean::booleanValue)
                .switchIfEmpty(Mono.error(new FranquiciaNotFoundException(idFranquicia)))
                .flatMapMany(existe -> productoRepository.listarConSucursalPorFranquicia(idFranquicia))
                .map(this::aRespuestaConSucursal);
    }

    private ProductoSucursalResponse aRespuestaConSucursal(ProductoSucursal proyeccion) {
        return new ProductoSucursalResponse(
                proyeccion.getId(),
                proyeccion.getNombre(),
                proyeccion.getStock(),
                proyeccion.getIdSucursal(),
                proyeccion.getNombreSucursal());
    }
}
