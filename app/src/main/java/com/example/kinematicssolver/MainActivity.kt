package com.example.kinematicssolver

// (Todas las importaciones siguen igual)
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Canvas as AndroidCanvas
import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.clipPath
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.FileProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.kinematicssolver.ui.theme.KinematicsSolverTheme
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.min
import kotlin.math.sin


// (Sealed class Screen sigue igual)
sealed class Screen(val route: String) {
    object Solver : Screen("solver_screen")
    object Simulation : Screen("simulation_screen")
}

class MainActivity : ComponentActivity() {
    // (onCreate sigue igual)
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
    // (Esta pantalla no tiene cambios)
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
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
        vm.errorMessage?.let {
            Text(
                it,
                color = MaterialTheme.colorScheme.error,
                modifier = Modifier.padding(top = 16.dp)
            )
        }
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

    // (Variables para compartir siguen igual)
    val context = LocalContext.current
    val view = LocalView.current
    val coroutineScope = rememberCoroutineScope()
    val shareLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { }


    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {

        // (Botones Volver y Compartir siguen igual)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Button(
                onClick = { navController.popBackStack() },
                modifier = Modifier.weight(1f),
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary)
            ) {
                Icon(Icons.Filled.ArrowBack, contentDescription = "Volver", modifier = Modifier.size(20.dp))
                Spacer(Modifier.width(8.dp))
                Text("VOLVER", color = MaterialTheme.colorScheme.onSecondary)
            }
            Button(
                onClick = {
                    coroutineScope.launch {
                        // 1. Preparar el Texto
                        val solutionType = if (vm.isElbowUpSelected) "Codo Arriba" else "Codo Abajo"
                        val currentSolution = if (vm.isElbowUpSelected) vm.solutionElbowUpText else vm.solutionElbowDownText
                        val shareText = "Simulación de Cinemática Inversa:\n\n" +
                                "Parámetros:\n" +
                                "  L1: ${vm.l1InputCm} cm\n" +
                                "  L2: ${vm.l2InputCm} cm\n" +
                                "Objetivo:\n" +
                                "  X: ${vm.xInputCm} cm\n" +
                                "  Y: ${vm.yInputCm} cm\n\n" +
                                "Solución ($solutionType):\n" +
                                "  θ1: ${String.format("%.2f°", currentSolution?.theta1Deg ?: 0.0)}\n" +
                                "  θ2: ${String.format("%.2f°", currentSolution?.theta2Deg ?: 0.0)}"

                        // 2. Capturar la imagen
                        // AHORA ESTA FUNCIÓN ES SEGURA
                        val file = captureViewAsBitmap(view, context)
                        if (file == null) {
                            withContext(Dispatchers.Main) {
                                android.widget.Toast.makeText(context, "Error al capturar imagen", android.widget.Toast.LENGTH_SHORT).show()
                            }
                            return@launch
                        }

                        // 3. Obtener la URI segura
                        val uri = FileProvider.getUriForFile(
                            context,
                            "${context.packageName}.fileprovider",
                            file
                        )

                        // 4. Crear y lanzar el Intent de Compartir
                        val shareIntent = Intent(Intent.ACTION_SEND).apply {
                            type = "image/png"
                            putExtra(Intent.EXTRA_STREAM, uri)
                            putExtra(Intent.EXTRA_TEXT, shareText)
                            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                        }
                        shareLauncher.launch(Intent.createChooser(shareIntent, "Compartir Simulación..."))
                    }
                },
                modifier = Modifier.weight(1f),
                enabled = vm.hasValidSolution,
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.tertiary)
            ) {
                Icon(Icons.Filled.Share, contentDescription = "Compartir", modifier = Modifier.size(20.dp))
                Spacer(Modifier.width(8.dp))
                Text("COMPARTIR")
            }
        }

        Spacer(Modifier.height(16.dp))

        // (Switch de cuadrícula sigue igual)
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

        // (Box del Canvas y HUD sigue igual)
        val currentSolutionText = if (vm.isElbowUpSelected) vm.solutionElbowUpText else vm.solutionElbowDownText
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .border(2.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.5f), RoundedCornerShape(12.dp))
        ) {
            RobotArmCanvas(
                modifier = Modifier.matchParentSize(),
                l1 = vm.l1Meters,
                l2 = vm.l2Meters,
                xTargetM = vm.xMeters,
                yTargetM = vm.yMeters,
                solution = vm.selectedSolutionRad,
                showGrid = vm.showGrid
            )
            val hudTextStyle = LocalTextStyle.current.copy(
                shadow = Shadow(color = Color.Black.copy(alpha = 0.8f), offset = Offset(2f, 2f), blurRadius = 4f)
            )
            if (vm.hasValidSolution && currentSolutionText != null) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp)
                        .align(Alignment.TopCenter),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = String.format("θ1: %.1f°\nθ2: %.1f°", currentSolutionText.theta1Deg, currentSolutionText.theta2Deg),
                        color = MaterialTheme.colorScheme.onSurface,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        style = hudTextStyle
                    )
                    Text(
                        text = String.format("X: %.1f cm\nY: %.1f cm", vm.xInputCm.toDoubleOrNull() ?: 0.0, vm.yInputCm.toDoubleOrNull() ?: 0.0),
                        color = MaterialTheme.colorScheme.onSurface,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.End,
                        style = hudTextStyle
                    )
                }
            } else {
                Text(
                    text = "Sin Solución Válida",
                    modifier = Modifier
                        .align(Alignment.TopCenter)
                        .padding(top = 12.dp),
                    color = MaterialTheme.colorScheme.error,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    style = hudTextStyle
                )
            }
        }
    }
}


