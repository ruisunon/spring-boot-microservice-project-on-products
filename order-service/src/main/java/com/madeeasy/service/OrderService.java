package com.madeeasy.service;

import com.madeeasy.dto.OrderCancelRequest;
import com.madeeasy.dto.OrderRequest;
import org.springframework.http.ResponseEntity;

public interface OrderService {

    ResponseEntity<String> createOrder(Long productId, OrderRequest orderRequest);

    ResponseEntity<String> cancelOrder(OrderCancelRequest orderCancelRequest);
}
