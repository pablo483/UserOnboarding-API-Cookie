# User Onboarding API

Spring Boot 2.7 REST API for user registration, email verification, and JWT authentication with tokens stored in HTTP-only cookies.

## What’s inside
- Email-based sign-up/verification for USER and ADMIN roles.
- JWT access token (15 min) + refresh token rotation stored in DB and cookies.
- Role-protected endpoints (`/user/**` for authenticated users, `/admin/**` for admins).
- Cookie-based auth by default; Authorization header (Bearer) also works.

## Tech stack
- Java 11, Spring Boot 2.7.18, Spring Security, Spring Data JPA (MySQL 8.0)
- JWT via `jjwt` 0.12.5
- Spring Mail (Gmail SMTP)
- Lombok, Maven

## Prerequisites
- Java 11+, Maven 3.6+
- MySQL 8.0+ running and reachable
- Gmail account + App Password for SMTP

## Setup
1) Clone and enter the project
```bash
git clone <repo-url>
cd UserUnboardingApi
```

2) Configure properties  
Copy the example and fill in DB + mail settings:
```bash
cp src/main/resources/application.properties.example src/main/resources/application.properties
```
Key fields: `spring.datasource.url`, `spring.datasource.username`, `spring.datasource.password`, `spring.mail.*`.

JWT secret is currently hard-coded in `JwtUtils.SECRET_KEY`; set it via env/props for real deployments.

3) Create the database
```sql
CREATE DATABASE user_onboarding_db;
```

4) Build and run
```bash
mvn clean install
mvn spring-boot:run
```
Service runs on `http://localhost:8080`.

## Auth flow (cookies)
1. Register → verification email contains `/public/verify-account?token=...`.
2. After verifying, call `/public/login`; `accessToken` (15m, path `/`) and `refreshToken` cookies are issued.
3. When access token expires, call `/auth/refresh-token`; refresh token is read from the cookie, rotated, and new cookies are set.
4. Logout via `/public/log-out` removes refresh token from DB and clears cookies.

## Endpoints
- Public (no auth)
  - `POST /public/register` – register USER (email verification required)
  - `GET  /public/verify-account?token=...` – activate account
  - `POST /public/login` – issue auth cookies
  - `DELETE /public/log-out` – clear cookies and revoke refresh token
- Auth
  - `POST /auth/refresh-token` – rotate refresh token (uses cookie)
- User (requires auth)
  - `PUT /user` – update username/email/password for the current user
- Admin (ROLE_ADMIN)
  - `POST /admin/create-admin` – register admin (email verification)
  - `GET /admin` – list users
  - `GET /admin/{username}` – get user by username
  - `DELETE /admin/{username}` – delete user

## Tokens & cookies
- Access token: 15 minutes, cookie `accessToken`, path `/`, httpOnly, SameSite=Lax.
- Refresh token: cookie `refreshToken`, path `/auth/refresh-token`, httpOnly, SameSite=Lax. Stored in DB with a 7-hour server-side expiry and rotated on use; old token is deleted.
- For production, set `secure=true` on cookies and move secrets to env/config.

## Testing
```bash
mvn test
```


## Author
Sudarshan GC
