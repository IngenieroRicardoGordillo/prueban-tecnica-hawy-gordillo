package com.linktic.inventory.dto;

import java.time.LocalDateTime;
import java.util.UUID;

public record InventoryDTO(
        UUID id,
        UUID productoId,
        Integer cantidad,
        LocalDateTime updatedAt
) {}
