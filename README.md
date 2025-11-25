
<div align="center">
  <br>
  <h1>Universidad Autónoma de Querétaro</h1>
  <h3>Facultad de Ingeniería</h3>
  <h3>Ingeniería en Automatización</h3>
  <br>
  <br>
  <strong>Materia:</strong><br>
  Programación de Dispositivos Móviles
  <br><br>
  <strong>Proyecto Final:</strong><br>
  Solucionador de Cinemática Inversa para Robot R||R
  <br><br>
  <strong>Integrantes (Equipo 2):</strong><br>
  Ayala Osornio Eduardo<br>
  Nuñez Camacho Jesús Alejandro
  <br><br>
  <strong>Docente:</strong><br>
  Dr. Adriana Rojas Molina
  <br><br>
  <strong>Fecha:</strong><br>
  25 de noviembre de 2025
  <br>
  <br>
</div>

---

## Resumen Ejecutivo
El proyecto consiste en una aplicación móvil nativa para Android desarrollada en Kotlin que resuelve la cinemática inversa de un robot manipulador plano de dos grados de libertad (2-GDL). Su propósito es proporcionar a estudiantes e ingenieros una herramienta portátil para calcular ángulos articulares, validar colisiones físicas con el entorno y visualizar el comportamiento del robot mediante una simulación gráfica interactiva. Los resultados obtenidos incluyen una interfaz moderna basada en Jetpack Compose, persistencia de datos para parámetros geométricos y la capacidad de exportar reportes visuales de la simulación.

---

## Introducción

### Contexto del problema
En el estudio de la robótica, el cálculo de la cinemática inversa es fundamental pero repetitivo. Los estudiantes suelen requerir herramientas visuales rápidas para verificar sus cálculos teóricos y validar si una configuración geométrica es físicamente posible sin necesidad de utilizar software de escritorio pesado o entornos de simulación complejos.

### Motivación del proyecto
La motivación principal es integrar los conocimientos de ingeniería robótica con el desarrollo de software móvil moderno, creando una herramienta de bolsillo ("Pocket Tool") que facilite la validación de trayectorias y configuraciones mecánicas en tiempo real.

### Objetivos
* **Objetivo General:** Desarrollar una aplicación Android capaz de resolver y simular la cinemática inversa de un robot R||R.
* **Objetivos Específicos:**
    1.  Implementar el algoritmo matemático de cinemática inversa (método geométrico).
    2.  Validar restricciones físicas (espacio de trabajo y colisiones con el suelo).
    3.  Visualizar el robot en un `Canvas` escalable y dinámico.
    4.  Guardar las preferencias del usuario mediante persistencia de datos local.

### Alcance del desarrollo
El desarrollo abarca la creación de la interfaz de usuario (UI) con Material Design, la lógica de negocio (ViewModel), el motor matemático y la gestión de almacenamiento local en dispositivos Android con API 24 (Nougat) o superior.

---

## Marco Teórico

### Descripción del ecosistema Android
El proyecto se desarrolla sobre el sistema operativo Android, utilizando el SDK oficial y las herramientas de desarrollo más recientes proporcionadas por Google (Android Studio Ladybug), garantizando compatibilidad, seguridad y rendimiento en dispositivos móviles actuales.

### Kotlin como lenguaje moderno
Se utiliza **Kotlin (versión 2.0+)** como lenguaje principal debido a su concisión, seguridad de tipos (null-safety) y su interoperabilidad total con las bibliotecas de Android. Es el lenguaje recomendado por Google para el desarrollo móvil moderno debido a su eficiencia y reducción de código repetitivo (boilerplate).

### Revisión de tecnología relacionada
* **Jetpack Compose:** Toolkit moderno para construir interfaces de usuario nativas de forma declarativa, eliminando la necesidad de archivos XML complejos.
* **ViewModel (Architecture Components):** Componente encargado de gestionar y almacenar datos relacionados con la UI de forma consciente del ciclo de vida, evitando pérdida de datos al rotar la pantalla.
* **DataStore Preferences:** Solución de almacenamiento de datos que reemplaza a SharedPreferences, utilizando Corrutinas y Flow para almacenar datos pequeños (configuraciones) de forma asíncrona y segura.

---

