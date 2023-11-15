package com.madeeasy.service.impl;

import com.madeeasy.entity.Inventory;
import com.madeeasy.exception.ProductNotFoundException;
import com.madeeasy.repository.InventoryServiceRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class InventoryServiceImplTest {

    @Mock
    private InventoryServiceRepository inventoryServiceRepository;

    @Mock
    private RestTemplate restTemplate;

    @InjectMocks
    private InventoryServiceImpl inventoryService;

    private Long productId;
    private Long size;
    private Inventory inventory;

    @BeforeEach
    public void setUp() {
        productId = 1L;
        size = 10L;
        inventory = Inventory.builder()
                .productId(productId)
                .totalProductAvailable(size)
                .build();
    }

    @Test
    public void testDecrementProductByTheProductIdWhenProductIsFoundThenDecrementProduct() {
        when(inventoryServiceRepository.findById(productId)).thenReturn(Optional.of(inventory));

        ResponseEntity<?> responseEntity = inventoryService.decrementProductByTheProductId(productId, size);

        verify(inventoryServiceRepository, times(1)).save(any(Inventory.class));
        assertThat(responseEntity).isNull();
    }

    @Test
    public void testDecrementProductByTheProductIdWhenProductIsNotFoundThenThrowException() {
        when(inventoryServiceRepository.findById(productId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> inventoryService.decrementProductByTheProductId(productId, size))
                .isInstanceOf(ProductNotFoundException.class)
                .hasMessageContaining("Product " + productId + " you are looking for is ");

        verify(inventoryServiceRepository, times(0)).save(any(Inventory.class));
    }
}