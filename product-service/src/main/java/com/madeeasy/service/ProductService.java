package com.madeeasy.service;

import com.madeeasy.dto.ProductRequest;
import com.madeeasy.dto.ProductResponse;
import org.springframework.http.ResponseEntity;

import java.util.List;

public interface ProductService {
    ResponseEntity<?> createProduct(ProductRequest productRequest);
    List<ProductResponse> getAllProducts();

    List<ProductResponse> getProductByName(Long productId);

    List<ProductResponse> updateProductPartially(Long productId,ProductRequest productRequest);

    List<ProductResponse> updateProductFully(Long productId, ProductRequest productRequest);

    void deleteProductById(Long productId);
}
