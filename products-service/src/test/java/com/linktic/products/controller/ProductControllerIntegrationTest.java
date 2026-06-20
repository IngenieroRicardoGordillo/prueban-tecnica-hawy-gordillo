package com.linktic.products.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.linktic.products.dto.CreateProductRequest;
import com.linktic.products.model.Product;
import com.linktic.products.repository.ProductRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.UUID;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.hamcrest.Matchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@DisplayName("ProductController Integration Tests")
class ProductControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private ProductRepository productRepository;

    @Value("${api.key}")
    private String apiKey;

    @BeforeEach
    void setUp() {
        productRepository.deleteAll();
    }

    @Test
    @DisplayName("POST /api/v1/products - crea producto y retorna 201")
    void createProduct_returns201() throws Exception {
        CreateProductRequest request = new CreateProductRequest(
                "Laptop Dell", new BigDecimal("1500.00"), "Descripción de prueba");

        mockMvc.perform(post("/api/v1/products")
                        .header("X-API-Key", apiKey)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success", is(true)))
                .andExpect(jsonPath("$.data.nombre", is("Laptop Dell")))
                .andExpect(jsonPath("$.data.id", notNullValue()));
    }

    @Test
    @DisplayName("POST /api/v1/products - sin API key retorna 401")
    void createProduct_withoutApiKey_returns401() throws Exception {
        CreateProductRequest request = new CreateProductRequest(
                "Test", new BigDecimal("100"), null);

        mockMvc.perform(post("/api/v1/products")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("POST /api/v1/products - datos inválidos retorna 400")
    void createProduct_withInvalidData_returns400() throws Exception {
        CreateProductRequest request = new CreateProductRequest("", new BigDecimal("-1"), null);

        mockMvc.perform(post("/api/v1/products")
                        .header("X-API-Key", apiKey)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success", is(false)));
    }

    @Test
    @DisplayName("GET /api/v1/products/{id} - retorna producto existente")
    void getProductById_returnsProduct() throws Exception {
        Product saved = productRepository.save(Product.builder()
                .nombre("Monitor LG")
                .precio(new BigDecimal("350.00"))
                .descripcion("Monitor 4K")
                .build());

        mockMvc.perform(get("/api/v1/products/{id}", saved.getId())
                        .header("X-API-Key", apiKey))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.nombre", is("Monitor LG")));
    }

    @Test
    @DisplayName("GET /api/v1/products/{id} - ID inexistente retorna 404")
    void getProductById_notFound_returns404() throws Exception {
        mockMvc.perform(get("/api/v1/products/{id}", UUID.randomUUID())
                        .header("X-API-Key", apiKey))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.success", is(false)));
    }

    @Test
    @DisplayName("GET /api/v1/products - retorna página con contenido y metadatos")
    void getAllProducts_returnsPaginatedResponse() throws Exception {
        productRepository.save(Product.builder()
                .nombre("Teclado Mecánico")
                .precio(new BigDecimal("120.00"))
                .descripcion("Teclado RGB")
                .build());

        mockMvc.perform(get("/api/v1/products")
                        .header("X-API-Key", apiKey)
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.content", hasSize(greaterThanOrEqualTo(1))))
                .andExpect(jsonPath("$.data.page").value(0))
                .andExpect(jsonPath("$.data.totalElements").isNumber())
                .andExpect(jsonPath("$.data.totalPages").isNumber());
    }

    @Test
    @DisplayName("GET /api/v1/products - size mayor a 50 retorna 400")
    void getAllProducts_withInvalidSize_returns400() throws Exception {
        mockMvc.perform(get("/api/v1/products")
                        .header("X-API-Key", apiKey)
                        .param("size", "100"))
                .andExpect(status().isBadRequest());
    }
}
