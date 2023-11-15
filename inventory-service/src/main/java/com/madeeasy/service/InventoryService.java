package com.madeeasy.service;

import org.springframework.http.ResponseEntity;

public interface InventoryService {

    ResponseEntity<String> incrementProductByTheProductId(Long productId, Long size);

    Long getTotalProductByTheProductId(Long productId);

    ResponseEntity<?> decrementProductByTheProductId(Long productId, Long size);

    ResponseEntity<?> createInventory(Long productId);
}
