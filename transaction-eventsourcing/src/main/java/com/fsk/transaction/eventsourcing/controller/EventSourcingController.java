package com.fsk.transaction.eventsourcing.controller;

import com.fsk.transaction.eventsourcing.entity.EventStore;
import com.fsk.transaction.eventsourcing.service.EventSourcingService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/eventsourcing")
@RequiredArgsConstructor
public class EventSourcingController {
    
    private final EventSourcingService eventSourcingService;
    
    /**
     * Event Store'a event kaydetme
     */
    @PostMapping("/event")
    public ResponseEntity<String> appendEvent(@RequestBody EventRequest request) {
        eventSourcingService.appendEvent(request.getAggregateId(), request.getEventType(), request.getEventData());
        return ResponseEntity.ok("Event kaydedildi");
    }
    
    /**
     * Event Replay
     */
    @GetMapping("/replay/{aggregateId}")
    public ResponseEntity<List<EventStore>> replayEvents(@PathVariable String aggregateId) {
        List<EventStore> events = eventSourcingService.replayEvents(aggregateId);
        return ResponseEntity.ok(events);
    }
    
    /**
     * Snapshot oluşturma
     */
    @PostMapping("/snapshot")
    public ResponseEntity<String> createSnapshot(@RequestBody SnapshotRequest request) {
        eventSourcingService.createSnapshot(request.getAggregateId(), request.getSnapshotData());
        return ResponseEntity.ok("Snapshot oluşturuldu");
    }
    
    // DTOs
    public record EventRequest(String aggregateId, String eventType, String eventData) {}
    public record SnapshotRequest(String aggregateId, String snapshotData) {}
}



