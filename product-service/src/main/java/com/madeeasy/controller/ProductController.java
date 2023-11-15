package com.madeeasy.controller;

import com.madeeasy.dto.ProductRequest;
import com.madeeasy.dto.ProductResponse;
import com.madeeasy.service.ProductService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/product-service")
@Validated
@RequiredArgsConstructor
@CrossOrigin("*")
public class ProductController {

    private final ProductService productService;

    @PostMapping("/create/product") // POST http://host:port/product-service/create/product
    @ResponseStatus(HttpStatus.CREATED)
    public ResponseEntity<?> createProduct(@RequestBody @Valid ProductRequest productRequest) {
        return this.productService.createProduct(productRequest);
    }

    @GetMapping("/get/product") // GET http://host:port/product-service/get/product
    public ResponseEntity<?> getAllProducts() {
        List<ProductResponse> allProducts = this.productService.getAllProducts();
        return ResponseEntity.ok(allProducts);
    }

    @GetMapping("/get/product/{productId}")
    // to use PathVariable i.e. GET http://host:port/product-service/get/product/{productId}
    public ResponseEntity<?> getProductByName(@PathVariable("productId") Long productId) {
        List<ProductResponse> productFoundByName = this.productService.getProductByName(productId);
        return ResponseEntity.ok(productFoundByName);
    }

    // updating products {partial update}
    @PatchMapping("/update/product")
    public ResponseEntity<?> updateProductPartially(@RequestParam("productId") Long productId,
                                                    @RequestBody ProductRequest productRequest) {
        List<ProductResponse> updatedProductPartially = this.productService.updateProductPartially(productId,
                productRequest);
        return ResponseEntity.ok(updatedProductPartially);
    }

    // full product update
    @PutMapping("/update/product")
    public ResponseEntity<?> updateProductFully(@RequestParam("productId") Long productId,
                                                @RequestBody @Valid ProductRequest productRequest) {
        List<ProductResponse> updatedProductFully = this.productService.updateProductFully(productId,
                productRequest);
        return ResponseEntity.ok(updatedProductFully);

    }

    @DeleteMapping("/delete/product/{productId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public ResponseEntity<?> deleteProductById(@PathVariable("productId") Long productId) {
        this.productService.deleteProductById(productId);
        return ResponseEntity.ok("Product deleted successfully for productId: " + productId);
    }
}
