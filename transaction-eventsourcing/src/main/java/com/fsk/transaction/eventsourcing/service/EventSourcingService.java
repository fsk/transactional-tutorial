package com.fsk.transaction.eventsourcing.service;

import com.fsk.transaction.eventsourcing.entity.EventStore;
import com.fsk.transaction.eventsourcing.repository.EventStoreRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Transaction + Event Sourcing Konuları
 * 
 * Event Store
 * Snapshot Pattern
 * Event Replay
 * Consistency in Event Sourcing
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class EventSourcingService {
    
    private final EventStoreRepository eventStoreRepository;
    
    /**
     * Event Store'a event kaydetme
     */
    @Transactional
    public void appendEvent(String aggregateId, String eventType, String eventData) {
        log.info("appendEvent - Event Store'a event kaydediliyor");
        
        // Son version'ı bul
        List<EventStore> events = eventStoreRepository.findByAggregateIdOrderByVersionAsc(aggregateId);
        Long nextVersion = events.isEmpty() ? 1L : events.get(events.size() - 1).getVersion() + 1;
        
        EventStore event = new EventStore();
        event.setAggregateId(aggregateId);
        event.setEventType(eventType);
        event.setEventData(eventData);
        event.setVersion(nextVersion);
        event.setOccurredAt(LocalDateTime.now());
        
        eventStoreRepository.save(event);
        log.info("Event kaydedildi - Aggregate: {}, Version: {}", aggregateId, nextVersion);
    }
    
    /**
     * Event Replay - Aggregate'i event'lerden yeniden oluştur
     */
    @Transactional(readOnly = true)
    public List<EventStore> replayEvents(String aggregateId) {
        log.info("replayEvents - Event'ler replay ediliyor: {}", aggregateId);
        
        List<EventStore> events = eventStoreRepository.findByAggregateIdOrderByVersionAsc(aggregateId);
        log.info("{} event bulundu", events.size());
        
        return events;
    }
    
    /**
     * Snapshot oluşturma
     */
    @Transactional
    public void createSnapshot(String aggregateId, String snapshotData) {
        log.info("createSnapshot - Snapshot oluşturuluyor: {}", aggregateId);
        
        EventStore snapshot = new EventStore();
        snapshot.setAggregateId(aggregateId);
        snapshot.setEventType("SNAPSHOT");
        snapshot.setEventData(snapshotData);
        snapshot.setVersion(0L); // Snapshot version 0
        snapshot.setOccurredAt(LocalDateTime.now());
        
        eventStoreRepository.save(snapshot);
        log.info("Snapshot oluşturuldu: {}", aggregateId);
    }
}


