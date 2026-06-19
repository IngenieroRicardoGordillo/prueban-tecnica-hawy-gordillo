package com.linktic.inventory.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

public record PurchaseResultDTO(
        UUID purchaseId,
        UUID productoId,
        String productoNombre,
        Integer cantidad,
        BigDecimal precioUnitario,
        BigDecimal total,
        String status,
        LocalDateTime purchasedAt
) {}
