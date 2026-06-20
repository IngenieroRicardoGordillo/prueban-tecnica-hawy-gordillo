package com.linktic.products.service;

import com.linktic.products.dto.CreateProductRequest;
import com.linktic.products.dto.PageResponse;
import com.linktic.products.dto.ProductDTO;
import org.springframework.data.domain.Pageable;

import java.util.UUID;

public interface ProductService {
    ProductDTO create(CreateProductRequest request);
    ProductDTO findById(UUID id);
    PageResponse<ProductDTO> findAll(Pageable pageable);
}
