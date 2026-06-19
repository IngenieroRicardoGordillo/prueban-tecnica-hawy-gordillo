package com.linktic.inventory.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.linktic.inventory.client.ProductsClient;
import com.linktic.inventory.dto.*;
import com.linktic.inventory.model.Inventory;
import com.linktic.inventory.repository.InventoryRepository;
import com.linktic.inventory.repository.PurchaseRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.UUID;

import static org.hamcrest.Matchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@DisplayName("InventoryController Integration Tests")
class InventoryControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private InventoryRepository inventoryRepository;

    @Autowired
    private PurchaseRepository purchaseRepository;

    @MockBean
    private ProductsClient productsClient;

    @Value("${api.key}")
    private String apiKey;

    private UUID productoId;

    @BeforeEach
    void setUp() {
        purchaseRepository.deleteAll();
        inventoryRepository.deleteAll();
        productoId = UUID.randomUUID();
    }

    @Test
    @DisplayName("GET /api/v1/inventory - retorna lista")
    void getAllInventory_returnsOk() throws Exception {
        mockMvc.perform(get("/api/v1/inventory")
                        .header("X-API-Key", apiKey))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success", is(true)));
    }

    @Test
    @DisplayName("PUT /api/v1/inventory/{productoId} - actualiza inventario")
    void updateInventory_returnsOk() throws Exception {
        UpdateInventoryRequest request = new UpdateInventoryRequest(50);

        mockMvc.perform(put("/api/v1/inventory/{productoId}", productoId)
                        .header("X-API-Key", apiKey)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success", is(true)))
                .andExpect(jsonPath("$.data.cantidad", is(50)));
    }

    @Test
    @DisplayName("GET /api/v1/inventory/{productoId} - retorna inventario existente")
    void getInventoryByProductoId_returnsOk() throws Exception {
        inventoryRepository.save(Inventory.builder()
                .productoId(productoId)
                .cantidad(30)
                .build());

        mockMvc.perform(get("/api/v1/inventory/{productoId}", productoId)
                        .header("X-API-Key", apiKey))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.cantidad", is(30)));
    }

    @Test
    @DisplayName("POST /api/v1/purchases - compra exitosa")
    void purchase_withSufficientStock_returnsOk() throws Exception {
        inventoryRepository.save(Inventory.builder()
                .productoId(productoId)
                .cantidad(100)
                .build());

        ProductDTO mockProduct = new ProductDTO(productoId, "Laptop", new BigDecimal("1500.00"), "Desc");
        when(productsClient.findById(productoId)).thenReturn(Optional.of(mockProduct));

        PurchaseRequest request = new PurchaseRequest(productoId, 2);

        mockMvc.perform(post("/api/v1/purchases")
                        .header("X-API-Key", apiKey)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.status", is("COMPLETED")))
                .andExpect(jsonPath("$.data.cantidad", is(2)));
    }

    @Test
    @DisplayName("POST /api/v1/purchases - stock insuficiente retorna 409")
    void purchase_withInsufficientStock_returns409() throws Exception {
        inventoryRepository.save(Inventory.builder()
                .productoId(productoId)
                .cantidad(1)
                .build());

        ProductDTO mockProduct = new ProductDTO(productoId, "Laptop", new BigDecimal("1500.00"), "Desc");
        when(productsClient.findById(productoId)).thenReturn(Optional.of(mockProduct));

        PurchaseRequest request = new PurchaseRequest(productoId, 100);

        mockMvc.perform(post("/api/v1/purchases")
                        .header("X-API-Key", apiKey)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.success", is(false)));
    }

    @Test
    @DisplayName("Sin API key retorna 401")
    void withoutApiKey_returns401() throws Exception {
        mockMvc.perform(get("/api/v1/inventory"))
                .andExpect(status().isUnauthorized());
    }
}
