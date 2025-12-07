package com.fsk.transaction.advanced.repository;

import com.fsk.transaction.advanced.entity.EmployeeWithVersion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface EmployeeWithVersionRepository extends JpaRepository<EmployeeWithVersion, Long> {
}


