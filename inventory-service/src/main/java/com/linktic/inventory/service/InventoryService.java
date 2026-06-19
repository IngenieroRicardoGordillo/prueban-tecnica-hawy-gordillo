package com.linktic.inventory.service;

import com.linktic.inventory.dto.InventoryDTO;
import com.linktic.inventory.dto.PurchaseRequest;
import com.linktic.inventory.dto.PurchaseResultDTO;
import com.linktic.inventory.dto.UpdateInventoryRequest;

import java.util.List;
import java.util.UUID;

public interface InventoryService {
    List<InventoryDTO> findAll();
    InventoryDTO findByProductoId(UUID productoId);
    InventoryDTO update(UUID productoId, UpdateInventoryRequest request);
    PurchaseResultDTO purchase(PurchaseRequest request);
}
