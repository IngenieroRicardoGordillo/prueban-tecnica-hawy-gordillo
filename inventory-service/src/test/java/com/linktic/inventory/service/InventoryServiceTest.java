package com.linktic.inventory.service;

import com.linktic.inventory.client.ProductsClient;
import com.linktic.inventory.dto.*;
import com.linktic.inventory.exception.InsufficientStockException;
import com.linktic.inventory.exception.InventoryNotFoundException;
import com.linktic.inventory.exception.ProductsServiceException;
import com.linktic.inventory.model.Inventory;
import com.linktic.inventory.model.Purchase;
import com.linktic.inventory.repository.InventoryRepository;
import com.linktic.inventory.repository.PurchaseRepository;
import com.linktic.inventory.service.impl.InventoryServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("InventoryService Unit Tests")
class InventoryServiceTest {

    @Mock
    private InventoryRepository inventoryRepository;

    @Mock
    private PurchaseRepository purchaseRepository;

    @Mock
    private ProductsClient productsClient;

    @InjectMocks
    private InventoryServiceImpl inventoryService;

    private UUID productoId;
    private Inventory inventory;
    private ProductDTO productDTO;

    @BeforeEach
    void setUp() {
        productoId = UUID.randomUUID();
        inventory = Inventory.builder()
                .id(UUID.randomUUID())
                .productoId(productoId)
                .cantidad(100)
                .updatedAt(LocalDateTime.now())
                .build();
        productDTO = new ProductDTO(productoId, "Laptop Dell", new BigDecimal("1500.00"), "Descripción");
    }

    @Test
    @DisplayName("findAll - retorna página de registros de inventario")
    void findAll_returnsPaginatedInventory() {
        Pageable pageable = PageRequest.of(0, 10);
        when(inventoryRepository.findAll(pageable)).thenReturn(new PageImpl<>(List.of(inventory), pageable, 1));

        PageResponse<InventoryDTO> result = inventoryService.findAll(pageable);

        assertThat(result.content()).hasSize(1);
        assertThat(result.content().get(0).productoId()).isEqualTo(productoId);
        assertThat(result.totalElements()).isEqualTo(1);
    }

    @Test
    @DisplayName("findByProductoId - inventario encontrado")
    void findByProductoId_whenExists_returnsInventoryDTO() {
        when(inventoryRepository.findByProductoId(productoId)).thenReturn(Optional.of(inventory));

        InventoryDTO result = inventoryService.findByProductoId(productoId);

        assertThat(result.productoId()).isEqualTo(productoId);
        assertThat(result.cantidad()).isEqualTo(100);
    }

    @Test
    @DisplayName("findByProductoId - no encontrado lanza excepción")
    void findByProductoId_whenNotFound_throwsException() {
        when(inventoryRepository.findByProductoId(productoId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> inventoryService.findByProductoId(productoId))
                .isInstanceOf(InventoryNotFoundException.class)
                .hasMessageContaining(productoId.toString());
    }

    @Test
    @DisplayName("update - crea o actualiza inventario correctamente")
    void update_createsOrUpdatesInventory() {
        when(inventoryRepository.findByProductoId(productoId)).thenReturn(Optional.of(inventory));
        when(inventoryRepository.saveAndFlush(any(Inventory.class))).thenReturn(inventory);

        UpdateInventoryRequest request = new UpdateInventoryRequest(50);
        InventoryDTO result = inventoryService.update(productoId, request);

        assertThat(result.productoId()).isEqualTo(productoId);
        verify(inventoryRepository).saveAndFlush(any(Inventory.class));
    }

    @Test
    @DisplayName("purchase - compra exitosa decrementa inventario")
    void purchase_whenStockAvailable_completesSuccessfully() {
        PurchaseRequest request = new PurchaseRequest(productoId, 5);
        Purchase savedPurchase = Purchase.builder()
                .id(UUID.randomUUID())
                .productoId(productoId)
                .cantidad(5)
                .precioUnitario(new BigDecimal("1500.00"))
                .total(new BigDecimal("7500.00"))
                .status("COMPLETED")
                .purchasedAt(LocalDateTime.now())
                .build();

        when(productsClient.findById(productoId)).thenReturn(Optional.of(productDTO));
        when(inventoryRepository.findByProductoId(productoId)).thenReturn(Optional.of(inventory));
        when(inventoryRepository.saveAndFlush(any(Inventory.class))).thenReturn(inventory);
        when(purchaseRepository.saveAndFlush(any(Purchase.class))).thenReturn(savedPurchase);

        PurchaseResultDTO result = inventoryService.purchase(request);

        assertThat(result.status()).isEqualTo("COMPLETED");
        assertThat(result.cantidad()).isEqualTo(5);
        assertThat(result.total()).isEqualByComparingTo("7500.00");
        verify(inventoryRepository).saveAndFlush(any(Inventory.class));
        verify(purchaseRepository).saveAndFlush(any(Purchase.class));
    }

    @Test
    @DisplayName("purchase - stock insuficiente lanza excepción")
    void purchase_whenInsufficientStock_throwsException() {
        PurchaseRequest request = new PurchaseRequest(productoId, 200);

        when(productsClient.findById(productoId)).thenReturn(Optional.of(productDTO));
        when(inventoryRepository.findByProductoId(productoId)).thenReturn(Optional.of(inventory));

        assertThatThrownBy(() -> inventoryService.purchase(request))
                .isInstanceOf(InsufficientStockException.class)
                .hasMessageContaining("200")
                .hasMessageContaining("100");
    }

    @Test
    @DisplayName("purchase - producto no encontrado en products-service lanza excepción")
    void purchase_whenProductNotFound_throwsException() {
        PurchaseRequest request = new PurchaseRequest(productoId, 5);
        when(productsClient.findById(productoId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> inventoryService.purchase(request))
                .isInstanceOf(ProductsServiceException.class);
    }
}
