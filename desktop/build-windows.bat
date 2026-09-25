@echo off
echo ========================================================
echo   Building LeeTalk Windows Executables (.exe)
echo ========================================================
echo.
echo Installing dependencies...
call npm install
echo.
echo Packaging Windows Setup Installer (.exe) and Portable (.exe)...
call npm run dist
echo.
echo Build complete! Check the "dist" folder for:
echo   - LeeTalk-Setup-1.0.0.exe
echo   - LeeTalk-1.0.0-portable.exe
echo.
pause
