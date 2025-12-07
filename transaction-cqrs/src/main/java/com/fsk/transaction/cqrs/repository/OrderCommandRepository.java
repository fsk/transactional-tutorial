package com.fsk.transaction.cqrs.repository;

import com.fsk.transaction.cqrs.entity.OrderCommand;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface OrderCommandRepository extends JpaRepository<OrderCommand, Long> {
}


