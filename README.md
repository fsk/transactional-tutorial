# Spring Transaction Öğrenme Projesi

Bu proje, Spring Framework'te transaction yönetimini öğrenmek için hazırlanmış multi-module bir Maven projesidir.

## Proje Yapısı

Proje 6 modülden oluşmaktadır:

1. **transaction-proxy** (Port: 8081)
   - @Transactional nasıl çalışır? (Proxy mekanizması)
   - Spring AOP proxy kullanımı
   - Transaction yönetimi method body'de değil, method çağrısı öncesi/sonrası çalışır

2. **transaction-self-invocation** (Port: 8082)
   - Self-invocation problemi
   - Aynı class içindeki @Transactional method neden çalışmaz?
   - Çözüm yolları: Ayrı service, ApplicationContext, @Transactional'ı outer metoda koymak

3. **transaction-rollback** (Port: 8083)
   - Rollback ne zaman olur?
   - RuntimeException vs Checked Exception
   - rollbackFor ve noRollbackFor kullanımı

4. **transaction-propagation** (Port: 8084)
   - Transaction propagation türleri
   - REQUIRED, REQUIRES_NEW, NESTED, SUPPORTS, NOT_SUPPORTED, MANDATORY, NEVER
   - Audit log örneği (REQUIRES_NEW)

5. **transaction-isolation** (Port: 8085)
   - Transaction isolation level'ları
   - READ_COMMITTED, REPEATABLE_READ, SERIALIZABLE
   - Dirty read, non-repeatable read, phantom read problemleri
   - PostgreSQL MVCC davranışı

6. **transaction-advanced** (Port: 8086)
   - @Async + @Transactional
   - Read-only transaction
   - Transaction timeout
   - @TransactionalEventListener (AFTER_COMMIT, AFTER_ROLLBACK)
   - Transaction ne zaman commit edilir?
   - @Transactional + private method (16)
   - Final class / final method (17)
   - Interface vs Class Annotation (18)
   - Transaction + LazyInitializationException (19)
   - Flush vs Commit farkı (20)
   - Manual flush ne zaman kullanılır? (21)
   - Nested transaction (22)
   - Transaction boundary neden Controller'a kadar uzatılmaz? (23 - OSIV)
   - Transaction + Cache tutarsızlığı (24)
   - Deadlock'lar transaction ile nasıl ilişkilidir? (25)
   - Transaction propagation zinciri (26)
   - Message gönderimi transaction içinde mi olmalı? (27)
   - @Transactional testlerde neden farklı davranır? (28)
   - Read-only transaction içinde write olursa? (29)
   - Transaction + Stream API tuzağı (30)
   - Distributed transaction neden önerilmez? (31)
   - Transaction ile eventual consistency arasında nasıl karar verirsin? (32)

## Gereksinimler

- Java 25
- Maven 3.6+
- Docker ve Docker Compose (PostgreSQL için)

## Kurulum

### 1. PostgreSQL'i Başlatma

```bash
# Docker Compose ile PostgreSQL'i başlat
docker-compose up -d

# PostgreSQL'in hazır olduğunu kontrol et
docker-compose ps
```

### 2. Projeyi Build Etme

```bash
# Tüm modülleri build et
mvn clean install

# Sadece belirli bir modülü build et
mvn clean install -pl transaction-proxy
```

### 3. Modülleri Çalıştırma

Her modülü ayrı ayrı çalıştırabilirsiniz:

```bash
# transaction-proxy modülünü çalıştır
cd transaction-proxy
mvn spring-boot:run

# Veya IDE'den her modülün Application sınıfını çalıştırın
```

## Modül Portları

- transaction-proxy: **8081**
- transaction-self-invocation: **8082**
- transaction-rollback: **8083**
- transaction-propagation: **8084**
- transaction-isolation: **8085**
- transaction-advanced: **8086**

## HTTP Test Dosyaları

Her modül için hazır HTTP test dosyaları mevcuttur:

