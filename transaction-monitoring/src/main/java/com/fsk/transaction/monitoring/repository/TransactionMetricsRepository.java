package com.fsk.transaction.monitoring.repository;

import com.fsk.transaction.monitoring.entity.TransactionMetrics;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TransactionMetricsRepository extends JpaRepository<TransactionMetrics, Long> {
}



