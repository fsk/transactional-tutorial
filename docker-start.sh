#!/bin/bash

# Docker ile Transaction Projesini Başlatma Scripti

echo "🚀 Transaction Projesi Docker ile başlatılıyor..."

# Sadece PostgreSQL'i başlat
if [ "$1" == "postgres" ] || [ "$1" == "db" ]; then
    echo "📦 Sadece PostgreSQL başlatılıyor..."
    docker-compose up -d postgres
    echo "✅ PostgreSQL başlatıldı: localhost:5432"
    echo "   Database: transaction_db"
    echo "   User: postgres"
    echo "   Password: postgres"
    exit 0
fi

# Belirli bir modülü başlat
if [ -n "$1" ]; then
    MODULE_NAME="transaction-$1"
    echo "📦 Modül başlatılıyor: $MODULE_NAME"
    docker-compose up -d postgres
    echo "⏳ PostgreSQL'in hazır olması bekleniyor..."
    sleep 5
    docker-compose up -d $MODULE_NAME
    echo "✅ $MODULE_NAME başlatıldı!"
    docker-compose ps
    exit 0
fi

# Tüm modülleri başlat
echo "📦 Tüm modüller başlatılıyor..."
docker-compose up -d postgres
echo "⏳ PostgreSQL'in hazır olması bekleniyor..."
sleep 5
docker-compose up -d

echo ""
echo "✅ Tüm servisler başlatıldı!"
echo ""
echo "📊 Çalışan servisler:"
docker-compose ps
echo ""
echo "🌐 Erişilebilir endpoint'ler:"
echo "   - PostgreSQL: localhost:5432"
echo "   - Transaction Proxy: http://localhost:8081"
echo "   - Transaction Self Invocation: http://localhost:8082"
echo "   - Transaction Rollback: http://localhost:8083"
echo "   - Transaction Propagation: http://localhost:8084"
echo "   - Transaction Isolation: http://localhost:8085"
echo "   - Transaction Context: http://localhost:8087"
echo "   - Transaction Hibernate: http://localhost:8088"
echo "   - Transaction Locking: http://localhost:8089"
echo "   - Transaction Patterns: http://localhost:8090"
echo "   - Transaction Theory: http://localhost:8091"
echo "   - Transaction Basics: http://localhost:8092"
echo "   - Transaction Distributed: http://localhost:8093"
echo "   - Transaction Caching: http://localhost:8094"
echo "   - Transaction Event Sourcing: http://localhost:8095"
echo "   - Transaction CQRS: http://localhost:8096"
echo "   - Transaction Microservices: http://localhost:8097"
echo "   - Transaction Performance: http://localhost:8098"
echo "   - Transaction Security: http://localhost:8099"
echo "   - Transaction Monitoring: http://localhost:8100"
echo ""
echo "📝 Logları görmek için: docker-compose logs -f [service-name]"
echo "🛑 Durdurmak için: docker-compose down"


