package com.fsk.transaction.cqrs.controller;

import com.fsk.transaction.cqrs.entity.OrderCommand;
import com.fsk.transaction.cqrs.entity.OrderReadModel;
import com.fsk.transaction.cqrs.service.CqrsService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/cqrs")
@RequiredArgsConstructor
public class CqrsController {
    
    private final CqrsService cqrsService;
    
    /**
     * Command Handler - Write Model
     */
    @PostMapping("/order")
    public ResponseEntity<OrderCommand> createOrder(@RequestBody OrderRequest request) {
        OrderCommand order = cqrsService.createOrderCommand(request.getOrderNumber(), request.getTotalAmount());
        return ResponseEntity.ok(order);
    }
    
    /**
     * Query Handler - Read Model
     */
    @GetMapping("/order/{id}")
    public ResponseEntity<OrderReadModel> getOrder(@PathVariable Long id) {
        OrderReadModel order = cqrsService.getOrderReadModel(id);
        return ResponseEntity.ok(order);
    }
    
    // DTO
    public record OrderRequest(String orderNumber, Double totalAmount) {}
}


