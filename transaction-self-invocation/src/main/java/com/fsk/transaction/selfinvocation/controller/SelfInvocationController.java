package com.fsk.transaction.selfinvocation.controller;

import com.fsk.transaction.selfinvocation.service.ProductInnerService;
import com.fsk.transaction.selfinvocation.service.ProductService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/self-invocation")
@RequiredArgsConstructor
public class SelfInvocationController {
    
    private final ProductService productService;
    private final ProductInnerService productInnerService;
    
    /**
     * PROBLEM: Self-invocation - Transaction çalışmaz
     * 
     * curl -X POST http://localhost:8082/api/self-invocation/problem \
     *   -H "Content-Type: application/json" \
     *   -d '{"name":"Problem Product","price":99.99}'
     */
    @PostMapping("/problem")
    public ResponseEntity<String> testProblem(@RequestBody ProductRequest request) {
        productService.outerMethod(request.name(), request.price());
        return ResponseEntity.ok("Problem test edildi - Logları kontrol et!");
    }
    
    /**
     * ÇÖZÜM 1: @Transactional'ı outer metoda koymak
     * 
     * curl -X POST http://localhost:8082/api/self-invocation/solution1 \
     *   -H "Content-Type: application/json" \
     *   -d '{"name":"Solution1 Product","price":199.99}'
     */
    @PostMapping("/solution1")
    public ResponseEntity<String> testSolution1(@RequestBody ProductRequest request) {
        productService.outerMethodWithTransaction(request.name(), request.price());
        return ResponseEntity.ok("Çözüm 1 test edildi - Transaction çalışıyor!");
    }
    
    /**
     * ÇÖZÜM 2: ApplicationContext üzerinden proxy çağrısı
     * 
     * curl -X POST http://localhost:8082/api/self-invocation/solution2 \
     *   -H "Content-Type: application/json" \
     *   -d '{"name":"Solution2 Product","price":299.99}'
     */
    @PostMapping("/solution2")
    public ResponseEntity<String> testSolution2(@RequestBody ProductRequest request) {
        productService.outerMethodWithProxyCall(request.name(), request.price());
        return ResponseEntity.ok("Çözüm 2 test edildi - Proxy üzerinden çağrı!");
    }
    
    /**
     * ÇÖZÜM 3: Ayrı service'e taşımak (EN İYİ)
     * 
     * curl -X POST http://localhost:8082/api/self-invocation/solution3 \
     *   -H "Content-Type: application/json" \
     *   -d '{"name":"Solution3 Product","price":399.99}'
     */
    @PostMapping("/solution3")
    public ResponseEntity<String> testSolution3(@RequestBody ProductRequest request) {
        productInnerService.saveProduct(request.name(), request.price());
        return ResponseEntity.ok("Çözüm 3 test edildi - Ayrı service (EN İYİ ÇÖZÜM)!");
    }
    
    // DTO
    public record ProductRequest(String name, Double price) {}
}



