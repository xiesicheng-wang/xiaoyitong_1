@echo off
REM ===== youlinbang local services starter (run as needed) =====
REM Starts Redis + MinIO + backend(8080). Frontend: cd web ^&^& npm run dev

echo [1/3] Starting Redis (port 6379)...
start "ylb-redis" /min cmd /c "cd /d E:\new_work\tools\redis && redis-server.exe --bind 127.0.0.1 --port 6379 --save \"\" > redis_server.log 2>&1"

echo [2/3] Starting MinIO (port 9000)...
start "ylb-minio" /min cmd /c "cd /d E:\new_work\tools\minio && set MINIO_ROOT_USER=minioadmin&& set MINIO_ROOT_PASSWORD=minioadmin&& minio.exe server E:\new_work\tools\minio\data --address 127.0.0.1:9000 --console-address 127.0.0.1:9001 > minio_server.log 2>&1"

echo [3/3] Starting backend (port 8080)...
start "ylb-backend" /min cmd /c "cd /d E:\bishe\youlinbang && set MYSQL_PASSWORD=123456&& \"C:\Program Files\Eclipse Adoptium\jdk-21.0.12.101-hotspot\bin\java.exe\" -jar target\youlinbang-0.1.0-SNAPSHOT.jar > backend.log 2>&1"

echo.
echo All services launched in minimized windows.
echo   Backend : http://localhost:8080/api/health
echo   Frontend: cd web ^&^& npm run dev  (then open http://localhost:5173)
echo First login requires Redis/MySQL running - if login fails, re-run this script.
pause
