package com.madeeasy.controller;

import com.madeeasy.dto.OrderCancelRequest;
import com.madeeasy.dto.OrderRequest;
import com.madeeasy.service.OrderService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/order-service")
@RequiredArgsConstructor
@CrossOrigin("*")
public class OrderServiceController {

    private final OrderService orderService;


    // Create a new Order with the specified productId
    @PostMapping("/create/order/{productId}")
    public ResponseEntity<?> createOrder(@PathVariable("productId") Long productId,
                                         @RequestBody OrderRequest orderRequest) {
        ResponseEntity<String> order = this.orderService.createOrder(productId, orderRequest);
        return ResponseEntity.ok(order);
    }

    // candle a order with the specified productId method
    // Cancel an order with the specified productId
    @DeleteMapping("/cancel/order")
    public ResponseEntity<?> cancelOrder(@RequestBody OrderCancelRequest orderCancelRequest) {
        ResponseEntity<String> response = this.orderService.cancelOrder(orderCancelRequest);
        return ResponseEntity.ok(response);
    }
}