- `transaction-proxy/transaction-proxy.http`
- `transaction-self-invocation/transaction-self-invocation.http`
- `transaction-rollback/transaction-rollback.http`
- `transaction-propagation/transaction-propagation.http`
- `transaction-isolation/transaction-isolation.http`
- `transaction-advanced/transaction-advanced.http`

Bu dosyaları IntelliJ IDEA, VS Code (REST Client extension) veya benzeri araçlarla kullanabilirsiniz.

## API Endpoint'leri

### transaction-proxy

```bash
# @Transactional ile proxy üzerinden çağrı
curl -X POST http://localhost:8081/api/proxy/with-transaction \
  -H "Content-Type: application/json" \
  -d '{"name":"Test User","email":"test@example.com","age":25}'

# Transaction olmadan çağrı
curl -X POST http://localhost:8081/api/proxy/without-transaction \
  -H "Content-Type: application/json" \
  -d '{"name":"Test User 2","email":"test2@example.com","age":30}'
```

### transaction-self-invocation

```bash
# PROBLEM: Self-invocation
curl -X POST http://localhost:8082/api/self-invocation/problem \
  -H "Content-Type: application/json" \
  -d '{"name":"Problem Product","price":99.99}'

# ÇÖZÜM 1: @Transactional'ı outer metoda koymak
curl -X POST http://localhost:8082/api/self-invocation/solution1 \
  -H "Content-Type: application/json" \
  -d '{"name":"Solution1 Product","price":199.99}'

# ÇÖZÜM 2: ApplicationContext üzerinden proxy çağrısı
curl -X POST http://localhost:8082/api/self-invocation/solution2 \
  -H "Content-Type: application/json" \
  -d '{"name":"Solution2 Product","price":299.99}'

# ÇÖZÜM 3: Ayrı service'e taşımak (EN İYİ)
curl -X POST http://localhost:8082/api/self-invocation/solution3 \
  -H "Content-Type: application/json" \
  -d '{"name":"Solution3 Product","price":399.99}'
```

### transaction-rollback

```bash
# RuntimeException → Rollback olur
curl -X POST http://localhost:8083/api/rollback/runtime-exception \
  -H "Content-Type: application/json" \
  -d '{"orderNumber":"ORD-001","amount":100.0}'

# Checked Exception → Rollback olmaz (default)
curl -X POST http://localhost:8083/api/rollback/checked-exception \
  -H "Content-Type: application/json" \
  -d '{"orderNumber":"ORD-002","amount":200.0}'

# rollbackFor = Exception.class → Tüm exception'larda rollback
curl -X POST http://localhost:8083/api/rollback/rollback-for-all \
  -H "Content-Type: application/json" \
  -d '{"orderNumber":"ORD-003","amount":300.0}'
```

### transaction-propagation

```bash
# REQUIRED: Varsa mevcut transaction'a katılır
curl -X POST http://localhost:8084/api/propagation/required \
  -H "Content-Type: application/json" \
  -d '{"accountNumber":"ACC-001","balance":1000.0,"ownerName":"John Doe"}'

# REQUIRES_NEW: Yeni transaction açar
curl -X POST http://localhost:8084/api/propagation/requires-new \
  -H "Content-Type: application/json" \
  -d '{"accountNumber":"ACC-002","balance":2000.0,"ownerName":"Jane Doe"}'
```

### transaction-isolation

```bash
# READ_COMMITTED testi
curl -X PUT http://localhost:8085/api/isolation/read-committed/1?quantity=50

# REPEATABLE_READ testi
curl -X PUT http://localhost:8085/api/isolation/repeatable-read/1?quantity=75

# Phantom read testi
curl -X GET "http://localhost:8085/api/isolation/phantom-read-read-committed?productName=Laptop"
```

### transaction-advanced

