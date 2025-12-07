package com.fsk.transaction.advanced.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 50. En zor soru (Staff / Principal level)
 * 
 * Bir transaction'da olmaması gereken 5 şey say.
 * 
 * Beklenen cevap:
 * - Network call
 * - File IO
 * - Long computation
 * - Retry loop
 * - User interaction
 * 
 * 34. Transaction + Reactor (WebFlux) neden birlikte çalışmaz?
 * 
 * 43. Event publishing transaction'dan ayrılır mı?
 * 
 * 46. TransactionState neden loglarda görünmez?
 * 
 * 47. Transaction + JVM crash senaryosu
 * 
 * 48. Exactly-once semantics neden zordur?
 */
@Service
@Slf4j
public class TransactionBestPracticesService {
    
    /**
     * 50. Transaction'da olmaması gerekenler
     */
    @Transactional
    public String whatShouldNotBeInTransaction() {
        return """
            Transaction'da OLMAMASI Gereken 5 Şey:
            
            1. Network Call (REST, gRPC, HTTP):
               - Lock leak riski
               - Timeout propagation
               - Unbounded transaction süresi
               - Partial failure
            
            2. File IO:
               - Disk latency
               - Transaction süresi uzar
               - Lock duration artar
            
            3. Long Computation:
               - CPU-bound işlemler
               - Transaction süresi uzar
               - Resource starvation
            
            4. Retry Loop:
               - Transaction içinde retry yapılmaz
               - Retry transaction dışında olmalı
               - @Retryable kullanılmalı
            
            5. User Interaction:
               - Blocking operation
               - Transaction süresi çok uzar
               - Connection pool tükenir
            
            Golden Rule:
            Transaction boundary = DB only
            """;
    }
    
    /**
     * 34. Transaction + Reactor (WebFlux) açıklaması
     */
    @Transactional
    public String reactorAndTransaction() {
        return """
            Transaction + Reactor (WebFlux) Neden Birlikte Çalışmaz?
            
            Problem:
            - Reactive execution model thread-per-request değil
            - ThreadLocal işe yaramaz
            - R2DBC farklı transaction modeli kullanır
            
            Çözüm:
            - Reactive transaction ≠ Spring JDBC transaction
            - R2DBC transaction management kullanılmalı
            - Reactive streams içinde transaction yönetimi farklı
            
            Best Practice:
            - WebFlux + R2DBC kullan
            - Traditional @Transactional çalışmaz
            - Reactive transaction API kullan
            """;
    }
    
    /**
     * 43. Event publishing açıklaması
     */
    @Transactional
    public String eventPublishingExplanation() {
        return """
            Event Publishing Transaction'dan Ayrılır mı?
            
            ❌ Hayır - Transaction içinde yayınlama
            
            ✅ DOĞRU: AFTER_COMMIT
            - Transaction commit olduktan sonra event yayınla
            - @TransactionalEventListener(phase = AFTER_COMMIT)
            - DB commit garantisi var
            
            Neden?
            - DB rollback olsa bile event gitmiş olabilir
            - Eventual consistency problemi
            - Outbox pattern kullanılmalı
            
            Best Practice:
            - Event publishing AFTER_COMMIT'te yapılmalı
            - Outbox pattern ile garantili delivery
            """;
    }
    
    /**
     * 46. TransactionState logging açıklaması
     */
    @Transactional
    public String transactionStateLogging() {
        return """
            TransactionState Neden Loglarda Görünmez?
            
            Problem:
            - Proxy-based transaction management
            - Implicit begin/commit
            - Logs async
            - No explicit boundary
            
            Çözüm:
            - TransactionSynchronizationManager kullan
            - Custom TransactionSynchronization implement et
            - Transaction event'lerini logla
            
            Best Practice:
            - Transaction boundary'leri explicit yap
            - Transaction event listener'ları kullan
            - Monitoring ve observability ekle
            """;
    }
    
    /**
     * 47. Transaction + JVM crash açıklaması
     */
    @Transactional
    public String jvmCrashScenario() {
        return """
            Transaction + JVM Crash Senaryosu
            
            JVM crash ederse transaction ne olur?
            
            ✅ DB atomicity korunur:
               - DB transaction log'u var
               - In-flight transaction rollback olur
               - ACID garantisi korunur
            
            ❌ External side effects korunmaz:
               - Cache güncellemeleri kaybolur
               - Message gönderimleri kaybolur
               - File system değişiklikleri kaybolur
            
            Best Practice:
            - Idempotent operations kullan
            - Compensation pattern
            - Event sourcing
            - Outbox pattern
            """;
    }
    
    /**
     * 48. Exactly-once semantics açıklaması
     */
    @Transactional
    public String exactlyOnceSemantics() {
        return """
            Exactly-once Semantics Neden Zordur?
            
            Transaction olmasına rağmen neden exactly-once garanti edemezsin?
            
            Problem:
            - DB ACID ≠ Distributed system
            - Message duplication
            - Network partition
            - Retry mechanisms
            
            Çözüm:
            - Outbox pattern
            - Idempotency keys
            - Deduplication
            - Idempotent operations
            
            Best Practice:
            - Exactly-once yerine at-least-once + idempotency
            - Idempotent operations tasarla
            - Deduplication mekanizması kullan
            - Outbox pattern ile message delivery
            """;
    }
}


