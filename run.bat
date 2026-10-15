@echo off
setlocal EnableExtensions EnableDelayedExpansion
cd /d "%~dp0"

if exist ".env" (
    for /f "usebackq delims== tokens=1,*" %%A in (".env") do (
        set "%%A=%%B"
    )
)

powershell.exe -NoProfile -ExecutionPolicy Bypass -File "%~dp0run.ps1"
exit /b %ERRORLEVEL%
