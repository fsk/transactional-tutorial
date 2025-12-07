package com.fsk.transaction.distributed.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "saga_steps")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class SagaStep {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false)
    private String sagaId;
    
    @Column(nullable = false)
    private String stepName;
    
    @Column(nullable = false)
    private String status; // PENDING, COMPLETED, FAILED, COMPENSATED
    
    private LocalDateTime executedAt;
    
    private String compensationData;
}


