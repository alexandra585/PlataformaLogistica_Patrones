@echo off
REM Publica esta carpeta en https://github.com/alexandra585/PlataformaLogistica_Patrones
REM Requiere Git para Windows (https://git-scm.com). La primera vez se abre el navegador
REM para iniciar sesion en GitHub con la cuenta alexandra585.
cd /d "%~dp0"
where git >nul 2>nul
if errorlevel 1 (
  echo No se encontro Git. Instalelo desde https://git-scm.com y vuelva a ejecutar este archivo.
  pause
  exit /b 1
)
if not exist .git (
  git init
  git branch -M main
  git remote add origin https://github.com/alexandra585/PlataformaLogistica_Patrones.git
)
git fetch origin
if errorlevel 1 goto error
git reset origin/main
REM Nombre para el commit si este equipo no tiene uno configurado
git config user.name >nul 2>nul || git config user.name "alexandra585"
git config user.email >nul 2>nul || git config user.email "alexandra585@users.noreply.github.com"
REM La estructura ahora es igual a la del proyecto del docente: el proyecto NetBeans
REM esta en la raiz. Se retiran del repositorio las carpetas antiguas.
git rm -r -q --cached --ignore-unmatch AppLogistica base_datos >nul 2>nul
git add -A
git commit -m "Semana 13: Command y Memento en la nomina, reportes JasperReports, scripts SQL 01 y 02, informe actualizado y estructura igual a AppEmpresa"
git push -u origin main
if errorlevel 1 goto error
echo.
echo Listo. Revise https://github.com/alexandra585/PlataformaLogistica_Patrones
pause
exit /b 0
:error
echo.
echo Ocurrio un error. Copie el mensaje de arriba y envielo para revisarlo.
pause
exit /b 1
