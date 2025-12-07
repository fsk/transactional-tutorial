# Transaction Öğrenme Yolu (Learning Path)

## 📚 Önerilen Çalışma Sırası

### 🟢 SEVIYE 1: Temel Konular (Başlangıç)
Bu modüller transaction'ın temel kavramlarını öğretir. **Mutlaka bu sırayla çalışın!**

#### 1. **transaction-proxy** (Port: 8081) ⭐ İLK BAŞLA
**Neden ilk?** Transaction'ın nasıl çalıştığını anlamak için temel.
- @Transactional nasıl çalışır? (Proxy mekanizması)
- Spring AOP proxy kullanımı
- Transaction yönetimi method body'de değil, method çağrısı öncesi/sonrası çalışır

**Süre:** 1-2 saat

#### 2. **transaction-self-invocation** (Port: 8082)
**Neden ikinci?** Proxy mekanizmasını anladıktan sonra en yaygın hatayı öğrenin.
- Self-invocation problemi
- Aynı class içindeki @Transactional method neden çalışmaz?
- 3 farklı çözüm yöntemi

**Süre:** 1 saat

#### 3. **transaction-rollback** (Port: 8083)
**Neden üçüncü?** Transaction'ın ne zaman geri alındığını bilmek kritik.
- Rollback ne zaman olur?
- RuntimeException vs Checked Exception
- rollbackFor ve noRollbackFor kullanımı

**Süre:** 1 saat

#### 4. **transaction-propagation** (Port: 8084) ⭐ ÖNEMLİ
**Neden dördüncü?** En sık sorulan konu, gerçek hayatta çok kullanılır.
- Transaction propagation türleri
- REQUIRED, REQUIRES_NEW, NESTED, SUPPORTS, NOT_SUPPORTED, MANDATORY, NEVER
- Audit log örneği (REQUIRES_NEW)

**Süre:** 2-3 saat

#### 5. **transaction-isolation** (Port: 8085) ⭐ ÖNEMLİ
**Neden beşinci?** Database seviyesinde transaction anlayışı.
- Transaction isolation level'ları
- READ_COMMITTED, REPEATABLE_READ, SERIALIZABLE
- Dirty read, non-repeatable read, phantom read problemleri

**Süre:** 2-3 saat

---

### 🟡 SEVIYE 2: İleri Seviye Temel (Orta)
Temel konuları öğrendikten sonra bu modüllere geçin.

#### 6. **transaction-basics** (Port: 8092)
**Neden altıncı?** Temel transaction özelliklerini öğrenin.
- Read-only transaction
- Transaction timeout
- Transaction ne zaman commit edilir?
- @TransactionalEventListener

**Süre:** 1-2 saat

#### 7. **transaction-context** (Port: 8087)
**Neden yedinci?** ThreadLocal ve async işlemlerle transaction ilişkisi.
- Transaction context propagation (ThreadLocal)
- @Async + Transaction
- CompletableFuture ile transaction context kaybı

**Süre:** 1-2 saat

#### 8. **transaction-hibernate** (Port: 8088)
**Neden sekizinci?** Hibernate'in transaction ile nasıl çalıştığını anlayın.
- Hibernate dirty checking
- Detached entity
- Lost Update problemi
- Flush vs Commit

**Süre:** 2-3 saat

#### 9. **transaction-locking** (Port: 8089) ⭐ ÖNEMLİ
**Neden dokuzuncu?** Concurrency problemlerini çözmek için kritik.
- Optimistic locking (@Version)
- Pessimistic locking
- Deadlock'lar ve çözümleri
- Serialization failure

**Süre:** 2-3 saat

#### 10. **transaction-patterns** (Port: 8090) ⭐ ÖNEMLİ
**Neden onuncu?** Gerçek hayatta kullanılan pattern'leri öğrenin.
- Retry pattern
- Remote call (transaction içinde yasak)
- Batch processing
- Silent rollback
- Cache tutarsızlığı
- Message gönderimi
- Stream API tuzağı

**Süre:** 3-4 saat

#### 11. **transaction-theory** (Port: 8091)
**Neden on birinci?** Teorik bilgiler, pratik yaptıktan sonra daha anlamlı.
- Interface vs Class Annotation
- OSIV (Open Session in View)
- @Transactional testlerde
- Distributed transaction
- Transaction vs Eventual Consistency
- JVM crash senaryosu
- Exactly-once semantics

**Süre:** 2-3 saat

---

### 🔴 SEVIYE 3: İleri Seviye Advanced (İleri)
Temel ve orta seviyeyi tamamladıktan sonra bu modüllere geçin.

#### 12. **transaction-distributed** (Port: 8093) ⭐ ÖNEMLİ
**Neden on ikinci?** Microservices ve distributed system'lerde kritik.
- Saga Pattern (Orchestration, Choreography)
- 2PC (Two-Phase Commit)
- Compensation Pattern

**Süre:** 3-4 saat

#### 13. **transaction-microservices** (Port: 8097)
**Neden on üçüncü?** Distributed'tan sonra microservices pattern'leri.
- Outbox Pattern
- Idempotency
- Circuit Breaker Pattern

