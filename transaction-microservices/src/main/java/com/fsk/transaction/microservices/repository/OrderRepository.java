package com.fsk.transaction.microservices.repository;

import com.fsk.transaction.microservices.entity.Order;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface OrderRepository extends JpaRepository<Order, Long> {
}



