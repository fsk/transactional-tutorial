package com.fsk.transaction.advanced.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Transaction Teorik Konuları
 * 
 * 18. Interface vs Class Annotation
 * 23. Transaction boundary neden Controller'a kadar uzatılmaz? (OSIV)
 * 28. @Transactional testlerde neden farklı davranır?
 * 31. Distributed transaction neden önerilmez?
 * 32. Transaction ile eventual consistency arasında nasıl karar verirsin?
 */
@Service
@Slf4j
public class TransactionTheoryService {
    
    /**
     * 18. Interface vs Class Annotation
     * 
     * @Transactional interface üstünde mi class üstünde mi olmalı?
     * 
     * JDK proxy kullanılıyorsa interface üzerindeki annotation geçerli
     * CGLIB'de class üzeri okunur
     * 
     * Best practice: class / method üzerinde
     */
    @Transactional
    public String interfaceVsClassAnnotation() {
        return """
            Interface vs Class Annotation:
            
            JDK Proxy:
            - Interface üzerindeki @Transactional okunur
            - Class üzerindeki annotation ignore edilir
            
            CGLIB Proxy:
            - Class üzerindeki @Transactional okunur
            - Interface üzerindeki annotation ignore edilir
            
            Best Practice:
            - @Transactional'ı class veya method üzerinde kullan
            - Interface'e koyma (JDK proxy kullanılıyorsa çalışır ama CGLIB'de çalışmaz)
            """;
    }
    
    /**
     * 23. Transaction boundary neden Controller'a kadar uzatılmaz? (OSIV)
     * 
     * OSIV (Open Session in View) neden risklidir?
     * ✅ Hidden N+1
     * ✅ Transaction süresi uzar
     * ✅ Session leak riski
     * ✅ Debug zorlaşır
     */
    @Transactional
    public String transactionBoundaryExplanation() {
        return """
            Transaction Boundary Neden Controller'a Kadar Uzatılmaz?
            
            OSIV (Open Session in View) Riskleri:
            
            1. Hidden N+1 Problem:
               - View layer'da lazy loading yapılırsa N+1 query oluşur
               - Transaction uzun sürer, performans düşer
            
            2. Transaction Süresi Uzar:
               - HTTP request süresi kadar transaction açık kalır
               - DB connection pool tükenebilir
               - Lock'lar uzun süre tutulur
            
            3. Session Leak Riski:
               - Exception durumunda session kapanmayabilir
               - Connection pool tükenir
            
            4. Debug Zorlaşır:
               - Transaction boundary belirsizleşir
               - Hangi katmanda transaction başladı anlaşılmaz
            
            Best Practice:
            - Transaction Service layer'da başlatılmalı
            - Controller'da transaction olmamalı
            - View layer'da lazy loading yapılmamalı
            - DTO mapping transaction içinde yapılmalı
            """;
    }
    
    /**
     * 28. @Transactional testlerde neden farklı davranır?
     * 
     * Spring testlerinde neden DB'ye hiçbir şey yazılmıyor?
     * ✅ Test transaction sonunda rollback olur
     */
    @Transactional
    public String transactionalInTestsExplanation() {
        return """
            @Transactional Testlerde Neden Farklı Davranır?
            
            Spring Test Framework:
            - @Transactional test method'ları otomatik rollback edilir
            - Test sonunda DB'ye yazılan veriler geri alınır
            - Test isolation sağlanır
            
            Örnek:
            @Transactional
            @Test
            void test() {
                // Bu işlemler DB'ye yazılır
                // Ama test sonunda rollback olur
            }
            
            Rollback'i Engellemek İçin:
            @Rollback(false) // Test sonunda rollback olmasın
            @Commit // Test sonunda commit olsun
            
            Veya:
            @Transactional(propagation = Propagation.NOT_SUPPORTED)
            // Transaction olmadan test et
            
            Best Practice:
            - Test isolation için @Transactional kullan
            - Gerçek commit testi için @Rollback(false) kullan
            """;
    }
    
    /**
     * 31. Distributed transaction neden önerilmez?
     * 
     * XA transaction neden kaçınılır?
     * ✅ Complexity
     * ✅ Performance
     * ✅ Debug kabusu
     * ➡️ Eventual consistency tercih edilir
     */
    @Transactional
    public String distributedTransactionExplanation() {
        return """
            Distributed Transaction Neden Önerilmez?
            
            XA Transaction Problemleri:
            
            1. Complexity:
               - İki-phase commit (2PC) karmaşık
               - Coordinator gerektirir
               - Failure senaryoları çok
            
            2. Performance:
               - Network latency artar
               - Lock'lar uzun süre tutulur
               - Throughput düşer
            
            3. Debug Kabusu:
               - Distributed system debug zor
               - Transaction state takip edilemez
               - Rollback senaryoları karmaşık
            
            4. Availability:
               - Single point of failure
               - Coordinator down olursa tüm sistem durur
            
            Alternatif: Eventual Consistency
            
            - Saga pattern
            - Outbox pattern
            - Event sourcing
            - Compensation transactions
            
            Best Practice:
            - Distributed transaction'dan kaçın
            - Eventual consistency tercih et
            - Idempotent operations kullan
            """;
    }
    
    /**
     * 32. Transaction ile eventual consistency arasında nasıl karar verirsin?
     * 
     * En zor kapanış sorusu (principal seviye)
     */
    @Transactional
    public String transactionVsEventualConsistencyDecision() {
        return """
            Transaction vs Eventual Consistency Karar Kriterleri:
            
            1. Business Criticality:
               - Kritik işlemler → ACID Transaction
               - Non-kritik işlemler → Eventual Consistency
            
            2. Compensation Cost:
               - Geri alma maliyeti düşükse → Eventual Consistency
               - Geri alma maliyeti yüksekse → ACID Transaction
            
            3. Failure Probability:
               - Yüksek failure riski → Eventual Consistency (resilient)
               - Düşük failure riski → ACID Transaction
            
            4. User Impact:
               - Kullanıcı etkileniyorsa → ACID Transaction
               - Background job → Eventual Consistency
            
            5. Observability:
               - Monitoring kolay mı?
               - Debug edilebilir mi?
               - Rollback mekanizması var mı?
            
            6. Performance Requirements:
               - Yüksek throughput → Eventual Consistency
               - Düşük latency → ACID Transaction (küçük scope)
            
            7. Data Consistency Requirements:
               - Strong consistency gerekli mi?
               - Eventual consistency yeterli mi?
            
            Örnek Senaryolar:
            
            Ödeme İşlemi:
            - ACID Transaction (kritik, compensation maliyeti yüksek)
            
            Email Gönderimi:
            - Eventual Consistency (non-kritik, retry mekanizması)
            
            Inventory Update:
            - ACID Transaction (stock tutarlılığı kritik)
            
            Analytics Update:
            - Eventual Consistency (background job, eventual consistency yeterli)
            
            Best Practice:
            - Her senaryo için ayrı değerlendir
            - ACID Transaction → küçük scope, hızlı
            - Eventual Consistency → büyük scope, resilient
            """;
    }
}



