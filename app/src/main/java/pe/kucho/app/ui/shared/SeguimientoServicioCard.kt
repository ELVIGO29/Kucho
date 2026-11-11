package pe.kucho.app.ui.shared

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import pe.kucho.app.data.model.SolicitudResponse

@Composable
fun SeguimientoServicioCard(
    solicitud: SolicitudResponse,
    esTecnico: Boolean = false,
    onCalificar: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val estadoUpper = solicitud.estado.uppercase()

    val pasoActual = when (estadoUpper) {
        "ASIGNADO" -> 1
        "EN_PROGRESO" -> 2
        "FINALIZADO" -> 3
        else -> 0
    }

    // Solo se anima cuando el servicio está activo (no Finalizado)
    val debePulsar = pasoActual in 1..2
    val infiniteTransition = rememberInfiniteTransition(label = "pulso")
    val scalePulse by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = if (debePulsar) 1.15f else 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(900, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulsoAnim"
    )

    Card(
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp),
        modifier = modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(18.dp)) {
            // Cabecera de estado
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(10.dp)
                            .scale(if (pasoActual in 1..2) scalePulse else 1f)
                            .background(
                                color = when (estadoUpper) {
                                    "FINALIZADO" -> Color(0xFF10B981)
                                    "EN_PROGRESO" -> Color(0xFF3B82F6)
                                    else -> Color(0xFFF59E0B)
                                },
                                shape = CircleShape
                            )
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = when (estadoUpper) {
                            "ASIGNADO" -> "Técnico Asignado"
                            "EN_PROGRESO" -> "Servicio en Curso"
                            "FINALIZADO" -> "Servicio Concluido"
                            else -> estadoUpper
                        },
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        color = Color(0xFF0F172A)
                    )
                }

                if (solicitud.precioPactado != null) {
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = Color(0xFFEEF2FF)
                    ) {
                        Text(
                            text = "Pactado: S/ ${String.format("%.2f", solicitud.precioPactado)}",
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp,
                            color = Color(0xFF4338CA),
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Stepper visual de 3 etapas de avance
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.Top
            ) {
                PasoItem(
                    numero = 1,
                    titulo = "Asignado",
                    activo = pasoActual >= 1,
                    enCurso = pasoActual == 1,
                    modifier = Modifier.weight(1f)
                )
                LineaConectora(activa = pasoActual >= 2, modifier = Modifier.weight(0.7f))
                PasoItem(
                    numero = 2,
                    titulo = "En Curso",
                    activo = pasoActual >= 2,
                    enCurso = pasoActual == 2,
                    modifier = Modifier.weight(1f)
                )
                LineaConectora(activa = pasoActual >= 3, modifier = Modifier.weight(0.7f))
                PasoItem(
                    numero = 3,
                    titulo = "Finalizado",
                    activo = pasoActual >= 3,
                    enCurso = pasoActual == 3,
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Datos del Técnico o Cliente
            val nombreContacto = if (esTecnico) solicitud.nombreCliente else solicitud.nombreTecnico
            val telefonoContacto = if (esTecnico) solicitud.telefonoCliente else solicitud.telefonoTecnico

            if (!nombreContacto.isNullOrBlank()) {
                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = Color(0xFFF8FAFC),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                            Box(
                                modifier = Modifier
                                    .size(38.dp)
                                    .background(
                                        Brush.linearGradient(listOf(Color(0xFF6366F1), Color(0xFF8B5CF6))),
                                        CircleShape
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = nombreContacto.take(1).uppercase(),
                                    color = Color.White,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 16.sp
                                )
                            }
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text(
                                    text = if (esTecnico) "Cliente: $nombreContacto" else "Técnico: $nombreContacto",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF1E293B)
                                )
                                Text(
                                    text = telefonoContacto ?: "Sin teléfono",
                                    fontSize = 11.sp,
                                    color = Color(0xFF64748B)
                                )
                            }
                        }

                        // Botones de acción directa
                        if (!telefonoContacto.isNullOrBlank()) {
                            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                IconButton(
                                    onClick = {
                                        val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:$telefonoContacto"))
                                        context.startActivity(intent)
                                    },
                                    modifier = Modifier
                                        .size(34.dp)
                                        .background(Color(0xFFE0E7FF), CircleShape)
                                ) {
                                    Icon(
                                        Icons.Default.Call,
                                        contentDescription = "Llamar",
                                        tint = Color(0xFF4338CA),
                                        modifier = Modifier.size(16.dp)
                                    )
                                }

                                IconButton(
                                    onClick = {
                                        val clean = telefonoContacto.replace("[^0-9]".toRegex(), "")
                                        val whatsappPhone = if (clean.length == 9) "51$clean" else clean
                                        val url = "https://api.whatsapp.com/send?phone=$whatsappPhone"
                                        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
                                        context.startActivity(intent)
                                    },
                                    modifier = Modifier
                                        .size(34.dp)
                                        .background(Color(0xFFDCFCE7), CircleShape)
                                ) {
                                    Text(
                                        "💬",
                                        fontSize = 14.sp
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // Geolocalización directa (Google Maps)
            val tieneCoordenadas = solicitud.latitud != null && solicitud.longitud != null
            if (tieneCoordenadas) {
                Spacer(modifier = Modifier.height(10.dp))
                OutlinedButton(
                    onClick = {
                        try {
                            val uri = Uri.parse("geo:${solicitud.latitud},${solicitud.longitud}?q=${solicitud.latitud},${solicitud.longitud}(${Uri.encode(solicitud.distrito)})")
                            val mapIntent = Intent(Intent.ACTION_VIEW, uri).apply {
                                setPackage("com.google.android.apps.maps")
                            }
                            context.startActivity(mapIntent)
                        } catch (e: Exception) {
                            val webUri = Uri.parse("https://www.google.com/maps/search/?api=1&query=${solicitud.latitud},${solicitud.longitud}")
                            context.startActivity(Intent(Intent.ACTION_VIEW, webUri))
                        }
                    },
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFF4F46E5)),
                    modifier = Modifier.fillMaxWidth().height(38.dp)
                ) {
                    Icon(Icons.Default.LocationOn, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Abrir ubicación en Google Maps", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                }
            }

            // Botón de calificar si ya finalizó y no tiene reseña
            if (!esTecnico && estadoUpper == "FINALIZADO" && solicitud.puntuacionResena == null && onCalificar != null) {
                Spacer(modifier = Modifier.height(12.dp))
                Button(
                    onClick = onCalificar,
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFF59E0B)),
                    modifier = Modifier.fillMaxWidth().height(42.dp)
                ) {
                    Icon(Icons.Default.Star, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Calificar Atención del Técnico", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                }
            }
        }
    }
}

@Composable
private fun PasoItem(
    numero: Int,
    titulo: String,
    activo: Boolean,
    enCurso: Boolean,
    modifier: Modifier = Modifier
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = modifier) {
        Box(
            modifier = Modifier
                .size(28.dp)
                .background(
                    color = when {
                        activo -> Color(0xFF6366F1)
                        else -> Color(0xFFE2E8F0)
                    },
                    shape = CircleShape
                ),
            contentAlignment = Alignment.Center
        ) {
            if (activo && !enCurso) {
                Icon(
                    Icons.Default.Check,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(14.dp)
                )
            } else {
                Text(
                    text = "$numero",
                    color = if (activo) Color.White else Color(0xFF64748B),
                    fontWeight = FontWeight.Bold,
                    fontSize = 12.sp
                )
            }
        }
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = titulo,
            fontSize = 10.sp,
            fontWeight = if (enCurso) FontWeight.Bold else FontWeight.Medium,
            color = if (activo) Color(0xFF0F172A) else Color(0xFF94A3B8),
            textAlign = TextAlign.Center
        )
    }
}

@Composable
private fun LineaConectora(activa: Boolean, modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .padding(top = 13.dp)
            .height(3.dp)
            .clip(RoundedCornerShape(1.5.dp))
            .background(if (activa) Color(0xFF6366F1) else Color(0xFFE2E8F0))
    )
}

