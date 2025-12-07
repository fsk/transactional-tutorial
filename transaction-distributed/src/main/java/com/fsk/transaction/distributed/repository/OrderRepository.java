package com.fsk.transaction.distributed.repository;

import com.fsk.transaction.distributed.entity.Order;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface OrderRepository extends JpaRepository<Order, Long> {
}


