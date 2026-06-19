package com.linktic.inventory.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

@Schema(description = "Payload para actualizar cantidad de inventario")
public record UpdateInventoryRequest(
        @Schema(description = "Nueva cantidad en inventario", example = "50")
        @NotNull(message = "La cantidad es obligatoria")
        @Min(value = 0, message = "La cantidad no puede ser negativa")
        Integer cantidad
) {}
