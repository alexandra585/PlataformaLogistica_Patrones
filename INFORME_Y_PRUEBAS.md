# Informe de cambios y pruebas – actualización de la semana 13

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
| VII Antipatrones (semana 14) | Pendiente – futuras entregas |
| VIII GRASP (Unidad 5) | Pendiente – futuras entregas |
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
