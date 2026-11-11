package pe.kucho.app.ui.onboarding

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.launch
import pe.kucho.app.data.local.SessionDataStore

data class OnboardingItem(
    val titulo: String,
    val subtitulo: String,
    val descripcion: String,
    val emoji: String,
    val pillTexto: String,
    val coloresGradiente: List<Color>
)

@Composable
fun OnboardingScreen(
    onCompletado: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val sessionDataStore = remember { SessionDataStore(context) }

    val items = remember {
        listOf(
            OnboardingItem(
                titulo = "Especialistas en Trujillo",
                subtitulo = "Gasfitería, electricidad y cerrajería",
                descripcion = "Encuentra técnicos calificados y con DNI verificado en Víctor Larco, Trujillo Centro, La Esperanza y Huanchaco.",
                emoji = "🛠️",
                pillTexto = "⚡ Asistencia Rápida",
                coloresGradiente = listOf(Color(0xFF6366F1), Color(0xFF8B5CF6))
            ),
            OnboardingItem(
                titulo = "Negociación Justa",
                subtitulo = "Cotizaciones transparentes",
                descripcion = "Recibe ofertas de distintos técnicos, contraoferta en tiempo real y pacta el precio exacto antes de que comience el trabajo.",
                emoji = "🤝",
                pillTexto = "🎯 Sin Sorpresas",
                coloresGradiente = listOf(Color(0xFF3B82F6), Color(0xFF06B6D4))
            ),
            OnboardingItem(
                titulo = "Rastreo en Vivo",
                subtitulo = "Sigue el avance con GPS",
                descripcion = "Conoce cuándo tu técnico sale de camino y monitorea las 3 etapas del servicio. Llama o escribe por WhatsApp con un solo toque.",
                emoji = "📍",
                pillTexto = "🚗 En Tiempo Real",
                coloresGradiente = listOf(Color(0xFF10B981), Color(0xFF059669))
            ),
            OnboardingItem(
                titulo = "Garantía Kucho",
                subtitulo = "Billetera digital y respaldo",
                descripcion = "Comisión transparente del 12% para técnicos. Califica la puntualidad y servicio para mantener la máxima confianza en nuestra comunidad.",
                emoji = "⭐",
                pillTexto = "🛡️ 100% Respaldado",
                coloresGradiente = listOf(Color(0xFFF59E0B), Color(0xFFEA580C))
            )
        )
    }

    val pagerState = rememberPagerState(pageCount = { items.size })

    fun finalizar() {
        scope.launch {
            sessionDataStore.setOnboardingCompletado(true)
            onCompletado()
        }
    }

    Scaffold(
        containerColor = Color.White
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .statusBarsPadding()
                .navigationBarsPadding()
        ) {
            // Barra superior con botón "Saltar"
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "KUCHO",
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 18.sp,
                    color = Color(0xFF4F46E5)
                )

                if (pagerState.currentPage < items.size - 1) {
                    TextButton(onClick = { finalizar() }) {
                        Text(
                            "Saltar",
                            fontWeight = FontWeight.SemiBold,
                            color = Color(0xFF64748B),
                            fontSize = 14.sp
                        )
                    }
                } else {
                    Spacer(modifier = Modifier.width(48.dp))
                }
            }

            // Pager con diapositivas
            HorizontalPager(
                state = pagerState,
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
            ) { page ->
                val item = items[page]
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 28.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    // Círculo decorativo con icono y gradiente
                    Box(
                        modifier = Modifier
                            .size(150.dp)
                            .background(
                                Brush.linearGradient(item.coloresGradiente),
                                CircleShape
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = item.emoji,
                            fontSize = 68.sp
                        )
                    }

                    Spacer(modifier = Modifier.height(28.dp))

                    // Pill decorativo
                    Surface(
                        shape = RoundedCornerShape(16.dp),
                        color = Color(0xFFEEF2FF)
                    ) {
                        Text(
                            text = item.pillTexto,
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp,
                            color = Color(0xFF4338CA),
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 5.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    Text(
                        text = item.titulo,
                        fontSize = 24.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = Color(0xFF0F172A),
                        textAlign = TextAlign.Center
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    Text(
                        text = item.subtitulo,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color(0xFF6366F1),
                        textAlign = TextAlign.Center
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    Text(
                        text = item.descripcion,
                        fontSize = 13.sp,
                        color = Color(0xFF64748B),
                        textAlign = TextAlign.Center,
                        lineHeight = 20.sp
                    )
                }
            }

            // Barra inferior con indicadores y botones
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Indicador de puntos tipo carrusel moderno
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    repeat(items.size) { index ->
                        val esActivo = pagerState.currentPage == index
                        val anchoDot by animateDpAsState(
                            targetValue = if (esActivo) 26.dp else 8.dp,
                            animationSpec = tween(300),
                            label = "anchoDot"
                        )
                        val colorDot by animateColorAsState(
                            targetValue = if (esActivo) Color(0xFF4F46E5) else Color(0xFFE2E8F0),
                            animationSpec = tween(300),
                            label = "colorDot"
                        )

                        Box(
                            modifier = Modifier
                                .height(8.dp)
                                .width(anchoDot)
                                .clip(RoundedCornerShape(4.dp))
                                .background(colorDot)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Botón Siguiente / Comenzar
                val esUltimaPagina = pagerState.currentPage == items.size - 1

                Button(
                    onClick = {
                        if (esUltimaPagina) {
                            finalizar()
                        } else {
                            scope.launch {
                                pagerState.animateScrollToPage(pagerState.currentPage + 1)
                            }
                        }
                    },
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (esUltimaPagina) Color(0xFF10B981) else Color(0xFF4F46E5)
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp)
                ) {
                    Text(
                        text = if (esUltimaPagina) "¡Comenzar ahora! 🚀" else "Continuar →",
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp,
                        color = Color.White
                    )
                }
            }
        }
    }
}

