# TUPESA · Hito 2 PMDM

App Android de **líneas de autobús interurbano de Sevilla**. Permite gestionar tus líneas (añadir, ver, editar y borrar), consultar las **líneas reales del Consorcio de Transportes** por internet y saber cuál es tu **parada más cercana** usando la ubicación.

- **Alumna:** Nerea García Vergara
- **Módulo:** Programación Multimedia y Dispositivos Móviles (PMDM) · UT3 · Hito 2 · Curso 2026/2027
- **Versión de la app:** 2.0 (versionCode 2)

---

## 1. Qué hace la app

| Función | Dónde está | Criterio |
| --- | --- | --- |
| Listado, detalle y formulario de líneas | `MainActivity`, `DetalleActivity`, `FormularioActivity` | RA2.a, RA2.b |
| Alta y edición validadas (mismo formulario) | `FormularioActivity` (botón Guardar) | RA2.g |
| Consulta y borrado con confirmación | `DetalleActivity` | RA2.g |
| Persistencia después de reiniciar | `AlmacenLineas` (SharedPreferences + JSON) | RA2.f |
| Preferencia funcional: ordenar por código o por recorrido | `Ajustes` + menú Opciones | RA2.f, RA2.g |
| Consulta HTTPS con estados de carga, vacío y error | `ConsorcioActivity` | RA2.c, RA2.e |
| Ubicación: parada más cercana | `CercaActivity` + `Paradas` | RA2.d |
| Permiso de ubicación y atención de la negativa | `CercaActivity` | RA2.j |

---

## 2. Versiones utilizadas

| Herramienta | Versión |
| --- | --- |
| Android Studio | Rabbit 1 · 2026.2.1 |
| Lenguaje | Kotlin |
| Gradle (Wrapper) | 9.6.0 |
| Android Gradle Plugin (AGP) | 9.4.1 |
| JDK | 25 (Gradle lo descarga solo con el toolchain) |
| compileSdk / targetSdk | 37 |
| minSdk | 24 (Android 7.0) |
| Emulador de pruebas | Medium Phone · API 37.0 |
| Librerías AndroidX | core-ktx 1.10.1 · appcompat 1.6.1 · activity-ktx 1.8.0 · material 1.10.0 · constraintlayout 2.1.4 |

No se usa ninguna librería externa para la red ni para el JSON: la consulta HTTPS usa `HttpsURLConnection` y el JSON se lee con `org.json`, que vienen con Android.

---

## 3. Estructura de la entrega

```
Garcia_Nerea_H2/
├── README.md          ← este archivo
├── app/               ← proyecto de Android Studio (código fuente completo)
├── apk/               ← TUPESA_H2_v2.0_debug.apk (APK de prueba)
├── documentacion/     ← registro con pruebas, permisos, incidencias y mejora
└── evidencias/        ← capturas de todas las pruebas (P1 a P28, I1, I2)
```

La fuente **no** incluye `.gradle`, `build` ni `local.properties`: se regeneran solos al abrir el proyecto. Sí incluye el **Gradle Wrapper** (`gradlew`, `gradlew.bat` y `gradle/wrapper`).

---

## 4. Cómo abrir y ejecutar el proyecto

1. Abre Android Studio → **File → Open**.
2. Selecciona la carpeta **`app`** (la que contiene `settings.gradle.kts` y `gradlew`), **no** la carpeta `Garcia_Nerea_H2`.
3. Si pregunta *Trust project?*, pulsa **Trust Project**.
4. Espera a que termine el **Gradle Sync**. La primera vez puede tardar porque descarga Gradle 9.6.0 y el JDK 25.
5. Elige un emulador (por ejemplo, *Medium Phone API 37*) y pulsa ▶️ **Run**.

### Para probar la parada más cercana en el emulador
El emulador no tiene GPS real, así que hay que darle una ubicación de prueba:

1. En la barra lateral del emulador: **⋯ (Extended controls) → Location**.
2. Busca **Camas, Sevilla** y pulsa **Set location**.
3. En la app: **Parada más cercana → Buscar mi parada** → acepta el permiso.

