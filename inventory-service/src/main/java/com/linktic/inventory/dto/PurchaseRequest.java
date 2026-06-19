package com.linktic.inventory.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

import java.util.UUID;

@Schema(description = "Payload para realizar una compra")
public record PurchaseRequest(
        @Schema(description = "ID del producto a comprar", example = "550e8400-e29b-41d4-a716-446655440000")
        @NotNull(message = "El productoId es obligatorio")
        UUID productoId,

        @Schema(description = "Cantidad a comprar", example = "3")
        @NotNull(message = "La cantidad es obligatoria")
        @Min(value = 1, message = "La cantidad mínima es 1")
        Integer cantidad
) {}
