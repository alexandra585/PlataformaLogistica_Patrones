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
REM Si esta carpeta solo tiene los archivos nuevos o modificados, se recuperan del
REM repositorio los que faltan (sin tocar los que ya estan aqui).
for /f "delims=" %%f in ('git ls-files -d') do git checkout -- "%%f"
REM Nombre para el commit si este equipo no tiene uno configurado
git config user.name >nul 2>nul || git config user.name "alexandra585"
git config user.email >nul 2>nul || git config user.email "alexandra585@users.noreply.github.com"
REM La estructura ahora es igual a la del proyecto del docente: el proyecto NetBeans
REM esta en la raiz. Se retiran del repositorio las carpetas antiguas.
git rm -r -q --cached --ignore-unmatch AppLogistica base_datos >nul 2>nul
REM El informe ahora se llama Final_Cap_I-XII: se retiran las versiones anteriores
if exist "docs\Informe_AppLogistica_Avance2_Cap_I-VI.docx" del /q "docs\Informe_AppLogistica_Avance2_Cap_I-VI.docx"
if exist "docs\Informe_AppLogistica_Avance2_Cap_I-VI.pdf" del /q "docs\Informe_AppLogistica_Avance2_Cap_I-VI.pdf"
if exist "docs\Informe_AppLogistica_Avance3_Cap_I-VIII.docx" del /q "docs\Informe_AppLogistica_Avance3_Cap_I-VIII.docx"
if exist "docs\Informe_AppLogistica_Avance3_Cap_I-VIII.pdf" del /q "docs\Informe_AppLogistica_Avance3_Cap_I-VIII.pdf"
git add -A
git commit -m "Version final: graficos circulares en reportes, bloqueo del login tras 3 intentos, GRASP completo (Creador, Controlador, Bajo acoplamiento, Alta cohesion), script SQL 04, README por semana e informe final Cap I-XII" --trailer "Co-Authored-By: Claude Opus 5.5 <noreply@anthropic.com>" --trailer "Claude-Session: https://claude.ai/code/session_017wfNEnPrVaBMsuFYxHMxP6"
if errorlevel 1 git commit -m "Version final: graficos circulares, bloqueo del login, GRASP completo, SQL 04, README por semana e informe final Cap I-XII"
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
