package com.fsk.transaction.distributed.controller;

import com.fsk.transaction.distributed.service.SagaService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/distributed")
@RequiredArgsConstructor
public class DistributedController {
    
    private final SagaService sagaService;
    
    /**
     * Saga Orchestration Pattern testi
     * 
     * curl -X POST http://localhost:8093/api/distributed/saga \
     *   -H "Content-Type: application/json" \
     *   -d '{"orderNumber":"ORD-001","amount":1000.0}'
     */
    @PostMapping("/saga")
    public ResponseEntity<String> executeSaga(@RequestBody SagaRequest request) {
        try {
            String sagaId = sagaService.executeSagaOrchestration(request.getOrderNumber(), request.getAmount());
            return ResponseEntity.ok("Saga başarıyla tamamlandı: " + sagaId);
        } catch (Exception e) {
            return ResponseEntity.ok("Saga başarısız oldu, compensation yapıldı: " + e.getMessage());
        }
    }
    
    /**
     * Saga Compensation testi
     * 
     * curl -X POST http://localhost:8093/api/distributed/compensate/{sagaId}
     */
    @PostMapping("/compensate/{sagaId}")
    public ResponseEntity<String> compensateSaga(@PathVariable String sagaId) {
        sagaService.compensateSaga(sagaId);
        return ResponseEntity.ok("Saga compensation tamamlandı: " + sagaId);
    }
    
    // DTO
    public record SagaRequest(String orderNumber, Double amount) {}
}



