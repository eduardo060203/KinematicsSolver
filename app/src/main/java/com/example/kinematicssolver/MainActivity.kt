package com.example.kinematicssolver

// ---- IMPORTACIONES COMPLETAS ----
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.clipPath
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.kinematicssolver.ui.theme.KinematicsSolverTheme
import kotlin.math.cos
import kotlin.math.min
import kotlin.math.sin
// ---- FIN DE IMPORTACIONES ----


// Definimos las rutas de nuestras pantallas
sealed class Screen(val route: String) {
    object Solver : Screen("solver_screen")
    object Simulation : Screen("simulation_screen")
}

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            KinematicsSolverTheme(darkTheme = true) {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    val navController = rememberNavController()
                    val vm: KinematicsViewModel = viewModel()

                    // Recolectar eventos de navegación del ViewModel
                    LaunchedEffect(Unit) {
                        vm.navigateToSimulation.collect {
                            navController.navigate(Screen.Simulation.route)
                        }
                    }

                    NavHost(navController = navController, startDestination = Screen.Solver.route) {
                        composable(Screen.Solver.route) {
                            SolverScreen(vm = vm, navController = navController)
                        }
                        composable(Screen.Simulation.route) {
                            SimulationScreen(vm = vm, navController = navController)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun SolverScreen(vm: KinematicsViewModel, navController: NavController) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {

        // Tarjeta de Parámetros
        Card(
            modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp),
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
        ) {
            Column(Modifier.padding(16.dp)) {
                Text(
                    "PARÁMETROS DEL ROBOT",
                    style = MaterialTheme.typography.titleMedium.copy(color = MaterialTheme.colorScheme.primary),
                    modifier = Modifier.padding(bottom = 8.dp)
                )
                NumericInputField(
                    label = "Longitud L1",
                    value = vm.l1InputCm,
                    onValueChange = { vm.onL1Changed(it) },
                    suffix = "cm",
                    errorText = vm.l1InputError
                )
                Spacer(Modifier.height(8.dp))
                NumericInputField(
                    label = "Longitud L2",
                    value = vm.l2InputCm,
                    onValueChange = { vm.onL2Changed(it) },
                    suffix = "cm",
                    errorText = vm.l2InputError
                )
            }
        }

        // Tarjeta de Objetivo (X,Y)
        Card(
            modifier = Modifier.fillMaxWidth().padding(bottom = 24.dp),
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
        ) {
            Column(Modifier.padding(16.dp)) {
                Text(
                    "OBJETIVO (X, Y)",
                    style = MaterialTheme.typography.titleMedium.copy(color = MaterialTheme.colorScheme.primary),
                    modifier = Modifier.padding(bottom = 8.dp)
                )
                NumericInputField(
                    label = "Coordenada X",
                    value = vm.xInputCm,
                    onValueChange = { vm.onXChanged(it) },
                    suffix = "cm",
                    errorText = vm.xInputError
                )
                Spacer(Modifier.height(8.dp))
                NumericInputField(
                    label = "Coordenada Y",
                    value = vm.yInputCm,
                    onValueChange = { vm.onYChanged(it) },
                    suffix = "cm",
                    errorText = vm.yInputError
                )
            }
        }

        // Botones Codo Abajo/Arriba
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Button(
                onClick = { vm.isElbowUpSelected = false },
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(8.dp),
                enabled = vm.solutionElbowDownRad != null,
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (!vm.isElbowUpSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant,
                    contentColor = if (!vm.isElbowUpSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant,
                    disabledContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                    disabledContentColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                )
            ) { Text("Codo Abajo") }
            Button(
                onClick = { vm.isElbowUpSelected = true },
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(8.dp),
                enabled = vm.solutionElbowUpRad != null,
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (vm.isElbowUpSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant,
                    contentColor = if (vm.isElbowUpSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant,
                    disabledContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                    disabledContentColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                )
            ) { Text("Codo Arriba") }
        }

        // Botones Calcular/Simulación
        Button(
            onClick = { vm.calculate() },
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp)
                .padding(horizontal = 8.dp),
            shape = RoundedCornerShape(8.dp),
            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
        ) { Text("CALCULAR CINEMÁTICA", color = MaterialTheme.colorScheme.onPrimary, fontSize = 18.sp, fontWeight = FontWeight.Bold) }
        Spacer(Modifier.height(16.dp))
        Button(
            onClick = { vm.viewSimulation() },
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp)
                .padding(horizontal = 8.dp),
            shape = RoundedCornerShape(8.dp),
            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary)
        ) { Text("VER SIMULACIÓN", color = MaterialTheme.colorScheme.onSecondary, fontSize = 18.sp, fontWeight = FontWeight.Bold) }

        // Mensaje de Error
        vm.errorMessage?.let {
            Text(
                it,
                color = MaterialTheme.colorScheme.error,
                modifier = Modifier.padding(top = 16.dp)
            )
        }

        // Mostrar Solución
        val currentSolution = if (vm.isElbowUpSelected) vm.solutionElbowUpText else vm.solutionElbowDownText
        if (currentSolution != null) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 24.dp)
                    .background(Color.Black.copy(alpha = 0.3f), RoundedCornerShape(8.dp))
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    "Solución encontrada:",
                    style = MaterialTheme.typography.titleMedium.copy(color = MaterialTheme.colorScheme.primary),
                    modifier = Modifier.padding(bottom = 4.dp)
                )
                Text(
                    String.format("Theta 1: %.2f°", currentSolution.theta1Deg),
                    style = MaterialTheme.typography.titleLarge.copy(color = MaterialTheme.colorScheme.onBackground)
                )
                Text(
                    String.format("Theta 2: %.2f°", currentSolution.theta2Deg),
                    style = MaterialTheme.typography.titleLarge.copy(color = MaterialTheme.colorScheme.onBackground)
                )
            }
        }
    }
}

