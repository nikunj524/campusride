# CampusRide

CampusRide is a Spring Boot Microservices based ride-sharing platform developed for college students.

## Technology Stack

### Frontend
- React
- Vite
- Tailwind CSS

### Backend
- Spring Boot
- Spring Cloud Gateway
- Eureka Server
- Spring Security
- Spring Data JPA

### Database
- MySQL Community Server 8.0

### Tools
- Git
- GitHub
- Maven
- Postman
- Docker

---

## Microservices

- Eureka Server
- API Gateway
- Auth Service

---

## Environment Variables

Create the following environment variables before running the project.

| Variable | Description |
|----------|-------------|
| DB_USERNAME | MySQL Username |
| DB_PASSWORD | MySQL Password |
| JWT_SECRET | Base64-encoded secret of at least 32 bytes, used to sign authentication tokens |

Example PowerShell command for local development:

```powershell
$env:JWT_SECRET = [Convert]::ToBase64String([Text.Encoding]::UTF8.GetBytes("replace-with-a-long-random-development-secret"))
```

---

## Run Order

1. Eureka Server
2. API Gateway
3. Auth Service

---

## Auth API

The Auth Service runs at `http://localhost:8081`.

| Method | Endpoint | Description |
|--------|----------|-------------|
| POST | `/auth/register` | Creates a Student or Driver account and returns a JWT |
| POST | `/auth/login` | Authenticates a user and returns a JWT |
| GET | `/auth/profile` | Returns the authenticated user's profile; requires `Authorization: Bearer <token>` |

The React frontend runs at `http://localhost:5173` and connects directly to the Auth Service by default. Set `VITE_AUTH_API_URL` only when using a different Auth Service URL.

---

## Author

Nikunj Mevada
