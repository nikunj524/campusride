# CampusRide - Quick Start Guide 🚀

Complete ride-sharing platform for campus communities with Spring Boot microservices and React frontend.

---

## 📋 Prerequisites

Before starting, install:

1. **Java 17+** - [Download](https://adoptium.net/)
2. **Maven 3.8+** - `brew install maven` (macOS) or [Download](https://maven.apache.org/download.cgi)
3. **Node.js 18+** - [Download](https://nodejs.org/)
4. **MySQL 8.0+** - `brew install mysql` (macOS) or [Download](https://dev.mysql.com/downloads/)

---

## 🚀 Setup (3 Easy Steps)

### Step 1: Clone & Install Dependencies

```bash
# Clone the repository
cd /path/to/campusride

# Install frontend dependencies
cd frontend
npm install
cd ..
```

### Step 2: Setup Database

```bash
# Go to database folder
cd database

# Run setup script
chmod +x setup_database.sh
./setup_database.sh
```

The script will:
- Create `campusride` database
- Import all tables and data
- Show you the admin credentials

**Manual Setup?** See [database/SETUP_GUIDE.md](database/SETUP_GUIDE.md)

### Step 3: Start All Services

```bash
# Go back to project root
cd ..

# Start all services
chmod +x start_services.sh
./start_services.sh
```

Wait ~60 seconds for all services to start, then:

🎉 **Open http://localhost:5173** in your browser!

---

## 🔐 Default Login Credentials

### Admin Dashboard
```
Email: admin@campusride.com
Password: admin123
URL: http://localhost:5173/login
```

After login, you'll be redirected to `/admin/dashboard` where you can:
- View driver request statistics
- Approve/Reject driver applications
- Manage users

### Test User (Student)
```
Email: testadmin@test.com
Password: admin123
```

---

## 🛑 Stop Services

```bash
./stop_services.sh
```

---

## 📦 Project Structure

```
campusride/
├── backend/
│   ├── eureka-server/      # Service Discovery (Port 8761)
│   ├── api-gateway/        # API Gateway (Port 8080)
│   ├── auth-service/       # Authentication (Port 8081)
│   ├── user-service/       # Users & Drivers (Port 8082)
│   ├── ride-service/       # Rides (Port 8083)
│   └── booking-service/    # Bookings (Port 8084)
├── frontend/               # React App (Port 5173)
├── database/               # SQL files & setup scripts
│   ├── setup_database.sh   # Automated database setup
│   ├── SETUP_GUIDE.md      # Detailed database guide
│   └── campusride_backup_*.sql
├── start_services.sh       # Start all services
├── stop_services.sh        # Stop all services
└── QUICK_START.md          # This file
```

---

## 🌐 Service URLs

| Service | URL | Description |
|---------|-----|-------------|
| **Frontend** | http://localhost:5173 | Main application UI |
| **Eureka Dashboard** | http://localhost:8761 | Service registry |
| **API Gateway** | http://localhost:8080 | Routes to microservices |
| **Auth Service** | http://localhost:8081 | Login/Register |
| **User Service** | http://localhost:8082 | Users/Drivers/Vehicles |
| **Ride Service** | http://localhost:8083 | Rides management |
| **Booking Service** | http://localhost:8084 | Ride bookings |

---

## 🎯 Features

### For Students
- 🔍 Search and book rides
- 🚗 Request to become a driver
- 📱 View booking history
- 👤 Profile management

### For Drivers
- ➕ Create ride offerings
- 🚙 Register vehicles
- 📋 Manage rides
- 👥 View passenger bookings

### For Admins
- ✅ Approve/Reject driver requests
- 📊 View platform statistics
- 👥 Manage users
- 📈 Dashboard analytics

---

## 🔧 Configuration

### Database Connection

Edit `backend/*/src/main/resources/application.properties`:

```properties
spring.datasource.url=jdbc:mysql://localhost:3306/campusride
spring.datasource.username=root
spring.datasource.password=YOUR_PASSWORD
```

### Frontend API URL

Edit `frontend/src/api/client.js` if needed (default: http://localhost:8080)

---

## 📝 Development

### Run Individual Services

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

### Build for Production

```bash
# Backend services
cd backend/service-name
mvn clean package

# Frontend
cd frontend
npm run build
```

---

## 🐛 Troubleshooting

### Services Won't Start
- Check if ports are already in use: `lsof -i :8761,8080,8081,8082,8083,8084,5173`
- Kill processes using ports: `kill -9 $(lsof -t -i:PORT)`

### Database Connection Error
- Verify MySQL is running: `brew services list` (macOS)
- Check credentials in `application.properties`
- Re-run database setup: `cd database && ./setup_database.sh`

### Frontend Not Loading
- Clear browser cache
- Check if backend services are running on Eureka: http://localhost:8761
- Check browser console for errors (F12)

### Can't Login
- Verify database has admin user: `mysql -u root -p campusride -e "SELECT * FROM users WHERE role='ADMIN';"`
- Try test credentials: `testadmin@test.com` / `admin123`

---

## 📚 Testing the Application

### 1. Admin Workflow
1. Login as admin: `admin@campusride.com` / `admin123`
2. Go to "Driver Requests"
3. Approve pending requests

### 2. Driver Workflow
1. Register new user (or use existing student)
2. Click "Switch to Driver"
3. Wait for admin approval
4. Register vehicle
5. Create ride offering

### 3. Passenger Workflow
1. Login as student
2. Search available rides
3. Book a ride
4. View booking details with driver's vehicle info

---

## 🔄 Database Reset

To start fresh:

```bash
cd database
mysql -u root -p << EOF
DROP DATABASE IF EXISTS campusride;
CREATE DATABASE campusride;
EOF
mysql -u root -p campusride < campusride_backup_20260912.sql
```

---

## 📞 Support

If you encounter issues:
1. Check service logs in `/tmp/`:
   - eureka.log
   - auth-service.log
   - user-service.log
   - ride-service.log
   - booking-service.log
   - api-gateway.log
   - frontend.log

2. Verify all services are registered: http://localhost:8761

3. Check database connection: `mysql -u root -p campusride -e "SHOW TABLES;"`

---

## ✅ Quick Health Check

```bash
# Check if all services are running
curl http://localhost:8761  # Eureka
curl http://localhost:8081/actuator/health  # Auth
curl http://localhost:8082/actuator/health  # User
curl http://localhost:8083/actuator/health  # Ride
curl http://localhost:8084/actuator/health  # Booking
curl http://localhost:8080/actuator/health  # Gateway
curl http://localhost:5173  # Frontend
```

All should return successful responses!

---

## 🎉 You're Ready!

Open http://localhost:5173 and start using CampusRide! 🚗💨
