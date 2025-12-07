package com.fsk.transaction.propagation.repository;

import com.fsk.transaction.propagation.entity.Account;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AccountRepository extends JpaRepository<Account, Long> {
}



