package com.fsk.transaction.cqrs.service;

import com.fsk.transaction.cqrs.entity.OrderCommand;
import com.fsk.transaction.cqrs.entity.OrderReadModel;
import com.fsk.transaction.cqrs.repository.OrderCommandRepository;
import com.fsk.transaction.cqrs.repository.OrderReadModelRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

/**
 * Transaction + CQRS Konuları
 * 
 * Command/Query Separation
 * Read/Write Models
 * Eventual Consistency
 * Read Model Synchronization
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class CqrsService {
    
    private final OrderCommandRepository commandRepository;
    private final OrderReadModelRepository readModelRepository;
    private final ApplicationEventPublisher eventPublisher;
    
    /**
     * Command Handler - Write Model (Transaction içinde)
     */
    @Transactional
    public OrderCommand createOrderCommand(String orderNumber, Double totalAmount) {
        log.info("createOrderCommand - Command handler (Write Model)");
        
        OrderCommand order = new OrderCommand();
        order.setOrderNumber(orderNumber);
        order.setTotalAmount(totalAmount);
        order.setStatus("PENDING");
        order.setCreatedAt(LocalDateTime.now());
        
        OrderCommand saved = commandRepository.save(order);
        log.info("Order command kaydedildi: {}", saved.getId());
        
        // Event yayınla - Read model güncellenecek (eventual consistency)
        eventPublisher.publishEvent(new OrderCreatedEvent(saved.getId(), orderNumber, totalAmount));
        
        return saved;
    }
    
    /**
     * Query Handler - Read Model (Read-only transaction)
     */
    @Transactional(readOnly = true)
    public OrderReadModel getOrderReadModel(Long id) {
        log.info("getOrderReadModel - Query handler (Read Model)");
        
        return readModelRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Order read model bulunamadı"));
    }
    
    /**
     * Read Model Synchronization (Event listener)
     */
    @org.springframework.transaction.event.TransactionalEventListener(phase = org.springframework.transaction.event.TransactionPhase.AFTER_COMMIT)
    public void synchronizeReadModel(OrderCreatedEvent event) {
        log.info("synchronizeReadModel - Read model senkronize ediliyor (Eventual Consistency)");
        
        OrderReadModel readModel = new OrderReadModel();
        readModel.setOrderNumber(event.getOrderNumber());
        readModel.setTotalAmount(event.getTotalAmount());
        readModel.setStatus("PENDING");
        readModel.setCreatedAt(LocalDateTime.now());
        readModel.setCustomerName("Customer Name"); // Read model'e özel alan
        readModel.setCustomerEmail("customer@example.com");
        
        readModelRepository.save(readModel);
        log.info("Read model senkronize edildi");
    }
    
    public static class OrderCreatedEvent {
        private final Long orderId;
        private final String orderNumber;
        private final Double totalAmount;
        
        public OrderCreatedEvent(Long orderId, String orderNumber, Double totalAmount) {
            this.orderId = orderId;
            this.orderNumber = orderNumber;
            this.totalAmount = totalAmount;
        }
        
        public Long getOrderId() {
            return orderId;
        }
        
        public String getOrderNumber() {
            return orderNumber;
        }
        
        public Double getTotalAmount() {
            return totalAmount;
        }
    }
}



