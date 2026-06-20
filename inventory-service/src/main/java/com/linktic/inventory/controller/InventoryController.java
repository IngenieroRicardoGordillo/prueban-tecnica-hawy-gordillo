package com.linktic.inventory.controller;

import com.linktic.inventory.dto.*;
import com.linktic.inventory.service.InventoryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@Validated
@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
@SecurityRequirement(name = "X-API-Key")
public class InventoryController {

    private final InventoryService inventoryService;

    @Tag(name = "Inventario", description = "API para gestión de inventario")
    @Operation(summary = "Listar inventario paginado",
            description = "Retorna registros de inventario con paginación. Ordenados por última actualización descendente.")
    @GetMapping("/inventory")
    public ResponseEntity<ApiResponse<PageResponse<InventoryDTO>>> getAll(
            @Parameter(description = "Número de página (0-indexed)")
            @RequestParam(defaultValue = "0") @Min(0) int page,
            @Parameter(description = "Cantidad de elementos por página (máx. 50)")
            @RequestParam(defaultValue = "10") @Min(1) @Max(50) int size) {
        var pageable = PageRequest.of(page, size, Sort.by("updatedAt").descending());
        return ResponseEntity.ok(ApiResponse.ok(inventoryService.findAll(pageable)));
    }

    @Tag(name = "Inventario")
    @Operation(summary = "Consultar inventario por producto")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Inventario encontrado"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Sin registro de inventario para el producto")
    })
    @GetMapping("/inventory/{productoId}")
    public ResponseEntity<ApiResponse<InventoryDTO>> getByProductoId(
            @Parameter(description = "ID del producto") @PathVariable UUID productoId) {
        return ResponseEntity.ok(ApiResponse.ok(inventoryService.findByProductoId(productoId)));
    }

    @Tag(name = "Inventario")
    @Operation(summary = "Actualizar cantidad de inventario",
            description = "Crea o actualiza el registro de inventario para el producto dado")
    @PutMapping("/inventory/{productoId}")
    public ResponseEntity<ApiResponse<InventoryDTO>> update(
            @Parameter(description = "ID del producto") @PathVariable UUID productoId,
            @Valid @RequestBody UpdateInventoryRequest request) {
        return ResponseEntity.ok(ApiResponse.ok(
                inventoryService.update(productoId, request), "Inventario actualizado exitosamente"));
    }

    @Tag(name = "Compras", description = "API para el flujo de compras")
    @Operation(summary = "Realizar compra",
            description = "Valida disponibilidad, descuenta inventario y registra la compra. " +
                    "Este endpoint reside en inventory-service porque posee los datos de stock " +
                    "y puede ejecutar la validación y actualización en una sola transacción atómica.")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Compra realizada exitosamente"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Producto o inventario no encontrado"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "409", description = "Stock insuficiente"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "503", description = "Servicio de productos no disponible")
    })
    @PostMapping("/purchases")
    public ResponseEntity<ApiResponse<PurchaseResultDTO>> purchase(@Valid @RequestBody PurchaseRequest request) {
        PurchaseResultDTO result = inventoryService.purchase(request);
        return ResponseEntity.ok(ApiResponse.ok(result, "Compra realizada exitosamente"));
    }
}
