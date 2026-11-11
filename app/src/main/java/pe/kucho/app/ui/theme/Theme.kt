package pe.kucho.app.ui.theme

import androidx.compose.material3.*
import androidx.compose.runtime.Composable

private val LightColorScheme = lightColorScheme(
    primary = OrangePrimary,          // color original de la app
    secondary = OrangeSecondary,
    tertiary = KuchoGreen,
    error = KuchoRed,
    background = LightBackground,
    surface = androidx.compose.ui.graphics.Color.White,
    onSurface = KuchoTextDark,
    onBackground = KuchoTextDark,
    outline = KuchoBorder,
    surfaceVariant = KuchoSurface
)

@Composable
fun KuchoTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = LightColorScheme,
        typography = Typography(),
        content = content
    )
}
