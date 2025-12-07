package com.fsk.transaction.microservices.repository;

import com.fsk.transaction.microservices.entity.OutboxMessage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface OutboxMessageRepository extends JpaRepository<OutboxMessage, Long> {
    List<OutboxMessage> findByStatusOrderByCreatedAtAsc(String status);
}



