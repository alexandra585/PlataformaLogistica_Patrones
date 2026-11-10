@echo off
REM Publica los cambios de esta carpeta en GitHub.
REM Requiere Git para Windows (https://git-scm.com) y haber iniciado sesion en GitHub.
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
  git fetch origin
  git reset origin/main
)
REM La estructura ahora es igual a la del proyecto del docente: el proyecto NetBeans
REM esta en la raiz. Se retiran las carpetas antiguas AppLogistica y base_datos.
git rm -r -q --ignore-unmatch AppLogistica base_datos >nul 2>nul
if exist AppLogistica rmdir /s /q AppLogistica
if exist base_datos rmdir /s /q base_datos
git add -A
git commit -m "Semana 13: Command y Memento en la nomina del empleado, reportes JasperReports y estructura igual a AppEmpresa"
git push -u origin main
echo.
echo Listo. Revise https://github.com/alexandra585/PlataformaLogistica_Patrones
pause
