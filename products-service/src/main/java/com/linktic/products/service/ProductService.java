package com.linktic.products.service;

import com.linktic.products.dto.CreateProductRequest;
import com.linktic.products.dto.ProductDTO;

import java.util.List;
import java.util.UUID;

public interface ProductService {
    ProductDTO create(CreateProductRequest request);
    ProductDTO findById(UUID id);
    List<ProductDTO> findAll();
}
