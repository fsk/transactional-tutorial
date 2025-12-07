package com.fsk.transaction.advanced.service;

import com.fsk.transaction.advanced.entity.Employee;
import com.fsk.transaction.advanced.repository.EmployeeRepository;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.hibernate.LazyInitializationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Stream;

/**
 * İleri Seviye Transaction Konuları (16-32)
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class TransactionAdvancedService {
    
    private final EmployeeRepository employeeRepository;
    
    @PersistenceContext
    private EntityManager entityManager;
    
    /**
     * 16. @Transactional + private method
     * 
     * @Transactional private bir methodta çalışır mı?
     * ✅ Hayır - Proxy sadece public methodları keser
     * private → proxy göremez
     */
    public void callPrivateMethod() {
        log.info("callPrivateMethod - Public method");
        // Private method çağrısı - @Transactional çalışmaz!
        privateTransactionalMethod();
    }
    
    @Transactional
    private void privateTransactionalMethod() {
        log.info("privateTransactionalMethod - @Transactional çalışmaz (private method)");
        log.info("Current transaction: {}", 
            org.springframework.transaction.support.TransactionSynchronizationManager.getCurrentTransactionName());
    }
    
    /**
     * 17. Final class / final method meselesi
     * 
     * CGLIB proxy, final class / method'u override edemez
     * Sonuç: transaction açılmaz
     */
    @Transactional
    public final void finalMethod() {
        log.info("finalMethod - @Transactional çalışmayabilir (final method)");
        log.info("Current transaction: {}", 
            org.springframework.transaction.support.TransactionSynchronizationManager.getCurrentTransactionName());
    }
    
    /**
     * 19. Transaction + LazyInitializationException
     * 
     * Session transaction scope'unda açık
     * Transaction bitince session kapanır
     * Lazy field erişimi sonrası → exception
     */
    @Transactional
    public Employee getEmployeeWithLazyField(Long id) {
        log.info("getEmployeeWithLazyField - Transaction içinde");
        Employee employee = employeeRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Employee bulunamadı"));
        
        // Transaction içinde lazy field'a erişim - OK
        log.info("Employee name: {}", employee.getName());
        return employee;
    }
    
    /**
     * LazyInitializationException örneği
     * Transaction dışında lazy field'a erişim
     */
    public Employee getEmployeeWithoutTransaction(Long id) {
        log.info("getEmployeeWithoutTransaction - Transaction YOK");
        Employee employee = employeeRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Employee bulunamadı"));
        
        // Transaction dışında lazy field'a erişim - LazyInitializationException!
        // Bu örnekte Employee'de lazy field yok ama konsept aynı
        return employee;
    }
    
    /**
     * 20. Flush vs Commit farkı
     * 
     * Flush = SQL gönderimi
     * Commit = transaction kalıcılaştırma
     * Flush ≠ Commit
     * Rollback SQL'i de geri alır
     */
    @Transactional
    public void flushVsCommitExample() {
        log.info("flushVsCommitExample - Flush vs Commit");
        
        Employee employee = new Employee();
        employee.setName("Flush Test");
        employee.setEmail("flush@example.com");
        employee.setDepartment("IT");
        employee.setSalary(5000.0);
        
        Employee saved = employeeRepository.save(employee);
        log.info("Employee kaydedildi: {}", saved.getId());
        
        // Flush - SQL gönderilir ama commit olmaz
        entityManager.flush();
        log.info("Flush yapıldı - SQL gönderildi ama commit olmadı");
        
        // Exception fırlatılıyor - Rollback olacak
        // Flush edilmiş SQL bile geri alınır
        throw new RuntimeException("Rollback - Flush edilmiş SQL geri alınacak!");
    }
    
    /**
     * 21. Manual flush ne zaman kullanılır?
     * 
     * ✅ Büyük batch insert
     * ✅ Constraint violation'ı erken görmek
     * ✅ Memory pressure azaltmak
     */
    @Transactional
    public void batchInsertWithFlush() {
        log.info("batchInsertWithFlush - Büyük batch insert örneği");
        
        for (int i = 0; i < 1000; i++) {
            Employee employee = new Employee();
            employee.setName("Batch Employee " + i);
            employee.setEmail("batch" + i + "@example.com");
            employee.setDepartment("IT");
            employee.setSalary(5000.0 + i);
            
            employeeRepository.save(employee);
            
            // Her 100 kayıtta bir flush - Memory pressure azaltmak için
            if (i % 100 == 0) {
                entityManager.flush();
                entityManager.clear(); // Memory temizleme
                log.info("Flush yapıldı: {} kayıt", i);
            }
        }
        
        log.info("Batch insert tamamlandı");
    }
    
    /**
     * 22. Nested transaction (Propagation.NESTED)
     * 
     * Propagation.NESTED → savepoint
     * DB destekliyorsa çalışır
     * PostgreSQL desteklemez (praktikte)
     */
    @Transactional
    public void nestedTransactionExample() {
        log.info("nestedTransactionExample - NESTED propagation testi");
        
        Employee employee = new Employee();
        employee.setName("Nested Test");
        employee.setEmail("nested@example.com");
        employee.setDepartment("IT");
        employee.setSalary(5000.0);
        
        employeeRepository.save(employee);
        log.info("Employee kaydedildi: {}", employee.getId());
        
        // NESTED propagation ile inner method çağrısı
        // PostgreSQL'de çalışmayabilir
        try {
            nestedInnerMethod();
        } catch (Exception e) {
            log.error("Nested transaction hatası: {}", e.getMessage());
            // Outer transaction devam eder
        }
    }
    
    @org.springframework.transaction.annotation.Transactional(propagation = org.springframework.transaction.annotation.Propagation.NESTED)
    public void nestedInnerMethod() {
        log.info("nestedInnerMethod - NESTED propagation");
        Employee employee = new Employee();
        employee.setName("Nested Inner");
        employee.setEmail("nested-inner@example.com");
        employee.setDepartment("IT");
        employee.setSalary(6000.0);
        
        employeeRepository.save(employee);
        throw new RuntimeException("Nested transaction rollback");
    }
    
    /**
     * 29. Read-only transaction içinde write olursa?
     * 
     * readOnly = true iken save çağrılırsa ne olur?
     * ✅ Hibernate flush etmez
     * ✅ Commit sırasında hata alabilirsin
     * ✅ Veya silent ignore (DB'ye göre)
     */
    @Transactional(readOnly = true)
    public void readOnlyWithWrite() {
        log.info("readOnlyWithWrite - Read-only transaction içinde write");
        log.info("Is read-only: {}", 
            org.springframework.transaction.support.TransactionSynchronizationManager.isCurrentTransactionReadOnly());
        
        Employee employee = new Employee();
        employee.setName("Read Only Test");
        employee.setEmail("readonly@example.com");
        employee.setDepartment("IT");
        employee.setSalary(5000.0);
        
        // Read-only transaction içinde write - Hibernate flush etmez
        // Commit sırasında hata alabilir veya silent ignore
        employeeRepository.save(employee);
        log.info("Read-only transaction içinde save çağrıldı - Flush edilmeyecek");
    }
    
    /**
     * 30. Transaction + Stream API tuzağı
     * 
     * Stream lazy evaluate
     * transaction scope dışına çıkabilir
     */
    @Transactional
    public List<Employee> streamApiTrap() {
        log.info("streamApiTrap - Stream API transaction tuzağı");
        
        // Stream oluşturuluyor - Lazy evaluation
        Stream<Employee> employeeStream = employeeRepository.findAll().stream();
        
        // Transaction burada bitiyor!
        // Stream henüz evaluate edilmedi
        
        // Stream evaluate edildiğinde transaction kapalı olabilir
        // LazyInitializationException riski
        return employeeStream
            .filter(e -> e.getSalary() > 1000)
            .toList();
    }
    
    /**
     * Stream API tuzağı çözümü
     * Stream'i transaction içinde evaluate et
     */
    @Transactional
    public List<Employee> streamApiSolution() {
        log.info("streamApiSolution - Stream API çözümü");
        
        // Stream'i transaction içinde hemen evaluate et
        List<Employee> employees = employeeRepository.findAll();
        
        // Artık transaction içinde işlem yapılabilir
        return employees.stream()
            .filter(e -> e.getSalary() > 1000)
            .toList();
    }
}