## Análisis del Problema

### Definición detallada del problema
Los robots manipuladores tienen restricciones geométricas estrictas. Calcular manualmente si un punto $(x, y)$ es alcanzable por un robot con eslabones $L_1$ y $L_2$, y calcular los ángulos necesarios, es un proceso propenso a errores humanos. La app debe automatizar este cálculo y proveer retroalimentación visual inmediata sobre la viabilidad de la solución.

### Requisitos funcionales
1.  El usuario debe poder ingresar longitudes de eslabones ($L_1, L_2$) y coordenadas objetivo ($X, Y$).
2.  La app debe calcular $\theta_1$ y $\theta_2$ para las dos configuraciones posibles: "Codo Arriba" y "Codo Abajo".
3.  La app debe guardar automáticamente los últimos parámetros ingresados para sesiones futuras.
4.  El sistema debe graficar el robot, el objetivo y su espacio de trabajo limitado.
5.  El usuario debe poder compartir una imagen de la simulación generada.

### Requisitos no funcionales
* **Usabilidad:** La interfaz debe ser intuitiva, con validación de formularios en tiempo real (mensajes de error en rojo).
* **Rendimiento:** La simulación gráfica (Canvas) debe renderizarse fluidamente (60fps) sin lags perceptibles.
* **Seguridad:** El manejo de archivos compartidos debe usar `FileProvider` para no exponer datos sensibles del sistema de archivos.

### Usuarios objetivo
Estudiantes de ingeniería mecatrónica, robótica, automatización y docentes de dichas áreas que requieren validaciones rápidas.

### Casos de uso principales
* **Caso 1: Punto Inalcanzable.** Usuario ingresa un punto fuera del alcance $\rightarrow$ Sistema muestra error "Posición Inalcanzable".
* **Caso 2: Colisión.** Usuario ingresa punto bajo el suelo ($y < 0$) $\rightarrow$ Sistema deshabilita la solución que choca y alerta al usuario.
* **Caso 3: Persistencia.** Usuario cierra la app y vuelve a entrar $\rightarrow$ Los valores de $L_1$ y $L_2$ persisten y se cargan automáticamente.

---

## Diseño del Sistema

### Diagrama de navegación
El flujo de la aplicación es lineal y simplificado para maximizar la eficiencia del usuario:
> `SolverScreen (Entrada de Parámetros)` $\leftrightarrow$ `SimulationScreen (Visualización Gráfica)`

### Diseño de interfaz
Se diseñó una interfaz limpia utilizando **Material Design 3**.
* **Pantalla Principal:** Uso de Tarjetas (`Card`) para agrupar lógicamente los parámetros del robot y del objetivo. Botones grandes y claros para las acciones principales.
* **Pantalla de Simulación:** Un área de `Canvas` maximizada con un HUD (Heads-Up Display) superpuesto para mostrar datos numéricos sin obstruir la visión del gráfico.

### Selección de librerías y herramientas
* **IDE:** Android Studio Ladybug.
* **Control de Versiones:** Git y GitHub.
* **Librerías Core:**
    * `androidx.navigation:navigation-compose` (Navegación entre pantallas).
    * `androidx.datastore:datastore-preferences` (Persistencia de datos).
    * `androidx.lifecycle:lifecycle-viewmodel-compose` (Gestión de estado MVVM).

---

## Desarrollo de la Aplicación

### Manejo de datos (Persistencia)
Se implementó la clase `SettingsRepository.kt` utilizando **Preferences DataStore**. Esto permite guardar las longitudes de los eslabones (`l1_input_cm`, `l2_input_cm`) de manera asíncrona. Al iniciar la app, se recuperan estos valores mediante un `Flow` de corrutinas, asegurando que la configuración del robot se mantenga entre sesiones.

### Integración de componentes y lógica
La aplicación sigue estrictamente la arquitectura **MVVM**:
1.  **Modelo (`InverseKinematics.kt`):** Contiene la lógica matemática pura (Ley de Cosenos, atan2).
2.  **ViewModel (`KinematicsViewModel.kt`):** Gestiona el estado de la UI, valida las entradas, maneja errores y se comunica con el repositorio de datos.
3.  **Vista (`MainActivity.kt`):** Dibuja la UI y reacciona a los cambios de estado del ViewModel.

