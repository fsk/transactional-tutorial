package com.fsk.transaction.selfinvocation.repository;

import com.fsk.transaction.selfinvocation.entity.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ProductRepository extends JpaRepository<Product, Long> {
}



