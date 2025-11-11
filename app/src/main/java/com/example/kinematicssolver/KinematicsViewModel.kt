package com.example.kinematicssolver

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlin.math.sin

/**
 * Para mostrar en la UI (en grados)
 */
data class UiSolution(
    val theta1Deg: Double,
    val theta2Deg: Double
)

class KinematicsViewModel(application: Application) : AndroidViewModel(application) {

    // Repositorio para guardar L1 y L2
    private val settingsRepository = SettingsRepository(getApplication())

    // --- Eventos de Navegación ---
    private val _navigateToSimulation = MutableSharedFlow<Unit>()
    val navigateToSimulation = _navigateToSimulation.asSharedFlow()

    // --- Entradas (Inputs) en CM ---
    var xInputCm by mutableStateOf("20.0")
    var yInputCm by mutableStateOf("10.0")
    var l1InputCm by mutableStateOf("") // Se carga desde DataStore
    var l2InputCm by mutableStateOf("") // Se carga desde DataStore

    // --- Variables de Estado de Error ---
    var xInputError by mutableStateOf<String?>(null)
    var yInputError by mutableStateOf<String?>(null)
    var l1InputError by mutableStateOf<String?>(null)
    var l2InputError by mutableStateOf<String?>(null)

    // --- Salidas (Outputs) ---
    var solutionElbowUpText by mutableStateOf<UiSolution?>(null)
    var solutionElbowDownText by mutableStateOf<UiSolution?>(null)
    internal var solutionElbowUpRad by mutableStateOf<KinematicSolution?>(null)
    internal var solutionElbowDownRad by mutableStateOf<KinematicSolution?>(null)
    internal var l1Meters by mutableStateOf(1.0)
    internal var l2Meters by mutableStateOf(1.0)
    internal var xMeters by mutableStateOf(0.0)
    internal var yMeters by mutableStateOf(0.0)
    var errorMessage by mutableStateOf<String?>(null)

    // --- Estado de UI ---
    var isElbowUpSelected by mutableStateOf(true)
    var showGrid by mutableStateOf(true) // Para el estado de la cuadrícula

    /**
     * El estado de la solución seleccionada (en Radianes) que el Canvas usará para dibujar.
     */
    val selectedSolutionRad: KinematicSolution?
        get() = if (isElbowUpSelected) {
            solutionElbowUpRad
        } else {
            solutionElbowDownRad
        }

    // --- Funciones de Validación de Entradas ---
    private fun validateLength(value: String): String? {
        val num = value.toDoubleOrNull()
        return when {
            num == null -> "Inválido"
            num <= 0.0 -> "Debe ser > 0"
            else -> null // Válido
        }
    }

    private fun validateCoordinate(value: String): String? {
        // Permite un "-" para números negativos
        if (value == "-") return null
        return if (value.toDoubleOrNull() == null) "Inválido" else null
    }

    private fun validatePositiveCoordinate(value: String): String? {
        val num = value.toDoubleOrNull()
        return when {
            num == null -> "Inválido"
            num < 0.0 -> "Debe ser >= 0"
            else -> null // Válido
        }
    }

    // --- Funciones de Eventos de UI ---
    fun onL1Changed(newValue: String) {
        l1InputCm = newValue
        l1InputError = validateLength(newValue)
    }

    fun onL2Changed(newValue: String) {
        l2InputCm = newValue
        l2InputError = validateLength(newValue)
    }

    fun onXChanged(newValue: String) {
        xInputCm = newValue
        xInputError = validateCoordinate(newValue)
    }

    fun onYChanged(newValue: String) {
        yInputCm = newValue
        yInputError = validatePositiveCoordinate(newValue)
    }

