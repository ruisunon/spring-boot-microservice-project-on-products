package com.madeeasy.repository;

import com.madeeasy.entity.Order;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface OrderRepository extends JpaRepository<Order, Long> {

    Optional<Order> findByProductId(Long aLong);
    Optional<Order> findByCustomerEmail(String customerEmail);
}
