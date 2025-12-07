package com.fsk.transaction.microservices.service;

import com.fsk.transaction.microservices.entity.Order;
import com.fsk.transaction.microservices.entity.OutboxMessage;
import com.fsk.transaction.microservices.repository.OrderRepository;
import com.fsk.transaction.microservices.repository.OutboxMessageRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Transaction + Microservices Konuları
 * 
 * Outbox Pattern
 * Idempotency
 * Circuit Breaker Pattern
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class MicroservicesService {
    
    private final OrderRepository orderRepository;
    private final OutboxMessageRepository outboxMessageRepository;
    
    // Idempotency key store (gerçek hayatta Redis veya DB'de olur)
    private final Map<String, Boolean> processedKeys = new HashMap<>();
    
    /**
     * Outbox Pattern - Transaction içinde outbox'a yaz
     */
    @Transactional
    public Order createOrderWithOutbox(String orderNumber, Double totalAmount) {
        log.info("createOrderWithOutbox - Outbox Pattern");
        
        // Order kaydet
        Order order = new Order();
        order.setOrderNumber(orderNumber);
        order.setTotalAmount(totalAmount);
        order.setStatus("PENDING");
        order.setCreatedAt(LocalDateTime.now());
        
        Order saved = orderRepository.save(order);
        log.info("Order kaydedildi: {}", saved.getId());
        
        // Outbox'a message yaz (aynı transaction içinde)
        OutboxMessage outboxMessage = new OutboxMessage();
        outboxMessage.setAggregateType("Order");
        outboxMessage.setAggregateId(saved.getId().toString());
        outboxMessage.setEventType("OrderCreated");
        outboxMessage.setPayload("{\"orderId\":" + saved.getId() + ",\"orderNumber\":\"" + orderNumber + "\"}");
        outboxMessage.setCreatedAt(LocalDateTime.now());
        outboxMessage.setStatus("PENDING");
        
        outboxMessageRepository.save(outboxMessage);
        log.info("Outbox message kaydedildi: {}", outboxMessage.getId());
        
        // Transaction commit olduktan sonra outbox processor message'ı gönderecek
        return saved;
    }
    
    /**
     * Idempotent operation - Aynı işlem birden fazla kez çağrılsa bile sadece bir kez çalışır
     */
    @Transactional
    public Order createOrderIdempotent(String idempotencyKey, String orderNumber, Double totalAmount) {
        log.info("createOrderIdempotent - Idempotent operation");
        
        // Idempotency key kontrolü
        if (processedKeys.containsKey(idempotencyKey)) {
            log.info("Idempotency key zaten işlenmiş: {}", idempotencyKey);
            throw new RuntimeException("Bu işlem zaten yapılmış: " + idempotencyKey);
        }
        
        // Order kaydet
        Order order = new Order();
        order.setOrderNumber(orderNumber);
        order.setTotalAmount(totalAmount);
        order.setStatus("PENDING");
        order.setCreatedAt(LocalDateTime.now());
        
        Order saved = orderRepository.save(order);
        log.info("Order kaydedildi: {}", saved.getId());
        
        // Idempotency key'i işaretle
        processedKeys.put(idempotencyKey, true);
        log.info("Idempotency key işlendi: {}", idempotencyKey);
        
        return saved;
    }
    
    /**
     * Outbox message'ları işle (Scheduled job tarafından çağrılır)
     */
    @Transactional
    public void processOutboxMessages() {
        log.info("processOutboxMessages - Outbox message'ları işleniyor");
        
        List<OutboxMessage> pendingMessages = outboxMessageRepository.findByStatusOrderByCreatedAtAsc("PENDING");
        
        for (OutboxMessage message : pendingMessages) {
            try {
                // Message'ı gönder (RabbitMQ, Kafka, etc.)
                sendMessage(message);
                
                message.setStatus("PROCESSED");
                message.setProcessedAt(LocalDateTime.now());
                outboxMessageRepository.save(message);
                
                log.info("Outbox message işlendi: {}", message.getId());
            } catch (Exception e) {
                log.error("Outbox message işlenirken hata: {}", message.getId(), e);
                message.setStatus("FAILED");
                outboxMessageRepository.save(message);
            }
        }
    }
    
    private void sendMessage(OutboxMessage message) {
        log.info("Message gönderiliyor: {}", message.getEventType());
        // Gerçek implementasyon: rabbitTemplate.send(...)
    }
}



