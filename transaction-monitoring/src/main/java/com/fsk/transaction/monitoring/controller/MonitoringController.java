package com.fsk.transaction.monitoring.controller;

import com.fsk.transaction.monitoring.service.MonitoringService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/monitoring")
@RequiredArgsConstructor
public class MonitoringController {
    
    private final MonitoringService monitoringService;
    
    /**
     * Transaction performance monitoring
     */
    @PostMapping("/operation/{operationName}")
    public ResponseEntity<String> performMonitoredOperation(@PathVariable String operationName) {
        try {
            monitoringService.performMonitoredOperation(operationName);
            return ResponseEntity.ok("Operation tamamlandı: " + operationName);
        } catch (Exception e) {
            return ResponseEntity.ok("Operation başarısız: " + e.getMessage());
        }
    }
}


