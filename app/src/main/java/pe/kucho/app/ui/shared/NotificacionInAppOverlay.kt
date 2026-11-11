package pe.kucho.app.ui.shared

import androidx.compose.animation.*
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay
import java.util.UUID

enum class TipoNotificacionInApp(
    val colorPrimario: Color,
    val colorFondo: Color,
    val emoji: String
) {
    NUEVA_OFERTA(Color(0xFF6366F1), Color(0xFFEEF2FF), "🏷️"),
    TECNICO_EN_CAMINO(Color(0xFF0284C7), Color(0xFFE0F2FE), "🚗"),
    SERVICIO_FINALIZADO(Color(0xFF059669), Color(0xFFECFDF5), "✅"),
    RECARGA_SALDO(Color(0xFF16A34A), Color(0xFFDCFCE7), "💰"),
    ALERTA(Color(0xFFD97706), Color(0xFFFEF3C7), "⚠️")
}

data class InAppNotificationData(
    val id: String = UUID.randomUUID().toString(),
    val tipo: TipoNotificacionInApp,
    val titulo: String,
    val mensaje: String,
    val textoAccion: String? = null,
    val duracionMs: Long = 4500L,
    val onAccion: (() -> Unit)? = null
)

class InAppNotificationState {
    var actualNotificacion by mutableStateOf<InAppNotificationData?>(null)
        private set

    fun mostrar(notificacion: InAppNotificationData) {
        actualNotificacion = notificacion
    }

    fun ocultar() {
        actualNotificacion = null
    }
}

@Composable
fun rememberInAppNotificationState(): InAppNotificationState {
    return remember { InAppNotificationState() }
}

@Composable
fun NotificacionInAppOverlay(
    state: InAppNotificationState,
    modifier: Modifier = Modifier
) {
    val notificacion = state.actualNotificacion
    var progreso by remember(notificacion?.id) { mutableFloatStateOf(1f) }

    LaunchedEffect(notificacion?.id) {
        if (notificacion != null) {
            progreso = 0f
            delay(notificacion.duracionMs)
            state.ocultar()
        }
    }

    val animatedProgreso by animateFloatAsState(
        targetValue = progreso,
        animationSpec = tween(
            durationMillis = (notificacion?.duracionMs ?: 4500L).toInt(),
            easing = LinearEasing
        ),
        label = "progresoTimer"
    )

    AnimatedVisibility(
        visible = notificacion != null,
        enter = slideInVertically(
            initialOffsetY = { -it },
            animationSpec = tween(350)
        ) + fadeIn(tween(350)),
        exit = slideOutVertically(
            targetOffsetY = { -it },
            animationSpec = tween(250)
        ) + fadeOut(tween(250)),
        modifier = modifier
            .fillMaxWidth()
            .statusBarsPadding()
            .padding(horizontal = 16.dp, vertical = 10.dp)
    ) {
        if (notificacion != null) {
            Card(
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(
                        width = 1.dp,
                        color = notificacion.tipo.colorPrimario.copy(alpha = 0.3f),
                        shape = RoundedCornerShape(18.dp)
                    )
                    .clickable {
                        notificacion.onAccion?.invoke()
                        state.ocultar()
                    }
            ) {
                Column(modifier = Modifier.fillMaxWidth()) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Badge con icono / emoji
                        Box(
                            modifier = Modifier
                                .size(42.dp)
                                .background(notificacion.tipo.colorFondo, CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = notificacion.tipo.emoji,
                                fontSize = 18.sp
                            )
                        }

                        Spacer(modifier = Modifier.width(12.dp))

                        // Título y mensaje
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = notificacion.titulo,
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp,
                                color = Color(0xFF0F172A)
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = notificacion.mensaje,
                                fontSize = 12.sp,
                                color = Color(0xFF475569),
                                maxLines = 2
                            )

                            if (notificacion.textoAccion != null) {
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    text = "${notificacion.textoAccion} →",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = notificacion.tipo.colorPrimario
                                )
                            }
                        }

                        // Botón cerrar
                        IconButton(
                            onClick = { state.ocultar() },
                            modifier = Modifier.size(28.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Cerrar notificación",
                                tint = Color(0xFF94A3B8),
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }

                    // Barra de progreso decreciente (Timer visual)
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(3.dp)
                            .background(Color(0xFFF1F5F9))
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth(animatedProgreso.coerceIn(0f, 1f))
                                .fillMaxHeight()
                                .background(notificacion.tipo.colorPrimario)
                        )
                    }
                }
            }
        }
    }
}

