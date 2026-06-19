package com.linktic.products.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

@Schema(description = "Payload para crear un nuevo producto")
public record CreateProductRequest(

        @Schema(description = "Nombre del producto", example = "Laptop Dell XPS 15")
        @NotBlank(message = "El nombre es obligatorio")
        @Size(min = 2, max = 255, message = "El nombre debe tener entre 2 y 255 caracteres")
        String nombre,

        @Schema(description = "Precio del producto", example = "2499.99")
        @NotNull(message = "El precio es obligatorio")
        @Positive(message = "El precio debe ser mayor a 0")
        BigDecimal precio,

        @Schema(description = "Descripción del producto", example = "Laptop de alto rendimiento con procesador Intel Core i9")
        @Size(max = 1000, message = "La descripción no puede superar los 1000 caracteres")
        String descripcion
) {}
