package com.accenture.pruebaaccenture.producto.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.accenture.pruebaaccenture.franquicia.repository.FranquiciaRepository;
import com.accenture.pruebaaccenture.producto.domain.Producto;
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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

@ExtendWith(MockitoExtension.class)
class ProductoServiceTest {

    private static final Long ID_SUCURSAL = 21L;
    private static final Long ID_FRANQUICIA = 7L;

    @Mock
    private FranquiciaRepository franquiciaRepository;

    @Mock
    private SucursalRepository sucursalRepository;

    @Mock
    private ProductoRepository productoRepository;

    private ProductoService productoService;

    @BeforeEach
    void setUp() {
        productoService = new ProductoService(franquiciaRepository, sucursalRepository, productoRepository);
    }

    @Test
    void agregarProductoValidaSucursalYRetornaProductoCreado() {
        when(sucursalRepository.existsById(ID_SUCURSAL)).thenReturn(Mono.just(true));
        when(productoRepository.save(any(Producto.class))).thenAnswer(invocacion -> {
            Producto guardado = invocacion.getArgument(0, Producto.class);
            guardado.setId(100L);
            return Mono.just(guardado);
        });

        StepVerifier.create(productoService.agregarProducto(
                        ID_SUCURSAL, new CrearProductoRequest("Producto A", 10)))
                .assertNext(respuesta -> {
                    assertThat(respuesta.id()).isEqualTo(100L);
                    assertThat(respuesta.nombre()).isEqualTo("Producto A");
                    assertThat(respuesta.stock()).isEqualTo(10);
                })
                .verifyComplete();

        verify(productoRepository).save(any(Producto.class));
    }

    @Test
    void agregarProductoConSucursalInexistenteLanzaExcepcion() {
        when(sucursalRepository.existsById(ID_SUCURSAL)).thenReturn(Mono.just(false));

        StepVerifier.create(productoService.agregarProducto(
                        ID_SUCURSAL, new CrearProductoRequest("Producto A", 10)))
                .expectError(SucursalNotFoundException.class)
                .verify();

        verify(productoRepository, never()).save(any(Producto.class));
    }

    @Test
    void eliminarProductoEliminaYCompletaCuandoExiste() {
        when(productoRepository.existsById(100L)).thenReturn(Mono.just(true));
        when(productoRepository.deleteById(100L)).thenReturn(Mono.empty());

        StepVerifier.create(productoService.eliminarProducto(100L))
                .verifyComplete();

        verify(productoRepository).deleteById(100L);
    }

    @Test
    void eliminarProductoConProductoInexistenteLanzaExcepcion() {
        when(productoRepository.existsById(999L)).thenReturn(Mono.just(false));

        StepVerifier.create(productoService.eliminarProducto(999L))
                .expectError(ProductoNotFoundException.class)
                .verify();

        verify(productoRepository, never()).deleteById(anyLong());
    }

    @Test
    void actualizarStockDevuelveProductoActualizado() {
        when(productoRepository.actualizarStock(100L, 25)).thenReturn(Mono.just(1L));
        when(productoRepository.findById(100L)).thenReturn(Mono.just(nuevoProducto(100L, "Producto A", 25)));

        StepVerifier.create(productoService.actualizarStock(100L, new ActualizarStockRequest(25)))
                .assertNext(respuesta -> {
                    assertThat(respuesta.id()).isEqualTo(100L);
                    assertThat(respuesta.nombre()).isEqualTo("Producto A");
                    assertThat(respuesta.stock()).isEqualTo(25);
                })
                .verifyComplete();
    }

    @Test
    void actualizarStockConProductoInexistenteLanzaExcepcionPorCeroFilas() {
        when(productoRepository.actualizarStock(999L, 25)).thenReturn(Mono.just(0L));

        StepVerifier.create(productoService.actualizarStock(999L, new ActualizarStockRequest(25)))
                .expectError(ProductoNotFoundException.class)
                .verify();

        verify(productoRepository, never()).findById(anyLong());
    }

    @Test
    void actualizarStockLanzaExcepcionSiProductoDesaparecioTrasElUpdate() {
        when(productoRepository.actualizarStock(100L, 25)).thenReturn(Mono.just(1L));
        when(productoRepository.findById(100L)).thenReturn(Mono.empty());

        StepVerifier.create(productoService.actualizarStock(100L, new ActualizarStockRequest(25)))
                .expectError(ProductoNotFoundException.class)
                .verify();
    }

