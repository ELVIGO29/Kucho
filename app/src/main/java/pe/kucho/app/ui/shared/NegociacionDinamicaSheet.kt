package pe.kucho.app.ui.shared

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import pe.kucho.app.data.model.NegociacionResponse

@Composable
fun NegociacionDinamicaSheet(
    negociacion: NegociacionResponse,
    esCliente: Boolean = true,
    precioInicial: Double? = null,
    procesando: Boolean = false,
    onAceptar: () -> Unit,
    onContraofertar: (Double) -> Unit,
    onRechazar: () -> Unit,
    onVerPerfilTecnico: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    var mostrarInputContraoferta by remember { mutableStateOf(false) }
    var textoPrecio by remember { mutableStateOf("") }

    val estadoNeg = negociacion.estado.uppercase()
    val esPendiente = estadoNeg == "PENDIENTE"
    val esAceptado = estadoNeg == "ACEPTADO"
    val esRechazado = estadoNeg == "RECHAZADO"

    // Determinar si es mi turno de responder (solo aplica cuando la negociación está PENDIENTE)
    val ultimoOfertante = negociacion.ultimoEnOfertar.uppercase()
    val esMiTurno = esPendiente && ((esCliente && ultimoOfertante == "TECNICO") || (!esCliente && ultimoOfertante == "CLIENTE"))

    Card(
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = modifier
            .fillMaxWidth()
            .border(
                width = if (esMiTurno || esAceptado) 1.5.dp else 0.5.dp,
                color = when {
                    esAceptado -> Color(0xFF10B981)
                    esRechazado -> Color(0xFFE2E8F0)
                    esMiTurno -> Color(0xFF6366F1)
                    else -> Color(0xFFE2E8F0)
                },
                shape = RoundedCornerShape(18.dp)
            )
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Fila superior: Nombre del técnico / cliente y estado
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = if (onVerPerfilTecnico != null) {
                        Modifier.clickable { onVerPerfilTecnico() }
                    } else Modifier
                ) {
                    Box(
                        modifier = Modifier
                            .size(34.dp)
                            .background(
                                Brush.linearGradient(
                                    if (esRechazado) listOf(Color(0xFF94A3B8), Color(0xFF64748B))
                                    else listOf(Color(0xFF6366F1), Color(0xFF8B5CF6))
                                ),
                                CircleShape
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = (negociacion.nombreTecnico ?: "T").take(1).uppercase(),
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = negociacion.nombreTecnico ?: "Técnico Especialista",
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp,
                                color = if (esRechazado) Color(0xFF64748B) else Color(0xFF0F172A)
                            )
                            if (onVerPerfilTecnico != null) {
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "↗",
                                    fontSize = 12.sp,
                                    color = Color(0xFF4F46E5),
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                        Text(
                            text = when {
                                esAceptado -> "✓ Oferta Aceptada"
                                esRechazado -> "✕ Oferta Rechazada"
                                esMiTurno -> "¡Tu turno de responder!"
                                else -> "Esperando respuesta del técnico..."
                            },
                            fontSize = 11.sp,
                            fontWeight = if (esMiTurno || esAceptado) FontWeight.Bold else FontWeight.Normal,
                            color = when {
                                esAceptado -> Color(0xFF059669)
                                esRechazado -> Color(0xFF94A3B8)
                                esMiTurno -> Color(0xFF4338CA)
                                else -> Color(0xFF64748B)
                            }
                        )
                    }
                }

                // Badge de estado / turno
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = when {
                        esAceptado -> Color(0xFFD1FAE5)
                        esRechazado -> Color(0xFFF1F5F9)
                        esMiTurno -> Color(0xFFEEF2FF)
                        else -> Color(0xFFF1F5F9)
                    }
                ) {
                    Text(
                        text = when {
                            esAceptado -> "✓ Aceptada"
                            esRechazado -> "✕ Rechazada"
                            esMiTurno -> "⚡ Acción requerida"
                            else -> "⏳ En espera"
                        },
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = when {
                            esAceptado -> Color(0xFF059669)
                            esRechazado -> Color(0xFF94A3B8)
                            esMiTurno -> Color(0xFF4F46E5)
                            else -> Color(0xFF64748B)
                        },
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Duelo de Precios / Tarjeta de cotización
            Surface(
                shape = RoundedCornerShape(12.dp),
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
                    if (precioInicial != null && precioInicial > 0) {
                        Column {
                            Text("Precio Base Sugerido", fontSize = 11.sp, color = Color(0xFF64748B))
                            Text(
                                "S/ ${String.format("%.2f", precioInicial)}",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = Color(0xFF64748B)
                            )
                        }
                    }

                    Column(horizontalAlignment = Alignment.End) {
                        Text(
                            text = "Propuesta Actual (${if (ultimoOfertante == "TECNICO") "Técnico" else "Cliente"})",
                            fontSize = 11.sp,
                            color = Color(0xFF4338CA),
                            fontWeight = FontWeight.Medium
                        )
                        Text(
                            text = "S/ ${String.format("%.2f", negociacion.precioActual)}",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = Color(0xFF0F172A)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Botones de acción cuando es el turno del usuario
            if (esMiTurno && !mostrarInputContraoferta) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    // Botón Rechazar
                    OutlinedButton(
                        onClick = onRechazar,
                        enabled = !procesando,
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFFE11D48)),
                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 10.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(Icons.Default.Close, contentDescription = null, modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(3.dp))
                        Text("Rech.", fontSize = 11.sp, fontWeight = FontWeight.Bold, maxLines = 1, softWrap = false)
                    }

                    // Botón Contraofertar
                    Button(
                        onClick = { mostrarInputContraoferta = true },
                        enabled = !procesando,
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4F46E5)),
                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 10.dp),
                        modifier = Modifier.weight(1.3f)
                    ) {
                        Text("Contraofertar", fontSize = 11.sp, fontWeight = FontWeight.Bold, maxLines = 1, softWrap = false)
                    }

                    // Botón Aceptar Oferta
                    Button(
                        onClick = onAceptar,
                        enabled = !procesando,
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF10B981)),
                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 10.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        if (procesando) {
                            CircularProgressIndicator(color = Color.White, modifier = Modifier.size(16.dp))
                        } else {
                            Icon(Icons.Default.Check, contentDescription = null, tint = Color.White, modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(3.dp))
                            Text("Aceptar", fontSize = 11.sp, fontWeight = FontWeight.Bold, maxLines = 1, softWrap = false)
                        }
                    }
                }
            }

            // Input expandible para enviar contraoferta
            AnimatedVisibility(
                visible = mostrarInputContraoferta,
                enter = fadeIn(tween(250)),
                exit = fadeOut(tween(200))
            ) {
                Column(modifier = Modifier.fillMaxWidth().padding(top = 8.dp)) {
                    Text(
                        "Ingresa tu contrapropuesta (S/):",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium,
                        color = Color(0xFF1E293B)
                    )
                    Spacer(modifier = Modifier.height(6.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        OutlinedTextField(
                            value = textoPrecio,
                            onValueChange = { textoPrecio = it },
                            placeholder = { Text("0.00") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                            singleLine = true,
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.weight(1f).height(50.dp)
                        )

                        Button(
                            onClick = {
                                val monto = textoPrecio.toDoubleOrNull()
                                if (monto != null && monto > 0) {
                                    onContraofertar(monto)
                                    mostrarInputContraoferta = false
                                    textoPrecio = ""
                                }
                            },
                            enabled = !procesando && (textoPrecio.toDoubleOrNull() ?: 0.0) > 0,
                            shape = RoundedCornerShape(10.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4F46E5)),
                            modifier = Modifier.height(50.dp)
                        ) {
                            Text("Enviar", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                        }

                        TextButton(
                            onClick = { mostrarInputContraoferta = false },
                            modifier = Modifier.height(50.dp)
                        ) {
                            Text("Cancelar", color = Color(0xFF64748B), fontSize = 12.sp)
                        }
                    }

                    // Sugerencias rápidas de contraoferta
                    val actual = negociacion.precioActual
                    val sugerencias = listOf(
                        actual - 10.0,
                        actual - 5.0,
                        actual + 5.0
                    ).filter { it > 0 }

                    if (sugerencias.isNotEmpty()) {
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            sugerencias.forEach { sugerido ->
                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = Color(0xFFF1F5F9),
                                    modifier = Modifier.weight(1f)
                                ) {
                                    TextButton(
                                        onClick = { textoPrecio = String.format(java.util.Locale.US, "%.0f", sugerido) },
                                        contentPadding = PaddingValues(2.dp)
                                    ) {
                                        Text("S/ ${String.format(java.util.Locale.US, "%.0f", sugerido)}", fontSize = 11.sp, color = Color(0xFF334155))
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

