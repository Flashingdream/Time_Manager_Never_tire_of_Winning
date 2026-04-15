@echo off
echo Starting Vue.js frontend...
cd frontend
powershell -ExecutionPolicy Bypass -Command "npm run serve"
pause