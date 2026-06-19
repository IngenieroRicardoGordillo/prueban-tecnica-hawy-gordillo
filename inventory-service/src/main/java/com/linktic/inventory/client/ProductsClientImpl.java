package com.linktic.inventory.client;

import com.linktic.inventory.dto.ApiResponse;
import com.linktic.inventory.dto.ProductDTO;
import com.linktic.inventory.exception.ProductsServiceException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.*;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Recover;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import java.util.Optional;
import java.util.UUID;

@Slf4j
@Component
@RequiredArgsConstructor
public class ProductsClientImpl implements ProductsClient {

    private final RestTemplate restTemplate;

    @Value("${products.service.url}")
    private String productsServiceUrl;

    @Value("${products.service.api-key}")
    private String productsApiKey;

    @Override
    @Retryable(
            retryFor = {ProductsServiceException.class},
            maxAttemptsExpression = "${products.service.max-retries:3}",
            backoff = @Backoff(delayExpression = "${products.service.retry-delay-ms:1000}", multiplier = 2)
    )
    public Optional<ProductDTO> findById(UUID productId) {
        log.info("Calling products-service for productId: {}", productId);
        String url = productsServiceUrl + "/api/v1/products/" + productId;

        HttpHeaders headers = new HttpHeaders();
        headers.set("X-API-Key", productsApiKey);
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<Void> entity = new HttpEntity<>(headers);

        try {
            ResponseEntity<ApiResponse<ProductDTO>> response = restTemplate.exchange(
                    url,
                    HttpMethod.GET,
                    entity,
                    new ParameterizedTypeReference<>() {}
            );
            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                return Optional.ofNullable(response.getBody().data());
            }
            return Optional.empty();
        } catch (HttpClientErrorException.NotFound e) {
            log.warn("Product not found in products-service: {}", productId);
            return Optional.empty();
        } catch (Exception e) {
            log.error("Error calling products-service for product {}: {}", productId, e.getMessage());
            throw new ProductsServiceException("Error al comunicarse con el servicio de productos: " + e.getMessage());
        }
    }

    @Recover
    public Optional<ProductDTO> recover(ProductsServiceException ex, UUID productId) {
        log.error("All retries exhausted for productId: {}. Error: {}", productId, ex.getMessage());
        throw new ProductsServiceException("Servicio de productos no disponible después de múltiples intentos");
    }
}
