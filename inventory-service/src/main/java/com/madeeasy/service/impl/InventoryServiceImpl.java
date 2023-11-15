package com.madeeasy.service.impl;

import com.madeeasy.entity.Inventory;
import com.madeeasy.exception.ProductNotFoundException;
import com.madeeasy.repository.InventoryServiceRepository;
import com.madeeasy.service.InventoryService;
import com.madeeasy.vo.ProductResponse;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import lombok.RequiredArgsConstructor;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class InventoryServiceImpl implements InventoryService {
    private final InventoryServiceRepository inventoryServiceRepository;
    private final RestTemplate restTemplate;
    private final WebClient webClient;

    @CircuitBreaker(name = "myCircuitBreaker", fallbackMethod = "productServiceIsNotAvailable")
    @Override
    public ResponseEntity<String> incrementProductByTheProductId(Long productId, Long size) {

        String url = "http://product-service/product-service/get/product/" + productId;

        /**
         * When you use `bodyToMono` with `WebClient`, the type parameter should correspond to the type of the
         * response you are expecting. In your case, since the other service is sending
         * `ResponseEntity<List<ProductResponse>>`, you should set the type as follows:
         *
         * ```java
         * WebClient webClient = WebClient.create();
         * List<ProductResponse> productResponses = webClient.get()
         *     .uri("http://example.com/service-endpoint")
         *     .retrieve()
         *     .bodyToMono(new ParameterizedTypeReference<List<ProductResponse>>() {})
         *     .block();
         * ```
         *
         * In this example, `bodyToMono` is set to the `ParameterizedTypeReference` of `List<ProductResponse>`, which
         * represents the expected type of the response. You can then use the `block()` method to retrieve the result.
         */

        List<ProductResponse> productResponse = webClient.get()
                .uri(url)
                .retrieve()
                .bodyToMono(new ParameterizedTypeReference<List<ProductResponse>>() {
                })
                .block();
        System.out.println("productResponse = " + productResponse);
        Optional<ProductResponse> foundProduct = productResponse.stream()
                .map(product -> ProductResponse.builder()
                        .id(product.getId())
                        .name(product.getName())
                        .description(product.getDescription())
                        .price(product.getPrice())
                        .build()
                ).findFirst();
        Inventory savedInventory = null;
        if (!foundProduct.isEmpty()) {
            ProductResponse product = foundProduct.get();
            Optional<Inventory> foundById = this.inventoryServiceRepository.findByProductId(product.getId());
            if (!foundById.isEmpty()) {
                Inventory inventory = foundById.get();
                inventory.setTotalProductAvailable(inventory.getTotalProductAvailable() + size);
                savedInventory = this.inventoryServiceRepository.save(inventory);
            } else {
                throw new ProductNotFoundException("Product " + productId + " you are looking for is ");
            }

        }
//        if you want to use RestTemplate the follow the below code
//        RestTemplate restTemplate = new RestTemplate();
//        ResponseEntity<List<ProductResponse>> response = restTemplate.exchange(
//                url,
//                HttpMethod.GET,
//                null,
//                new ParameterizedTypeReference<List<ProductResponse>>() {
//                }
//        );
//        List<ProductResponse> productResponses = response.getBody();

        return ResponseEntity.ok("New " + size + " products added successfully \nNow the total product available for " +
                "productId : " + savedInventory.getProductId() + " is " + savedInventory.getTotalProductAvailable());
    }

    // note : signature must be same
    public ResponseEntity<String> productServiceIsNotAvailable(Long productId,
                                                               Long size,
                                                               Exception exception) {
        System.out.println("...............ProductService is not available is called...............");
        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body("Sorry !! Product Service is not available");

    }

    @Override
    public Long getTotalProductByTheProductId(Long productId) {
        Optional<Inventory> foundById = this.inventoryServiceRepository.findById(productId);
        if (!foundById.isEmpty()) {
            Inventory inventory = foundById.get();
            return inventory.getTotalProductAvailable();
        } else {
            // throw product not found exception
            throw new ProductNotFoundException("Product " + productId + " you are looking for is ");
        }
    }

    @Override
    public ResponseEntity<?> decrementProductByTheProductId(Long productId, Long size) {
        Optional<Inventory> foundById = this.inventoryServiceRepository.findById(productId);
        if (!foundById.isEmpty()) {
            Inventory inventory = foundById.get();
            if (inventory.getTotalProductAvailable() > 0 && ((inventory.getTotalProductAvailable() - size) >= 0)) {
                inventory.setTotalProductAvailable(inventory.getTotalProductAvailable() - size);
            }
            this.inventoryServiceRepository.save(inventory);
        } else {
            throw new ProductNotFoundException("Product " + productId + " you are looking for is ");
        }
        return ResponseEntity.ok("SuccessFully Decremented");
    }

    @Override
    public ResponseEntity<?> createInventory(Long productId) {
        Optional<Inventory> foundById = this.inventoryServiceRepository.findById(productId);
        if (!foundById.isEmpty()) {
            Inventory inventory = foundById.get();
            inventory.setTotalProductAvailable(inventory.getTotalProductAvailable() + 1);
            this.inventoryServiceRepository.save(inventory);
        } else {
            Inventory inventory = Inventory.builder()
                    .productId(productId)
                    .totalProductAvailable(1L)
                    .build();
            this.inventoryServiceRepository.save(inventory);
        }
        return ResponseEntity.ok("Successfully saved inventory");
    }

}
