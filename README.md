# 🚗 CampusRide - Campus Ride Sharing Platform

Complete microservices-based ride-sharing application for campus communities built with Spring Boot and React.

## 🚀 Quick Start (3 Steps)

```bash
# 1. Setup Database
cd database
./setup_database.sh

# 2. Start All Services
cd ..
./start_services.sh

# 3. Open in Browser
# http://localhost:5173
```

**Admin Login:** `admin@campusride.com` / `admin123`

📖 **Detailed Guide:** See [QUICK_START.md](QUICK_START.md)

---

## ✨ Features

### 👨‍🎓 For Students
- Search and book rides
- Request driver approval
- View booking history
- Profile management

### 🚗 For Drivers  
- Create ride offerings
- Register vehicles
- Manage rides
- Accept bookings

### 👨‍💼 For Admins
- Approve/Reject driver requests
- View platform statistics
- User management
- Dashboard analytics

---

## 🏗️ Architecture

### Microservices
- **Eureka Server** (8761) - Service Discovery
- **API Gateway** (8080) - Request Routing
- **Auth Service** (8081) - Authentication & Authorization
- **User Service** (8082) - User, Driver, Vehicle Management
- **Ride Service** (8083) - Ride Operations
- **Booking Service** (8084) - Booking Management

### Frontend
- **React + Vite** (5173) - Modern UI with Tailwind CSS

### Database
- **MySQL** - Relational database with 5 tables (users, rides, vehicles, driver_requests, bookings)

---

## 📦 Tech Stack

**Backend:**
- Java 17
- Spring Boot 4.1.0
- Spring Cloud (Eureka, Gateway)
- Spring Security + JWT
- MySQL 8.0+
- Maven

**Frontend:**
- React 18
- Vite
- React Router
- Axios
- Lucide Icons

---

## 📁 Project Structure

```
campusride/
├── backend/
│   ├── eureka-server/      # Service Registry (Port 8761)
│   ├── api-gateway/        # API Gateway (Port 8080)
│   ├── auth-service/       # Authentication (Port 8081)
│   ├── user-service/       # Users & Drivers (Port 8082)
│   ├── ride-service/       # Rides (Port 8083)
│   └── booking-service/    # Bookings (Port 8084)
├── frontend/               # React Application (Port 5173)
├── database/               # Database setup & SQL files
│   ├── setup_database.sh   # Automated database setup
│   ├── SETUP_GUIDE.md      # Detailed database guide
│   ├── campusride_backup_20260912.sql  # Full backup
│   └── README.md           # Database documentation
├── start_services.sh       # Start all services
├── stop_services.sh        # Stop all services
├── QUICK_START.md          # Complete setup guide
└── README.md               # This file
```

---

## 🔧 Development Setup

See [QUICK_START.md](QUICK_START.md) for complete setup instructions.

