package com.fsk.transaction.rollback.controller;

import com.fsk.transaction.rollback.entity.Order;
import com.fsk.transaction.rollback.exception.CustomCheckedException;
import com.fsk.transaction.rollback.service.OrderService;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/rollback")
@RequiredArgsConstructor
public class RollbackController {
    
    private final OrderService orderService;
    
    /**
     * RuntimeException → Rollback olur
     * 
     * curl -X POST http://localhost:8083/api/rollback/runtime-exception \
     *   -H "Content-Type: application/json" \
     *   -d '{"orderNumber":"ORD-001","amount":100.0}'
     */
    @PostMapping("/runtime-exception")
    public ResponseEntity<String> testRuntimeException(@RequestBody OrderRequest request) {
        try {
            orderService.createOrderWithRuntimeException(request.getOrderNumber(), request.getAmount());
        } catch (Exception e) {
            return ResponseEntity.ok("RuntimeException thrown - Transaction ROLLBACK occurred! Exception: " + e.getMessage());
        }
        return ResponseEntity.ok("Unexpected situation");
    }
    
    /**
     * Checked Exception → Rollback olmaz (default)
     * 
     * curl -X POST http://localhost:8083/api/rollback/checked-exception \
     *   -H "Content-Type: application/json" \
     *   -d '{"orderNumber":"ORD-002","amount":200.0}'
     */
    @PostMapping("/checked-exception")
    public ResponseEntity<String> testCheckedException(@RequestBody OrderRequest request) {
        try {
            orderService.createOrderWithCheckedException(request.getOrderNumber(), request.getAmount());
        } catch (CustomCheckedException e) {
            return ResponseEntity.ok("CheckedException thrown - Transaction COMMIT occurred (no rollback)! Exception: " + e.getMessage());
        }
        return ResponseEntity.ok("Unexpected situation");
    }
    
    /**
     * rollbackFor = Exception.class → Tüm exception'larda rollback
     * 
     * curl -X POST http://localhost:8083/api/rollback/rollback-for-all \
     *   -H "Content-Type: application/json" \
     *   -d '{"orderNumber":"ORD-003","amount":300.0}'
     */
    @PostMapping("/rollback-for-all")
    public ResponseEntity<String> testRollbackForAll(@RequestBody OrderRequest request) {
        try {
            orderService.createOrderWithRollbackForAll(request.getOrderNumber(), request.getAmount());
        } catch (CustomCheckedException e) {
            return ResponseEntity.ok("CheckedException thrown but rollbackFor ensures ROLLBACK occurred! Exception: " + e.getMessage());
        }
        return ResponseEntity.ok("Unexpected situation");
    }
    
    /**
     * noRollbackFor → Belirli exception'da rollback olmasın
     * 
     * curl -X POST http://localhost:8083/api/rollback/no-rollback \
     *   -H "Content-Type: application/json" \
     *   -d '{"orderNumber":"ORD-004","amount":400.0}'
     */
    @PostMapping("/no-rollback")
    public ResponseEntity<String> testNoRollback(@RequestBody OrderRequest request) {
        try {
            orderService.createOrderWithNoRollback(request.getOrderNumber(), request.getAmount());
        } catch (Exception e) {
            return ResponseEntity.ok("NoRollbackException thrown - Transaction COMMIT occurred (no rollback)! Exception: " + e.getMessage());
        }
        return ResponseEntity.ok("Unexpected situation");
    }
    
    /**
     * Başarılı işlem
     * 
     * curl -X POST http://localhost:8083/api/rollback/success \
     *   -H "Content-Type: application/json" \
     *   -d '{"orderNumber":"ORD-005","amount":500.0}'
     */
    @PostMapping("/success")
    public ResponseEntity<Order> testSuccess(@RequestBody OrderRequest request) {
        Order order = orderService.createOrderSuccessfully(request.getOrderNumber(), request.getAmount());
        return ResponseEntity.ok(order);
    }
    
    // DTO
    @Setter
    @Getter
    public static class OrderRequest {
        private String orderNumber;
        private Double amount;

    }
}


