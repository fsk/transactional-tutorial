package com.fsk.transaction.isolation.service;

import com.fsk.transaction.isolation.entity.Inventory;
import com.fsk.transaction.isolation.repository.InventoryRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.util.List;

/**
 * Transaction Isolation Level'ları
 * <p>
 * READ_UNCOMMITTED: Dirty read, non-repeatable read, phantom read olabilir
 * READ_COMMITTED: Dirty read önlenir, non-repeatable read ve phantom read olabilir (DEFAULT - PostgreSQL)
 * REPEATABLE_READ: Dirty read ve non-repeatable read önlenir, phantom read olabilir (MySQL default)
 * SERIALIZABLE: Tüm problemler önlenir (en yavaş)
 * </p>
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
     * 
     * DIRTY READ NEDİR?
     * ==================
     * Bir transaction'ın, başka bir transaction tarafından henüz COMMIT edilmemiş
     * (ve ROLLBACK olabilecek) veriyi okumasıdır.
     * 
     * Örnek Senaryo:
     * -------------
     * Transaction A: UPDATE quantity = 100 (henüz commit yok)
     * Transaction B: SELECT quantity → 100 okur (DIRTY READ!)
     * Transaction A: ROLLBACK (quantity = 50'ye döner)
     * 
     * Sonuç: Transaction B yanlış veri okudu! (100 hiç var olmadı)
     * 
     * READ_COMMITTED ile:
     * - Transaction B, Transaction A commit edene kadar bekler
     * - Sadece commit edilmiş verileri okur
     * - Dirty read önlenir 
     */
    @Transactional(isolation = Isolation.READ_COMMITTED)
    public Inventory updateInventoryReadCommitted(Long id, Integer newQuantity) {
        log.info("updateInventoryReadCommitted - READ_COMMITTED isolation");
        log.info("Current transaction: {}", TransactionSynchronizationManager.getCurrentTransactionName());
        
        Inventory inventory = inventoryRepository.findById(id).orElseThrow(() -> new RuntimeException("Inventory not found"));
        
        log.info("First read - Quantity: {}", inventory.getQuantity());
        
        // Simüle edilmiş gecikme - başka bir transaction bu sırada değişiklik yapabilir
        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        
        // Tekrar okuma - non-repeatable read olabilir (READ_COMMITTED'de)
        // NOT: Başka bir transaction bu sırada quantity'yi değiştirip commit ederse,
        //      burada farklı bir değer okuyabiliriz (NON-REPEATABLE READ)
        Inventory inventory2 = inventoryRepository.findById(id).orElseThrow(() -> new RuntimeException("Inventory not found"));
        log.info("Second read - Quantity: {} (NON-REPEATABLE READ possible if another transaction committed)", inventory2.getQuantity());
        
        inventory.setQuantity(newQuantity);
        return inventoryRepository.save(inventory);
    }
    
    /**
     * REPEATABLE_READ Isolation Level
     * ================================
     * 
     * REPEATABLE_READ NEDİR?
     * ----------------------
     * Aynı transaction içinde aynı veriyi tekrar okuduğunuzda
     * HER ZAMAN AYNI değeri almanızı garanti eden isolation level'dır.
     * 
     * Özellikler:
     * -----------
     *  Dirty read önlenir (READ_COMMITTED'den daha güçlü)
     *  Non-repeatable read önlenir (ana özellik)
     *  Phantom read olabilir (standart SQL'de)
     *  PostgreSQL'de MVCC ile phantom read de önlenir
     * 
     * Nasıl Çalışır?
     * -------------
     * 1. Transaction başladığında (ilk SELECT'te) bir snapshot alınır
     * 2. Tüm okuma işlemleri bu snapshot'tan yapılır
     * 3. Diğer transaction'ların commit'leri görünmez
     * 4. Her zaman aynı veriyi okursunuz
     * 
     * Örnek Senaryo (Non-Repeatable Read Önleme):
     * --------------------------------------------
     * Transaction A (REPEATABLE_READ):
     *   BEGIN TRANSACTION
     *   SELECT quantity FROM inventory WHERE id=1 → 50 okur (Snapshot alındı)
     *   
     * Transaction B:
     *   UPDATE inventory SET quantity=100 WHERE id=1
     *   COMMIT
     *   
     * Transaction A:
     *   SELECT quantity FROM inventory WHERE id=1 → 50 okur (Aynı değer!)
     *   COMMIT
     * 
     * Sonuç: Transaction A her zaman 50 okur, 100'ü görmez!
     * 
     * PostgreSQL MVCC (Multi-Version Concurrency Control):
     * ----------------------------------------------------
     * PostgreSQL'de REPEATABLE_READ, MVCC sayesinde:
     * - Phantom read'i de önler (standart SQL'den farklı)
     * - Her transaction kendi snapshot'ını görür
     * - Yeni eklenen kayıtlar görünmez
     * 
     * MySQL vs PostgreSQL:
     * -------------------
     * - MySQL: REPEATABLE_READ default, phantom read olabilir
     * - PostgreSQL: READ_COMMITTED default, REPEATABLE_READ phantom read'i de önler
     * 
     * Ne Zaman Kullanılır?
     * -------------------
     * - Aynı veriyi birden fazla kez okumanız gerektiğinde
     * - Tutarlılık kritik olduğunda
     * - Raporlama işlemlerinde
     * - Finansal hesaplamalarda
     */
    @Transactional(isolation = Isolation.REPEATABLE_READ)
    public Inventory updateInventoryRepeatableRead(Long id, Integer newQuantity) {
        log.info("updateInventoryRepeatableRead - REPEATABLE_READ isolation");
        log.info("Current transaction: {}", TransactionSynchronizationManager.getCurrentTransactionName());
        
        Inventory inventory = inventoryRepository.findById(id).orElseThrow(() -> new RuntimeException("Inventory not found"));
        
        log.info("First read - Quantity: {}", inventory.getQuantity());
        
        // Simüle edilmiş gecikme
        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        
        // Tekrar okuma - non-repeatable read önlenir (REPEATABLE_READ'de)
        Inventory inventory2 = inventoryRepository.findById(id).orElseThrow(() -> new RuntimeException("Inventory not found"));
        log.info("Second read - Quantity: {} (Should be the same - non-repeatable read prevented)", inventory2.getQuantity());
        
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
        log.info("Current transaction: {}", TransactionSynchronizationManager.getCurrentTransactionName());
        
        Inventory inventory = inventoryRepository.findById(id).orElseThrow(() -> new RuntimeException("Inventory not found"));
        
        inventory.setQuantity(newQuantity);
        return inventoryRepository.save(inventory);
    }
    
    /**
     * Phantom read testi için
     * READ_COMMITTED'de phantom read olabilir
     * 
     * PHANTOM READ NEDİR?
     * ===================
     * Aynı transaction içinde aynı sorguyu iki kez çalıştırdığınızda
     * FARKLI sayıda satır görmenizdir. Yeni kayıtlar "hayalet" gibi görünür.
     * 
     * Örnek Senaryo:
     * -------------
     * Transaction A: SELECT * WHERE productName='Laptop' → 3 kayıt (İlk okuma)
     * Transaction B: INSERT INTO inventory (productName='Laptop') → COMMIT
     * Transaction A: SELECT * WHERE productName='Laptop' → 4 kayıt (İkinci okuma)
     * 
     * Sonuç: Aynı sorgu, aynı transaction içinde farklı sayıda satır!
     *        Yeni kayıt "hayalet" gibi göründü!
     * 
     * READ_COMMITTED'de:
     * - Phantom read olabilir
     * - Her sorgu en güncel commit edilmiş veriyi okur
     * - Yeni eklenen kayıtlar görünebilir
     * 
     * REPEATABLE_READ ile (PostgreSQL MVCC):
     * - Phantom read önlenir 
     * - İlk sorgudan snapshot alınır
     * - Tüm sorgular aynı snapshot'tan yapılır
     * - Yeni kayıtlar görünmez
     */
    @Transactional(isolation = Isolation.READ_COMMITTED)
    public List<Inventory> getInventoriesByProductName(String productName) {
        log.info("getInventoriesByProductName - READ_COMMITTED (Phantom read testi)");
        
        List<Inventory> firstRead = inventoryRepository.findAll()
            .stream()
            .filter(i -> i.getProductName().equals(productName))
            .toList();
        
        log.info("First read - Number of records found: {}", firstRead.size());
        
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
        
        log.info("Second read - Number of records found: {} (Phantom read possible)", secondRead.size());
        
        return secondRead;
    }
    
    /**
     * REPEATABLE_READ ile phantom read testi
     * PostgreSQL'de MVCC ile phantom read önlenir
     * 
     * REPEATABLE_READ ile Phantom Read Önleme:
     * -----------------------------------------
     * - Transaction ilk sorgudan snapshot alır
     * - Tüm sorgular aynı snapshot'tan yapılır
     * - Yeni eklenen kayıtlar görünmez
     * - Phantom read önlenir
     */
    @Transactional(isolation = Isolation.REPEATABLE_READ)
    public List<Inventory> getInventoriesByProductNameRepeatableRead(String productName) {
        log.info("getInventoriesByProductNameRepeatableRead - REPEATABLE_READ (Phantom read önleme)");
        
        List<Inventory> firstRead = inventoryRepository.findAll()
            .stream()
            .filter(i -> i.getProductName().equals(productName))
            .toList();
        
        log.info("First read - Number of records found: {}", firstRead.size());
        
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
        
        log.info("Second read - Number of records found: {} (Phantom read prevented by PostgreSQL MVCC)", secondRead.size());
        
        return secondRead;
    }
    
    /**
     * Yeni inventory ekleme
     */
    @Transactional
    public Inventory createInventory(String productName, Integer quantity, Double price) {
        log.info("createInventory - New record is being created");
        Inventory inventory = new Inventory();
        inventory.setProductName(productName);
        inventory.setQuantity(quantity);
        inventory.setPrice(price);
        return inventoryRepository.save(inventory);
    }
}


