package com.linktic.inventory.dto;

import java.math.BigDecimal;
import java.util.UUID;

public record ProductDTO(
        UUID id,
        String nombre,
        BigDecimal precio,
        String descripcion
) {}
