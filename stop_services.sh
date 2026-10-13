#!/bin/bash

# CampusRide Services Stop Script

echo "=========================================="
echo "Stopping CampusRide Services"
echo "=========================================="
echo ""

RED='\033[0;31m'
GREEN='\033[0;32m'
NC='\033[0m'

# Read PIDs from file
if [ -f /tmp/campusride_pids.txt ]; then
    echo "Stopping services..."
    while read pid; do
        if ps -p $pid > /dev/null 2>&1; then
            kill $pid
            echo -e "${GREEN}✓ Stopped process $pid${NC}"
        fi
    done < /tmp/campusride_pids.txt
    rm /tmp/campusride_pids.txt
else
    echo -e "${RED}No PID file found. Services may not be running.${NC}"
    echo "Trying to kill by port..."
    
    # Kill by port
    for port in 8761 8081 8082 8083 8084 8080 5173; do
        PID=$(lsof -ti:$port)
        if [ -n "$PID" ]; then
            kill $PID
            echo -e "${GREEN}✓ Stopped service on port $port${NC}"
        fi
    done
fi

echo ""
echo -e "${GREEN}All services stopped!${NC}"
