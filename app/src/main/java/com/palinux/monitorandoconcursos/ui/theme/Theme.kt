package com.palinux.monitorandoconcursos.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable

private val CustomDarkColorScheme = darkColorScheme(
    primary = AzulAco,
    onPrimary = CinzaClaro,
    primaryContainer = PretoAsfalto,
    onPrimaryContainer = CinzaClaro,
    secondary = AzulClaroFocado,
    background = AzulEscuroProfundo,
    surface = AzulNoite,
    surfaceVariant = AzulNoite,
    onBackground = CinzaClaro,
    onSurface = CinzaClaro,
    onSurfaceVariant = CinzaMutado,
    outline = CinzaBorda
)

@Composable
fun MonitorandoConcursosTheme(
    content: @Composable () -> Unit
) {
    // Forçamos o uso da nossa paleta Dark Customizada independente do sistema
    MaterialTheme(
        colorScheme = CustomDarkColorScheme,
        typography = Typography, // Mantém a tipografia padrão do projeto
        content = content
    )
}