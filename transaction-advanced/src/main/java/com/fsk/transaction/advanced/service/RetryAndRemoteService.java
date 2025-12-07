package com.fsk.transaction.advanced.service;

import com.fsk.transaction.advanced.entity.Employee;
import com.fsk.transaction.advanced.repository.EmployeeRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.time.Instant;

/**
 * 41. Retry + Transaction birlikte nasıl tasarlanır?
 * 
 * Transaction fail olurken retry nasıl yapılmalı?
 * ✅ Retry transaction dışında
 * ❌ Transaction içinde loop yapılmaz
 * 
 * 42. Transaction içinde remote call neden yasaktır?
 * 
 * REST / gRPC çağrısı transaction içindeyse risk nedir?
 * ✅ Lock leak
 * ✅ Timeout propagation
 * ✅ Partial failure
 * ✅ Unbounded transaction süresi
 * 
 * Golden rule: Transaction boundary = DB only
 * 
 * 44. Transaction + Clock (time-sensitive logic)
 * 
 * Transaction uzun sürerse time-based logic neden bozulur?
 * ✅ now() drift
 * ✅ Compare sonucu değişir
 * ✅ Deadline logic çöker
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class RetryAndRemoteService {
    
    private final EmployeeRepository employeeRepository;
    
    /**
     * YANLIŞ: Transaction içinde retry loop
     */
    @Transactional
    public Employee retryInsideTransaction(Long id, Double newSalary) {
        log.info("retryInsideTransaction - YANLIŞ: Transaction içinde retry");
        
        int maxRetries = 3;
        int attempt = 0;
        
        while (attempt < maxRetries) {
            try {
                Employee employee = employeeRepository.findById(id)
                    .orElseThrow(() -> new RuntimeException("Employee bulunamadı"));
                
                employee.setSalary(newSalary);
                return employeeRepository.save(employee);
            } catch (Exception e) {
                attempt++;
                log.warn("Retry attempt {} failed", attempt);
                if (attempt >= maxRetries) {
                    throw new RuntimeException("Max retries reached", e);
                }
            }
        }
        
        throw new RuntimeException("Should not reach here");
    }
    
    /**
     * DOĞRU: Retry transaction dışında
     */
    @Retryable(value = {Exception.class}, maxAttempts = 3, backoff = @Backoff(delay = 1000))
    @Transactional
    public Employee retryOutsideTransaction(Long id, Double newSalary) {
        log.info("retryOutsideTransaction - DOĞRU: Retry transaction dışında");
        
        Employee employee = employeeRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Employee bulunamadı"));
        
        employee.setSalary(newSalary);
        return employeeRepository.save(employee);
    }
    
    /**
     * YANLIŞ: Transaction içinde remote call
     */
    @Transactional
    public Employee remoteCallInsideTransaction(Long id, Double newSalary) {
        log.info("remoteCallInsideTransaction - YANLIŞ: Transaction içinde remote call");
        
        Employee employee = employeeRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Employee bulunamadı"));
        
        // YANLIŞ: Remote call transaction içinde
        try {
            HttpClient client = HttpClient.newHttpClient();
            HttpRequest request = HttpRequest.newBuilder()
                .uri(java.net.URI.create("https://httpbin.org/delay/5"))
                .timeout(Duration.ofSeconds(10))
                .build();
            
            // Bu çağrı transaction süresini uzatır
            // Lock leak riski
            // Timeout propagation
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            log.info("Remote call completed: {}", response.statusCode());
        } catch (Exception e) {
            log.error("Remote call failed", e);
            throw new RuntimeException("Remote call failed", e);
        }
        
        employee.setSalary(newSalary);
        return employeeRepository.save(employee);
    }
    
    /**
     * DOĞRU: Remote call transaction dışında
     */
    @Transactional
    public Employee remoteCallOutsideTransaction(Long id, Double newSalary) {
        log.info("remoteCallOutsideTransaction - DOĞRU: Remote call transaction dışında");
        
        Employee employee = employeeRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Employee bulunamadı"));
        
        employee.setSalary(newSalary);
        Employee saved = employeeRepository.save(employee);
        
        // Remote call transaction dışında yapılmalı
        // AFTER_COMMIT event ile veya ayrı service'te
        log.info("Transaction commit oldu - Remote call şimdi yapılabilir");
        
        return saved;
    }
    
    /**
     * Time-sensitive logic problemi
     */
    @Transactional
    public Employee timeSensitiveLogicProblem(Long id, Double newSalary) {
        log.info("timeSensitiveLogicProblem - Time-sensitive logic problemi");
        
        Instant startTime = Instant.now();
        log.info("Transaction başladı: {}", startTime);
        
        Employee employee = employeeRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Employee bulunamadı"));
        
        // Simüle edilmiş uzun işlem
        try {
            Thread.sleep(5000);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        
        Instant currentTime = Instant.now();
        log.info("Şu anki zaman: {}", currentTime);
        log.info("Geçen süre: {} ms", Duration.between(startTime, currentTime).toMillis());
        
        // Problem: now() drift
        // Transaction başlangıcındaki zaman ile şimdiki zaman farklı
        // Time-based logic bozulabilir
        
        employee.setSalary(newSalary);
        return employeeRepository.save(employee);
    }
}


