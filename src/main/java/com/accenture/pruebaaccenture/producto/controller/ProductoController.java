package com.accenture.pruebaaccenture.producto.controller;

import com.accenture.pruebaaccenture.producto.dto.ActualizarStockRequest;
import com.accenture.pruebaaccenture.producto.dto.CrearProductoRequest;
import com.accenture.pruebaaccenture.producto.dto.ProductoResponse;
import com.accenture.pruebaaccenture.producto.dto.ProductoSucursalResponse;
import com.accenture.pruebaaccenture.producto.service.ProductoService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/api")
public class ProductoController {

    private final ProductoService productoService;

    public ProductoController(ProductoService productoService) {
        this.productoService = productoService;
    }

    @PostMapping("/sucursales/{idSucursal}/productos")
    @ResponseStatus(HttpStatus.CREATED)
    public Mono<ProductoResponse> agregar(
            @PathVariable Long idSucursal,
            @Valid @RequestBody CrearProductoRequest request) {
        return productoService.agregarProducto(idSucursal, request);
    }

    @DeleteMapping("/productos/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public Mono<Void> eliminar(@PathVariable Long id) {
        return productoService.eliminarProducto(id);
    }

    @PatchMapping("/productos/{id}/stock")
    public Mono<ProductoResponse> actualizarStock(
            @PathVariable Long id,
            @Valid @RequestBody ActualizarStockRequest request) {
        return productoService.actualizarStock(id, request);
    }

    @GetMapping("/franquicias/{idFranquicia}/top-productos")
    public Flux<ProductoSucursalResponse> consultarMaxStock(@PathVariable Long idFranquicia) {
        return productoService.consultarMaxStockPorFranquicia(idFranquicia);
    }

    @GetMapping("/franquicias/{idFranquicia}/productos")
    public Flux<ProductoSucursalResponse> listarProductos(@PathVariable Long idFranquicia) {
        return productoService.listarProductosDeFranquicia(idFranquicia);
    }
}