    fun calculate() {
        // Re-validar todo antes de calcular
        l1InputError = validateLength(l1InputCm)
        l2InputError = validateLength(l2InputCm)
        xInputError = validateCoordinate(xInputCm)
        yInputError = validatePositiveCoordinate(yInputCm)

        // Si algún error existe, no calculamos
        if (l1InputError != null || l2InputError != null || xInputError != null || yInputError != null) {
            errorMessage = "Revisa los campos en rojo antes de calcular."
            return
        }

        // Guardar L1 y L2 en DataStore
        viewModelScope.launch {
            settingsRepository.saveSettings(l1InputCm, l2InputCm)
        }

        // Si llegamos aquí, las entradas son números válidos
        val xCm = xInputCm.toDouble()
        val yCm = yInputCm.toDouble()
        val l1Cm = l1InputCm.toDouble()
        val l2Cm = l2InputCm.toDouble()

        val x = xCm / 100.0
        val y = yCm / 100.0
        val l1 = l1Cm / 100.0
        val l2 = l2Cm / 100.0

        if (y < 0.0) { // Redundante por la validación de Y, pero seguro
            errorMessage = "Objetivo (Y) está bajo tierra. Imposible."
            clearSolutions(true)
            return
        }

        val solutions = solveInverseKinematics(x, y, l1, l2)

        if (solutions == null) {
            errorMessage = "Posición Inalcanzable (Fuera de rango)."
            clearSolutions(true)
        } else {
            errorMessage = null // Limpiar error previo
            l1Meters = l1
            l2Meters = l2
            xMeters = x
            yMeters = y

            // Validar colisión de codo con la tierra (Y < 0)
            val sol_CodoArriba = solutions.second // theta2 negativo
            val sol_CodoAbajo = solutions.first  // theta2 positivo

            var isCodoArribaValid = true
            var isCodoAbajoValid = true

            val codoArriba_p1_y = l1 * sin(sol_CodoArriba.theta1)
            if (codoArriba_p1_y < 0.0) { isCodoArribaValid = false } // Colisión

            val codoAbajo_p1_y = l1 * sin(sol_CodoAbajo.theta1)
            if (codoAbajo_p1_y < 0.0) { isCodoAbajoValid = false } // Colisión

            clearSolutions(false) // Limpiar soluciones anteriores

            if (isCodoArribaValid) {
                solutionElbowUpRad = sol_CodoArriba
                solutionElbowUpText = UiSolution(
                    theta1Deg = sol_CodoArriba.theta1.toDegrees(),
                    theta2Deg = sol_CodoArriba.theta2.toDegrees()
                )
            }
            if (isCodoAbajoValid) {
                solutionElbowDownRad = sol_CodoAbajo
                solutionElbowDownText = UiSolution(
                    theta1Deg = sol_CodoAbajo.theta1.toDegrees(),
                    theta2Deg = sol_CodoAbajo.theta2.toDegrees()
                )
            }
            if (!isCodoArribaValid && !isCodoAbajoValid) {
                errorMessage = "Soluciones bajo tierra. Imposible."
            } else {
                errorMessage = null
                // Auto-cambiar selección si la actual se vuelve inválida
                if (isElbowUpSelected && !isCodoArribaValid && isCodoAbajoValid) {
                    isElbowUpSelected = false
                } else if (!isElbowUpSelected && !isCodoAbajoValid && isCodoArribaValid) {
                    isElbowUpSelected = true
                }
            }
        }
    }

    private fun clearSolutions(clearMetrics: Boolean) {
        solutionElbowUpText = null
        solutionElbowDownText = null
        solutionElbowUpRad = null
        solutionElbowDownRad = null

        if (clearMetrics) {
            l1Meters = 0.0
            l2Meters = 0.0
            xMeters = 0.0
            yMeters = 0.0
        }
    }

    fun viewSimulation() {
        if (solutionElbowUpRad != null || solutionElbowDownRad != null) {
            viewModelScope.launch {
                _navigateToSimulation.emit(Unit)
            }
        } else {
            errorMessage = "No hay soluciones válidas para simular."
        }
    }

    // Bloque init para cargar datos de DataStore
    init {
        viewModelScope.launch {
            val (l1, l2) = settingsRepository.getSettings.first()
            onL1Changed(l1)
            onL2Changed(l2)
            onXChanged(xInputCm)
            onYChanged(yInputCm)
            calculate()
        }
    }
}