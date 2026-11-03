@echo off
REM Sube esta carpeta al repositorio de GitHub (requiere Git instalado: https://git-scm.com)
cd /d "%~dp0"
if not exist .git (
  git init
  git branch -M main
  git remote add origin https://github.com/alexandra585/PlataformaLogistica_Patrones.git
)
git add -A
git commit -m "AppLogistica: avance 2 (capitulos I al VI) con README"
git push -u origin main
echo.
echo Listo. Revisa https://github.com/alexandra585/PlataformaLogistica_Patrones
pause
