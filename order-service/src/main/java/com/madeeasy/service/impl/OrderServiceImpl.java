package com.madeeasy.service.impl;

import com.madeeasy.dto.OrderCancelRequest;
import com.madeeasy.dto.OrderRequest;
import com.madeeasy.entity.Order;
import com.madeeasy.repository.OrderRepository;
import com.madeeasy.service.OrderService;
import com.madeeasy.vo.EmailRequest;
import com.madeeasy.vo.ProductResponse;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import lombok.RequiredArgsConstructor;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;

import java.nio.file.ProviderNotFoundException;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class OrderServiceImpl implements OrderService {

    private final OrderRepository orderRepository;
    private final RestTemplate restTemplate;
    private final WebClient webClient;

    @CircuitBreaker(name = "myCircuitBreaker", fallbackMethod = "productServiceIsNotResponding")
    @Override
    public ResponseEntity<String> createOrder(Long productId, OrderRequest orderRequest) {
        String url = "http://product-service/product-service/get/product/" + productId;

        // getting the product by its id from product service
        List<ProductResponse> productResponse = webClient.get()
                .uri(url)
                .retrieve().bodyToMono(new ParameterizedTypeReference<List<ProductResponse>>() {
                })
                .block();
        System.out.println("productResponse = " + productResponse);

        // getting one product
        Optional<ProductResponse> foundProduct = productResponse.stream()
                .map(product -> ProductResponse.builder()
                        .id(product.getId())
                        .name(product.getName())
                        .description(product.getDescription())
                        .price(product.getPrice())
                        .build()
                ).findFirst();
        // getting the productId
        Long productIdFromProductResponse;
        if (!foundProduct.isEmpty()) {
            ProductResponse product = foundProduct.get();
            productIdFromProductResponse = product.getId();

        } else {
            throw new ProviderNotFoundException("Product not found for product with id " + productId);
        }
        System.out.println("................product is fetched successfully ................");

        // call to inventory service to check whether the specified product is available or not
        String urlToKnowIfProductIsAvailable =
                "http://inventory-service/inventory-service/get/number-of-products-available/" + productId;
        Long numberOfProductsAvailable = webClient.get()
                .uri(urlToKnowIfProductIsAvailable)
                .retrieve()
                .bodyToMono(Long.class)
                .block();
        System.out.println(numberOfProductsAvailable);
        if (numberOfProductsAvailable > 0) {

            Order approvedOrder = Order.builder()
                    .status("APPROVED")
                    .productId(productId)
                    .customerName(orderRequest.getName())
                    .customerEmail(orderRequest.getEmail())
                    .build();
            // remove one item as it is already approved
            String urlToDecreaseTheNumberOfProducts =
                    "http://inventory-service/inventory-service/decrement-product/product-id/" + productId +
                            "/numbers" +
                            "-to-be-decremented/" + 1;
            System.out.println("before calling to decrement endpoint");
            webClient.delete()
                    .uri(urlToDecreaseTheNumberOfProducts)
                    .retrieve()
                    .bodyToMono(String.class)
                    .block();
            Order savedOrder = this.orderRepository.save(approvedOrder);
            ProductResponse product;
            String htmlContent;
            if (!foundProduct.isEmpty()) {
                product = foundProduct.get();
                htmlContent = String.format(
                        "Order ID: %d\n\nProduct Information:\n" +
                                "ID: %d\n" +
                                "Name: %s\n" +
                                "Description: %s\n" +
                                "Price: $%.2f\n" +
                                "Status: %s",
                        savedOrder.getId(), product.getId(), product.getName(), product.getDescription(),
                        product.getPrice(), savedOrder.getStatus()
                );
                EmailRequest emailRequest = EmailRequest.builder()
                        .toEmail(orderRequest.getEmail())
                        .subject("approval of order from pabitra-app")
                        .text(htmlContent)
                        .build();
                webClient.post()
                        .uri("http://notification-service/notification-service/with-no-attachment")
                        .body((BodyInserters.fromValue(emailRequest)))
                        .retrieve()
                        .bodyToMono(String.class)
                        .block();
            }
            return ResponseEntity.ok("Successfully Order is placed!! Please check your email");
        }

        return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Sorry !! Product is not available");
    }

    @Override
    public ResponseEntity<String> cancelOrder(OrderCancelRequest orderCancelRequest) {

        /**
         * inventory-service
         * notification-service
         * order-service
         * product-service
         * gateway
         */
        Optional<Order> foundByOrderId = this.orderRepository.findById(orderCancelRequest.getOrderId());
        Optional<Order> foundByProductId = this.orderRepository.findByProductId(orderCancelRequest.getOrderId());
        Optional<Order> foundByCustomerEmail =
                this.orderRepository.findByCustomerEmail(orderCancelRequest.getCustomerEmail());

        if (foundByCustomerEmail.isPresent() && foundByOrderId.isPresent() && foundByProductId.isPresent()) {

            String urlToDecreaseTheNumberOfProducts =
                    "http://inventory-service/inventory-service/increment-product/product-id/" + orderCancelRequest.getProductId() +
                            "/numbers" +
                            "-to-be-incremented/" + 1;
            System.out.println("before calling to decrement endpoint");
            webClient.post()
                    .uri(urlToDecreaseTheNumberOfProducts)
                    .retrieve()
                    .bodyToMono(String.class)
                    .block();

            Order orderById = foundByOrderId.get();
            orderById.setStatus("CANCELLED");
            Order savedOrder = this.orderRepository.save(orderById);

            // getting product details by the productId

            String url = "http://product-service/product-service/get/product/" + orderCancelRequest.getProductId();

            // getting the product by its id from product service
            List<ProductResponse> productResponse = webClient.get()
                    .uri(url)
                    .retrieve().bodyToMono(new ParameterizedTypeReference<List<ProductResponse>>() {
                    })
                    .block();
            System.out.println("productResponse = " + productResponse);

            // getting one product
            Optional<ProductResponse> foundProduct = productResponse.stream()
                    .map(product -> ProductResponse.builder()
                            .id(product.getId())
                            .name(product.getName())
                            .description(product.getDescription())
                            .price(product.getPrice())
                            .build()
                    ).findFirst();
            // getting the productId
            ProductResponse product = null;
            if (!foundProduct.isEmpty()) {
                product = foundProduct.get();
            } else {
                throw new ProviderNotFoundException("Product not found for product with id " + orderCancelRequest.getProductId());
            }

            String htmlContent = String.format(
                    "Order ID: %d\n\nProduct Information:\n" +
                            "ID: %d\n" +
                            "Name: %s\n" +
                            "Description: %s\n" +
                            "Price: $%.2f\n" +
                            "Status: %s",
                    savedOrder.getId(), product.getId(), product.getName(), product.getDescription(),
                    product.getPrice(), savedOrder.getStatus()
            );
            EmailRequest emailRequest = EmailRequest.builder()
                    .toEmail(orderCancelRequest.getCustomerEmail())
                    .subject("cancellation of order from pabitra-app")
                    .text(htmlContent)
                    .build();
            webClient.post()
                    .uri("http://notification-service/notification-service/with-no-attachment")
                    .body((BodyInserters.fromValue(emailRequest)))
                    .retrieve()
                    .bodyToMono(String.class)
                    .block();

        }

        return ResponseEntity.ok("Order has been cancelled successfully");

    }

    // note : fallback method and which method is throwing exception that both signature must be same . [return type
    // + method parameters (extra is Exception)]
    public ResponseEntity<String> productServiceIsNotResponding(Long productId,
                                                                OrderRequest orderRequest,
                                                                Exception exception) {
        /**
         *  HttpHeaders headers = new HttpHeaders();
         *     headers.add("Custom-Header", "Custom-Value"); // Add any custom headers here
         *
         *     return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).headers(headers).body("Sorry !!
         *     ProductService is not responding");
         */
        System.out.println("................productServiceIsNotResponding................");
        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body("Sorry !! ProductService is not responding");
    }
}
