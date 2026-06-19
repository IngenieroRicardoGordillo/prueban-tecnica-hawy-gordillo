package com.linktic.inventory.client;

import com.linktic.inventory.dto.ProductDTO;

import java.util.Optional;
import java.util.UUID;

public interface ProductsClient {
    Optional<ProductDTO> findById(UUID productId);
}
