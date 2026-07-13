@echo off
echo ========================================
echo   AY Ticket Booking System - Startup
echo ========================================
echo.

echo [1/3] Starting Docker infrastructure...
cd /d "C:\VS Holder\Project\AY-Booking-System\ticket-booking-system\infrastructure"
docker-compose up -d
echo Waiting 30 seconds for containers to be healthy...
timeout /t 30 /nobreak

echo.
echo [2/3] Setting database password...
docker exec ticketing-postgres psql -U ticketuser -d ticketdb -c "ALTER USER ticketuser WITH PASSWORD 'ticketpass';"

echo.
echo [3/3] Starting all 5 microservices...
echo Each service will open in its own window.
echo.

start "booking-service" cmd /k "cd /d C:\VS Holder\Project\AY-Booking-System\ticket-booking-system\booking-service && mvn spring-boot:run"
timeout /t 5 /nobreak

start "payment-service" cmd /k "cd /d C:\VS Holder\Project\AY-Booking-System\ticket-booking-system\payment-service && mvn spring-boot:run"
timeout /t 5 /nobreak

start "notification-service" cmd /k "cd /d C:\VS Holder\Project\AY-Booking-System\ticket-booking-system\notification-service && mvn spring-boot:run"
timeout /t 5 /nobreak

start "cancellation-service" cmd /k "cd /d C:\VS Holder\Project\AY-Booking-System\ticket-booking-system\cancellation-service && mvn spring-boot:run"
timeout /t 5 /nobreak

start "api-gateway" cmd /k "cd /d C:\VS Holder\Project\AY-Booking-System\ticket-booking-system\api-gateway && mvn spring-boot:run"

echo.
echo ========================================
echo All services starting up!
echo Wait 60 seconds for all to be ready.
echo.
echo Ports:
echo   api-gateway        : http://localhost:8080
echo   booking-service    : http://localhost:8081
echo   payment-service    : http://localhost:8082
echo   notification-service: http://localhost:8083
echo   cancellation-service: http://localhost:8084
echo   kafka-ui           : http://localhost:8090
echo ========================================
pause