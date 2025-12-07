package com.fsk.transaction.eventsourcing.repository;

import com.fsk.transaction.eventsourcing.entity.EventStore;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface EventStoreRepository extends JpaRepository<EventStore, Long> {
    List<EventStore> findByAggregateIdOrderByVersionAsc(String aggregateId);
}


