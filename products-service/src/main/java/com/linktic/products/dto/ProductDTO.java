package com.linktic.products.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

public record ProductDTO(
        UUID id,
        String nombre,
        BigDecimal precio,
        String descripcion,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {}
