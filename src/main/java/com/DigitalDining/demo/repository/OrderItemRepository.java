package com.DigitalDining.demo.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.DigitalDining.demo.model.OrderItemEntity;

public interface OrderItemRepository extends JpaRepository<OrderItemEntity, Long> {

}
