package com.example.kinematicssolver

import kotlin.math.*

/**
 * Contiene los dos ángulos de la solución.
 * Las unidades están en RADIANES.
 */
data class KinematicSolution(
    val theta1: Double,
    val theta2: Double
)

/**
 * Contiene ambas soluciones (codo arriba y codo abajo).
 * Si la posición es inalcanzable, devuelve null.
 */
fun solveInverseKinematics(x: Double, y: Double, l1: Double, l2: Double): Pair<KinematicSolution, KinematicSolution>? {

    // 1. Validar longitudes (no pueden ser cero o negativas)
    if (l1 <= 0 || l2 <= 0) {
        return null
    }

    // 2. Calcular la distancia al cuadrado desde el origen
    val rSquared = x.pow(2) + y.pow(2)
    val r = sqrt(rSquared)

    // 3. Verificar si el punto es alcanzable
    // Si la distancia es mayor que la suma de los eslabones (muy lejos)
    if (r > l1 + l2) {
        return null
    }
    // Si la distancia es menor que la diferencia (muy cerca)
    if (r < abs(l1 - l2)) {
        return null
    }
    // Si el punto es (0,0) y los eslabones no son iguales, es inalcanzable
    if (r == 0.0 && l1 != l2) {
        return null
    }


    // 4. Calcular theta2 (usando la Ley de Cosenos)
    val cosTheta2 = (rSquared - l1.pow(2) - l2.pow(2)) / (2 * l1 * l2)
    val clampedCosTheta2 = cosTheta2.coerceIn(-1.0, 1.0) // Corregir errores de punto flotante

    val theta2_up = acos(clampedCosTheta2)   // Solución "codo arriba" (positiva)
    val theta2_down = -theta2_up            // Solución "codo abajo" (negativa)

    // 5. Calcular theta1
    val k1_up = l1 + l2 * cos(theta2_up)
    val k2_up = l2 * sin(theta2_up)
    val theta1_up = atan2(y, x) - atan2(k2_up, k1_up)

    val solutionUp = KinematicSolution(theta1 = theta1_up, theta2 = theta2_up)

    val k1_down = l1 + l2 * cos(theta2_down)
    val k2_down = l2 * sin(theta2_down)
    val theta1_down = atan2(y, x) - atan2(k2_down, k1_down)

    val solutionDown = KinematicSolution(theta1 = theta1_down, theta2 = theta2_down)

    return Pair(solutionUp, solutionDown)
}

/**
 * Función helper para convertir radianes a grados.
 */
fun Double.toDegrees(): Double {
    return this * 180.0 / PI
}