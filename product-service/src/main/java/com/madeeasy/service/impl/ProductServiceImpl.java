package com.madeeasy.service.impl;

import com.madeeasy.dto.ProductRequest;
import com.madeeasy.dto.ProductResponse;
import com.madeeasy.entity.Product;
import com.madeeasy.exception.ProductNotFoundException;
import com.madeeasy.repository.ProductRepository;
import com.madeeasy.service.ProductService;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class ProductServiceImpl implements ProductService {

    private final ProductRepository productRepository;
    private final RestTemplate restTemplate;
    private final WebClient webClient;

//    @CircuitBreaker(name = "myCircuitBreaker", fallbackMethod = "inventoryServiceIsNotResponding")
    @Override
    public ResponseEntity<?> createProduct(ProductRequest productRequest) {
        log.info("........Creating product........");
        Product product = Product.builder()
                .name(productRequest.getName())
                .price(productRequest.getPrice())
                .description(productRequest.getDescription())
                .build();
        Product savedProduct = productRepository.save(product);
        log.info("product created successfully with id {}", savedProduct.getId());

        // we are sending the productId to the inventory service

        String url = "http://inventory-service/inventory-service/add-inventory/product-id/" + savedProduct.getId();

        webClient.post()
                .uri(url)
                .retrieve()
                .bodyToMono(String.class)
                .block();
//        restTemplate.getForObject(url,String.class);
        ProductResponse productResponse = ProductResponse.builder()
                .id(savedProduct.getId())
                .name(savedProduct.getName())
                .description(savedProduct.getDescription())
                .price(savedProduct.getPrice())
                .build();
        return ResponseEntity.status(HttpStatus.OK).body(productResponse);
    }

    public ResponseEntity<?> inventoryServiceIsNotResponding(ProductRequest productRequest,
                                                             Exception exception) {
        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body("Sorry !! InventoryService is not " +
                "responding");
    }

    @Override
    public List<ProductResponse> getAllProducts() {
        List<Product> listOfAllProducts = this.productRepository.findAll();
        if (!listOfAllProducts.isEmpty()) {
            return listOfAllProducts.stream()
                    .map(product -> ProductResponse.builder()
                            .id(product.getId())
                            .name(product.getName())
                            .description(product.getDescription())
                            .price(product.getPrice())
                            .build())
                    .collect(Collectors.toList());
        } else {
            throw new ProductNotFoundException("Sorry there is no available products");
        }
    }

    @Override
    public List<ProductResponse> getProductByName(Long productId) {
        Optional<Product> productFoundByName = this.productRepository.findById(productId);
        if (!productFoundByName.isEmpty()) {
            return productFoundByName.stream()
                    .map(product -> ProductResponse.builder()
                            .id(product.getId())
                            .name(product.getName())
                            .description(product.getDescription())
                            .price(product.getPrice())
                            .build())
                    .toList();
        } else {
            throw new ProductNotFoundException("Product " + productId + " you are looking for is ");
        }

    }

    @Override
    public List<ProductResponse> updateProductPartially(Long productId, ProductRequest productRequest) {
        Optional<Product> productFoundByName = this.productRepository.findById(productId);
        if (!productFoundByName.isEmpty()) {
            Product product = productFoundByName.get();
            if (!productRequest.getName().isBlank()) {
                product.setName(productRequest.getName());
            }
            if (!productRequest.getDescription().isBlank()) {
                product.setDescription(productRequest.getDescription());
            }
            if (!Objects.isNull(productRequest.getPrice())) {
                product.setPrice(productRequest.getPrice());
            }
            Product updatedProduct = this.productRepository.save(product);
            return List.of(
                    ProductResponse.builder()
                            .id(updatedProduct.getId())
                            .name(updatedProduct.getName())
                            .price(updatedProduct.getPrice())
                            .build()
            );
        } else {
            throw new ProductNotFoundException("Product " + productId + " you are looking for is ");
        }
    }

    @Override
    public List<ProductResponse> updateProductFully(Long productId, ProductRequest productRequest) {
        Optional<Product> productFoundByName = this.productRepository.findById(productId);
        if (!productFoundByName.isEmpty()) {
            Product product = productFoundByName.get();
            product.setName(productRequest.getName());
            product.setDescription(productRequest.getDescription());
            product.setPrice(productRequest.getPrice());

            Product updatedProduct = this.productRepository.save(product);
            return List.of(
                    ProductResponse.builder()
                            .id(updatedProduct.getId())
                            .name(updatedProduct.getName())
                            .description(updatedProduct.getDescription())
                            .price(updatedProduct.getPrice())
                            .build()
            );
        } else {
            throw new ProductNotFoundException("Product " + productId + " you are looking for is ");
        }
    }

    @Override
    public void deleteProductById(Long productId) {
        Optional<Product> foundById = this.productRepository.findById(productId);
        if (!foundById.isEmpty()) {
            this.productRepository.deleteById(productId);
        } else {
            throw new ProductNotFoundException("Product with id : " + productId + " is ");
        }
    }
}
