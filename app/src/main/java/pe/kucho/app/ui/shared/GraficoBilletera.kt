package pe.kucho.app.ui.shared

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import pe.kucho.app.data.model.TransaccionBilleteraResponse

@Composable
fun GraficoBilletera(
    transacciones: List<TransaccionBilleteraResponse>,
    saldoActual: Double,
    onSeleccionarMontoRecarga: (Double) -> Unit,
    modifier: Modifier = Modifier
) {
    // Cálculo de agregados
    val totalRecargas = transacciones
        .filter { it.tipo.equals("RECARGA", ignoreCase = true) }
        .sumOf { it.monto }

    val totalComisiones = transacciones
        .filter { it.tipo.contains("COMISION", ignoreCase = true) || it.tipo.contains("DESCUENTO", ignoreCase = true) }
        .sumOf { it.monto }

    // En el modelo de negocio Kucho, la comisión es del 12% del precio pactado
    // Por tanto, el volumen de trabajo generado = comisiones / 0.12
    val volumenServiciosEstimado = if (totalComisiones > 0) totalComisiones / 0.12 else 0.0
    val gananciaNetaEstimada = (volumenServiciosEstimado - totalComisiones).coerceAtLeast(0.0)

    val maxValor = maxOf(totalRecargas, totalComisiones, gananciaNetaEstimada, 50.0)

    var animacionIniciada by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) { animacionIniciada = true }

    val factorAnimacion by animateFloatAsState(
        targetValue = if (animacionIniciada) 1f else 0f,
        animationSpec = tween(durationMillis = 800),
        label = "animacionBarras"
    )

    Column(modifier = modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(16.dp)) {

        // 1. Semáforo de Salud de Saldo
        val (colorSemaforo, textoEstado, descripcionEstado) = when {
            saldoActual >= 15.0 -> Triple(
                Color(0xFF10B981),
                "Saldo Saludable",
                "Cuentas con saldo suficiente para cubrir la comisión del 12% en nuevos servicios."
            )
            saldoActual >= 5.0 -> Triple(
                Color(0xFFF59E0B),
                "Saldo Moderado",
                "Podrías requerir recarga previa si aceptas solicitudes de presupuesto mayor a S/ 50."
            )
            else -> Triple(
                Color(0xFFEF4444),
                "Saldo Crítico",
                "Saldo insuficiente. El sistema requiere saldo para debitar el 12% al asignarte un trabajo."
            )
        }

        Surface(
            shape = RoundedCornerShape(16.dp),
            color = Color.White,
            shadowElevation = 2.dp,
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier.padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .background(colorSemaforo.copy(alpha = 0.15f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Box(
                        modifier = Modifier
                            .size(16.dp)
                            .background(colorSemaforo, CircleShape)
                    )
                }
                Spacer(modifier = Modifier.width(14.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(textoEstado, fontWeight = FontWeight.Bold, fontSize = 14.sp, color = Color(0xFF0F172A))
                    Text(descripcionEstado, fontSize = 11.sp, color = Color(0xFF64748B), lineHeight = 15.sp)
                }
            }
        }

        // 2. Gráfico Interactivo de Rendimiento en Canvas
        Card(
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(18.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text("Analítica de Billetera", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = Color(0xFF0F172A))
                        Text("Balance acumulado y comisiones (12%)", fontSize = 11.sp, color = Color(0xFF64748B))
                    }
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = Color(0xFFEEF2FF)
                    ) {
                        Text(
                            "Comisión 12%",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF4338CA),
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                if (transacciones.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 20.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("📊", fontSize = 32.sp)
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                "Aún no registras movimientos",
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp,
                                color = Color(0xFF0F172A)
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                "Recarga saldo para postular a trabajos. Aquí verás tus ganancias y comisiones del 12%.",
                                fontSize = 11.sp,
                                color = Color(0xFF64748B),
                                textAlign = TextAlign.Center,
                                modifier = Modifier.padding(horizontal = 16.dp)
                            )
                        }
                    }
                } else {
                    // Canvas con barras comparativas
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(140.dp),
                        contentAlignment = Alignment.BottomCenter
                    ) {
                        Canvas(modifier = Modifier.fillMaxSize()) {
                            val canvasWidth = size.width
                            val canvasHeight = size.height
                            val barWidth = (canvasWidth / 5.2f).coerceAtMost(48.dp.toPx())
                            val spacing = (canvasWidth - (barWidth * 3)) / 4

                            // Valores normalizados
                            val alturaRecargas = ((totalRecargas / maxValor).toFloat() * canvasHeight * factorAnimacion).coerceAtLeast(8f)
                            val alturaComisiones = ((totalComisiones / maxValor).toFloat() * canvasHeight * factorAnimacion).coerceAtLeast(8f)
                            val alturaGanancia = ((gananciaNetaEstimada / maxValor).toFloat() * canvasHeight * factorAnimacion).coerceAtLeast(8f)

                            // 1. Barra Recargas (Azul)
                            val x1 = spacing
                            drawRoundRect(
                                brush = Brush.verticalGradient(listOf(Color(0xFF6366F1), Color(0xFF818CF8))),
                                topLeft = Offset(x1, canvasHeight - alturaRecargas),
                                size = Size(barWidth, alturaRecargas),
                                cornerRadius = CornerRadius(12f, 12f)
                            )

                            // 2. Barra Comisiones 12% (Rojo suave)
                            val x2 = spacing * 2 + barWidth
                            drawRoundRect(
                                brush = Brush.verticalGradient(listOf(Color(0xFFF43F5E), Color(0xFFFB7185))),
                                topLeft = Offset(x2, canvasHeight - alturaComisiones),
                                size = Size(barWidth, alturaComisiones),
                                cornerRadius = CornerRadius(12f, 12f)
                            )

                            // 3. Barra Ganancia Neta (Verde)
                            val x3 = spacing * 3 + (barWidth * 2)
                            drawRoundRect(
                                brush = Brush.verticalGradient(listOf(Color(0xFF10B981), Color(0xFF34D399))),
                                topLeft = Offset(x3, canvasHeight - alturaGanancia),
                                size = Size(barWidth, alturaGanancia),
                                cornerRadius = CornerRadius(12f, 12f)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Etiquetas bajo las barras
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceAround
                    ) {
                        ColumnaLeyenda("Recargas", "S/ ${String.format("%.2f", totalRecargas)}", Color(0xFF6366F1))
                        ColumnaLeyenda("Comisión 12%", "S/ ${String.format("%.2f", totalComisiones)}", Color(0xFFF43F5E))
                        ColumnaLeyenda("Ganancia Neta", "S/ ${String.format("%.2f", gananciaNetaEstimada)}", Color(0xFF10B981))
                    }
                }
            }
        }

        // 3. Chips de Recarga Rápida
        Card(
            shape = RoundedCornerShape(18.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    "Recarga Rápida de Saldo Ficticio",
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    color = Color(0xFF0F172A)
                )
                Text(
                    "Selecciona un monto predefinido para recargar inmediatamente:",
                    fontSize = 11.sp,
                    color = Color(0xFF64748B)
                )

                Spacer(modifier = Modifier.height(12.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    listOf(10.0, 20.0, 50.0, 100.0).forEach { monto ->
                        OutlinedButton(
                            onClick = { onSeleccionarMontoRecarga(monto) },
                            shape = RoundedCornerShape(10.dp),
                            colors = ButtonDefaults.outlinedButtonColors(
                                containerColor = Color(0xFFF8FAFC),
                                contentColor = Color(0xFF4F46E5)
                            ),
                            contentPadding = PaddingValues(horizontal = 4.dp, vertical = 6.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("+ S/ ${monto.toInt()}", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ColumnaLeyenda(titulo: String, valor: String, color: Color) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(modifier = Modifier.size(8.dp).background(color, CircleShape))
            Spacer(modifier = Modifier.width(4.dp))
            Text(titulo, fontSize = 10.sp, color = Color(0xFF64748B), fontWeight = FontWeight.Medium)
        }
        Spacer(modifier = Modifier.height(2.dp))
        Text(valor, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color(0xFF0F172A))
    }
}

