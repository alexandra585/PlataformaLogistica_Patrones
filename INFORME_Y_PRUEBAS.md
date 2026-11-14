# Informe de cambios y pruebas – actualización de las semanas 14 y 17

## Cambios de esta versión

- **Aportes del equipo (integrados):** buscador modal del remitente (`ClienteBuscarDialog`) en el registro de
  envíos, gráficos de barras en los seis reportes `.jrxml` y la opción *Archivo › Cerrar sesión*.
- **Semana 14 – Antipatrones (Cap. VII):** se documentan los antipatrones de desarrollo y organizacionales que el
  diseño evitó o corrigió, con la tabla antipatrón → patrón que lo reemplaza. Se replica el caso de clase del
  Empleado: `EmpleadoAntipatron` (solo didáctico) frente a `DescuentoStrategy`, `DescuentoAFP`, `DescuentoSNP`,
  `DescuentoFactory` y `ParametrosPlanilla`.
- **Semana 17 – GRASP (Cap. VIII):** nueva **boleta de pago**. Experto en información: `EmpleadoNomina` calcula
  horas extras, aporte, salud y neto. Polimorfismo: `DescuentoStrategy` (AFP 11 %, SNP 6 %). Fabricación pura:
  `GeneradorBoleta` y `BoletaDao`. Reporte `boletaPago.jrxml`, ventana `BoletaPagoView` y prueba `PruebaGrasp`.
  Creador, Controlador, Bajo acoplamiento y Alta cohesión quedan para cuando se vean en clase.
- **Base de datos:** `empleados.regimen_pensionario`, tabla `boletas_pago`, procedimientos `sp_Boleta_Registrar` y
  `sp_Empleado_CambiarRegimen`, una boleta de ejemplo y el script `sql/03_actualizacion_semana17.sql`.
- **Informe:** pasa a *Avance 3 (Capítulos I al VIII)*: Cap. VII y VIII nuevos, evidencias de los aportes del
  equipo, modelo entidad-relación, diccionario, casos de prueba, referencias y anexo actualizados (153 páginas).

## Pruebas de esta versión

- `ant clean jar` con JDK 21: correcto.
- `01_instalacion_inicial.sql` desde cero y repetido, con el modo seguro de Workbench activo: correcto.
- `02` + `03` sobre la base del Avance 2 con datos, y `03` ejecutado dos veces: correcto, sin duplicados.
- `PruebaGrasp`: la versión con antipatrones y la refactorizada dan el mismo neto (S/ 2254.17); AFP/SNP,
  periodos y horas inválidas rechazados; PDF de la boleta generado.
- Con MySQL: boleta de E0011 (SNP, 12 h) = S/ 1536.00; re-emitir el mismo periodo actualiza la misma boleta.
- Los 6 reportes con gráfico y la boleta se exportaron a PDF (`docs/reportes_ejemplo`).
- Capturas 24 y 30 a 37: modal del remitente, menú con Cerrar sesión, boleta (vista previa y visor) y reportes con gráfico.

---

# Versión anterior – actualización de la semana 13

## Cambios principales

- **Sesión 13 (Command + Memento sobre el empleado):** se replicó el ejemplo de clase con los mismos nombres
  (`MenuComandos`, `MostrarDatosCommand`, `CalcularBonoCommand`, `CalcularSueldoCommand`, `EmpleadoMemento`) y el
  ejercicio resuelto `AumentarSueldoCommand`; se agregó `CambiarAreaCommand`. `EmpleadoNomina` es el Receiver y el
  Originator, y `HistorialEmpleado` es el Caretaker. Nueva ventana `NominaEmpleadoView` (Archivo › Nómina del
  empleado) y botón *Nómina* en Empleados.
- **Base de datos:** columnas `empleados.sueldo` y `empleados.area`, tabla `historial_remuneracion`, procedimiento
  `sp_Empleado_ActualizarRemuneracion` y manifiesto de ejemplo `MF-2026-0458`. Se mantienen todos los datos
  precargados del Avance 2.
