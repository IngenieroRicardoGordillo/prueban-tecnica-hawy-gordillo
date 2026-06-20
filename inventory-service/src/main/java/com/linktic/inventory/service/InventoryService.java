package com.linktic.inventory.service;

import com.linktic.inventory.dto.InventoryDTO;
import com.linktic.inventory.dto.PageResponse;
import com.linktic.inventory.dto.PurchaseRequest;
import com.linktic.inventory.dto.PurchaseResultDTO;
import com.linktic.inventory.dto.UpdateInventoryRequest;
import org.springframework.data.domain.Pageable;

import java.util.UUID;

public interface InventoryService {
    PageResponse<InventoryDTO> findAll(Pageable pageable);
    InventoryDTO findByProductoId(UUID productoId);
    InventoryDTO update(UUID productoId, UpdateInventoryRequest request);
    PurchaseResultDTO purchase(PurchaseRequest request);
}
