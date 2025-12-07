package com.fsk.transaction.security.controller;

import com.fsk.transaction.security.entity.Employee;
import com.fsk.transaction.security.service.SecurityService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/security")
@RequiredArgsConstructor
public class SecurityController {
    
    private final SecurityService securityService;
    
    /**
     * Audit trail ile employee oluşturma
     */
    @PostMapping("/employee")
    public ResponseEntity<Employee> createEmployeeWithAudit(@RequestBody EmployeeRequest request) {
        Employee employee = securityService.createEmployeeWithAudit(
            request.getName(),
            request.getEmail(),
            request.getDepartment(),
            request.getSalary(),
            request.getUserId()
        );
        return ResponseEntity.ok(employee);
    }
    
    /**
     * Audit trail ile employee güncelleme
     */
    @PutMapping("/employee/{id}")
    public ResponseEntity<Employee> updateEmployeeWithAudit(
            @PathVariable Long id,
            @RequestParam Double newSalary,
            @RequestParam String userId) {
        Employee employee = securityService.updateEmployeeWithAudit(id, newSalary, userId);
        return ResponseEntity.ok(employee);
    }
    
    // DTO
    public record EmployeeRequest(String name, String email, String department, Double salary, String userId) {}
}



