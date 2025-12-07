package com.fsk.transaction.basics.controller;

import com.fsk.transaction.basics.entity.Employee;
import com.fsk.transaction.basics.service.EmployeeService;
import com.fsk.transaction.basics.service.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/basics")
@RequiredArgsConstructor
public class BasicsController {
    
    private final EmployeeService employeeService;
    private final NotificationService notificationService;
    
    /**
     * 8. Read-only transaction testi
     * 
     * curl -X GET http://localhost:8092/api/basics/employees
     */
    @GetMapping("/employees")
    public ResponseEntity<List<Employee>> getAllEmployees() {
        List<Employee> employees = employeeService.getAllEmployees();
        return ResponseEntity.ok(employees);
    }
    
    /**
     * 9. Normal transactional write - Transaction ne zaman commit edilir?
     * 
     * curl -X POST http://localhost:8092/api/basics/employee \
     *   -H "Content-Type: application/json" \
     *   -d '{"name":"John Doe","email":"john@example.com","department":"IT","salary":5000.0}'
     */
    @PostMapping("/employee")
    public ResponseEntity<Employee> createEmployee(@RequestBody EmployeeRequest request) {
        Employee employee = employeeService.createEmployee(request.name(), request.email(), request.department(), request.salary());
        return ResponseEntity.ok(employee);
    }
    
    /**
     * 10. Transaction timeout testi
     * 
     * curl -X POST http://localhost:8092/api/basics/employee-timeout \
     *   -H "Content-Type: application/json" \
     *   -d '{"name":"Timeout Test","email":"timeout@example.com","department":"IT","salary":6000.0}'
     */
    @PostMapping("/employee-timeout")
    public ResponseEntity<String> createEmployeeWithTimeout(@RequestBody EmployeeRequest request) {
        try {
            employeeService.createEmployeeWithTimeout(request.name(), request.email(), request.department(), request.salary());
            return ResponseEntity.ok("Employee kaydedildi");
        } catch (Exception e) {
            return ResponseEntity.ok("Timeout hatası: " + e.getMessage());
        }
    }
    
    /**
     * Exception ile rollback testi
     * 
     * curl -X POST http://localhost:8092/api/basics/employee-exception \
     *   -H "Content-Type: application/json" \
     *   -d '{"name":"Exception Test","email":"exception@example.com","department":"IT","salary":9000.0}'
     */
    @PostMapping("/employee-exception")
    public ResponseEntity<String> createEmployeeWithException(@RequestBody EmployeeRequest request) {
        try {
            employeeService.createEmployeeWithException(request.name(), request.email(), request.department(), request.salary());
        } catch (RuntimeException e) {
            return ResponseEntity.ok("Exception fırlatıldı - Transaction ROLLBACK oldu! " + e.getMessage());
        }
        return ResponseEntity.ok("Beklenmeyen durum");
    }
    
    /**
     * 12. @TransactionalEventListener testi - AFTER_COMMIT
     * 
     * curl -X POST http://localhost:8092/api/basics/notification \
     *   -H "Content-Type: application/json" \
     *   -d '{"message":"Test Notification","recipient":"user@example.com"}'
     */
    @PostMapping("/notification")
    public ResponseEntity<String> sendNotification(@RequestBody NotificationRequest request) {
        notificationService.sendNotification(request.message(), request.recipient());
        return ResponseEntity.ok("Notification kaydedildi - Transaction commit olduktan sonra event dinlenecek");
    }


    public record EmployeeRequest(String name, String email, String department, Double salary) {}
    public record NotificationRequest(String message, String recipient) {}
}



