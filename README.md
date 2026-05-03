# CineLog — Backend

Spring Boot REST API for CineLog.

## Prerequisites

- Java 21+
- PostgreSQL running on `localhost:5432` with a database named `cinelog`

## Environment variables

Create a `.env` file or export these before running:

```
DB_USERNAME=your_db_user
DB_PASSWORD=your_db_password

JWT_SECRET=your_jwt_secret

MAIL_HOST=smtp.example.com
MAIL_PORT=587
MAIL_USERNAME=your@email.com
MAIL_PASSWORD=your_mail_password
MAIL_FROM=your@email.com

GOOGLE_CLIENT_ID=your_google_client_id
```

## Run

```bash
./gradlew bootRun
```

Server starts on `http://localhost:8080`. The schema is managed automatically by Hibernate (`ddl-auto: update`).

## First admin user

Run this SQL against your `cinelog` database after the first startup:

```sql
INSERT INTO users (username, email, password_hash, auth_provider, role, email_verified, email_verified_at, created_at)
VALUES ('admin', 'admin@cinelog.com', '$2a$10$.wp1Zre.qNLnpux1T7fzXeozfFJHm50cywgLtsYffz.PXc/quZCl6', 'LOCAL', 'ADMIN', true, NOW(), NOW());
```

Default password is `Admin@123`
