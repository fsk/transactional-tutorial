package com.fsk.transaction.monitoring.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "transaction_metrics")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class TransactionMetrics {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false)
    private String transactionName;
    
    @Column(nullable = false)
    private Long durationMillis;
    
    @Column(nullable = false)
    private Boolean success;
    
    @Column(nullable = false)
    private LocalDateTime timestamp;
    
    private String errorMessage;
}



