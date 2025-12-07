package com.fsk.transaction.basics.service;

import com.fsk.transaction.basics.entity.Employee;
import com.fsk.transaction.basics.repository.EmployeeRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.util.List;

/**
 * Temel Transaction Konuları
 * 
 * 8. Read-only transaction ne işe yarar?
 * Hibernate flush kapatılır, dirty checking devre dışı, performans artar
 * 
 * 9. Transaction ne zaman commit edilir?
 * Method return edince (proxy dışına çıkınca)
 * 
 * 10. Transaction timeout ne işe yarar?
 * Long-running transaction'lar DB lock'ları uzun süre tutar, deadlock riski
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class EmployeeService {
    
    private final EmployeeRepository employeeRepository;
    
    /**
     * Read-only transaction
     * 
     * 1. HIBERNATE FLUSH NEDİR VE NEDEN KAPATILIR?
     * ============================================
     * Flush: Hibernate'in session içindeki değişiklikleri (INSERT, UPDATE, DELETE) 
     * veritabanına yazma işlemidir. Normal transaction'larda Hibernate:
     * - Query çalıştırmadan önce flush yapar (consistency için)
     * - Transaction commit edilmeden önce flush yapar
     * - Session.flush() manuel çağrıldığında flush yapar
     * 
     * Read-only transaction'da flush KAPATILIR çünkü:
     * - Sadece okuma yapılacağı için değişiklik yok
     * - Gereksiz flush işlemleri performans kaybına neden olur
     * - Flush işlemi SQL statement'ları oluşturur ve DB'ye gönderir (maliyetli)
     * 
     * 2. DIRTY CHECK NEDEN DEVRE DIŞI KALIR?
     * =======================================
     * Dirty Checking: Hibernate'in entity'lerdeki değişiklikleri otomatik 
     * tespit edip UPDATE SQL'i oluşturmasıdır.
     * 
     * Normal transaction'da:
     * - Entity yüklenir (findById, findAll vb.)
     * - Entity'de değişiklik yapılır (setName, setSalary vb.)
     * - Hibernate değişikliği tespit eder (dirty checking)
     * - Flush sırasında UPDATE SQL'i oluşturur ve çalıştırır
     * 
     * Read-only transaction'da:
     * - Hibernate dirty checking'i ATLAR (çünkü değişiklik olmayacak)
     * - Entity'ler "read-only" olarak işaretlenir
     * - setter çağrılsa bile Hibernate bunu görmezden gelir
     * - Bu sayede gereksiz UPDATE SQL'leri oluşturulmaz
     * 
     * Örnek:
     *   Employee emp = employeeRepository.findById(1L);
     *   emp.setSalary(9999.0); // Read-only'de bu değişiklik IGNORE edilir!
     * 
     * 3. YANLIŞ WRITE'LAR NASIL ÖNLENİR?
     * ===================================
     * Read-only transaction, yanlışlıkla yapılan değişiklikleri önler:
     * 
     * Senaryo 1: Yanlışlıkla entity değiştirme
     *   - Normal transaction'da: setter çağrısı DB'ye yazılır (YANLIŞ!)
     *   - Read-only'de: setter çağrısı IGNORE edilir (GÜVENLİ!)
     * 
     * Senaryo 2: Yanlışlıkla save/delete çağrısı
     *   - Read-only transaction'da save/delete çağrılırsa exception fırlatılır
     *   - HibernateOptimisticLockingFailureException veya benzeri
     *   - Bu sayede kod hatası erken yakalanır
     * 
     * Senaryo 3: Cascade işlemler
     *   - Read-only'de cascade DELETE/UPDATE işlemleri çalışmaz
     *   - İlişkili entity'lerde yanlışlıkla değişiklik yapılamaz
     * 
     * 4. PERFORMANS NASIL ARTAR?
     * ============================
     * a) Flush işlemleri yapılmaz:
     *    - SQL statement oluşturma maliyeti yok
     *    - Network round-trip yok (DB'ye gönderim)
     *    - DB'de write lock alınmaz
     * 
     * b) Dirty checking yapılmaz:
     *    - Entity state tracking maliyeti yok
     *    - Snapshot karşılaştırması yapılmaz
     *    - Memory kullanımı azalır
     * 
     * c) Optimizasyonlar:
     *    - Hibernate read-only entity'leri daha hafif tutar
     *    - Second-level cache'den okuma daha verimli olur
     *    - Connection pool'da read-only connection'lar ayrı tutulabilir
     * 
     * d) Database seviyesinde:
     *    - Bazı DB'ler (PostgreSQL, MySQL) read-only transaction'larda
     *      özel optimizasyonlar yapar
     *    - Read-only transaction'lar daha az lock alır
     *    - Replication'da master yerine slave'den okuma yapılabilir
     * 
     * PERFORMANS KAZANCI ÖRNEĞİ:
     * - Normal transaction: 100ms (flush + dirty check)
     * - Read-only transaction: 50ms (sadece SELECT)
     * - %50 performans artışı!
     * 
     * 5. NE ZAMAN KULLANILMALI?
     * ==========================
     * Sadece okuma yapılan method'larda (findAll, findById, search vb.)
     * Raporlama işlemlerinde
     * Dashboard/analitik sorgularında
     * Cache'den okuma işlemlerinde
     * 
     * Write işlemi yapılacaksa KULLANMA (save, update, delete)
     * Entity'de değişiklik yapılacaksa KULLANMA
     */
    @Transactional(readOnly = true)
    public List<Employee> getAllEmployees() {
        log.info("getAllEmployees - Read-only transaction");
        log.info("Current transaction: {}", TransactionSynchronizationManager.getCurrentTransactionName());
        log.info("Is read-only: {}", TransactionSynchronizationManager.isCurrentTransactionReadOnly());
        
        // Read-only transaction'da bu değişiklikler IGNORE edilir:
        // List<Employee> employees = employeeRepository.findAll();
        // employees.get(0).setSalary(9999.0); // Bu değişiklik DB'ye yazılmaz!
        
        return employeeRepository.findAll();
    }
    
    /**
     * Normal transactional write işlemi
     * Transaction ne zaman commit edilir?
     * Method return edince (proxy dışına çıkınca)
     */
    @Transactional
    public Employee createEmployee(String name, String email, String department, Double salary) {
        log.info("Current transaction: {}", TransactionSynchronizationManager.getCurrentTransactionName());
        
        Employee employee = new Employee();
        employee.setName(name);
        employee.setEmail(email);
        employee.setDepartment(department);
        employee.setSalary(salary);
        
        Employee saved = employeeRepository.save(employee);
        log.info("Employee kaydedildi: {}", saved.getId());
        
        // Method return edince transaction commit edilir (proxy dışına çıkınca)
        log.info("Method return ediyor - Transaction commit edilecek");
        return saved;
    }
    
    /**
     * Transaction timeout
     * 
     * 1. LONG-RUNNING TRANSACTION NEDİR?
     * ===================================
     * Long-running transaction: Normal süreden (genellikle birkaç saniye) 
     * çok daha uzun süren transaction'lardır.
     * 
     * Normal transaction süresi:
     * - Basit CRUD işlemleri: 10-100ms
     * - Karmaşık işlemler: 100ms - 2 saniye
     * - Uzun transaction: 5 saniye ve üzeri
     * 
     * Long-running transaction örnekleri:
     * - Büyük veri setlerini işleme (100.000+ kayıt)
     * - Dış servis çağrıları (API, web service)
     * - Dosya işlemleri (upload, download, parse)
     * - Karmaşık hesaplamalar (raporlama, analitik)
     * - Thread.sleep() veya blocking işlemler
     * - Kullanıcı input'u bekleme (ASLA YAPMA!)
     * 
     * 2. LONG-RUNNING TRANSACTION NEDEN PROBLEMLİDİR?
     * ================================================
     * 
     * a) DATABASE LOCK PROBLEMLERİ:
     * -----------------------------
     * Transaction açık kaldığı sürece DB lock'ları tutulur:
     * 
     * Senaryo:
     *   Thread 1: UPDATE employee SET salary = 5000 WHERE id = 1
     *             (Transaction başladı, lock alındı)
     *             [UZUN İŞLEM - 10 saniye bekleme]
     *             (Transaction hala açık, lock hala tutuluyor!)
     * 
     *   Thread 2: UPDATE employee SET salary = 6000 WHERE id = 1
     *             (Lock bekliyor - Thread 1'in transaction'ı bitmesini bekliyor)
     *             [BLOCKED - 10 saniye beklemek zorunda!]
     * 
     * Sonuç:
     * - Thread 2, Thread 1'in işlemi bitene kadar bekler
     * - Database connection pool tükenir
     * - Uygulama yavaşlar veya donar
     * 
     * b) DEADLOCK RİSKİ:
     * ------------------
     * İki transaction birbirini beklediğinde deadlock oluşur:
     * 
     * Thread 1: Lock A alır → Lock B bekler
     * Thread 2: Lock B alır → Lock A bekler
     * 
     * Sonuç: Her ikisi de sonsuza kadar bekler (deadlock!)
     * 
     * c) CONNECTION POOL TÜKENMESİ:
     * -----------------------------
     * Her transaction bir DB connection kullanır:
     * 
     * Connection Pool: 10 connection
     * - 10 uzun transaction = Tüm connection'lar kullanılıyor
     * - 11. istek geldiğinde: Connection bekliyor (timeout olabilir!)
     * 
     * Sonuç:
     * - Yeni istekler işlenemez
     * - Uygulama yanıt veremez
     * - "Connection pool exhausted" hatası
     * 
     * d) MEMORY KULLANIMI:
     * --------------------
     * Transaction açık kaldığı sürece:
     * - Hibernate session açık kalır
     * - Entity'ler memory'de tutulur
     * - Dirty checking için snapshot'lar saklanır
     * 
     * Uzun transaction = Uzun süre memory kullanımı
     * → Memory leak riski!
     * 
     * e) DATA CONSISTENCY PROBLEMLERİ:
     * ---------------------------------
     * Transaction uzun sürerse:
     * - Diğer kullanıcılar güncel veriyi göremez
     * - Stale data okunur
     * - Business logic hataları oluşur
     * 
     * 3. TRANSACTION TIMEOUT NASIL ÇALIŞIR?
     * ======================================
     * 
     * @Transactional(timeout = 5): Transaction 5 saniyeden fazla sürerse
     * otomatik olarak rollback edilir ve exception fırlatılır.
     * 
     * Çalışma Prensibi:
     * 1. Transaction başlar (t=0)
     * 2. İşlemler yapılır (save, update vb.)
     * 3. Uzun işlem başlar (Thread.sleep, API call vb.)
     * 4. Timeout süresi dolduğunda (t=5 saniye):
     *    - Spring transaction manager transaction'ı iptal eder
     *    - Rollback yapılır
     *    - TransactionTimedOutException fırlatılır
     * 
     * Örnek Senaryo (Bu method'da):
     * - Timeout: 5 saniye
     * - Thread.sleep(6000): 6 saniye bekleme
     * - Sonuç: 5. saniyede timeout olur, exception fırlatılır
     * 
     * 4. TIMEOUT NE ZAMAN DEVREYE GİRER?
     * ===================================
     * 
     * Timeout süresi, transaction'ın BAŞLAMASINDAN itibaren sayılır:
     * 
     * YANLIŞ ANLAYIŞ:
     *   - "Sadece DB işlemleri için geçerli"
     *   - "Sadece commit sırasında kontrol edilir"
     * 
     * DOĞRU ANLAYIŞ:
     *   - Transaction açıldığı andan itibaren süre başlar
     *   - Tüm method execution süresi sayılır
     *   - DB işlemleri + business logic + external calls hepsi dahil
     * 
     * Örnek:
     *   @Transactional(timeout = 5)
     *   public void process() {
     *       save();              // 1 saniye
     *       callExternalAPI();   // 3 saniye
     *       calculate();         // 2 saniye
     *       // Toplam: 6 saniye → TIMEOUT!
     *   }
     * 
     * 5. TIMEOUT EXCEPTION'LARI:
     * ==========================
     * 
     * Timeout olduğunda fırlatılan exception'lar:
     * 
     * - TransactionTimedOutException (Spring)
     * - QueryTimeoutException (Hibernate/JPA)
     * - SQLTimeoutException (JDBC)
     * 
     * Exception handling:
     *   try {
     *       service.createEmployeeWithTimeout(...);
     *   } catch (TransactionTimedOutException e) {
     *       // Timeout oldu, rollback yapıldı
     *       // Kullanıcıya bilgi ver, retry öner
     *   }
     * 
     * 6. TIMEOUT DEĞERİ NASIL BELİRLENMELİ?
     * =====================================
     * 
     * Timeout değeri işlemin tipine göre belirlenmelidir:
     * 
     * Kısa işlemler (CRUD):
     *    timeout = 5-10 saniye
     *    - Basit save/update/delete
     *    - Tek entity işlemleri
     * 
     * Orta işlemler (Batch):
     *    timeout = 30-60 saniye
     *    - Toplu güncellemeler
     *    - Karmaşık hesaplamalar
     * 
     * Uzun işlemler (Raporlama):
     *    timeout = 120-300 saniye
     *    - Büyük raporlar
     *    - Data export işlemleri
     * 
     * ASLA YAPMA:
     *    - timeout = 0 (sınırsız - çok tehlikeli!)
     *    - timeout = Integer.MAX_VALUE
     *    - Timeout kullanmamak
     * 
     * 7. LONG-RUNNING TRANSACTION ÇÖZÜMLERİ:
     * =======================================
     * 
     * Çözüm 1: Transaction'ı Böl (Chunk Processing)
     * ----------------------------------------------
     * Büyük işlemi küçük parçalara böl:
     * 
     * YANLIŞ:
     *   @Transactional
     *   public void process100000Records() {
     *       for (int i = 0; i < 100000; i++) {
     *           save(records[i]); // 100.000 kayıt tek transaction'da!
     *       }
     *   }
     * 
     * DOĞRU:
     *   public void process100000Records() {
     *       for (int i = 0; i < 100000; i += 1000) {
     *           processChunk(i, i + 1000); // Her 1000 kayıt ayrı transaction
     *       }
     *   }
     *   
     *   @Transactional(timeout = 10)
     *   private void processChunk(int start, int end) {
     *       // 1000 kayıt işle
     *   }
     * 
     * Çözüm 2: External Call'ları Transaction Dışına Çıkar
     * ------------------------------------------------------
     * API çağrıları, dosya işlemleri transaction dışında yapılmalı:
     * 
     * YANLIŞ:
     *   @Transactional
     *   public void saveAndNotify() {
     *       save(employee);
     *       callExternalAPI(); // 5 saniye sürüyor!
     *   }
     * 
     * DOĞRU:
     *   @Transactional
     *   public void saveEmployee() {
     *       save(employee);
     *   }
     *   
     *   public void saveAndNotify() {
     *       saveEmployee(); // Transaction içinde
     *       callExternalAPI(); // Transaction dışında
     *   }
     * 
     * Çözüm 3: Async Processing
     * --------------------------
     * Uzun işlemleri asenkron yap:
     * 
     * @Transactional
     * public void saveEmployee() {
     *     Employee saved = save(employee);
     *     asyncService.processLater(saved.getId()); // Async, transaction dışı
     * }
     * 
     * Çözüm 4: Read-Only Transaction Kullan
     * ---------------------------------------
     * Sadece okuma yapılıyorsa read-only kullan:
     * 
     * @Transactional(readOnly = true, timeout = 30)
     * public List<Employee> generateReport() {
     *     // Uzun sürebilir ama lock yok, güvenli
     * }
     * 
     * 8. TIMEOUT TEST ETME:
     * =====================
     * 
     * Bu method timeout'u test etmek için tasarlanmıştır:
     * - Timeout: 5 saniye
     * - Thread.sleep(6000): 6 saniye bekleme
     * - Sonuç: 5. saniyede TransactionTimedOutException fırlatılır
     * 
     * UYARI: Gerçek uygulamada Thread.sleep() kullanma!
     * Bu sadece test amaçlıdır.
     * 
     * 9. BEST PRACTICES:
     * ==================
     * 
     * Her transaction'a uygun timeout değeri ver
     * Long-running işlemleri transaction dışına çıkar
     * Büyük işlemleri chunk'lara böl
     * External call'ları transaction dışında yap
     * Timeout exception'larını handle et
     * Monitoring ile transaction sürelerini takip et
     * 
     * Timeout kullanmamak
     * Çok yüksek timeout değerleri (sınırsız gibi)
     * Transaction içinde external call yapmak
     * Transaction içinde kullanıcı input beklemek
     * Büyük batch işlemlerini tek transaction'da yapmak
     */
    @Transactional(timeout = 5) // 5 saniye timeout
    public void createEmployeeWithTimeout(String name, String email, String department, Double salary) {
        log.info("createEmployeeWithTimeout - 5 saniye timeout ile");
        
        Employee employee = new Employee();
        employee.setName(name);
        employee.setEmail(email);
        employee.setDepartment(department);
        employee.setSalary(salary);
        
        Employee saved = employeeRepository.save(employee);
        log.info("Employee kaydedildi: {}", saved.getId());
        
        // UYARI: Bu sadece timeout'u test etmek içindir!
        // Gerçek uygulamada Thread.sleep() kullanma!
        // Simüle edilmiş uzun işlem - timeout'u test etmek için
        try {
            Thread.sleep(6000); // 6 saniye bekle - timeout olacak
            // Timeout 5 saniye olduğu için, 5. saniyede TransactionTimedOutException fırlatılır
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

    }
    
    /**
     * Exception ile rollback testi
     * Method return etmeden exception fırlatılırsa rollback olur
     */
    @Transactional
    public void createEmployeeWithException(String name, String email, String department, Double salary) {
        log.info("createEmployeeWithException - Exception ile rollback testi");
        
        Employee employee = new Employee();
        employee.setName(name);
        employee.setEmail(email);
        employee.setDepartment(department);
        employee.setSalary(salary);
        
        Employee saved = employeeRepository.save(employee);
        log.info("Employee kaydedildi: {}", saved.getId());
        
        // Exception fırlatılıyor - rollback olacak
        throw new RuntimeException("Exception - Transaction rollback olacak!");
    }
}



