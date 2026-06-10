@echo off
echo Starting RAG Server...
cd /d "%~dp0rag-server"
call conda activate rag
python main.py
pause
