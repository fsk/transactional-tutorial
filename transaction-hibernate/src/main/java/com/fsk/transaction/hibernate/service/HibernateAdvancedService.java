package com.fsk.transaction.hibernate.service;

import com.fsk.transaction.hibernate.entity.Employee;
import com.fsk.transaction.hibernate.repository.EmployeeRepository;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Hibernate Transaction Konuları
 * 
 * 35. Hibernate dirty checking tam olarak ne zaman çalışır?
 * Dirty checking hangi anlarda tetiklenir?
 * ✅ flush sırasında
 * ✅ commit öncesi
 * ✅ persistence context açıkken
 * ❌ getter çağrısında değil
 * ❌ entity'ye dokunulduğu anda değil
 * 
 * 36. Detached entity transaction içinde save edilirse ne olur?
 * Detached entity'yi save edersen ne olur?
 * ✅ merge edilir
 * ✅ new persistence context açılır
 * ✅ overwrite riski vardır
 * 
 * 37. Lost Update problemi nedir?
 * @Transactional olmasına rağmen nasıl veri kaybı olur?
 * ✅ Read → modify → write pattern
 * ✅ Two transactions aynı başlangıç state'ini okur
 * ✅ Son yazan kazanır
 * 
 * 20. Flush vs Commit farkı
 * Flush = SQL gönderimi
 * Commit = transaction kalıcılaştırma
 * Flush ≠ Commit
 * Rollback SQL'i de geri alır
 * 
 * 21. Manual flush ne zaman kullanılır?
 * ✅ Büyük batch insert
 * ✅ Constraint violation'ı erken görmek
 * ✅ Memory pressure azaltmak
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class HibernateAdvancedService {
    
    private final EmployeeRepository employeeRepository;
    
    @PersistenceContext
    private EntityManager entityManager;
    
    /**
     * Dirty checking flush sırasında çalışır
     */
    @Transactional
    public Employee demonstrateDirtyChecking(Long id) {
        log.info("demonstrateDirtyChecking - Dirty checking flush sırasında çalışır");
        
        // Entity yükleniyor - persistence context'e ekleniyor
        Employee employee = employeeRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Employee bulunamadı"));
        
        log.info("Employee yüklendi: {}", employee.getName());
        
        // Entity değiştiriliyor - ama henüz dirty checking yok
        employee.setSalary(employee.getSalary() + 1000);
        log.info("Salary güncellendi - Ama henüz dirty checking yok");
        
        // Flush yapılıyor - Burada dirty checking çalışır
        entityManager.flush();
        log.info("Flush yapıldı - Dirty checking çalıştı, değişiklikler tespit edildi");
        
        return employee;
    }
    
    /**
     * Detached entity merge örneği
     */
    @Transactional
    public Employee demonstrateDetachedEntity(Long id) {
        log.info("demonstrateDetachedEntity - Detached entity merge");
        
        // Entity yükleniyor
        Employee employee = employeeRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Employee bulunamadı"));
        
        log.info("Employee yüklendi: {}", employee.getName());
        
        // Entity persistence context'ten çıkarılıyor (detached)
        entityManager.detach(employee);
        log.info("Entity detached edildi");
        
        // Detached entity değiştiriliyor
        employee.setSalary(employee.getSalary() + 2000);
        log.info("Detached entity değiştirildi");
        
        // Detached entity save ediliyor - merge edilir
        // Overwrite riski var - başka bir transaction aynı entity'yi değiştirdiyse
        Employee merged = entityManager.merge(employee);
        log.info("Entity merge edildi - Yeni persistence context açıldı");
        
        return merged;
    }
    
    /**
     * Lost Update problemi örneği
     */
    @Transactional
    public Employee lostUpdateProblem(Long id, Double increment) {
        log.info("lostUpdateProblem - Lost Update problemi");
        
        // Read
        Employee employee = employeeRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Employee bulunamadı"));
        
        Double oldSalary = employee.getSalary();
        log.info("İlk okuma - Salary: {}", oldSalary);
        
        // Simüle edilmiş gecikme - başka bir transaction bu sırada değişiklik yapabilir
        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        
        // Modify
        employee.setSalary(oldSalary + increment);
        log.info("Salary güncellendi: {}", employee.getSalary());
        
        // Write - Son yazan kazanır (Lost Update!)
        Employee saved = employeeRepository.save(employee);
        log.info("Kaydedildi - Ama başka transaction'ın değişiklikleri kaybolmuş olabilir!");
        
        return saved;
    }
    
    /**
     * Flush vs Commit farkı
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
     * Manual flush ne zaman kullanılır?
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
}


