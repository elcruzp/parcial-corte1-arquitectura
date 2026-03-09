@echo off
REM ========================================================================
REM SCRIPT DE EJECUCIÓN - CAMBIO 5: CONFIGURACIÓN EXTERNALIZADA
REM ========================================================================
REM ADR-010: Configuración Externalizada
REM Permite ejecutar con diferentes perfiles sin recompilar
REM ========================================================================

if "%1"=="local" goto local
if "%1"=="prod" goto prod
if "%1"=="test" goto test

echo Uso: run.bat [local^|prod^|test]
echo.
echo Ejemplos:
echo   run.bat local    - Desarrollo con configuracion local
echo   run.bat prod     - Produccion con configuracion segura
echo   run.bat test     - Tests con configuracion de testing
echo.
goto end

:local
echo Iniciando aplicacion en modo DESARROLLO...
echo Perfil: local
echo Configuracion: application-local.yml
echo Logging: DEBUG
echo Base de datos: Recrear esquema
set SPRING_PROFILES_ACTIVE=local
goto run

:prod
echo Iniciando aplicacion en modo PRODUCCION...
echo Perfil: prod
echo Configuracion: application-prod.yml
echo Logging: INFO + archivo
echo Base de datos: Validar esquema
echo.
echo ADVERTENCIA: Asegurate de configurar las variables de entorno:
echo   - DB_URL, DB_USER, DB_PASSWORD
echo   - JWT_SECRET (minimo 32 caracteres)
echo   - LOG_FILE (opcional)
echo.
set SPRING_PROFILES_ACTIVE=prod
goto run

:test
echo Iniciando aplicacion en modo TESTING...
echo Perfil: test
echo Configuracion: application-test.yml (si existe)
set SPRING_PROFILES_ACTIVE=test
goto run

:run
echo.
echo Variables de entorno actuales:
echo SPRING_PROFILES_ACTIVE=%SPRING_PROFILES_ACTIVE%
echo DB_URL=%DB_URL%
echo DB_USER=%DB_USER%
echo JWT_SECRET=%JWT_SECRET%
echo SERVER_PORT=%SERVER_PORT%
echo.
echo Presiona Ctrl+C para cancelar...
timeout /t 5 >nul
echo.
echo Ejecutando aplicacion...
mvn spring-boot:run
goto end

:end