package com.fsk.transaction.distributed.service;

import com.fsk.transaction.distributed.entity.Order;
import com.fsk.transaction.distributed.entity.SagaStep;
import com.fsk.transaction.distributed.repository.OrderRepository;
import com.fsk.transaction.distributed.repository.SagaStepRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

/**
 * Saga Pattern Implementation
 * 
 * Distributed transaction problemi için Saga pattern kullanımı
 * - Choreography: Her servis kendi event'ini yayınlar
 * - Orchestration: Merkezi orchestrator tüm adımları yönetir
 * - Compensation: Her adım için rollback mekanizması
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class SagaService {
    
    private final OrderRepository orderRepository;
    private final SagaStepRepository sagaStepRepository;
    
    /**
     * Saga Orchestration Pattern
     * Merkezi orchestrator tüm adımları yönetir
     */
    @Transactional
    public String executeSagaOrchestration(String orderNumber, Double amount) {
        String sagaId = UUID.randomUUID().toString();
        log.info("Saga başlatıldı: {}", sagaId);
        
        try {
            // Step 1: Create Order
            Order order = createOrder(sagaId, orderNumber, amount);
            recordSagaStep(sagaId, "CREATE_ORDER", "COMPLETED", null);
            
            // Step 2: Reserve Inventory (simulated)
            recordSagaStep(sagaId, "RESERVE_INVENTORY", "COMPLETED", null);
            
            // Step 3: Process Payment (simulated)
            recordSagaStep(sagaId, "PROCESS_PAYMENT", "COMPLETED", null);
            
            log.info("Saga başarıyla tamamlandı: {}", sagaId);
            return sagaId;
            
        } catch (Exception e) {
            log.error("Saga başarısız oldu, compensation başlatılıyor: {}", sagaId, e);
            compensateSaga(sagaId);
            throw new RuntimeException("Saga failed: " + sagaId, e);
        }
    }
    
    /**
     * Saga Compensation
     * Başarısız adımlar için geri alma işlemleri
     */
    @Transactional
    public void compensateSaga(String sagaId) {
        log.info("Saga compensation başlatıldı: {}", sagaId);
        
        List<SagaStep> steps = sagaStepRepository.findBySagaId(sagaId);
        
        // Ters sırada compensation
        for (int i = steps.size() - 1; i >= 0; i--) {
            SagaStep step = steps.get(i);
            if ("COMPLETED".equals(step.getStatus())) {
                log.info("Compensating step: {}", step.getStepName());
                recordSagaStep(sagaId, step.getStepName() + "_COMPENSATED", "COMPENSATED", step.getCompensationData());
            }
        }
        
        log.info("Saga compensation tamamlandı: {}", sagaId);
    }
    
    private Order createOrder(String sagaId, String orderNumber, Double amount) {
        Order order = new Order();
        order.setOrderNumber(orderNumber);
        order.setTotalAmount(amount);
        order.setStatus("PENDING");
        order.setCreatedAt(LocalDateTime.now());
        return orderRepository.save(order);
    }
    
    private void recordSagaStep(String sagaId, String stepName, String status, String compensationData) {
        SagaStep step = new SagaStep();
        step.setSagaId(sagaId);
        step.setStepName(stepName);
        step.setStatus(status);
        step.setExecutedAt(LocalDateTime.now());
        step.setCompensationData(compensationData);
        sagaStepRepository.save(step);
    }
}



