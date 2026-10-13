# CampusRide Database Setup Guide

## 📋 Prerequisites

Before setting up the database, make sure you have:

1. **MySQL Server** installed and running
   - macOS: `brew install mysql`
   - Ubuntu: `sudo apt install mysql-server`
   - Windows: Download from [MySQL.com](https://dev.mysql.com/downloads/installer/)

2. **MySQL root access** (username and password)

---

## 🚀 Quick Setup (Automated)

### Option 1: Using Setup Script (Recommended)

1. Open terminal in the `database` folder
2. Make the script executable:
   ```bash
   chmod +x setup_database.sh
   ```
3. Run the setup script:
   ```bash
   ./setup_database.sh
   ```
4. Follow the prompts to enter your MySQL credentials
5. The script will:
   - Create the `campusride` database
   - Import all tables and data
   - Show you the admin credentials

---

## 📝 Manual Setup

### Step 1: Create Database

```bash
mysql -u root -p
```

Enter your MySQL password, then:

```sql
CREATE DATABASE campusride CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
EXIT;
```

### Step 2: Import Full Backup

```bash
mysql -u root -p campusride < campusride_backup_20260912.sql
```

### Step 3: Verify Import

```bash
mysql -u root -p campusride -e "SHOW TABLES;"
```

You should see 5 tables:
- bookings
- driver_requests
- rides
- users
- vehicles

---

## 🗂️ Database Files

### Full Backup (Use This!)
- **campusride_backup_20260912.sql** - Complete database with schema and all data

### Individual Files (For Reference)
- **campusride_schema.sql** - Database structure only
- **campusride_users_data.sql** - Users table data
- **campusride_rides_data.sql** - Rides table data
- **campusride_vehicles_data.sql** - Vehicles table data
- **campusride_driver_requests_data.sql** - Driver requests data
- **campusride_bookings_data.sql** - Bookings table data

---

## 📊 Database Structure

### Tables Overview

1. **users**
   - User accounts (students, drivers, admins)
   - Fields: id, first_name, last_name, email, password, phone_number, role, driver_eligible, created_at

2. **rides**
   - Ride offerings and requests
   - Fields: id, driver_id, passenger_id, source, destination, departure_time, available_seats, price_per_seat, status, type, notes, created_at, updated_at

3. **vehicles**
   - Vehicle registrations for drivers
   - Fields: id, user_id, vehicle_number, vehicle_type, vehicle_model, vehicle_color, total_seats, ownership_type, status, created_at, updated_at

4. **driver_requests**
   - Driver approval requests
   - Fields: id, user_id, status, requested_at, reviewed_at, reviewed_by

5. **bookings**
   - Ride booking records
   - Fields: id, ride_id, passenger_id, status, seats, total_price, created_at, updated_at

---

## 🔐 Default Credentials

### Admin Account
```
Email: admin@campusride.com
Password: admin123
```

### Test User
```
Email: testadmin@test.com
Password: admin123
```

---

## 📍 Database Connection Details

Update these in your Spring Boot `application.properties`:

```properties
spring.datasource.url=jdbc:mysql://localhost:3306/campusride
spring.datasource.username=root
spring.datasource.password=YOUR_MYSQL_PASSWORD
spring.jpa.hibernate.ddl-auto=update
```

---

## ❓ Troubleshooting

### Error: "Access denied for user 'root'@'localhost'"
- Make sure you're using the correct MySQL username and password
- Try: `mysql -u root -p` to verify credentials

### Error: "Unknown database 'campusride'"
- Database wasn't created. Run:
  ```bash
  mysql -u root -p -e "CREATE DATABASE campusride;"
  ```

### Error: "Table already exists"
- Database already has data. Either:
  - Drop and recreate: `DROP DATABASE campusride; CREATE DATABASE campusride;`
  - Or skip this import

### Can't connect to MySQL server
- Check if MySQL is running: `brew services list` (macOS) or `sudo service mysql status` (Linux)
- Start MySQL: `brew services start mysql` (macOS) or `sudo service mysql start` (Linux)

---

## 🔄 Fresh Install (Clean Slate)

To completely reset the database:

```bash
mysql -u root -p << EOF
DROP DATABASE IF EXISTS campusride;
CREATE DATABASE campusride CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
EOF

mysql -u root -p campusride < campusride_backup_20260912.sql
```

---

## 📦 What's Included in Backup

### Current Data (as of Sep 12, 2026)
- **8 users** (4 students, 3 drivers, 1 admin)
- **4 rides** (1 pending, 3 accepted)
- **0 vehicles** (ready for driver registrations)
- **2 driver requests** (1 pending, 1 approved)
- **0 bookings** (ready for ride bookings)

---

## ✅ Verification

After setup, verify everything is working:

```bash
mysql -u root -p campusride << EOF
SELECT COUNT(*) as user_count FROM users;
SELECT COUNT(*) as ride_count FROM rides;
SELECT email, role FROM users WHERE role = 'ADMIN';
EOF
```

You should see:
- 8 users
- 4 rides
- 1 admin (admin@campusride.com)

---

## 🆘 Need Help?

If you encounter issues:
1. Check the error message carefully
2. Verify MySQL is running
3. Confirm database credentials
4. Make sure you're in the `database` folder when running scripts

---

**Ready to start the application?** Go back to the project root and run:
```bash
cd ..
./start_services.sh
```
