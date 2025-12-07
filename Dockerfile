# PostgreSQL için Dockerfile (mevcut)
FROM postgres:16-alpine

# PostgreSQL environment variables
ENV POSTGRES_DB=transaction_db
ENV POSTGRES_USER=postgres
ENV POSTGRES_PASSWORD=postgres

# Expose PostgreSQL port
EXPOSE 5432

# PostgreSQL will automatically create the database on first run
# Data will be persisted in a volume
