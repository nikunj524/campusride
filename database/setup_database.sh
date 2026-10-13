#!/bin/bash

# CampusRide Database Setup Script
# This script will create the database and import all data

echo "=========================================="
echo "CampusRide Database Setup"
echo "=========================================="
echo ""

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

# Check if MySQL is installed
if ! command -v mysql &> /dev/null
then
    echo -e "${RED}Error: MySQL is not installed!${NC}"
    echo "Please install MySQL first:"
    echo "  - macOS: brew install mysql"
    echo "  - Ubuntu: sudo apt install mysql-server"
    exit 1
fi

# Get MySQL credentials
echo -e "${YELLOW}Enter MySQL root username (default: root):${NC}"
read -r MYSQL_USER
MYSQL_USER=${MYSQL_USER:-root}

echo -e "${YELLOW}Enter MySQL root password (press Enter if no password):${NC}"
read -rs MYSQL_PASSWORD

# Test MySQL connection
if [ -z "$MYSQL_PASSWORD" ]; then
    MYSQL_CMD="mysql -u $MYSQL_USER"
else
    MYSQL_CMD="mysql -u $MYSQL_USER -p$MYSQL_PASSWORD"
fi

echo ""
echo "Testing MySQL connection..."
if ! $MYSQL_CMD -e "SELECT 1;" &> /dev/null; then
    echo -e "${RED}Error: Cannot connect to MySQL!${NC}"
    echo "Please check your username and password."
    exit 1
fi

echo -e "${GREEN}✓ MySQL connection successful${NC}"
echo ""

# Drop existing database if exists
echo "Checking for existing 'campusride' database..."
DB_EXISTS=$($MYSQL_CMD -e "SHOW DATABASES LIKE 'campusride';" | grep campusride)

if [ -n "$DB_EXISTS" ]; then
    echo -e "${YELLOW}Warning: Database 'campusride' already exists!${NC}"
    echo "Do you want to drop and recreate it? (yes/no)"
    read -r CONFIRM
    if [ "$CONFIRM" = "yes" ]; then
        echo "Dropping existing database..."
        $MYSQL_CMD -e "DROP DATABASE campusride;"
        echo -e "${GREEN}✓ Existing database dropped${NC}"
    else
        echo -e "${RED}Setup cancelled.${NC}"
        exit 0
    fi
fi

# Create database
echo "Creating database 'campusride'..."
$MYSQL_CMD -e "CREATE DATABASE campusride CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;"
echo -e "${GREEN}✓ Database created${NC}"
echo ""

# Import full backup
echo "Importing database schema and data..."
if [ -z "$MYSQL_PASSWORD" ]; then
    mysql -u $MYSQL_USER campusride < campusride_backup_20260912.sql
else
    mysql -u $MYSQL_USER -p$MYSQL_PASSWORD campusride < campusride_backup_20260912.sql
fi

if [ $? -eq 0 ]; then
    echo -e "${GREEN}✓ Database imported successfully${NC}"
else
    echo -e "${RED}✗ Error importing database${NC}"
    exit 1
fi

echo ""
echo "=========================================="
echo "Database Setup Complete!"
echo "=========================================="
echo ""
echo "Database Details:"
echo "  - Database Name: campusride"
echo "  - Tables: users, rides, vehicles, driver_requests, bookings"
echo ""
echo "Admin Credentials:"
echo "  - Email: admin@campusride.com"
echo "  - Password: admin123"
echo ""
echo "Test User:"
echo "  - Email: testadmin@test.com"
echo "  - Password: admin123"
echo ""
echo "Next Steps:"
echo "  1. Start all backend services (see start_services.sh)"
echo "  2. Open http://localhost:5173 in your browser"
echo "  3. Login with admin credentials"
echo ""
echo -e "${GREEN}Setup completed successfully! 🎉${NC}"
