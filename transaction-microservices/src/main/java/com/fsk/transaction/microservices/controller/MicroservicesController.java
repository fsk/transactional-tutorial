package com.fsk.transaction.microservices.controller;

import com.fsk.transaction.microservices.entity.Order;
import com.fsk.transaction.microservices.service.MicroservicesService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/microservices")
@RequiredArgsConstructor
public class MicroservicesController {
    
    private final MicroservicesService microservicesService;
    
    /**
     * Outbox Pattern testi
     */
    @PostMapping("/order/outbox")
    public ResponseEntity<Order> createOrderWithOutbox(@RequestBody OrderRequest request) {
        Order order = microservicesService.createOrderWithOutbox(request.getOrderNumber(), request.getTotalAmount());
        return ResponseEntity.ok(order);
    }
    
    /**
     * Idempotent operation testi
     */
    @PostMapping("/order/idempotent")
    public ResponseEntity<Order> createOrderIdempotent(@RequestBody IdempotentOrderRequest request) {
        Order order = microservicesService.createOrderIdempotent(
            request.getIdempotencyKey(),
            request.getOrderNumber(),
            request.getTotalAmount()
        );
        return ResponseEntity.ok(order);
    }
    
    /**
     * Outbox message'ları işle
     */
    @PostMapping("/outbox/process")
    public ResponseEntity<String> processOutboxMessages() {
        microservicesService.processOutboxMessages();
        return ResponseEntity.ok("Outbox message'ları işlendi");
    }
    
    // DTOs
    public record OrderRequest(String orderNumber, Double totalAmount) {}
    public record IdempotentOrderRequest(String idempotencyKey, String orderNumber, Double totalAmount) {}
}


