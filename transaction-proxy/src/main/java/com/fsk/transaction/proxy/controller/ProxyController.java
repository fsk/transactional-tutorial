package com.fsk.transaction.proxy.controller;

import com.fsk.transaction.proxy.entity.User;
import com.fsk.transaction.proxy.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/proxy")
@RequiredArgsConstructor
public class ProxyController {
    
    private final UserService userService;
    
    /**
     * Test endpoint: @Transactional ile proxy üzerinden çağrı
     * 
     * curl -X POST http://localhost:8081/api/proxy/with-transaction \
     *   -H "Content-Type: application/json" \
     *   -d '{"name":"Test User","email":"test@example.com","age":25}'
     */
    @PostMapping("/with-transaction")
    public ResponseEntity<User> createWithTransaction(@RequestBody CreateUserRequest request) {
        User user = userService.createUser(request.name(), request.email(), request.age());
        return ResponseEntity.ok(user);
    }
    
    /**
     * Test endpoint: Transaction olmadan çağrı
     * 
     * curl -X POST http://localhost:8081/api/proxy/without-transaction \
     *   -H "Content-Type: application/json" \
     *   -d '{"name":"Test User 2","email":"test2@example.com","age":30}'
     */
    @PostMapping("/without-transaction")
    public ResponseEntity<User> createWithoutTransaction(@RequestBody CreateUserRequest request) {
        User user = userService.createUserWithoutTransaction(request.name(), request.email(), request.age());
        return ResponseEntity.ok(user);
    }
    
    // DTO
    public record CreateUserRequest(String name, String email, Integer age) {}
}


