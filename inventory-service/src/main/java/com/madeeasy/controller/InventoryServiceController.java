package com.madeeasy.controller;

import com.madeeasy.service.InventoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/inventory-service")
@RequiredArgsConstructor
@CrossOrigin("*")
public class InventoryServiceController {

    private final InventoryService inventoryService;

    // get total number of products for the specified productId
    @GetMapping("/get/number-of-products-available/{productId}")
    public Long getTotalProductByTheProductId(@PathVariable("productId") Long productId) {
        return this.inventoryService.getTotalProductByTheProductId(productId);
    }

    // add new product with the specified productId i.e. we are increasing he number of products
    // i.e. if product is sold out then we need to increment the number of products .

    @PostMapping("/increment-product/product-id/{productId}/numbers-to-be-incremented/{size}")
    public ResponseEntity<?> incrementProductByTheProductId(@PathVariable("productId") Long productId,
                                                            @PathVariable("size") Long size) {
        return this.inventoryService.incrementProductByTheProductId(productId, size);
    }

    // remove or decrement the number of the product i.e. when number of the product is ordered
    // then that particular number of the product should be decremented.
    // i.e. n product is ordered => ( total - n ).

    @DeleteMapping("/decrement-product/product-id/{productId}/numbers-to-be-decremented/{size}")
    public ResponseEntity<?> decrementProductByTheProductId(@PathVariable("productId") Long productId,
                                                            @PathVariable("size") Long size) {
        return this.inventoryService.decrementProductByTheProductId(productId, size);
    }

    @PostMapping("/add-inventory/product-id/{productId}")
    public ResponseEntity<?> createInventory(@PathVariable("productId") Long productId) {
        return this.inventoryService.createInventory(productId);
    }
}