@Composable
fun SimulationScreen(vm: KinematicsViewModel, navController: NavController) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        // Botón Volver
        Button(
            onClick = { navController.popBackStack() },
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp),
            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary)
        ) {
            Text("VOLVER A PARÁMETROS", color = MaterialTheme.colorScheme.onSecondary)
        }

        // Tarjeta de Info
        val currentSolutionText = if (vm.isElbowUpSelected) vm.solutionElbowUpText else vm.solutionElbowDownText
        currentSolutionText?.let {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
            ) {
                Column(Modifier.padding(16.dp)) {
                    Text(
                        text = String.format("θ1: %.1f°", it.theta1Deg),
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    )
                    Text(
                        text = String.format("θ2: %.1f°", it.theta2Deg),
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    )
                    Spacer(Modifier.height(8.dp))
                    Text(
                        text = String.format(
                            "Punto (X,Y):\n(%.1f, %.1f)",
                            vm.xInputCm.toDoubleOrNull() ?: 0.0,
                            vm.yInputCm.toDoubleOrNull() ?: 0.0
                        ),
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    )
                }
            }
        } ?: run {
            // Tarjeta de Error si no hay solución
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer)
            ) {
                Text(
                    "No hay soluciones válidas para mostrar la simulación.",
                    color = MaterialTheme.colorScheme.onErrorContainer,
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.padding(16.dp)
                )
            }
        }

        // Switch para Cuadrícula
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Text(text = "Mostrar Cuadrícula", style = MaterialTheme.typography.bodyLarge)
            Spacer(modifier = Modifier.width(8.dp))
            Switch(
                checked = vm.showGrid,
                onCheckedChange = { vm.showGrid = it },
                colors = SwitchDefaults.colors(
                    checkedTrackColor = MaterialTheme.colorScheme.primary,
                    uncheckedTrackColor = MaterialTheme.colorScheme.surfaceVariant,
                    checkedThumbColor = MaterialTheme.colorScheme.onPrimary
                )
            )
        }

        // Canvas
        RobotArmCanvas(
            l1 = vm.l1Meters,
            l2 = vm.l2Meters,
            xTarget = vm.xMeters,
            yTarget = vm.yMeters,
            solution = vm.selectedSolutionRad,
            showGrid = vm.showGrid
        )
    }
}

@Composable
fun NumericInputField(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    suffix: String = "",
    errorText: String? = null
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label) },
        suffix = { Text(suffix) },
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
        singleLine = true,
        modifier = modifier.fillMaxWidth(),
        isError = errorText != null,
        supportingText = {
            if (errorText != null) {
                Text(errorText, color = MaterialTheme.colorScheme.error)
            }
        },
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = MaterialTheme.colorScheme.primary,
            unfocusedBorderColor = MaterialTheme.colorScheme.onSurfaceVariant,
            focusedLabelColor = MaterialTheme.colorScheme.primary,
            unfocusedLabelColor = MaterialTheme.colorScheme.onSurfaceVariant,
            focusedTextColor = MaterialTheme.colorScheme.onSurface,
            unfocusedTextColor = MaterialTheme.colorScheme.onSurface,
            errorBorderColor = MaterialTheme.colorScheme.error,
            errorLabelColor = MaterialTheme.colorScheme.error
        )
    )
}

