package com.fsk.transaction.rollback.repository;

import com.fsk.transaction.rollback.entity.Order;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface OrderRepository extends JpaRepository<Order, Long> {
}


