package com.fsk.transaction.isolation.service;

import com.fsk.transaction.isolation.entity.Inventory;
import com.fsk.transaction.isolation.repository.InventoryRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Transaction Isolation Level'ları
 * 
 * READ_UNCOMMITTED: Dirty read, non-repeatable read, phantom read olabilir
 * READ_COMMITTED: Dirty read önlenir, non-repeatable read ve phantom read olabilir (DEFAULT - PostgreSQL)
 * REPEATABLE_READ: Dirty read ve non-repeatable read önlenir, phantom read olabilir (MySQL default)
 * SERIALIZABLE: Tüm problemler önlenir (en yavaş)
 * 
 * PostgreSQL'de MVCC ile davranış farklıdır
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class InventoryService {
    
    private final InventoryRepository inventoryRepository;
    
    /**
     * READ_COMMITTED (DEFAULT - PostgreSQL)
     * Dirty read önlenir ama non-repeatable read ve phantom read olabilir
     */
    @Transactional(isolation = Isolation.READ_COMMITTED)
    public Inventory updateInventoryReadCommitted(Long id, Integer newQuantity) {
        log.info("updateInventoryReadCommitted - READ_COMMITTED isolation");
        log.info("Current transaction: {}", 
            org.springframework.transaction.support.TransactionSynchronizationManager.getCurrentTransactionName());
        
        Inventory inventory = inventoryRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Inventory bulunamadı"));
        
        log.info("İlk okuma - Quantity: {}", inventory.getQuantity());
        
        // Simüle edilmiş gecikme - başka bir transaction bu sırada değişiklik yapabilir
        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        
        // Tekrar okuma - non-repeatable read olabilir (READ_COMMITTED'de)
        Inventory inventory2 = inventoryRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Inventory bulunamadı"));
        log.info("İkinci okuma - Quantity: {}", inventory2.getQuantity());
        
        inventory.setQuantity(newQuantity);
        return inventoryRepository.save(inventory);
    }
    
    /**
     * REPEATABLE_READ
     * Dirty read ve non-repeatable read önlenir
     * Phantom read hala olabilir (PostgreSQL'de MVCC ile farklı davranır)
     */
    @Transactional(isolation = Isolation.REPEATABLE_READ)
    public Inventory updateInventoryRepeatableRead(Long id, Integer newQuantity) {
        log.info("updateInventoryRepeatableRead - REPEATABLE_READ isolation");
        log.info("Current transaction: {}", 
            org.springframework.transaction.support.TransactionSynchronizationManager.getCurrentTransactionName());
        
        Inventory inventory = inventoryRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Inventory bulunamadı"));
        
        log.info("İlk okuma - Quantity: {}", inventory.getQuantity());
        
        // Simüle edilmiş gecikme
        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        
        // Tekrar okuma - non-repeatable read önlenir (REPEATABLE_READ'de)
        Inventory inventory2 = inventoryRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Inventory bulunamadı"));
        log.info("İkinci okuma - Quantity: {} (Aynı olmalı - non-repeatable read önlendi)", inventory2.getQuantity());
        
        inventory.setQuantity(newQuantity);
        return inventoryRepository.save(inventory);
    }
    
    /**
     * SERIALIZABLE
     * Tüm isolation problemleri önlenir (en yavaş)
     */
    @Transactional(isolation = Isolation.SERIALIZABLE)
    public Inventory updateInventorySerializable(Long id, Integer newQuantity) {
        log.info("updateInventorySerializable - SERIALIZABLE isolation");
        log.info("Current transaction: {}", 
            org.springframework.transaction.support.TransactionSynchronizationManager.getCurrentTransactionName());
        
        Inventory inventory = inventoryRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Inventory bulunamadı"));
        
        inventory.setQuantity(newQuantity);
        return inventoryRepository.save(inventory);
    }
    
    /**
     * Phantom read testi için
     * READ_COMMITTED'de phantom read olabilir
     */
    @Transactional(isolation = Isolation.READ_COMMITTED)
    public List<Inventory> getInventoriesByProductName(String productName) {
        log.info("getInventoriesByProductName - READ_COMMITTED (Phantom read testi)");
        
        List<Inventory> firstRead = inventoryRepository.findAll()
            .stream()
            .filter(i -> i.getProductName().equals(productName))
            .toList();
        
        log.info("İlk okuma - Bulunan kayıt sayısı: {}", firstRead.size());
        
        // Simüle edilmiş gecikme - başka bir transaction yeni kayıt ekleyebilir
        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        
        // Tekrar okuma - phantom read olabilir (READ_COMMITTED'de)
        List<Inventory> secondRead = inventoryRepository.findAll()
            .stream()
            .filter(i -> i.getProductName().equals(productName))
            .toList();
        
        log.info("İkinci okuma - Bulunan kayıt sayısı: {} (Phantom read olabilir)", secondRead.size());
        
        return secondRead;
    }
    
    /**
     * REPEATABLE_READ ile phantom read testi
     * PostgreSQL'de MVCC ile phantom read önlenir
     */
    @Transactional(isolation = Isolation.REPEATABLE_READ)
    public List<Inventory> getInventoriesByProductNameRepeatableRead(String productName) {
        log.info("getInventoriesByProductNameRepeatableRead - REPEATABLE_READ (Phantom read önleme)");
        
        List<Inventory> firstRead = inventoryRepository.findAll()
            .stream()
            .filter(i -> i.getProductName().equals(productName))
            .toList();
        
        log.info("İlk okuma - Bulunan kayıt sayısı: {}", firstRead.size());
        
        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        
        // PostgreSQL MVCC ile phantom read önlenir
        List<Inventory> secondRead = inventoryRepository.findAll()
            .stream()
            .filter(i -> i.getProductName().equals(productName))
            .toList();
        
        log.info("İkinci okuma - Bulunan kayıt sayısı: {} (PostgreSQL MVCC ile phantom read önlendi)", secondRead.size());
        
        return secondRead;
    }
    
    /**
     * Yeni inventory ekleme
     */
    @Transactional
    public Inventory createInventory(String productName, Integer quantity, Double price) {
        log.info("createInventory - Yeni kayıt ekleniyor");
        Inventory inventory = new Inventory();
        inventory.setProductName(productName);
        inventory.setQuantity(quantity);
        inventory.setPrice(price);
        return inventoryRepository.save(inventory);
    }
}