    @Test
    void consultarMaxStockRetornaUnProductoPorSucursalConNombreDeSucursal() {
        when(franquiciaRepository.existsById(ID_FRANQUICIA)).thenReturn(Mono.just(true));
        when(productoRepository.findMaxStockPorSucursalDeFranquicia(ID_FRANQUICIA))
                .thenReturn(Flux.just(
                        nuevoProducto(100L, "Producto A", 50, ID_SUCURSAL),
                        nuevoProducto(101L, "Producto B", 45, 22L)));
        when(sucursalRepository.findAllById(anyList())).thenReturn(Flux.just(
                sucursal(22L, "Sucursal Sur"),
                sucursal(ID_SUCURSAL, "Sucursal Norte")));

        StepVerifier.create(productoService.consultarMaxStockPorFranquicia(ID_FRANQUICIA))
                .assertNext(respuesta -> {
                    assertThat(respuesta.id()).isEqualTo(100L);
                    assertThat(respuesta.idSucursal()).isEqualTo(ID_SUCURSAL);
                    assertThat(respuesta.nombreSucursal()).isEqualTo("Sucursal Norte");
                    assertThat(respuesta.stock()).isEqualTo(50);
                })
                .assertNext(respuesta -> {
                    assertThat(respuesta.id()).isEqualTo(101L);
                    assertThat(respuesta.nombreSucursal()).isEqualTo("Sucursal Sur");
                })
                .verifyComplete();
    }

    @Test
    void consultarMaxStockConFranquiciaInexistenteLanzaExcepcion() {
        when(franquiciaRepository.existsById(ID_FRANQUICIA)).thenReturn(Mono.just(false));

        StepVerifier.create(productoService.consultarMaxStockPorFranquicia(ID_FRANQUICIA))
                .expectError(FranquiciaNotFoundException.class)
                .verify();

        verify(productoRepository, never()).findMaxStockPorSucursalDeFranquicia(anyLong());
        verify(sucursalRepository, never()).findAllById(anyList());
    }

    @Test
    void listarProductosRetornaTodosConSuSucursal() {
        when(franquiciaRepository.existsById(ID_FRANQUICIA)).thenReturn(Mono.just(true));
        when(productoRepository.listarProductosPorFranquicia(ID_FRANQUICIA))
                .thenReturn(Flux.just(
                        nuevoProducto(100L, "Producto A", 10, ID_SUCURSAL),
                        nuevoProducto(101L, "Producto B", 0, ID_SUCURSAL),
                        nuevoProducto(105L, "Producto C", 3, 22L)));
        when(sucursalRepository.findAllById(anyList())).thenReturn(Flux.just(
                sucursal(ID_SUCURSAL, "Sucursal Norte"),
                sucursal(22L, "Sucursal Sur")));

        StepVerifier.create(productoService.listarProductosDeFranquicia(ID_FRANQUICIA))
                .assertNext(respuesta -> {
                    assertThat(respuesta.id()).isEqualTo(100L);
                    assertThat(respuesta.nombreSucursal()).isEqualTo("Sucursal Norte");
                })
                .assertNext(respuesta -> assertThat(respuesta.stock()).isZero())
                .assertNext(respuesta -> {
                    assertThat(respuesta.idSucursal()).isEqualTo(22L);
                    assertThat(respuesta.nombreSucursal()).isEqualTo("Sucursal Sur");
                    assertThat(respuesta.nombre()).isEqualTo("Producto C");
                })
                .verifyComplete();
    }

    @Test
    void listarProductosConFranquiciaSinProductosRetornaFluxVacio() {
        when(franquiciaRepository.existsById(ID_FRANQUICIA)).thenReturn(Mono.just(true));
        when(productoRepository.listarProductosPorFranquicia(ID_FRANQUICIA)).thenReturn(Flux.empty());

        StepVerifier.create(productoService.listarProductosDeFranquicia(ID_FRANQUICIA))
                .verifyComplete();

        verify(sucursalRepository, never()).findAllById(anyList());
    }

    @Test
    void listarProductosConFranquiciaInexistenteLanzaExcepcion() {
        when(franquiciaRepository.existsById(ID_FRANQUICIA)).thenReturn(Mono.just(false));

        StepVerifier.create(productoService.listarProductosDeFranquicia(ID_FRANQUICIA))
                .expectError(FranquiciaNotFoundException.class)
                .verify();

        verify(productoRepository, never()).listarProductosPorFranquicia(anyLong());
        verify(sucursalRepository, never()).findAllById(anyList());
    }

    private Producto nuevoProducto(Long id, String nombre, Integer stock) {
        Producto producto = new Producto(ID_SUCURSAL, nombre, stock);
        producto.setId(id);
        return producto;
    }

    private Producto nuevoProducto(Long id, String nombre, Integer stock, Long idSucursal) {
        Producto producto = new Producto(idSucursal, nombre, stock);
        producto.setId(id);
        return producto;
    }

    private Sucursal sucursal(Long id, String nombre) {
        Sucursal sucursal = new Sucursal(ID_FRANQUICIA, nombre);
        sucursal.setId(id);
        return sucursal;
    }
}

