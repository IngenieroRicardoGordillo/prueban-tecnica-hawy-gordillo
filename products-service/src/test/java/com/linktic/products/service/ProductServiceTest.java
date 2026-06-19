package com.linktic.products.service;

import com.linktic.products.dto.CreateProductRequest;
import com.linktic.products.dto.ProductDTO;
import com.linktic.products.exception.ProductNotFoundException;
import com.linktic.products.model.Product;
import com.linktic.products.repository.ProductRepository;
import com.linktic.products.service.impl.ProductServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("ProductService Unit Tests")
class ProductServiceTest {

    @Mock
    private ProductRepository productRepository;

    @InjectMocks
    private ProductServiceImpl productService;

    private Product product;
    private UUID productId;

    @BeforeEach
    void setUp() {
        productId = UUID.randomUUID();
        product = Product.builder()
                .id(productId)
                .nombre("Laptop Dell XPS")
                .precio(new BigDecimal("2499.99"))
                .descripcion("Laptop de alto rendimiento")
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
    }

    @Test
    @DisplayName("Crear producto - éxito")
    void create_whenValidRequest_returnsProductDTO() {
        CreateProductRequest request = new CreateProductRequest(
                "Laptop Dell XPS", new BigDecimal("2499.99"), "Laptop de alto rendimiento");
        when(productRepository.save(any(Product.class))).thenReturn(product);

        ProductDTO result = productService.create(request);

        assertThat(result).isNotNull();
        assertThat(result.nombre()).isEqualTo("Laptop Dell XPS");
        assertThat(result.precio()).isEqualByComparingTo("2499.99");
        verify(productRepository, times(1)).save(any(Product.class));
    }

    @Test
    @DisplayName("Buscar por ID - producto encontrado")
    void findById_whenProductExists_returnsProductDTO() {
        when(productRepository.findById(productId)).thenReturn(Optional.of(product));

        ProductDTO result = productService.findById(productId);

        assertThat(result).isNotNull();
        assertThat(result.id()).isEqualTo(productId);
        assertThat(result.nombre()).isEqualTo("Laptop Dell XPS");
    }

    @Test
    @DisplayName("Buscar por ID - producto no encontrado lanza excepción")
    void findById_whenProductNotFound_throwsProductNotFoundException() {
        UUID unknownId = UUID.randomUUID();
        when(productRepository.findById(unknownId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> productService.findById(unknownId))
                .isInstanceOf(ProductNotFoundException.class)
                .hasMessageContaining(unknownId.toString());
    }

    @Test
    @DisplayName("Listar productos - retorna lista completa")
    void findAll_returnsAllProducts() {
        when(productRepository.findAll()).thenReturn(List.of(product));

        List<ProductDTO> result = productService.findAll();

        assertThat(result).hasSize(1);
        assertThat(result.get(0).nombre()).isEqualTo("Laptop Dell XPS");
    }

    @Test
    @DisplayName("Listar productos - lista vacía")
    void findAll_whenNoProducts_returnsEmptyList() {
        when(productRepository.findAll()).thenReturn(List.of());

        List<ProductDTO> result = productService.findAll();

        assertThat(result).isEmpty();
    }
}