```bash
# Read-only transaction
curl -X GET http://localhost:8086/api/advanced/employees

# Transaction timeout testi
curl -X POST http://localhost:8086/api/advanced/employee-timeout \
  -H "Content-Type: application/json" \
  -d '{"name":"Timeout Test","email":"timeout@example.com","department":"IT","salary":6000.0}'

# @TransactionalEventListener testi
curl -X POST http://localhost:8086/api/advanced/notification \
  -H "Content-Type: application/json" \
  -d '{"message":"Test Notification","recipient":"user@example.com"}'

# 16. @Transactional + private method testi
curl -X POST http://localhost:8086/api/advanced/private-method

# 20. Flush vs Commit testi
curl -X POST http://localhost:8086/api/advanced/flush-vs-commit

# 21. Batch insert (Manual flush)
curl -X POST http://localhost:8086/api/advanced/batch-insert

# 25. Deadlock testi
curl -X POST http://localhost:8086/api/advanced/deadlock-test?idA=1&idB=2

# 26. Transaction propagation zinciri
curl -X POST http://localhost:8086/api/advanced/propagation-chain \
  -H "Content-Type: application/json" \
  -d '{"name":"Chain Test","email":"chain@example.com"}'

# 27. Message gönderimi - PROBLEM
curl -X POST http://localhost:8086/api/advanced/message-problem \
  -H "Content-Type: application/json" \
  -d '{"name":"Message Problem","email":"message@example.com"}'

# 27. Message gönderimi - ÇÖZÜM
curl -X POST http://localhost:8086/api/advanced/message-solution \
  -H "Content-Type: application/json" \
  -d '{"name":"Message Solution","email":"message2@example.com"}'

# 24. Cache tutarsızlığı - PROBLEM
curl -X PUT http://localhost:8086/api/advanced/cache-problem/1?newSalary=10000

# 24. Cache tutarsızlığı - ÇÖZÜM
curl -X PUT http://localhost:8086/api/advanced/cache-solution/1?newSalary=10000

# 29. Read-only transaction içinde write
curl -X POST http://localhost:8086/api/advanced/read-only-write

# 30. Stream API tuzağı
curl -X GET http://localhost:8086/api/advanced/stream-trap

# Teorik konular
curl -X GET http://localhost:8086/api/advanced/theory/interface-vs-class
curl -X GET http://localhost:8086/api/advanced/theory/transaction-boundary
curl -X GET http://localhost:8086/api/advanced/theory/transactional-in-tests
curl -X GET http://localhost:8086/api/advanced/theory/distributed-transaction
curl -X GET http://localhost:8086/api/advanced/theory/transaction-vs-consistency

# 33. Transaction context propagation
curl -X POST http://localhost:8086/api/advanced/context/threadlocal
curl -X POST http://localhost:8086/api/advanced/context/async-problem

# 35-37. Hibernate advanced
curl -X PUT http://localhost:8086/api/advanced/hibernate/dirty-checking/1
curl -X PUT http://localhost:8086/api/advanced/hibernate/detached-entity/1
curl -X PUT http://localhost:8086/api/advanced/hibernate/lost-update/1?increment=1000

# 38-40. Locking
curl -X PUT http://localhost:8086/api/advanced/locking/optimistic/1?newSalary=10000
curl -X PUT http://localhost:8086/api/advanced/locking/pessimistic/1?newSalary=10000
curl -X PUT http://localhost:8086/api/advanced/locking/serialization/1?newSalary=10000

# 41-42. Retry ve Remote call
curl -X PUT http://localhost:8086/api/advanced/retry/inside-transaction/1?newSalary=10000
curl -X PUT http://localhost:8086/api/advanced/remote/inside-transaction/1?newSalary=10000

# 44. Time-sensitive logic
curl -X PUT http://localhost:8086/api/advanced/time-sensitive/1?newSalary=10000

# 45. Silent rollback
curl -X POST http://localhost:8086/api/advanced/silent-rollback \
  -H "Content-Type: application/json" \
  -d '{"name":"Silent Test","email":"silent@example.com"}'

# 49. Batch processing
curl -X POST http://localhost:8086/api/advanced/batch/wrong?count=1000
curl -X POST http://localhost:8086/api/advanced/batch/correct?totalCount=1000&chunkSize=100

# İleri seviye teorik konular
curl -X GET http://localhost:8086/api/advanced/theory/reactor-transaction
curl -X GET http://localhost:8086/api/advanced/theory/event-publishing
curl -X GET http://localhost:8086/api/advanced/theory/transaction-logging
curl -X GET http://localhost:8086/api/advanced/theory/jvm-crash
curl -X GET http://localhost:8086/api/advanced/theory/exactly-once
curl -X GET http://localhost:8086/api/advanced/theory/what-not-in-transaction
```

