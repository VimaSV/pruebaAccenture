package com.accenture.pruebaaccenture.producto.service;

import com.accenture.pruebaaccenture.franquicia.repository.FranquiciaRepository;
import com.accenture.pruebaaccenture.producto.domain.Producto;
import com.accenture.pruebaaccenture.producto.dto.ActualizarNombreRequest;
import com.accenture.pruebaaccenture.producto.dto.ActualizarStockRequest;
import com.accenture.pruebaaccenture.producto.dto.CrearProductoRequest;
import com.accenture.pruebaaccenture.producto.dto.ProductoResponse;
import com.accenture.pruebaaccenture.producto.dto.ProductoSucursalResponse;
import com.accenture.pruebaaccenture.producto.repository.ProductoRepository;
import com.accenture.pruebaaccenture.shared.exception.FranquiciaNotFoundException;
import com.accenture.pruebaaccenture.shared.exception.ProductoNotFoundException;
import com.accenture.pruebaaccenture.shared.exception.SucursalNotFoundException;
import com.accenture.pruebaaccenture.sucursal.domain.Sucursal;
import com.accenture.pruebaaccenture.sucursal.repository.SucursalRepository;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
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

    public Mono<ProductoResponse> renombrarProducto(Long id, ActualizarNombreRequest request) {
        return productoRepository.actualizarNombre(id, request.nombre())
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
                .flatMapMany(existe -> productoRepository.findMaxStockPorSucursalDeFranquicia(idFranquicia))
                .collectList()
                .flatMapMany(this::aRespuestasConSucursal);
    }

    public Flux<ProductoSucursalResponse> listarProductosDeFranquicia(Long idFranquicia) {
        return franquiciaRepository.existsById(idFranquicia)
                .filter(Boolean::booleanValue)
                .switchIfEmpty(Mono.error(new FranquiciaNotFoundException(idFranquicia)))
                .flatMapMany(existe -> productoRepository.listarProductosPorFranquicia(idFranquicia))
                .collectList()
                .flatMapMany(this::aRespuestasConSucursal);
    }

    private Flux<ProductoSucursalResponse> aRespuestasConSucursal(List<Producto> productos) {
        if (productos.isEmpty()) {
            return Flux.empty();
        }
        return sucursalRepository.findAllById(idsSucursales(productos))
                .collectMap(Sucursal::getId, Sucursal::getNombre)
                .flatMapMany(nombresPorSucursal -> Flux.fromIterable(productos)
                        .map(producto -> aRespuestaConSucursal(producto, nombresPorSucursal)));
    }

    private List<Long> idsSucursales(List<Producto> productos) {
        return productos.stream()
                .map(Producto::getIdSucursal)
                .distinct()
                .collect(Collectors.toList());
    }

    private ProductoSucursalResponse aRespuestaConSucursal(Producto producto, Map<Long, String> nombresPorSucursal) {
        return new ProductoSucursalResponse(
                producto.getId(),
                producto.getNombre(),
                producto.getStock(),
                producto.getIdSucursal(),
                nombresPorSucursal.get(producto.getIdSucursal()));
    }
}