**Süre:** 2-3 saat

#### 14. **transaction-cqrs** (Port: 8096)
**Neden on dördüncü?** CQRS pattern'i transaction ile nasıl kullanılır.
- Command/Query Separation
- Read/Write Models
- Eventual Consistency

**Süre:** 2-3 saat

#### 15. **transaction-eventsourcing** (Port: 8095)
**Neden on beşinci?** Event sourcing ile transaction ilişkisi.
- Event Store
- Snapshot Pattern
- Event Replay

**Süre:** 2-3 saat

#### 16. **transaction-caching** (Port: 8094)
**Neden on altıncı?** Cache ile transaction tutarlılığı.
- Cache Coherence
- Cache Invalidation Strategies
- Write-Through vs Write-Behind

**Süre:** 2 saat

#### 17. **transaction-performance** (Port: 8098)
**Neden on yedinci?** Performance optimizasyonu.
- Connection Pooling
- Query Optimization
- N+1 Problem
- Batch Operations

**Süre:** 2-3 saat

#### 18. **transaction-security** (Port: 8099)
**Neden on sekizinci?** Security konuları.
- Row-Level Security
- Audit Trails
- Data Encryption

**Süre:** 2 saat

#### 19. **transaction-monitoring** (Port: 8100)
**Neden son?** Monitoring ve observability.
- Metrics Collection
- Distributed Tracing
- Performance Monitoring

**Süre:** 2 saat

---

## 📊 Özet Tablo

| Sıra | Modül | Port | Seviye | Süre | Öncelik |
|------|-------|------|--------|------|---------|
| 1 | transaction-proxy | 8081 | Temel | 1-2h | ⭐⭐⭐ |
| 2 | transaction-self-invocation | 8082 | Temel | 1h | ⭐⭐ |
| 3 | transaction-rollback | 8083 | Temel | 1h | ⭐⭐⭐ |
| 4 | transaction-propagation | 8084 | Temel | 2-3h | ⭐⭐⭐ |
| 5 | transaction-isolation | 8085 | Temel | 2-3h | ⭐⭐⭐ |
| 6 | transaction-basics | 8092 | Orta | 1-2h | ⭐⭐ |
| 7 | transaction-context | 8087 | Orta | 1-2h | ⭐⭐ |
| 8 | transaction-hibernate | 8088 | Orta | 2-3h | ⭐⭐ |
| 9 | transaction-locking | 8089 | Orta | 2-3h | ⭐⭐⭐ |
| 10 | transaction-patterns | 8090 | Orta | 3-4h | ⭐⭐⭐ |
| 11 | transaction-theory | 8091 | Orta | 2-3h | ⭐ |
| 12 | transaction-distributed | 8093 | İleri | 3-4h | ⭐⭐⭐ |
| 13 | transaction-microservices | 8097 | İleri | 2-3h | ⭐⭐ |
| 14 | transaction-cqrs | 8096 | İleri | 2-3h | ⭐⭐ |
| 15 | transaction-eventsourcing | 8095 | İleri | 2-3h | ⭐ |
| 16 | transaction-caching | 8094 | İleri | 2h | ⭐ |
| 17 | transaction-performance | 8098 | İleri | 2-3h | ⭐⭐ |
| 18 | transaction-security | 8099 | İleri | 2h | ⭐ |
| 19 | transaction-monitoring | 8100 | İleri | 2h | ⭐ |

## 🎯 Hızlı Başlangıç (Minimum Viable Path)

Eğer zamanınız kısıtlıysa, sadece şu modülleri çalışın:

1. **transaction-proxy** (8081) - Temel
2. **transaction-rollback** (8083) - Kritik
3. **transaction-propagation** (8084) - En önemli
4. **transaction-isolation** (8085) - Database seviyesi
5. **transaction-locking** (8089) - Concurrency
6. **transaction-patterns** (8090) - Gerçek hayat
7. **transaction-distributed** (8093) - Microservices

**Toplam Süre:** ~15-20 saat

## 💡 İpuçları

1. **Her modülü çalıştırın ve test edin** - Sadece okumak yeterli değil
2. **HTTP dosyalarını kullanın** - Her modülde hazır test dosyaları var
3. **Logları takip edin** - Transaction logları çok öğretici
4. **Hata yapın** - Yanlış kullanımları görerek öğrenin
5. **Kodları değiştirin** - Farklı senaryoları deneyin

## 🚀 Başlangıç Komutları

```bash
# 1. PostgreSQL'i başlat
docker-compose up -d

# 2. İlk modülü çalıştır
cd transaction-proxy
mvn spring-boot:run

# 3. HTTP dosyasını kullanarak test et
# transaction-proxy.http dosyasını IDE'de açın ve test edin
```

## 📝 Notlar

- Her modül bağımsız çalışır
- Tüm modüller aynı PostgreSQL veritabanını kullanır
- Her modül farklı bir port'ta çalışır
- Modüller arasında bağımlılık yok (sıralama öğrenme için)

**İyi çalışmalar! 🎓**