## Veritabanı

PostgreSQL Docker container'ı kullanılmaktadır:

- **Host**: localhost
- **Port**: 2345
- **Database**: transaction_db
- **Username**: postgres
- **Password**: postgres

## Loglama

Her modülde transaction logları aktif edilmiştir. Logları görmek için:

```properties
logging.level.org.springframework.transaction=DEBUG
logging.level.org.springframework.aop=DEBUG
```

## Öğrenme Konuları

### Temel Konular (1-15)
1. ✅ @Transactional nasıl çalışır? (Proxy mekanizması)
2. ✅ Self-invocation problemi ve çözümleri
3. ✅ Rollback ne zaman olur?
4. ✅ Transaction propagation türleri
5. ✅ Transaction isolation level'ları
6. ✅ @Transactional hangi katmanda olmalı?
7. ✅ Transaction + @Async birlikte çalışır mı?
8. ✅ Read-only transaction ne işe yarar?
9. ✅ Transaction ne zaman commit edilir?
10. ✅ Transaction timeout ne işe yarar?
11. ✅ Multiple datasource varsa ne olur?
12. ✅ @TransactionalEventListener ne zaman tetiklenir?
13. ✅ Repository methodları zaten transactional mı?
14. ✅ Transaction neden bazen "çalışmıyor gibi" görünür?
15. ✅ Transaction boundary'yi nasıl tasarlarsın?

### İleri Seviye Konular (16-32)
16. ✅ @Transactional + private method
17. ✅ Final class / final method meselesi
18. ✅ Interface vs Class Annotation
19. ✅ Transaction + LazyInitializationException
20. ✅ Flush vs Commit farkı
21. ✅ Manual flush ne zaman kullanılır?
22. ✅ Nested transaction var mı?
23. ✅ Transaction boundary neden Controller'a kadar uzatılmaz? (OSIV)
24. ✅ Transaction + Cache tutarsızlığı
25. ✅ Deadlock'lar transaction ile nasıl ilişkilidir?
26. ✅ Transaction propagation zinciri
27. ✅ Message gönderimi transaction içinde mi olmalı?
28. ✅ @Transactional testlerde neden farklı davranır?
29. ✅ Read-only transaction içinde write olursa?
30. ✅ Transaction + Stream API tuzağı
31. ✅ Distributed transaction neden önerilmez?
32. ✅ Transaction ile eventual consistency arasında nasıl karar verirsin?

### Çok İleri Seviye Konular (33-50)
33. ✅ Transaction context propagation (ThreadLocal)
34. ✅ Transaction + Reactor (WebFlux)
35. ✅ Hibernate dirty checking
36. ✅ Detached entity
37. ✅ Lost Update problemi
38. ✅ Optimistic locking
39. ✅ Pessimistic lock
40. ✅ Serialization failure
41. ✅ Retry + Transaction
42. ✅ Transaction içinde remote call
43. ✅ Event publishing
44. ✅ Transaction + Clock (time-sensitive logic)
45. ✅ Silent rollback
46. ✅ TransactionState logging
47. ✅ Transaction + JVM crash
48. ✅ Exactly-once semantics
49. ✅ Transaction + Batch Processing
50. ✅ Transaction'da olmaması gerekenler

## Notlar

- Her modül bağımsız olarak çalışabilir
- Tüm modüller aynı PostgreSQL veritabanını kullanır
- Her modül farklı bir port'ta çalışır
- Loglar transaction davranışını anlamak için detaylıdır

## Lisans

Bu proje eğitim amaçlıdır.

