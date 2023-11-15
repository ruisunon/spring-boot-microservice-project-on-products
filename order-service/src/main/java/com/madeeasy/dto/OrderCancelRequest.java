package com.madeeasy.dto;

import lombok.Data;

@Data
public class OrderCancelRequest {
    private Long orderId;
    private Long productId;
    private String customerEmail;
}
