package com.example.kinematicssolver.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color  // <-- ¡ESTA ES LA IMPORTACIÓN CORRECTA Y LA SOLUCIÓN!
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

// Define tus colores personalizados aquí, inspirados en la imagen
val PrimaryCian = Color(0xFF00E0FF) // Un cian brillante
val SecondaryAzure = Color(0xFF53A8FF) // Un azul secundario
val DarkBackground = Color(0xFF121B27) // Fondo muy oscuro
val DarkSurface = Color(0xFF1E2835) // Superficie de tarjetas
val DarkError = Color(0xFFFF416C) // Rojo de error
val DarkOnPrimary = Color.Black // Texto sobre el cian (Color.Black ahora funcionará)
val DarkOnSecondary = Color.White // Texto sobre el azul
val DarkOnBackground = Color.White // Texto general
val DarkOnSurface = Color.White // Texto en superficies
val DarkOutline = Color(0xFF334A66) // Bordes sutiles

private val DarkColorScheme = darkColorScheme(
    primary = PrimaryCian,
    onPrimary = DarkOnPrimary,
    secondary = SecondaryAzure,
    onSecondary = DarkOnSecondary,
    tertiary = Color(0xFF88D8B0), // Un color terciario
    background = DarkBackground,
    onBackground = DarkOnBackground,
    surface = DarkSurface,
    onSurface = DarkOnSurface,
    error = DarkError,
    onError = Color.White,
    primaryContainer = PrimaryCian.copy(alpha = 0.2f), // Un poco de cian para los contenedores seleccionados
    surfaceVariant = Color(0xFF2E3B4B), // Un gris-azul más oscuro para los fondos de tarjetas
    onSurfaceVariant = Color.LightGray,
    outline = DarkOutline
)

@Composable
fun KinematicsSolverTheme(
    darkTheme: Boolean = true, // Forzamos a true para que siempre sea oscuro
    // Dynamic color is available on Android 12+
    dynamicColor: Boolean = false, // Lo dejamos en false para usar nuestros colores
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme -> DarkColorScheme
        else -> DarkColorScheme // Si no es dark, aún así usamos el dark (porque lo forzamos)
    }
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            // Usamos .toArgb() para convertir el Color de Compose a un Int de Android
            window.statusBarColor = colorScheme.background.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = !darkTheme // Falso para íconos claros
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography, // Asegúrate de que esta línea esté presente
        content = content
    )
}