### Para probar los estados de la consulta HTTPS
| Estado | Cómo provocarlo |
| --- | --- |
| Con datos | Buscar `camas` |
| Vacío | Buscar `madrid` |
| Error | Activar el modo avión y buscar |
| Cargando | *Extended controls → Cellular → Network type: GSM* y apagar el wifi del emulador |

---

## 5. Cómo instalar el APK

1. Copia `apk/TUPESA_H2_v2.0_debug.apk` al móvil o arrástralo encima del emulador.
2. En el móvil, ábrelo. Si sale *"no se pueden instalar apps desconocidas"*, permite la instalación desde esa fuente (es normal: el APK no viene de Google Play).
3. Instala y abre **TUPESA**.

Es un APK **debug** (de prueba), firmado con la clave de prueba de Android Studio. Identificador de la app: `es.iespoligonosur.tupesa`.

---

## 6. Datos externos (consulta HTTPS)

Las líneas reales se descargan de los datos abiertos de la **Red de Consorcios de Transporte de Andalucía** (https://api.ctan.es/), sin clave:

```
https://api.ctan.es/v1/Consorcios/1/lineas
```

El consorcio `1` es el Área de Sevilla. La respuesta es un JSON con una lista `"lineas"`; la app usa los campos `nombre` y `operadores`.

---

## 7. Permisos

| Permiso | Tipo | Para qué | Si se deniega |
| --- | --- | --- | --- |
| `INTERNET` | Normal | Consulta HTTPS al Consorcio | No se puede denegar |
| `ACCESS_NETWORK_STATE` | Normal | Saber si hay wifi o datos antes de descargar | No se puede denegar |
| `ACCESS_COARSE_LOCATION` | Peligroso | Parada más cercana (ubicación aproximada) | Mensaje explicativo; el resto de la app funciona igual |
| `ACCESS_FINE_LOCATION` | Peligroso | Parada más cercana (ubicación precisa) | Igual. Si se bloquea, la app ofrece **Abrir ajustes** |

**Privacidad:** la app nunca muestra ni guarda las coordenadas del usuario; solo enseña el nombre de la parada y la distancia. Las coordenadas de `Paradas.kt` son de lugares públicos.

---

## 8. Qué he reutilizado

| De dónde | Qué |
| --- | --- |
| Práctica **UT2.1** (TUPESA) | Proyecto base: listado, detalle, formulario con validación y menú de ayuda |
| Práctica **UT2.2** (Biblioteca de aula) | El sistema de guardado de `AlmacenLibros` (SharedPreferences + JSON) y la recarga de la lista en `onResume()` |

---

## 9. Pruebas, incidencias y mejora

El detalle completo, con capturas, está en `documentacion/`.

- **Pruebas:** P1 a P28 (persistencia, editar y borrar, preferencia, consulta HTTPS y sus estados, permiso de ubicación, APK). Capturas en `evidencias/`.
- **Incidencia I1:** atributo `layout_width` repetido en `activity_formulario.xml` → no compilaba. Corregida borrando la línea repetida.
- **Incidencia I2:** funciones de la descarga pegadas dentro de `onCreate` → errores *"Modifier 'private' is not applicable to 'local function'"*. Corregida sacándolas fuera de `onCreate`.
- **Mejora tras la prueba de un compañero:** pendiente.
- **Instalación en un dispositivo del aula:** pendiente.

---

## 10. Historial (commits)

| Commit | Contenido |
| --- | --- |
| Fase 0 | Base del H2 a partir de la UT2.1 |
| Fase 1 | Persistencia con SharedPreferences y JSON (`AlmacenLineas`) |
| Fase 2 | Editar y borrar líneas; formulario en modo edición |
| Fase 3 | Preferencia de orden de la lista (`Ajustes`) |
| Fase 4 | Consulta HTTPS al Consorcio con estados de carga, vacío y error |
| Fase 5 | Ubicación con permiso en tiempo de ejecución y parada más cercana |
| Fase 6 | Versión 2.0 y APK de prueba |

Repositorio: https://github.com/lcfxr/HITO2_PMM
