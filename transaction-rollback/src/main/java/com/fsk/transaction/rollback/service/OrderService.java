package com.fsk.transaction.rollback.service;

import com.fsk.transaction.rollback.entity.Order;
import com.fsk.transaction.rollback.exception.CustomCheckedException;
import com.fsk.transaction.rollback.exception.CustomUncheckedException;
import com.fsk.transaction.rollback.exception.NoRollbackException;
import com.fsk.transaction.rollback.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Rollback ne zaman olur?
 * <p>
 * ✅ RuntimeException, Error → rollback
 * ❌ Checked Exception → rollback olmaz (default)
 * 
 * @Transactional(rollbackFor = Exception.class) → Tüm exception'larda rollback
 * @Transactional(noRollbackFor = CustomException.class) → Belirli exception'da rollback olmaz
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class OrderService {
    
    private final OrderRepository orderRepository;
    
    /**
     * DEFAULT DAVRANIŞ: RuntimeException → rollback
     * <p>
     * Order kaydedilir ama sonra RuntimeException fırlatılır
     * Transaction rollback olur, order DB'ye gitmez
     */
    @Transactional
    public void createOrderWithRuntimeException(String orderNumber, Double amount) throws CustomUncheckedException {
        Order order = new Order();
        order.setOrderNumber(orderNumber);
        order.setTotalAmount(amount);
        order.setStatus("PENDING");
        
        Order saved = orderRepository.save(order);
        log.info("Order saved with ID: {} (NOTE: ID assigned but not yet committed!)", saved.getId());
        
        // RuntimeException fırlatılıyor → ROLLBACK
        log.error("RuntimeException thrown - Transaction ROLLBACK will occur!");
        throw new CustomUncheckedException("RuntimeException - Transaction ROLLBACK olacak!");
    }
    
    /**
     * DEFAULT DAVRANIŞ: Checked Exception → rollback OLMAZ
     * <p>
     * Order kaydedilir ve commit edilir (checked exception olsa bile)
     */
    @Transactional
    public void createOrderWithCheckedException(String orderNumber, Double amount) throws CustomCheckedException {
        Order order = new Order();
        order.setOrderNumber(orderNumber);
        order.setTotalAmount(amount);
        order.setStatus("PENDING");
        
        Order saved = orderRepository.save(order);
        log.info("Order saved with ID: {} (NOTE: ID assigned but not yet committed!)", saved.getId());
        
        // Checked Exception fırlatılıyor → ROLLBACK OLMAZ (default)
        throw new CustomCheckedException("CheckedException - Rollback olmayacak!");
    }
    
    /**
     * ÇÖZÜM: rollbackFor ile tüm exception'larda rollback
     */
    @Transactional(rollbackFor = Exception.class)
    public void createOrderWithRollbackForAll(String orderNumber, Double amount) throws CustomCheckedException {
        Order order = new Order();
        order.setOrderNumber(orderNumber);
        order.setTotalAmount(amount);
        order.setStatus("PENDING");
        
        Order saved = orderRepository.save(order);
        log.info("Order saved with ID: {} (NOTE: ID assigned but not yet committed!)", saved.getId());
        
        // Checked Exception fırlatılıyor → ROLLBACK OLACAK (rollbackFor sayesinde)
        throw new CustomCheckedException("CheckedException - Transaction ROLLBACK olacak!");
    }
    
    /**
     * noRollbackFor: Belirli exception'da rollback olmasın
     */
    @Transactional(noRollbackFor = NoRollbackException.class)
    public void createOrderWithNoRollback(String orderNumber, Double amount) {
        Order order = new Order();
        order.setOrderNumber(orderNumber);
        order.setTotalAmount(amount);
        order.setStatus("PENDING");
        
        Order saved = orderRepository.save(order);
        log.info("Order saved with ID: {} (NOTE: ID assigned but not yet committed!)", saved.getId());
        
        // NoRollbackException fırlatılıyor → ROLLBACK OLMAYACAK
        throw new NoRollbackException("NoRollbackException - Transaction ROLLBACK olmayacak!");
    }
    
    /**
     * Normal başarılı işlem
     */
    @Transactional
    public Order createOrderSuccessfully(String orderNumber, Double amount) {
        Order order = new Order();
        order.setOrderNumber(orderNumber);
        order.setTotalAmount(amount);
        order.setStatus("PENDING");
        
        Order saved = orderRepository.save(order);
        log.info("Order saved with ID: {} (NOTE: ID assigned but not yet committed!)", saved.getId());
        
        return saved;
    }
}


