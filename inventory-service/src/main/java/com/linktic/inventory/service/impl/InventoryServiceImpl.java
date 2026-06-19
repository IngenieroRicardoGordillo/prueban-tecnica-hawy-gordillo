package com.linktic.inventory.service.impl;

import com.linktic.inventory.client.ProductsClient;
import com.linktic.inventory.dto.*;
import com.linktic.inventory.exception.InventoryNotFoundException;
import com.linktic.inventory.exception.InsufficientStockException;
import com.linktic.inventory.exception.ProductsServiceException;
import com.linktic.inventory.model.Inventory;
import com.linktic.inventory.model.Purchase;
import com.linktic.inventory.repository.InventoryRepository;
import com.linktic.inventory.repository.PurchaseRepository;
import com.linktic.inventory.service.InventoryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class InventoryServiceImpl implements InventoryService {

    private final InventoryRepository inventoryRepository;
    private final PurchaseRepository purchaseRepository;
    private final ProductsClient productsClient;

    @Override
    @Transactional(readOnly = true)
    public List<InventoryDTO> findAll() {
        log.info("Fetching all inventory records");
        return inventoryRepository.findAll().stream()
                .map(this::toDTO)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public InventoryDTO findByProductoId(UUID productoId) {
        log.info("Fetching inventory for productoId: {}", productoId);
        return inventoryRepository.findByProductoId(productoId)
                .map(this::toDTO)
                .orElseThrow(() -> new InventoryNotFoundException(productoId));
    }

    @Override
    @Transactional
    public InventoryDTO update(UUID productoId, UpdateInventoryRequest request) {
        log.info("Updating inventory for productoId: {} with cantidad: {}", productoId, request.cantidad());

        // Upsert: crea o actualiza el registro de inventario
        Inventory inventory = inventoryRepository.findByProductoId(productoId)
                .orElse(Inventory.builder()
                        .productoId(productoId)
                        .build());

        inventory.setCantidad(request.cantidad());
        Inventory saved = inventoryRepository.saveAndFlush(inventory);
        log.info("Inventory updated for productoId: {}", productoId);
        return toDTO(saved);
    }

    @Override
    @Transactional
    public PurchaseResultDTO purchase(PurchaseRequest request) {
        log.info("Processing purchase for productoId: {}, cantidad: {}", request.productoId(), request.cantidad());

        // 1. Validar que el producto exista en el products-service
        ProductDTO product = productsClient.findById(request.productoId())
                .orElseThrow(() -> new ProductsServiceException(
                        "Producto no encontrado con id: " + request.productoId()));

        // 2. Verificar disponibilidad de inventario
        Inventory inventory = inventoryRepository.findByProductoId(request.productoId())
                .orElseThrow(() -> new InventoryNotFoundException(request.productoId()));

        if (inventory.getCantidad() < request.cantidad()) {
            throw new InsufficientStockException(request.cantidad(), inventory.getCantidad());
        }

        // 3. Actualizar inventario
        inventory.setCantidad(inventory.getCantidad() - request.cantidad());
        inventoryRepository.saveAndFlush(inventory);

        // 4. Registrar la compra
        BigDecimal total = product.precio().multiply(BigDecimal.valueOf(request.cantidad()));
        Purchase purchase = Purchase.builder()
                .productoId(request.productoId())
                .cantidad(request.cantidad())
                .precioUnitario(product.precio())
                .total(total)
                .status("COMPLETED")
                .build();
        Purchase saved = purchaseRepository.saveAndFlush(purchase);

        log.info("Purchase completed. Id: {}, Total: {}", saved.getId(), total);

        return new PurchaseResultDTO(
                saved.getId(),
                saved.getProductoId(),
                product.nombre(),
                saved.getCantidad(),
                saved.getPrecioUnitario(),
                saved.getTotal(),
                saved.getStatus(),
                saved.getPurchasedAt()
        );
    }

    private InventoryDTO toDTO(Inventory inventory) {
        return new InventoryDTO(
                inventory.getId(),
                inventory.getProductoId(),
                inventory.getCantidad(),
                inventory.getUpdatedAt()
        );
    }
}
