
#!/bin/bash

services=(
    "eureka-server"
    "api-gateway"
    "auth-service"
    "user-service"
    "ride-service"
    "booking-service"
)

echo "Stopping CampusRide services..."

for service in "${services[@]}"; do
    echo "Stopping $service..."

    pkill -f "spring-boot:run.*$service" 2>/dev/null
done

echo "Stop commands sent!"
