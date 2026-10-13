# CampusRide Database Exports

## Database Information
- **Database Name:** campusride
- **Export Date:** September 12, 2026
- **MySQL Version:** 9.7.1

## Files

### Full Backup
- `campusride_backup_20260912.sql` - Complete database dump (schema + data)

### Schema Only
- `campusride_schema.sql` - Database structure without data

### Data Only (Individual Tables)
- `campusride_users_data.sql` - Users table data
- `campusride_rides_data.sql` - Rides table data
- `campusride_vehicles_data.sql` - Vehicles table data
- `campusride_driver_requests_data.sql` - Driver requests table data
- `campusride_bookings_data.sql` - Bookings table data

### Legacy
- `campusride_users.sql` - Old users table export

## Database Structure

### Tables:
1. **users** - User accounts (students, drivers, admins)
2. **rides** - Ride offerings
3. **vehicles** - Vehicle registrations
4. **driver_requests** - Driver approval requests
5. **bookings** - Ride bookings

## How to Import

### Full Database
```bash
mysql -u root -p -e "CREATE DATABASE IF NOT EXISTS campusride;"
mysql -u root -p campusride < campusride_backup_20260912.sql
```

### Schema Only
```bash
mysql -u root -p campusride < campusride_schema.sql
```

### Individual Table Data
```bash
mysql -u root -p campusride < campusride_users_data.sql
mysql -u root -p campusride < campusride_rides_data.sql
# ... and so on
```

## Admin Credentials
- **Email:** admin@campusride.com
- **Password:** admin123

## Test User
- **Email:** testadmin@test.com
- **Password:** admin123
