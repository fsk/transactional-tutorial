package com.fsk.transaction.isolation.controller;

import com.fsk.transaction.isolation.entity.Inventory;
import com.fsk.transaction.isolation.service.InventoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/isolation")
@RequiredArgsConstructor
public class IsolationController {
    
    private final InventoryService inventoryService;
    
    /**
     * READ_COMMITTED testi - Non-repeatable read olabilir
     * <p>
     * curl -X PUT <a href="http://localhost:8085/api/isolation/read-committed/1?quantity=50">...</a>
     */
    @PutMapping("/read-committed/{id}")
    public ResponseEntity<Inventory> testReadCommitted(@PathVariable Long id, @RequestParam Integer quantity) {
        Inventory inventory = inventoryService.updateInventoryReadCommitted(id, quantity);
        return ResponseEntity.ok(inventory);
    }
    
    /**
     * REPEATABLE_READ testi - Non-repeatable read önlenir
     * <p>
     * curl -X PUT <a href="http://localhost:8085/api/isolation/repeatable-read/1?quantity=75">...</a>
     */
    @PutMapping("/repeatable-read/{id}")
    public ResponseEntity<Inventory> testRepeatableRead(@PathVariable Long id, @RequestParam Integer quantity) {
        Inventory inventory = inventoryService.updateInventoryRepeatableRead(id, quantity);
        return ResponseEntity.ok(inventory);
    }
    
    /**
     * SERIALIZABLE testi - Tüm problemler önlenir
     * <p>
     * curl -X PUT <a href="http://localhost:8085/api/isolation/serializable/1?quantity=100">...</a>
     */
    @PutMapping("/serializable/{id}")
    public ResponseEntity<Inventory> testSerializable(@PathVariable Long id, @RequestParam Integer quantity) {
        Inventory inventory = inventoryService.updateInventorySerializable(id, quantity);
        return ResponseEntity.ok(inventory);
    }
    
    /**
     * Phantom read testi - READ_COMMITTED
     * <p>
     * curl -X GET "<a href="http://localhost:8085/api/isolation/phantom-read-read-committed?productName=Laptop">...</a>"
     */
    @GetMapping("/phantom-read-read-committed")
    public ResponseEntity<List<Inventory>> testPhantomReadReadCommitted(@RequestParam String productName) {
        List<Inventory> inventories = inventoryService.getInventoriesByProductName(productName);
        return ResponseEntity.ok(inventories);
    }
    
    /**
     * Phantom read testi - REPEATABLE_READ (PostgreSQL MVCC)
     * <p>
     * curl -X GET "<a href="http://localhost:8085/api/isolation/phantom-read-repeatable-read?productName=Laptop">...</a>"
     */
    @GetMapping("/phantom-read-repeatable-read")
    public ResponseEntity<List<Inventory>> testPhantomReadRepeatableRead(@RequestParam String productName) {
        List<Inventory> inventories = inventoryService.getInventoriesByProductNameRepeatableRead(productName);
        return ResponseEntity.ok(inventories);
    }
    
    /**
     * Yeni inventory ekleme
     * <p>
     * curl -X POST <a href="http://localhost:8085/api/isolation/create">...</a> \
     *   -H "Content-Type: application/json" \
     *   -d '{"productName":"Laptop","quantity":10,"price":999.99}'
     */
    @PostMapping("/create")
    public ResponseEntity<Inventory> createInventory(@RequestBody InventoryRequest request) {
        Inventory inventory = inventoryService.createInventory(request.productName(), request.quantity(), request.price());
        return ResponseEntity.ok(inventory);
    }
    
    // DTO
    public record InventoryRequest(String productName, Integer quantity, Double price) {}
}


