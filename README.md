
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
