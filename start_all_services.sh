
#!/bin/bash

ROOT_DIR="$(cd "$(dirname "$0")" && pwd)"
BACKEND_DIR="$ROOT_DIR/backend"

services=(
    "eureka-server"
    "api-gateway"
    "auth-service"
    "user-service"
    "ride-service"
    "booking-service"
)

echo "Starting CampusRide Microservices..."

for service in "${services[@]}"; do

    SERVICE_DIR="$BACKEND_DIR/$service"

    if [ ! -d "$SERVICE_DIR" ]; then
        echo "ERROR: $service folder not found!"
        continue
    fi

    echo "Opening terminal for $service..."

    mintty --title="$service" bash -lc "
        cd '$SERVICE_DIR' &&
        echo 'Starting $service...' &&
        mvn spring-boot:run;
        echo;
        echo '$service process stopped or failed.';
        read -p 'Press Enter to close this terminal...'
    " &

    if [ "$service" = "eureka-server" ]; then
        sleep 15
    else
        sleep 5
    fi

done

echo "All CampusRide service terminals opened!"