### Manejo de permisos y características de hardware
Se gestionan permisos de almacenamiento para la funcionalidad de compartir resultados:
* Uso de `FileProvider` en el `AndroidManifest.xml` para compartir de forma segura la captura de pantalla (`bitmap`) generada desde el Canvas con otras aplicaciones externas (como WhatsApp, Gmail o Drive).

---

## Pruebas

### Casos de prueba y resultados
| Caso de Prueba | Acción | Resultado Esperado | Resultado Obtenido |
| :--- | :--- | :--- | :--- |
| **Entrada Inválida** | Ingresar texto o números negativos en L1. | El campo muestra error en rojo y bloquea el cálculo. | ✅ Exitoso |
| **Colisión con Suelo** | Calcular un punto donde el codo baja de Y=0. | El botón de esa solución se deshabilita. | ✅ Exitoso |
| **Persistencia** | Cambiar L1, cerrar la app (matar proceso) y reabrir. | El valor de L1 modificado debe aparecer al inicio. | ✅ Exitoso |
| **Exportación** | Pulsar botón "Compartir". | Se abre el menú de Android y se envía la imagen. | ✅ Exitoso |

### Problemas encontrados y cómo se resolvieron
* **Problema:** El texto dentro del `Canvas` no se renderizaba correctamente debido a problemas de importación con la librería `drawText` en ciertas versiones de Compose.
* **Solución:** Se rediseñó la interfaz utilizando un `Box` (contenedor). El `Canvas` se colocó en el fondo y los textos (`Text` Composable) se superpusieron en una capa superior. Esto no solo resolvió el problema técnico, sino que mejoró la legibilidad y el diseño visual (HUD).

---

## Resultados

### Funcionamiento final
La aplicación es totalmente funcional. Realiza cálculos trigonométricos instantáneos, valida físicamente las soluciones y presenta una simulación fluida que se adapta a diferentes tamaños de pantalla gracias al escalado automático.

### Capturas de pantalla

| Pantalla de Configuración | Simulación Visual (Codo Arriba) | Menú de Compartir |
| :---: | :---: | :---: |
| ![Configuración](ruta/imagen1.png) | ![Simulación](ruta/imagen2.png) | ![Compartir](ruta/imagen3.png) |

### Cumplimiento de objetivos
Se cumplieron el 100% de los objetivos planteados: la app resuelve la cinemática inversa, simula el movimiento correctamente, guarda los datos del usuario y permite compartir los resultados.

### Limitaciones actuales
La aplicación está limitada a robots planos de 2 grados de libertad. No soporta robots de 3 grados o configuraciones espaciales (3D) complejas.

---

## Conclusiones

### Aprendizajes obtenidos
Se reforzó significativamente el conocimiento sobre el ciclo de vida de Android, la implementación correcta de la arquitectura MVVM y el uso avanzado de gráficos vectoriales en Jetpack Compose. Además, se aprendió a integrar lógica matemática compleja (robótica) dentro de un entorno móvil eficiente.

### Impacto del proyecto
El proyecto sirve como una base sólida para futuras herramientas educativas en la facultad, demostrando que es posible crear software de ingeniería de alta calidad y utilidad práctica en plataformas móviles accesibles.

### Reflexión del proceso
La separación de responsabilidades (UI vs Lógica) fue clave para mantener un desarrollo ordenado. El uso de Git permitió un control de versiones efectivo, y la elección de Kotlin simplificó considerablemente la sintaxis del código comparado con Java.

---

## Referencias Bibliográficas

1.  **Android Developers.** (2024). *Guide to App Architecture*. Recuperado de: developer.android.com
2.  **Kotlin Foundation.** (2024). *Kotlin Documentation*. Recuperado de: kotlinlang.org/docs/home.html
3.  **Craig, J. J.** (2005). *Introduction to Robotics: Mechanics and Control*. 3rd Edition. Pearson Prentice Hall.
4.  **Spong, M. W., & Vidyasagar, M.** (1989). *Robot Dynamics and Control*. John Wiley & Sons.

---

## Anexos

### Enlaces a repositorios
* **GitHub:** [https://github.com/eduardo060203/KinematicsSolver]
