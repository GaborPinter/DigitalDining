package com.DigitalDining.demo.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.DigitalDining.demo.model.OrderEntity;
import com.DigitalDining.demo.model.User;

public interface OrderRepository extends JpaRepository<OrderEntity, Long> {
    Optional<OrderEntity> findByOrderNumber(String orderNumber);
    
    List<OrderEntity> findByUserOrderByCreatedAtDesc(User user);

}
