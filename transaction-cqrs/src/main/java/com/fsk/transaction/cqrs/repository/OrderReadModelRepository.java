package com.fsk.transaction.cqrs.repository;

import com.fsk.transaction.cqrs.entity.OrderReadModel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface OrderReadModelRepository extends JpaRepository<OrderReadModel, Long> {
}


