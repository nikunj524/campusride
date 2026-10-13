#!/bin/bash

# CampusRide Services Start Script
# This script will start all microservices in the correct order

echo "=========================================="
echo "CampusRide Services Startup"
echo "=========================================="
echo ""

# Colors
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m'

# Check if Maven is installed
if ! command -v mvn &> /dev/null
then
    echo -e "${YELLOW}Warning: Maven is not installed!${NC}"
    echo "Please install Maven first:"
    echo "  - macOS: brew install maven"
    echo "  - Ubuntu: sudo apt install maven"
    exit 1
fi

# Check if Node.js is installed
if ! command -v node &> /dev/null
then
    echo -e "${YELLOW}Warning: Node.js is not installed!${NC}"
    echo "Please install Node.js first:"
    echo "  - Visit: https://nodejs.org/"
    exit 1
fi

# Get script directory
SCRIPT_DIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd )"
BACKEND_DIR="$SCRIPT_DIR/backend"
FRONTEND_DIR="$SCRIPT_DIR/frontend"

echo -e "${BLUE}Starting services in background...${NC}"
echo ""

# Start Eureka Server
echo "1. Starting Eureka Server (Port 8761)..."
cd "$BACKEND_DIR/eureka-server"
mvn spring-boot:run > /tmp/eureka.log 2>&1 &
EUREKA_PID=$!
echo -e "${GREEN}✓ Eureka Server started (PID: $EUREKA_PID)${NC}"
sleep 15

# Start Auth Service
echo "2. Starting Auth Service (Port 8081)..."
cd "$BACKEND_DIR/auth-service"
mvn spring-boot:run > /tmp/auth-service.log 2>&1 &
AUTH_PID=$!
echo -e "${GREEN}✓ Auth Service started (PID: $AUTH_PID)${NC}"
sleep 10

# Start User Service
echo "3. Starting User Service (Port 8082)..."
cd "$BACKEND_DIR/user-service"
mvn spring-boot:run > /tmp/user-service.log 2>&1 &
USER_PID=$!
echo -e "${GREEN}✓ User Service started (PID: $USER_PID)${NC}"
sleep 10

# Start Ride Service
echo "4. Starting Ride Service (Port 8083)..."
cd "$BACKEND_DIR/ride-service"
mvn spring-boot:run > /tmp/ride-service.log 2>&1 &
RIDE_PID=$!
echo -e "${GREEN}✓ Ride Service started (PID: $RIDE_PID)${NC}"
sleep 10

# Start Booking Service
echo "5. Starting Booking Service (Port 8084)..."
cd "$BACKEND_DIR/booking-service"
mvn spring-boot:run > /tmp/booking-service.log 2>&1 &
BOOKING_PID=$!
echo -e "${GREEN}✓ Booking Service started (PID: $BOOKING_PID)${NC}"
sleep 10

# Start API Gateway
echo "6. Starting API Gateway (Port 8080)..."
cd "$BACKEND_DIR/api-gateway"
mvn spring-boot:run > /tmp/api-gateway.log 2>&1 &
GATEWAY_PID=$!
echo -e "${GREEN}✓ API Gateway started (PID: $GATEWAY_PID)${NC}"
sleep 15

# Start Frontend
echo "7. Starting Frontend (Port 5173)..."
cd "$FRONTEND_DIR"
npm run dev > /tmp/frontend.log 2>&1 &
FRONTEND_PID=$!
echo -e "${GREEN}✓ Frontend started (PID: $FRONTEND_PID)${NC}"
sleep 5

echo ""
echo "=========================================="
echo "All Services Started!"
echo "=========================================="
echo ""
echo "Service Status:"
echo "  ✓ Eureka Server   : http://localhost:8761 (PID: $EUREKA_PID)"
echo "  ✓ Auth Service    : http://localhost:8081 (PID: $AUTH_PID)"
echo "  ✓ User Service    : http://localhost:8082 (PID: $USER_PID)"
echo "  ✓ Ride Service    : http://localhost:8083 (PID: $RIDE_PID)"
echo "  ✓ Booking Service : http://localhost:8084 (PID: $BOOKING_PID)"
echo "  ✓ API Gateway     : http://localhost:8080 (PID: $GATEWAY_PID)"
echo "  ✓ Frontend        : http://localhost:5173 (PID: $FRONTEND_PID)"
echo ""
echo "Log Files Location: /tmp/"
echo "  - eureka.log"
echo "  - auth-service.log"
echo "  - user-service.log"
echo "  - ride-service.log"
echo "  - booking-service.log"
echo "  - api-gateway.log"
echo "  - frontend.log"
echo ""
echo "Admin Login:"
echo "  URL: http://localhost:5173/login"
echo "  Email: admin@campusride.com"
echo "  Password: admin123"
echo ""
echo "To stop all services, run: ./stop_services.sh"
echo ""
echo -e "${GREEN}Services are ready! Open http://localhost:5173 🚀${NC}"

# Save PIDs to file for stop script
cat > /tmp/campusride_pids.txt << EOF
$EUREKA_PID
$AUTH_PID
$USER_PID
$RIDE_PID
$BOOKING_PID
$GATEWAY_PID
$FRONTEND_PID
EOF
