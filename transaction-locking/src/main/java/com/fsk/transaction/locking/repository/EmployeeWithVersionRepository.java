package com.fsk.transaction.locking.repository;

import com.fsk.transaction.locking.entity.EmployeeWithVersion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface EmployeeWithVersionRepository extends JpaRepository<EmployeeWithVersion, Long> {
}


