package com.linktic.products.service.impl;

import com.linktic.products.dto.CreateProductRequest;
import com.linktic.products.dto.ProductDTO;
import com.linktic.products.exception.ProductNotFoundException;
import com.linktic.products.model.Product;
import com.linktic.products.repository.ProductRepository;
import com.linktic.products.service.ProductService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class ProductServiceImpl implements ProductService {

    private final ProductRepository productRepository;

    @Override
    @Transactional
    public ProductDTO create(CreateProductRequest request) {
        log.info("Creating product: {}", request.nombre());
        Product product = Product.builder()
                .nombre(request.nombre())
                .precio(request.precio())
                .descripcion(request.descripcion())
                .build();
        Product saved = productRepository.saveAndFlush(product);
        log.info("Product created with id: {}", saved.getId());
        return toDTO(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public ProductDTO findById(UUID id) {
        log.info("Fetching product by id: {}", id);
        return productRepository.findById(id)
                .map(this::toDTO)
                .orElseThrow(() -> new ProductNotFoundException(id));
    }

    @Override
    @Transactional(readOnly = true)
    public List<ProductDTO> findAll() {
        log.info("Fetching all products");
        return productRepository.findAll().stream()
                .map(this::toDTO)
                .toList();
    }

    private ProductDTO toDTO(Product product) {
        return new ProductDTO(
                product.getId(),
                product.getNombre(),
                product.getPrecio(),
                product.getDescripcion(),
                product.getCreatedAt(),
                product.getUpdatedAt()
        );
    }
}
