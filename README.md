# 🎵 Gestión de Recitales (TP Paradigmas de Programación)

Este proyecto es un trabajo práctico para la materia **Paradigmas de Programación** de la Universidad Nacional de La Matanza (UNLaM).

El objetivo es diseñar e implementar un sistema en Java que gestione la planificación de un recital. El sistema debe optimizar la contratación de artistas externos para cubrir todos los roles requeridos por las canciones, minimizando el costo total y respetando las reglas de negocio (descuentos, límites de canciones, etc.).

Además, el proyecto integra una base de conocimiento en **Prolog** para resolver consultas lógicas sobre la dotación de artistas.

## ✨ Características Principales

El sistema se maneja a través de un menú de consola con las siguientes funcionalidades:

* **Consultar roles faltantes** (para una canción específica o para todo el recital).
* **Contratar artistas** (para una canción específica o para todo el recital, ejecutando el algoritmo de optimización).
* **Entrenar artistas:** Permite agregar un nuevo rol a un artista (con un incremento del 50% en su costo).
* **Listar artistas contratados** y su costo total.
* **Ver estado de canciones** (completa o con roles faltantes).
* **Integración con Prolog:** Calcular cuántos entrenamientos mínimos se necesitarían para cubrir todos los roles usando solo a los miembros de la banda base.

---

## 🛠️ Tecnologías Utilizadas

* **Lenguaje:** Java (JDK 24)
* **Gestión de Dependencias:** Apache Maven
* **Pruebas Unitarias:** JUnit 5
* **Lectura de Datos:** Jackson (para parsear JSON)
* **Paradigma Lógico:** SWI-Prolog
* **Integración Java-Prolog:** JPL7

---

## 🚀 Configuración y Puesta en Marcha

Para ejecutar este proyecto, se requiere una configuración específica debido a la integración con SWI-Prolog.

### 1. Requisitos Previos

* Tener instalado un **JDK 24** (o compatible).
* Tener instalado **Apache Maven**.
* Tener instalado **SWI-Prolog** (v9.x recomendado).
    * **¡Importante!** Durante la instalación de SWI-Prolog, asegurarse de marcar la casilla **"Add swi-prolog to the system PATH"**.

### 2. Configuración del Proyecto

1.  **Clonar el repositorio:**
    ```bash
    git clone https://github.com/Facu-caceres/TP-Paradigmas-Recital
    cd TP_Paradigmas_Recital_
    ```

2.  **Instalar Dependencias de Maven:**
    * Al abrir el proyecto en IntelliJ (o tu IDE de preferencia), importa las dependencias de Maven (Reload All Maven Projects).
    * Esto descargará **JUnit** y **Jackson**.
    * La librería **JPL7** (`jpl.jar`) está incluida localmente en la carpeta `/lib` y es gestionada por el `pom.xml` (vía `systemPath`).

3.  **Configurar las VM Options (Solo para IntelliJ IDEA):**
    * Debido a las restricciones de acceso nativo de Java, es necesario configurar las opciones de la VM para la ejecución.
    * Ve a `Run > Edit Configurations...`.
    * Selecciona tu configuración de `Main App`.
    * En el campo **"VM options"**, agrega la siguiente línea (ajustando la ruta a tu instalación de SWI-Prolog si es diferente):
    
    ```
    --enable-native-access=ALL-UNNAMED -Djava.library.path="C:\Program Files\swipl\bin"
    ```

---

## 🏃 Cómo Ejecutar

### Ejecutar la Aplicación Principal

* Una vez configurado, ejecuta la clase `Main.java` ubicada en:
    `src/main/java/ar/edu/UNLaM/tp_paradigmas/app/Main.java`
* Esto iniciará el menú interactivo en la consola.

### Ejecutar las Pruebas Unitarias

* Para validar que toda la lógica de negocio funciona correctamente, puedes ejecutar la suite de tests.
* Haz clic derecho en la clase `ProductoraTest.java` ubicada en:
    `src/test/java/ar/edu/UNLaM/tp_paradigmas/service/ProductoraTest.java`
* Selecciona **"Run 'ProductoraTest'"**. Todos los 11 tests deberían pasar en verde ✅.

---

## 📁 Estructura de Datos

* **`/archivos_fuente/`**: Contiene todos los archivos fuente de entrada originales.
    * `artistas.json`: Lista de todos los artistas disponibles para contratar.
    * `artistas-discografica.json`: Lista de los artistas base (costo 0).
    * `recital.json`: Lista de canciones y los roles que requiere cada una.
    * `recital.pl`: Base de conocimiento Prolog con los hechos y reglas.