@Composable
fun NumericInputField(
    // (Esta función no tiene cambios)
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
    // (Esta función no tiene cambios)
    modifier: Modifier = Modifier,
    l1: Double,
    l2: Double,
    xTargetM: Double,
    yTargetM: Double,
    solution: KinematicSolution?,
    showGrid: Boolean
) {
    val theta1 = solution?.theta1 ?: 0.0
    val theta2 = solution?.theta2 ?: 0.0
    val link1Color = MaterialTheme.colorScheme.primary
    val link2Color = MaterialTheme.colorScheme.secondary
    val jointColor = MaterialTheme.colorScheme.onSurface
    val gridColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.2f)
    val backgroundColor = Color(0xFF1A2633)
    val effectorColor = MaterialTheme.colorScheme.error
    val targetColor = Color(0xFF00FF00)
    val groundColor = Color(0xFF6B4B3E)
    val grassColor = Color(0xFF558B2F)
    val workspaceColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.4f)
    val dashEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 10f), 0f)

    Canvas(
        modifier = modifier.background(backgroundColor, RoundedCornerShape(12.dp))
    ) {
        val maxReach = (l1 + l2) * 1.1
        if (maxReach <= 0) return@Canvas
        val origin = Offset(size.width / 2, size.height * 0.75f)
        val scale = min(size.width, size.height) / (maxReach * 2).toFloat()
        val groundLineY = origin.y
        drawRect(backgroundColor)
        if (showGrid) {
            val stepSizePx = 100f
            var y = groundLineY
            while (y >= 0) {
                drawLine(gridColor, start = Offset(0f, y), end = Offset(size.width, y), strokeWidth = 1f)
                y -= stepSizePx
            }
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
            drawLine(gridColor, start = Offset(origin.x, 0f), end = Offset(origin.x, size.height), strokeWidth = 2f)
        }
        if (l1 > 0 && l2 > 0) {
            val outerRadius = (l1 + l2).toFloat() * scale
            val innerRadius = abs(l1 - l2).toFloat() * scale
            drawArc(
                color = workspaceColor,
                startAngle = 180f,
                sweepAngle = 180f,
                useCenter = false,
                topLeft = Offset(origin.x - outerRadius, origin.y - outerRadius),
                size = Size(outerRadius * 2, outerRadius * 2),
                style = Stroke(width = 3f, pathEffect = dashEffect)
            )
            if (innerRadius > 0.01f * scale) {
                drawArc(
                    color = workspaceColor,
                    startAngle = 180f,
                    sweepAngle = 180f,
                    useCenter = false,
                    topLeft = Offset(origin.x - innerRadius, origin.y - innerRadius),
                    size = Size(innerRadius * 2, innerRadius * 2),
                    style = Stroke(width = 3f, pathEffect = dashEffect)
                )
            }
        }
        val groundThickness = 20f
        drawRect(
            color = groundColor,
            topLeft = Offset(0f, groundLineY),
            size = Size(size.width, groundThickness)
        )
        drawLine(
            color = grassColor,
            start = Offset(0f, groundLineY),
            end = Offset(size.width, groundLineY),
            strokeWidth = 8f
        )
        val p0 = origin
        val p1_x = origin.x + (l1 * scale * cos(theta1)).toFloat()
        val p1_y = origin.y - (l1 * scale * sin(theta1)).toFloat()
        val p1 = Offset(p1_x, p1_y)
        val p2_x = p1.x + (l2 * scale * cos(theta1 + theta2)).toFloat()
        val p2_y = p1.y - (l2 * scale * sin(theta1 + theta2)).toFloat()
        val p2 = Offset(p2_x, p2_y)
        val targetPxX = origin.x + (xTargetM * scale).toFloat()
        val targetPxY = origin.y - (yTargetM * scale).toFloat()
        drawCircle(targetColor, radius = 15f, center = Offset(targetPxX, targetPxY), style = Stroke(width = 4f))
        drawCircle(targetColor, radius = 5f, center = Offset(targetPxX, targetPxY))
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
            drawCircle(jointColor, radius = 12f, center = p0)
            drawCircle(jointColor, radius = 15f, center = p1)
            drawCircle(effectorColor, radius = 18f, center = p2, style = Stroke(width = 6f))
            drawCircle(effectorColor, radius = 8f, center = p2)
        }
    }
}


// --- INICIO: FUNCIÓN DE CAPTURA CORREGIDA ---
@Suppress("DEPRECATION") // Necesario para view.draw()
suspend fun captureViewAsBitmap(view: android.view.View, context: Context): File? {

    // 1. Crear Bitmap y dibujar en el HILO PRINCIPAL
    val bitmap = withContext(Dispatchers.Main) {
        try {
            val b = Bitmap.createBitmap(view.width, view.height, Bitmap.Config.ARGB_8888)
            val canvas = AndroidCanvas(b)
            view.draw(canvas)
            b // Devuelve el bitmap
        } catch (e: Exception) {
            e.printStackTrace()
            null // Devuelve null si falla el dibujo
        }
    } ?: return null // Si el bitmap es nulo (falló), salimos

    // 2. Guardar el archivo en el HILO DE IO
    return withContext(Dispatchers.IO) {
        try {
            val imagesFolder = File(context.cacheDir, "shared_images")
            imagesFolder.mkdirs() // Asegurarse de que la carpeta exista
            val file = File(imagesFolder, "kinematics_simulation.png")

            FileOutputStream(file).use { out ->
                bitmap.compress(Bitmap.CompressFormat.PNG, 90, out) // 90% de calidad
            }
            file // Devuelve el archivo
        } catch (e: Exception) {
            e.printStackTrace()
            null // Devuelve null si falla el guardado
        }
    }
}