- **Reportes con JasperReports**, siguiendo AppEmpresa: 6 plantillas `.jrxml` compiladas en tiempo de ejecución,
  visor `JRViewer` con opción de guardar en PDF, exportación directa a PDF e impresión. Se retiraron las salidas
  HTML/CSV/texto y el PDF simulado; el patrón Bridge ahora combina `Reporte` (qué) con `ISalidaReporte` (cómo).
- **Estructura del repositorio igual a la del docente:** proyecto NetBeans en la raíz, `lib/runtime` con
  JasperReports 6.21.3 y el conector oficial `mysql-connector-j 8.4.0` (reemplaza al driver MariaDB), scripts en `sql/`,
  diagramas PlantUML en `diagramas/` y este archivo.
- **Informe:** sección 5.6 (Bridge con JasperReports) reescrita, nueva sección 6.6, tablas de RF, CU08, paquetes,
  diccionario de datos y casos de prueba actualizados, nuevo modelo entidad-relación, referencias y anexo.

## Validación de temas frente a la estructura del proyecto final

| Capítulo de la estructura | Estado en el informe |
|---|---|
| Portada, I Introducción, II Análisis del negocio | Completo (empresa, problemática, objetivos, RF, RNF, casos de uso) |
| III Principios SOLID (SRP, OCP, LSP, ISP, DIP + beneficios) | Completo, con problema, solución y código |
| IV Creacionales (Singleton, Factory, Abstract Factory, Builder, Prototype + comparación) | Completo |
| V Estructurales (Adapter, Facade, Decorator, Composite, Proxy, Bridge) | Completo; Bridge actualizado a JasperReports |
| VI Comportamiento (Observer, State, Command, Memento) | Completo + 6.5 (Sesión 12) y 6.6 (Sesión 13) |
| VII Antipatrones (semana 14) | Completo en el Avance 3 |
| VIII GRASP (Unidad 5) | Experto, Fabricación pura y Polimorfismo completos; el resto pendiente |
| IX Implementación, X Pruebas | Avance (arquitectura, BD, casos de prueba, evidencias) |
| XI Conclusiones, XII Recomendaciones | Pendiente – entrega final |
| Referencias APA 7, Anexos | Actualizados (guía de instalación y usuarios) |

## Pruebas realizadas

- Compilación `ant clean jar` con JDK 21 (destino Java 11): correcta.
- Script `sql/01_instalacion_inicial.sql` ejecutado en MySQL 8.0 desde cero y repetido: correcto.
- Script `sql/02_actualizacion_sesion13.sql` ejecutado sobre la base del Avance 2 con datos y vuelto a ejecutar:
  agrega columnas, tabla, procedimiento y manifiesto sin duplicar ni borrar datos.
- `uni.database.prueba`: conexión conforme con `com.mysql.cj.jdbc.Driver`.
- `uni.util.PruebaPatrones` y `uni.util.PruebaComportamiento` (sin MySQL): todas las secciones se ejecutan; la
  sección `sesion13_command_memento` reproduce el ejemplo de clase (Roy Saavedra, S/ 3000 → bono S/ 300, sueldo
  final S/ 3300) y el ejercicio resuelto (aumento 10 % → S/ 3300; rechazo de -5 %; cuatro deshacer consecutivos).
- Los 6 reportes Jasper se llenaron contra MySQL y se exportaron a PDF (`docs/reportes_ejemplo`); un manifiesto
  inexistente muestra "No hay datos para mostrar".
- Interfaz (capturas 24 a 32): aumento de sueldo, cambio de área, guardar y restaurar mementos, historial en la
  base de datos, selección de reporte y visor de JasperReports.
- Pruebas de integración previas (envíos, seguimiento, comandos, clientes, empleados) repetidas con el nuevo
  driver: correctas.

## Pendientes y limitaciones

- Capítulos VII, VIII, XI y XII se desarrollarán en las siguientes semanas.
- Los mementos de la nómina viven en memoria mientras la aplicación está abierta (como en el ejemplo de clase);
  la trazabilidad permanente está en `historial_remuneracion`.
- La salida a impresora depende de que el equipo tenga una impresora configurada.
