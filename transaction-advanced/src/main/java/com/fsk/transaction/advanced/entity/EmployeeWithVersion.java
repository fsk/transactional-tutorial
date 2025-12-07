package com.fsk.transaction.advanced.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Optimistic locking için @Version kullanımı
 */
@Entity
@Table(name = "employees_with_version")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class EmployeeWithVersion {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false)
    private String name;
    
    @Column(nullable = false)
    private String email;
    
    private Double salary;
    
    @Version
    private Long version; // Optimistic locking için
}



