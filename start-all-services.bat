@echo off
echo Starting all AI Platform services...

echo.
echo 1. Starting RAG Server...
start "RAG Server" cmd /k "cd /d %~dp0rag-server && conda activate rag && python main.py"

echo.
echo 2. Starting Backend Services...
cd /d "%~dp0ai-studio"
start "Backend Gateway" cmd /k "mvn spring-boot:run -pl ai-studio-gateway"

echo.
echo 3. Starting Frontend...
cd /d "%~dp0ai-studio-front"
start "Frontend" cmd /k "npm run dev"

echo.
echo All services are starting...
echo - RAG Server: http://localhost:8765
echo - Backend Gateway: http://localhost:8080
echo - Frontend: http://localhost:5173
echo.
pause
