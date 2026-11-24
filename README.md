# AppLogistica – Plataforma de logística y seguimiento de envíos

Proyecto final del curso **Diseño de Patrones** (Facultad de Ingeniería, ciclo 2026-II).
Aplicación de escritorio en **Java Swing (Apache NetBeans + Ant)** con base de datos **MySQL 8** y reportes
**JasperReports**, para la empresa ficticia **LogiRuta Perú S.A.C.** Aplica principios **SOLID** y patrones **GoF**
creacionales, estructurales y de comportamiento, identifica **antipatrones** y aplica los **7 patrones GRASP**.

> Estado: **versión final – Capítulos I al XII**. Incluye todos los temas del sílabo (SOLID, patrones GoF
> creacionales, estructurales y de comportamiento, antipatrones y los **7 patrones GRASP**), gráficos de barras y
> **circulares** en los reportes y **bloqueo de la cuenta tras 3 contraseñas incorrectas**. El detalle semana por
> semana está en [Avance por semana](#avance-por-semana).

El repositorio sigue la misma organización que el proyecto de ejemplo del docente (**AppEmpresa**): el proyecto
NetBeans está en la raíz, las librerías en `lib/runtime`, los scripts en `sql/` y los diagramas en `diagramas/`.

---

## Contenido del repositorio

```
PlataformaLogistica_Patrones/
├── build.xml, manifest.mf, nbproject/   Proyecto NetBeans/Ant (abrir esta carpeta)
├── db.properties                        Conexión a MySQL (usuario logistica / logistica)
├── src/uni/
│   ├── controller   Controladores, Facade, Invokers (GestorComandos, MenuComandos)
│   ├── dao          Acceso a datos con procedimientos almacenados
│   ├── database     AccesoDB (Singleton) y prueba de conexión
│   ├── entity       Transfer Objects y enumeraciones
│   ├── formularios  Ventanas Swing (LoginView = clase principal)
│   ├── reportes     Bridge + 7 plantillas JasperReports (.jrxml) con gráficos de barras y circulares
│   ├── service      Lógica de negocio y patrones
│   └── util         Builder, Factory, Adapters, Caretakers y pruebas por consola
├── lib/runtime/     JasperReports 6.21.3, mysql-connector-j 8.4.0 y dependencias
├── sql/
│   ├── 01_instalacion_inicial.sql       Crea la base "logistica" completa con datos de prueba
│   ├── 02_actualizacion_sesion13.sql    Actualiza una base del Avance 2 sin borrar datos
│   ├── 03_actualizacion_semana17.sql    Agrega régimen AFP/SNP y boletas de pago (después del 02)
│   └── 04_actualizacion_seguridad_login.sql  Bloqueo tras 3 intentos fallidos (después del 03)
├── diagramas/       Fuentes PlantUML (.puml) e imágenes: BD, arquitectura, clases, secuencias, Bridge, Sesiones 13, 14, 16 y 17
├── docs/
│   ├── Informe_AppLogistica_Final_Cap_I-XII.docx / .pdf
│   ├── reportes_ejemplo/   Los reportes Jasper (con gráficos) y una boleta de pago en PDF
│   ├── capturas/           Capturas de la aplicación
│   ├── diagramas_uml.md    Código Mermaid de los diagramas del informe
│   ├── salida_pruebas.txt  Salida de las pruebas por consola
│   └── caso_empresarial.txt
├── INFORME_Y_PRUEBAS.md     Cambios de la versión y pruebas realizadas
└── subir_a_github.bat       Publica los cambios en GitHub (Windows)
```

## Requisitos

- JDK 17 o superior (el proyecto compila con `source/target 11`).
- Apache NetBeans 17 o superior.
- MySQL Server 8 y MySQL Workbench.
- No se necesita descargar librerías: JasperReports, el conector de MySQL y sus dependencias ya están en
  `lib/runtime` y el proyecto las referencia con rutas relativas.

## Instalación y ejecución

1. **Clonar** el repositorio (o descargar el ZIP y extraerlo completo):
   ```bash
   git clone https://github.com/alexandra585/PlataformaLogistica_Patrones.git
   ```
2. **Base de datos**
   - *Instalación nueva:* abrir `sql/01_instalacion_inicial.sql` en MySQL Workbench (usuario root) y ejecutarlo
     completo (rayo). Crea la base `logistica`, el usuario `logistica` / `logistica`, tablas, procedimientos, triggers,
     vistas y **datos precargados**. Si se ejecuta de nuevo, borra y recrea la base.
   - *Ya tengo una base anterior con mis datos:* ejecutar en orden `sql/02_actualizacion_sesion13.sql`,
     `sql/03_actualizacion_semana17.sql` y `sql/04_actualizacion_seguridad_login.sql` (no borran datos y pueden
     ejecutarse más de una vez).
3. **Abrir el proyecto:** en NetBeans, *File › Open Project* y seleccionar la carpeta del repositorio
   (la que contiene `build.xml` y `nbproject`).
4. **(Opcional)** Si MySQL usa otro puerto o usuario, editar `db.properties`. La clave
   `integraciones=CONTINGENCIA` cambia la familia de proveedores externos (Abstract Factory).
5. **Probar la conexión:** clic derecho sobre `uni.database.prueba` › *Run File*.
6. **Ejecutar:** *Clean and Build* y luego *Run Project* (F6). Se abre `uni.formularios.LoginView`.

Desde consola (con Ant): `ant clean jar` y luego `java -jar dist/AppLogistica.jar`.

## Usuarios de prueba

| Usuario | Contraseña | Rol | Uso sugerido |
|---|---|---|---|
| admin | admin | ADMINISTRADOR | Todas las opciones (incluye Nómina, Boleta de pago y Reportes) |
| rquispe | 123456 | AGENTE_VENTANILLA | Clientes y registro de envíos |
| lmendoza | 123456 | OPERADOR_ALMACEN | Recoger, clasificar, manifiestos |
| jtorres | 123456 | SUPERVISOR_RUTA | Manifiestos, reportes, alertas, revertir entregas |
| icardenas | 123456 | REPARTIDOR | Envíos asignados LR00000004 y LR00000005 |
| mhuaman | 123456 | REPARTIDOR | Envío asignado LR00000008 |
| csalazar | 123456 | REPARTIDOR | De vacaciones: el patrón State le niega el acceso |

Las contraseñas se guardan con hash SHA-256. **Seguridad:** tras **3 contraseñas incorrectas seguidas** la cuenta
se bloquea **5 minutos**; el administrador puede liberarla antes en **Utilitarios › Desbloquear usuarios**.

---

## Patrones y principios aplicados

| Categoría | Patrón / principio | Dónde se aplica |
|---|---|---|
| SOLID | SRP, OCP, LSP, ISP, DIP | Servicios de envío, `ILectura`/`IEscritura`, `IRegistroRemuneracion`, inyección por constructor |
| Creacional | Singleton | `AccesoDB`, `SesionUsuario`, `ConfiguracionSistema` |
| Creacional | Factory | `ServicioEnvioFactory` (express, estándar, programado, recojo en tienda, interprovincial) |
| Creacional | Abstract Factory | `FabricaIntegracionProduccion` / `FabricaIntegracionContingencia` |
| Creacional | Builder | `EnvioBuilder`, `DirectorEnvio` |
| Creacional | Prototype | `EnvioTo.clone()`, `RegistroPlantillas` ("Repetir último envío") |
| Estructural | Adapter | Correo, SMS, WhatsApp, Google Maps, OpenStreetMap |
| Estructural | Facade | `RegistroEnvioFacade` |
| Estructural | Decorator | Tarifa + seguro, embalaje frágil, firma digital, GPS |
| Estructural | Composite | Manifiesto › sacos/paletas › paquetes |
| Estructural | Proxy | `SeguimientoServiceProxy` (protección por rol, caché y auditoría) |
| Estructural | Bridge | `Reporte` (6 reportes Jasper) × `ISalidaReporte` (visor, PDF, impresora) |
| Comportamiento | State | 9 estados del envío (`EnvioContexto`) y estado laboral del empleado |
| Comportamiento | Observer | Persistencia, historial, notificación, alertas, indicadores y pantalla en vivo |
| Comportamiento | Command | `GestorComandos` (envíos: deshacer/rehacer, cola sin conexión, macro) y `MenuComandos` (nómina, Sesión 13) |
| Comportamiento | Memento | `EnvioMemento` + `HistorialMementos`; `EmpleadoMemento` + `HistorialEmpleado` (Sesión 13) |
| Antipatrones | Caso Empleado (Sesión 14) | `EmpleadoAntipatron` (solo didáctico) frente a `DescuentoStrategy` + `DescuentoFactory` + `ParametrosPlanilla` |
| GRASP | Experto en información | `EmpleadoNomina` calcula horas extras, descuentos y sueldo neto |
| GRASP | Fabricación pura | `GeneradorBoleta`, `BoletaDao` (también `AccesoDB` y los DAO) |
| GRASP | Polimorfismo | `DescuentoStrategy` → `DescuentoAFP` / `DescuentoSNP` (también estados, servicios y salidas de reporte) |
| GRASP | Creador | `GeneradorBoleta` crea `BoletaPagoTo`; `ControlAccesoLogin` crea `BloqueoLoginTo`; `EmpleadoNomina` crea `EmpleadoMemento` |
| GRASP | Controlador | `EmpleadoController` (login, desbloqueo, boleta), `EnvioController`, `ClienteController`, `RegistroEnvioFacade` |
| GRASP | Bajo acoplamiento | `ControlAccesoLogin` depende de `IRegistroIntentosLogin` y de un `Clock` inyectado; `GeneradorBoleta` de `IRegistroBoleta` |
| GRASP | Alta cohesión | Login repartido en `LoginView`, `EmpleadoController`, `ControlAccesoLogin`, `BloqueoLoginDao`, `ParametrosSeguridad` |

## Avance por semana

| Semana | Tema del sílabo | Qué se hizo en AppLogistica | Dónde está |
|---|---|---|---|
| 1 | Principios de diseño, SOLID, SRP | Análisis del caso LogiRuta; separación de responsabilidades del registro de envío | Informe Cap. I–III (3.1) |
| 2 | LSP y OCP | Jerarquía `ServicioEnvioBase` con 5 servicios sustituibles; nuevos servicios sin modificar código | Cap. III (3.2, 3.3) |
| 3 | DIP e ISP | Interfaces `ILectura` / `IEscritura`, `ICrudDao`; controladores que dependen de abstracciones | Cap. III (3.4, 3.5) |
| 4 | Introducción a patrones GoF | Arquitectura en capas (molde AppEmpresa), requerimientos y casos de uso | Cap. I, II y IX |
| 5 | Repaso / Práctica calificada 1 | Consolidación del diseño y de la base de datos `logistica` | `sql/01_instalacion_inicial.sql` |
| 6 | Singleton y Prototype | `AccesoDB`, `SesionUsuario`, `ConfiguracionSistema`; `EnvioTo.clone()` y "Repetir último envío" | Cap. IV (4.1, 4.5) |
| 7 | Factory, Abstract Factory, Builder | `ServicioEnvioFactory`, familias de integraciones Producción/Contingencia, `EnvioBuilder` | Cap. IV (4.2–4.4) |
| 8 | Adapter y Facade | Adaptadores de correo, SMS, WhatsApp y mapas; `RegistroEnvioFacade` | Cap. V (5.1, 5.2) |
| 9 | Decorator y Composite | Tarifa + seguro, frágil, firma, GPS; manifiesto con sacos, paletas y paquetes | Cap. V (5.3, 5.4) |
| 10 | Repaso | Pruebas por consola de los patrones creacionales y estructurales (`PruebaPatrones`) | `docs/salida_pruebas.txt` |
| 11 | Proxy y Bridge | `SeguimientoServiceProxy` (protección por rol); reportes JasperReports `Reporte` × `ISalidaReporte` | Cap. V (5.5, 5.6) |
| 12 | State y Observer | 9 estados del envío y estado laboral del empleado; 6 observadores y alertas | Cap. VI (6.1, 6.2, 6.5) |
| 13 | Command y Memento | Deshacer/rehacer y cola sin conexión de envíos; nómina del empleado con `MenuComandos` y `EmpleadoMemento` | Cap. VI (6.3, 6.4, 6.6) |
| 14 | Antipatrones | Antipatrones de desarrollo y organizacionales; caso Empleado antes y después (Strategy + Factory) | Cap. VII |
| 15 | Repaso | Aportes del equipo: modal del remitente, gráficos de barras, Cerrar sesión | Cap. V y X |
| 16 | GRASP: Alta cohesión, Bajo acoplamiento, Controlador, Creador | **Seguridad del login**: bloqueo tras 3 intentos y desbloqueo por el administrador | Cap. VIII (8.2–8.5) |
| 17 | GRASP: Experto, Fabricación pura, Polimorfismo | **Boleta de pago** del empleado (AFP/SNP) con PDF Jasper | Cap. VIII (8.1, 8.6, 8.7) |
| 18 | Presentación del proyecto final | Gráficos circulares en 4 reportes; informe final Cap. I–XII (arquitectura, clases, secuencias, diccionario, pruebas, conclusiones) | `docs/Informe_AppLogistica_Final_Cap_I-XII.pdf` |

### ¿Ya se aplicaron los patrones GRASP?

Sí, **los 7** que pide la estructura del proyecto (Capítulo VIII del informe):

| Patrón GRASP | Funcionalidad | Clases |
|---|---|---|
| Experto en información | Boleta de pago | `EmpleadoNomina` calcula horas extras, aporte, salud y neto; `BloqueoLoginTo.estaBloqueado()` |
| Creador | Boleta y login | `GeneradorBoleta` → `BoletaPagoTo`; `ControlAccesoLogin` → `BloqueoLoginTo` |
| Controlador | Login, boleta, envíos | `EmpleadoController`, `EnvioController`, `ClienteController`, `RegistroEnvioFacade` |
| Bajo acoplamiento | Login | `ControlAccesoLogin` usa `IRegistroIntentosLogin` + `Clock` (se prueba sin MySQL) |
| Alta cohesión | Login | Cada clase una sola tarea: vista, controlador, reglas, DAO, constantes |
| Fabricación pura | Boleta y login | `GeneradorBoleta`, `BoletaDao`, `BloqueoLoginDao` |
| Polimorfismo | Boleta | `DescuentoStrategy` → `DescuentoAFP` (11 %) / `DescuentoSNP` (6 %) |

## Versión final: gráficos circulares y seguridad del login

- **Gráficos circulares (pieChart)** junto a los de barras, cada uno con una variable que expresa una proporción:

  | Reporte | Gráfico circular |
  |---|---|
  | Listado de envíos | Envíos por estado (%) |
  | Incidencias por zona | Incidencias por tipo (motivo) |
  | Lista de empleados | Empleados por área |
  | Cumplimiento de tiempos | Entregas a tiempo frente a fuera de plazo |

  Se agregaron solo en los `.jrxml` (subDataset con `GROUP BY`): gracias al Bridge no se tocó ninguna clase Java.
- **Bloqueo del login:** la 1.ª y 2.ª clave incorrecta muestran los intentos que quedan; la 3.ª bloquea la cuenta
  5 minutos (también rechaza la clave correcta mientras dure). Al ingresar bien el contador vuelve a cero. Todo queda
  en las tablas `bloqueos_login` e `intentos_login`. El administrador ve y libera las cuentas en
  **Utilitarios › Desbloquear usuarios** (`sp_Login_Desbloquear`). Las reglas (3 intentos, 5 minutos) están en
  `uni.util.ParametrosSeguridad`.

## Semana 13: Command y Memento sobre el empleado

Se replicó el ejemplo de clase (`Comando`, `MenuComandos`, `Empleado`, `EmpleadoMemento`, `Historial`) dentro de la
arquitectura del proyecto, incluido el **ejercicio resuelto `AumentarSueldoCommand`**:

| Rol | Clase en AppLogistica |
|---|---|
| Command | `uni.service.IComandoEmpleado` |
| Comandos concretos | `MostrarDatosCommand`, `CalcularBonoCommand`, `CalcularSueldoCommand`, `AumentarSueldoCommand`, `CambiarAreaCommand` |
| Invoker | `uni.controller.MenuComandos` |
| Receiver / Originator | `uni.service.EmpleadoNomina` |
| Memento | `uni.service.EmpleadoMemento` |
| Caretaker | `uni.util.HistorialEmpleado` |
| Cliente | `uni.formularios.NominaEmpleadoView` |

En la aplicación: **Archivo › Nómina del empleado (Command/Memento)** o el botón *Nómina* en **Archivo › Empleados**.
Cada aumento o cambio de área guarda antes un memento, y todo cambio queda registrado en la tabla
`historial_remuneracion`.

## Semanas 14 y 17: antipatrones y GRASP (boleta de pago)

- **Antipatrones (Cap. VII del informe):** un antipatrón no se implementa; se identifica y se corrige. Se documentan
  los de desarrollo (God Object, Spaghetti Code, Big Ball of Mud, Shotgun Surgery, Vendor Lock-In, Golden Hammer,
  Magic Numbers, Copy-Paste, Lava Flow, Singletonitis) y los organizacionales, y se replica el caso de clase del
  Empleado: `uni.util.EmpleadoAntipatron` (versión con errores) frente a la versión refactorizada con Strategy y
  Factory Method. Ambas calculan el mismo neto (S/ 2254.17).
- **Boleta de pago (Cap. VIII):** **Archivo › Boleta de pago (GRASP)** o el botón *Boleta* en Empleados. Se elige el
  periodo, las horas extras y el régimen (AFP 11 % o SNP 6 %, más salud 3 %); *Calcular* muestra la vista previa y
  *Emitir* la registra en `boletas_pago` y la abre en el visor de JasperReports.

## Aportes del equipo

- Búsqueda modal del remitente al registrar un envío (`ClienteBuscarDialog`).
- Gráficos de barras en los seis reportes JasperReports (en la versión final se sumaron 4 gráficos circulares).
- Opción **Archivo › Cerrar sesión**.

## Reportes con JasperReports

**Reportes › Reportes (JasperReports)**: se elige el reporte, el filtro y la salida (visor, archivo PDF o impresora).
Igual que en AppEmpresa, cada `.jrxml` se compila al abrir el reporte y se llena con la conexión a MySQL.

| Reporte | Plantilla | Filtro |
|---|---|---|
| Lista de empleados | `listaEmpleados.jrxml` | — |
| Listado de envíos | `listadoEnvios.jrxml` | Estado (opcional) |
| Manifiesto de carga | `manifiestoCarga.jrxml` | Código de manifiesto (ej. MF-2026-0458) |
| Cumplimiento de tiempos | `cumplimientoTiempos.jrxml` | — |
| Incidencias por zona | `incidenciasZona.jrxml` | — |
| Productividad de reparto | `productividadReparto.jrxml` | — |
| Boleta de pago (Sesión 17) | `boletaPago.jrxml` | Boleta emitida |

Los PDF generados con los datos precargados (con sus gráficos de barras y circulares) están en
[`docs/reportes_ejemplo`](docs/reportes_ejemplo).

## Dónde ver cada patrón de comportamiento

- **Procesos › Seguimiento y estados del envío** (buscar `LR00000012` o `LR00000004`): State, Command
  (deshacer/rehacer y modo sin conexión), Memento (puntos de restauración) y Observer (eventos en vivo).
- **Procesos › Manifiesto de carga:** árbol Composite y despacho con MacroComando.
- **Archivo › Nómina del empleado:** Command + Memento de la Sesión 13.
- **Archivo › Empleados › Estado laboral:** State + Observer de la Sesión 12.
- **Utilitarios › Alertas, notificaciones y bitácora:** lo registrado por observadores y comandos.
- **Utilitarios › Desbloquear usuarios:** cuentas bloqueadas por 3 intentos fallidos (GRASP, solo administrador).

## Pruebas por consola (no requieren MySQL)

- `uni.util.PruebaPatrones` – capítulos III al VIII (genera PDF Jasper de ejemplo en `salida_reportes/`).
- `uni.util.PruebaComportamiento` – solo patrones de comportamiento.
- `uni.util.PruebaGrasp` – antipatrones (Sesión 14), boleta de pago (Sesión 17) y bloqueo del login con reloj
  simulado (Sesión 16); genera `salida_reportes/boleta_pago.pdf`.

La salida está en [`docs/salida_pruebas.txt`](docs/salida_pruebas.txt) y el detalle de las pruebas en
[`INFORME_Y_PRUEBAS.md`](INFORME_Y_PRUEBAS.md).

## Capturas

| Gráfico circular de envíos por estado | Cuenta bloqueada tras 3 intentos | Desbloqueo por el administrador |
|---|---|---|
| ![Envíos por estado](docs/capturas/42_pie_envios_por_estado.png) | ![Bloqueo](docs/capturas/39_login_cuenta_bloqueada.png) | ![Desbloqueo](docs/capturas/41_desbloqueo_usuarios.png) |

| Boleta de pago (GRASP) | Reporte Jasper con gráfico | Nómina: Command + Memento |
|---|---|---|
| ![Boleta](docs/capturas/36_boleta_jasper_pdf.png) | ![Cumplimiento](docs/capturas/37_jasper_cumplimiento_grafico.png) | ![Nómina](docs/capturas/26_nomina_puntos_memento.png) |

Más capturas en [`docs/capturas`](docs/capturas).

## Documentación

- Informe final (Cap. I–XII): [`docs/Informe_AppLogistica_Final_Cap_I-XII.pdf`](docs/Informe_AppLogistica_Final_Cap_I-XII.pdf)
- Diagramas PlantUML: [`diagramas/`](diagramas) · Diagramas Mermaid del informe: [`docs/diagramas_uml.md`](docs/diagramas_uml.md)

---

Proyecto académico desarrollado con fines educativos para el curso Diseño de Patrones.
