package com.fsk.transaction.isolation.repository;

import com.fsk.transaction.isolation.entity.Inventory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface InventoryRepository extends JpaRepository<Inventory, Long> {
    
    @Query("SELECT SUM(i.quantity) FROM Inventory i WHERE i.productName = :productName")
    Integer sumQuantityByProductName(@Param("productName") String productName);
}