@Composable
fun RobotArmCanvas(
    l1: Double,
    l2: Double,
    xTarget: Double,
    yTarget: Double,
    solution: KinematicSolution?,
    showGrid: Boolean // <-- Parámetro nuevo
) {
    val theta1 = solution?.theta1 ?: 0.0
    val theta2 = solution?.theta2 ?: 0.0

    // Colores
    val link1Color = MaterialTheme.colorScheme.primary
    val link2Color = MaterialTheme.colorScheme.secondary
    val jointColor = MaterialTheme.colorScheme.onSurface
    val gridColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.2f)
    val backgroundColor = Color(0xFF1A2633)
    val effectorColor = MaterialTheme.colorScheme.error
    val targetColor = Color(0xFF00FF00) // Verde
    val borderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)
    val groundColor = Color(0xFF967969) // Marrón

    Canvas(
        modifier = Modifier
            .fillMaxWidth()
            .aspectRatio(0.5f) // Más alto
            .border(2.dp, borderColor, RoundedCornerShape(12.dp))
            .background(backgroundColor, RoundedCornerShape(12.dp))
    ) {
        // Configuración de Escala y Origen
        val maxReach = (l1 + l2) * 1.1
        if (maxReach <= 0) return@Canvas

        val origin = Offset(size.width / 2, size.height * 0.75f) // Tierra abajo
        val scale = min(size.width, size.height) / (maxReach * 2).toFloat()
        val groundLineY = origin.y

        drawRect(backgroundColor)

        // Dibujo de Cuadrícula (Condicional)
        if (showGrid) {
            // Usamos un tamaño de paso fijo en píxeles
            val stepSizePx = 50.dp.toPx()

            // Líneas horizontales
            var y = groundLineY
            while (y >= 0) {
                drawLine(gridColor, start = Offset(0f, y), end = Offset(size.width, y), strokeWidth = 1f)
                y -= stepSizePx
            }
            // Líneas verticales
            var x = origin.x
            while (x <= size.width) {
                drawLine(gridColor, start = Offset(x, 0f), end = Offset(x, size.height), strokeWidth = 1f)
                x += stepSizePx
            }
            x = origin.x - stepSizePx
            while (x >= 0) {
                drawLine(gridColor, start = Offset(x, 0f), end = Offset(x, size.height), strokeWidth = 1f)
                x -= stepSizePx
            }
            // Eje Y (más grueso)
            drawLine(gridColor, start = Offset(origin.x, 0f), end = Offset(origin.x, size.height), strokeWidth = 2f)
        }

        // Dibujo de Tierra (Siempre)
        val groundPath = Path().apply {
            moveTo(0f, groundLineY)
            lineTo(size.width, groundLineY)
            lineTo(size.width, size.height)
            lineTo(0f, size.height)
            close()
        }
        clipPath(path = groundPath) {
            drawRect(color = groundColor)
            val hachureColor = Color.Black.copy(alpha = 0.3f)
            val hachureSpacing = 40f
            var hachureX = -size.height
            while (hachureX < size.width + size.height) {
                drawLine(
                    color = hachureColor,
                    start = Offset(hachureX, groundLineY),
                    end = Offset(hachureX + size.height, size.height),
                    strokeWidth = 2f
                )
                hachureX += hachureSpacing
            }
        }

        // Calcular Coordenadas del Brazo
        val p0 = origin
        val p1_x = origin.x + (l1 * scale * cos(theta1)).toFloat()
        val p1_y = origin.y - (l1 * scale * sin(theta1)).toFloat()
        val p1 = Offset(p1_x, p1_y)
        val p2_x = p1.x + (l2 * scale * cos(theta1 + theta2)).toFloat()
        val p2_y = p1.y - (l2 * scale * sin(theta1 + theta2)).toFloat()
        val p2 = Offset(p2_x, p2_y)

        // Dibujar Objetivo
        val targetPxX = origin.x + (xTarget * scale).toFloat()
        val targetPxY = origin.y - (yTarget * scale).toFloat()
        drawCircle(targetColor, radius = 15f, center = Offset(targetPxX, targetPxY), style = Stroke(width = 4f))
        drawCircle(targetColor, radius = 5f, center = Offset(targetPxX, targetPxY))

        // Dibujar Brazo (si la solución es válida)
        if (solution != null) {
            drawLine(
                color = link1Color,
                start = p0,
                end = p1,
                strokeWidth = 16f,
                cap = StrokeCap.Round
            )
            drawLine(
                color = link2Color,
                start = p1,
                end = p2,
                strokeWidth = 16f,
                cap = StrokeCap.Round
            )
            // Dibujar Articulaciones
            drawCircle(jointColor, radius = 12f, center = p0)
            drawCircle(jointColor, radius = 15f, center = p1)
            drawCircle(effectorColor, radius = 18f, center = p2, style = Stroke(width = 6f))
            drawCircle(effectorColor, radius = 8f, center = p2)
        }
    }
}