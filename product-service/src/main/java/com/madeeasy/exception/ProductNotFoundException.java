package com.madeeasy.exception;

import org.springframework.stereotype.Component;

@Component
public class ProductNotFoundException extends RuntimeException {
    public ProductNotFoundException() {
    }

    public ProductNotFoundException(String message) {
        super(message);
    }
}