### Prerequisites
- Java 17+ ([Download](https://adoptium.net/))
- Maven 3.8+ (`brew install maven`)
- Node.js 18+ ([Download](https://nodejs.org/))
- MySQL 8.0+ (`brew install mysql`)

### Environment Variables

The following environment variables are automatically configured in `.env` files, but you can customize them if needed:

| Variable | Description | Default |
|----------|-------------|---------|
| DB_USERNAME | MySQL Username | root |
| DB_PASSWORD | MySQL Password | (empty) |
| JWT_SECRET | JWT signing secret | (pre-configured) |

---

## 🌐 Service URLs

| Service | URL | Purpose |
|---------|-----|---------|
| **Frontend** | http://localhost:5173 | Main application UI |
| **Eureka Dashboard** | http://localhost:8761 | Service registry monitoring |
| **API Gateway** | http://localhost:8080 | Routes requests to microservices |
| **Auth Service** | http://localhost:8081 | Login/Register APIs |
| **User Service** | http://localhost:8082 | User/Driver/Vehicle APIs |
| **Ride Service** | http://localhost:8083 | Ride management APIs |
| **Booking Service** | http://localhost:8084 | Booking APIs |

---

## 🔐 Default Credentials

### Admin Account
```
Email: admin@campusride.com
Password: admin123
```

### Test User (Student)
```
Email: testadmin@test.com
Password: admin123
```

---

## 🎯 Key Workflows

### 1. Driver Approval Flow
1. Student registers and requests driver access
2. Admin reviews request in admin dashboard
3. Admin approves/rejects the request
4. Approved user gains driver privileges
5. Driver registers vehicle
6. Driver creates ride offerings

### 2. Ride Booking Flow
1. Passenger searches available rides
2. Passenger books a ride
3. Ride status changes to "ACCEPTED"
4. Passenger sees driver's vehicle details
5. Driver manages the ride (start/complete)

### 3. Admin Management
1. Login as admin
2. View driver request statistics
3. Filter requests by status
4. Approve/Reject with confirmation
5. Monitor platform activity

---

## 📚 Documentation

- **[Quick Start Guide](QUICK_START.md)** - Complete setup and usage instructions
- **[Database Setup Guide](database/SETUP_GUIDE.md)** - Database configuration details
- **[Database README](database/README.md)** - Database structure and import instructions

---

## 🐛 Troubleshooting

### Services Won't Start
```bash
# Check if ports are in use
lsof -i :8761,8080,8081,8082,8083,8084,5173

# Kill processes on specific port
kill -9 $(lsof -t -i:PORT)
```

### Database Connection Error
```bash
# Verify MySQL is running
brew services list  # macOS
sudo service mysql status  # Linux

# Re-run database setup
cd database && ./setup_database.sh
```

### Can't Login
```bash
# Check if admin user exists
mysql -u root -p campusride -e "SELECT email, role FROM users WHERE role='ADMIN';"

# If not, re-import database
cd database
mysql -u root -p campusride < campusride_backup_20260912.sql
```

### Frontend Not Loading
- Clear browser cache and reload
- Check if all backend services are UP on Eureka: http://localhost:8761
- Check browser console for errors (F12)
- Verify API Gateway is running: `curl http://localhost:8080/actuator/health`

**More help:** See [QUICK_START.md](QUICK_START.md) troubleshooting section

---

## 🛑 Stop Services

```bash
./stop_services.sh
```

This will gracefully stop all running services.

---

## 🔄 Database Reset

To start with a fresh database:

```bash
cd database
mysql -u root -p << EOF
DROP DATABASE IF EXISTS campusride;
CREATE DATABASE campusride CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
EOF
mysql -u root -p campusride < campusride_backup_20260912.sql
```

---

## 📊 Database Schema

### Tables

1. **users** - User accounts (students, drivers, admins)
2. **rides** - Ride offerings and requests
3. **vehicles** - Vehicle registrations for drivers
4. **driver_requests** - Driver approval workflow
5. **bookings** - Ride booking records

See [database/SETUP_GUIDE.md](database/SETUP_GUIDE.md) for detailed schema.

---

## 🚦 Running Individual Services

```bash
# Eureka Server
cd backend/eureka-server && mvn spring-boot:run

# Auth Service
cd backend/auth-service && mvn spring-boot:run

# User Service
cd backend/user-service && mvn spring-boot:run

# Ride Service
cd backend/ride-service && mvn spring-boot:run

# Booking Service
cd backend/booking-service && mvn spring-boot:run

# API Gateway
cd backend/api-gateway && mvn spring-boot:run

# Frontend
cd frontend && npm run dev
```

---

## 🏗️ Build for Production

```bash
# Build all backend services
cd backend/eureka-server && mvn clean package
cd ../api-gateway && mvn clean package
cd ../auth-service && mvn clean package
cd ../user-service && mvn clean package
cd ../ride-service && mvn clean package
cd ../booking-service && mvn clean package

# Build frontend
cd ../../frontend && npm run build
```

JAR files will be in `target/` folders, frontend build in `frontend/dist/`

---

## ✅ Health Check

Verify all services are running:

```bash
curl http://localhost:8761  # Eureka
curl http://localhost:8081/actuator/health  # Auth
curl http://localhost:8082/actuator/health  # User
curl http://localhost:8083/actuator/health  # Ride
curl http://localhost:8084/actuator/health  # Booking
curl http://localhost:8080/actuator/health  # Gateway
curl http://localhost:5173  # Frontend
```

---

## 📝 Testing the Application

### 1. Test Admin Features
1. Login: `admin@campusride.com` / `admin123`
2. Navigate to "Driver Requests"
3. View statistics dashboard
4. Approve/Reject pending requests

### 2. Test Driver Features
1. Register new student account
2. Click "Switch to Driver"
3. Wait for admin approval (or approve via admin panel)
4. Register vehicle in Driver Dashboard
5. Create ride offering

### 3. Test Passenger Features
1. Login as student
2. Search available rides
3. Book a ride
4. View ride details with driver vehicle info

---

## 🔒 Security

- JWT-based authentication
- BCrypt password hashing
- CORS configuration
- Role-based access control (STUDENT, DRIVER, ADMIN)
- Secure password reset flow

---

## 🌟 Future Enhancements

- [ ] Real-time ride tracking
- [ ] In-app messaging
- [ ] Payment integration
- [ ] Rating system
- [ ] Email notifications
- [ ] Mobile app (React Native)

---

## 📄 License

MIT License - feel free to use this project for learning or commercial purposes.

---

## 👨‍💻 Developer

**Hardik Makwana**

---

## 🎉 Quick Start Summary

```bash
# Complete setup in 3 commands
cd database && ./setup_database.sh
cd .. && ./start_services.sh
# Open http://localhost:5173 and login!
```

**Happy Coding! 🚀**
