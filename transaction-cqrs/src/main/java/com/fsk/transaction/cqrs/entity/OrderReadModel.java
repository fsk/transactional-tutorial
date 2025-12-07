package com.fsk.transaction.cqrs.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "orders_read_model")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class OrderReadModel {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false)
    private String orderNumber;
    
    @Column(nullable = false)
    private Double totalAmount;
    
    private String status;
    
    @Column(nullable = false)
    private LocalDateTime createdAt;
    
    // Read model'e özel alanlar
    private String customerName;
    private String customerEmail;
}



