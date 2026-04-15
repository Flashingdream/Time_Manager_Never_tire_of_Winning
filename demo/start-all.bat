@echo off
echo Starting both backend and frontend...

start cmd /k "call start-backend.bat"
timeout /t 10 /nobreak > nul
start cmd /k "call start-frontend.bat"

echo Both services are starting...
pause