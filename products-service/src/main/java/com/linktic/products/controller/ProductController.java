package com.linktic.products.controller;

import com.linktic.products.dto.ApiResponse;
import com.linktic.products.dto.CreateProductRequest;
import com.linktic.products.dto.PageResponse;
import com.linktic.products.dto.ProductDTO;
import com.linktic.products.service.ProductService;
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
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@Validated
@RestController
@RequestMapping("/api/v1/products")
@RequiredArgsConstructor
@Tag(name = "Productos", description = "API para gestión de productos")
@SecurityRequirement(name = "X-API-Key")
public class ProductController {

    private final ProductService productService;

    @Operation(summary = "Crear producto", description = "Crea un nuevo producto en el catálogo")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "201", description = "Producto creado"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Datos inválidos"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "API Key inválida")
    })
    @PostMapping
    public ResponseEntity<ApiResponse<ProductDTO>> create(@Valid @RequestBody CreateProductRequest request) {
        ProductDTO product = productService.create(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.ok(product, "Producto creado exitosamente"));
    }

    @Operation(summary = "Obtener producto por ID")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Producto encontrado"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Producto no encontrado")
    })
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<ProductDTO>> getById(
            @Parameter(description = "ID del producto") @PathVariable UUID id) {
        return ResponseEntity.ok(ApiResponse.ok(productService.findById(id)));
    }

    @Operation(summary = "Listar productos paginados",
            description = "Retorna la lista de productos con paginación. Ordenados por fecha de creación descendente.")
    @GetMapping
    public ResponseEntity<ApiResponse<PageResponse<ProductDTO>>> getAll(
            @Parameter(description = "Número de página (0-indexed)")
            @RequestParam(defaultValue = "0") @Min(0) int page,
            @Parameter(description = "Cantidad de elementos por página (máx. 50)")
            @RequestParam(defaultValue = "10") @Min(1) @Max(50) int size) {
        var pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        return ResponseEntity.ok(ApiResponse.ok(productService.findAll(pageable)));
    }
}
