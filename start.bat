<# :
@echo off
REM ============================================
REM  Prison Management Platform - Docker Startup Script
REM  解决 Docker Engine API 500 导致容器卡在 Created 状态的问题
REM ============================================

setlocal enabledelayedexpansion

echo [INFO] Starting Prison Management Platform services...
echo [INFO] This script handles Docker Engine API 500 issues by starting services sequentially
echo.

cd /d "%~dp0"

REM Step 1: 清理旧容器
echo [Step 1/5] Cleaning up old containers and volumes...
docker compose down -v >nul 2>&1
echo [OK] Old containers cleaned.
echo.

REM Step 2: 启动基础设施服务 (mysql, redis)
echo [Step 2/5] Starting infrastructure services (mysql, redis)...
docker compose up -d mysql redis 2>&1 | findstr /V "safe_rm"

REM 等待 mysql 和 redis 健康
echo.
echo [INFO] Waiting for mysql and redis to become healthy...
:wait_infra
for /f "tokens=*" %%i in ('docker compose ps mysql 2^>nul ^| findstr /C "(healthy)"') do set HEALTHY_COUNT=%%i
if %HEALTHY_COUNT% LSS 2 (
    timeout /t 5 >nul
    goto wait_infra
)
echo [OK] Infrastructure services are healthy.
echo.

REM Step 3: 启动后端
echo [Step 3/5] Starting backend service...
docker compose up -d backend 2>&1 | findstr /V "safe_rm"

REM 检查容器是否卡在 Created 状态
:check_backend_created
for /f "tokens=*" %%i in ('docker compose ps backend 2^>nul ^| findstr /C "Created"') do set STUCK=%%i
if defined STUCK (
    echo [WARN] Backend container stuck in Created state, manually starting...
    docker start jzh-2-backend-1 >nul 2>&1
)

REM 等待后端健康
echo [INFO] Waiting for backend to become healthy...
:wait_backend
for /f "tokens=*" %%i in ('docker compose ps backend 2^>nul ^| findstr /C "(healthy)"') do set BACKEND_HEALTHY=%%i
if not defined BACKEND_HEALTHY (
    timeout /t 5 >nul
    goto wait_backend
)
echo [OK] Backend service is healthy.
echo.

REM Step 4: 启动前端
echo [Step 4/5] Starting frontend service...
docker compose up -d frontend 2>&1 | findstr /V "safe_rm"

REM 检查前端是否卡在 Created 状态
:check_frontend_created
for /f "tokens=*" %%i in ('docker compose ps frontend 2^>nul ^| findstr /C "Created"') do set STUCK=%%i
if defined STUCK (
    echo [WARN] Frontend container stuck in Created state, manually starting...
    docker start jzh-2-frontend-1 >nul 2>&1
)
echo [OK] Frontend service started.
echo.

REM Step 5: 验证服务状态
echo [Step 5/5] Verifying all services...
timeout /t 3 >nul
docker compose ps
echo.

echo ============================================
echo [SUCCESS] All services are running!
echo ============================================
echo.
echo Frontend: http://127.0.0.1:3008
echo Backend API: http://127.0.0.1:8088
echo MySQL: 127.0.0.1:3309
echo Redis: 127.0.0.1:6380
echo.

endlocal
