package pe.kucho.app.ui.tecnico

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import pe.kucho.app.data.model.UiState
import pe.kucho.app.ui.shared.GraficoBilletera
import pe.kucho.app.ui.tecnico.viewmodel.BilleteraViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BilleteraScreen(
    vm: BilleteraViewModel = viewModel(),
    onBack: (() -> Unit)? = null
) {
    val context = LocalContext.current
    var montoRecarga by remember { mutableStateOf("") }

    val billeteraState by vm.billeteraState.collectAsState()
    val recargarState by vm.recargarState.collectAsState()

    // Cargar billetera al entrar
    LaunchedEffect(Unit) { vm.cargarBilletera(context) }

    // Reaccionar a recarga exitosa
    LaunchedEffect(recargarState) {
        if (recargarState is UiState.Exito) {
            montoRecarga = ""
            vm.resetRecargarState()
            vm.cargarBilletera(context)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Mi Billetera Pro", fontWeight = FontWeight.Bold, color = Color(0xFF0F172A)) },
                navigationIcon = {
                    if (onBack != null) {
                        IconButton(onClick = onBack) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver", tint = Color(0xFF0F172A))
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White)
            )
        },
        containerColor = Color(0xFFF1F5F9)
    ) { padding ->
        when (val state = billeteraState) {
            is UiState.Cargando -> {
                Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = Color(0xFF6366F1))
                }
            }
            is UiState.SinConexion -> {
                Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        Text("📵", fontSize = 40.sp)
                        Text("Sin conexión a internet", fontWeight = FontWeight.Bold, color = Color(0xFF0F172A))
                        Text("Verifica tu red e intenta de nuevo.", fontSize = 13.sp, color = Color(0xFF64748B))
                        Button(onClick = { vm.cargarBilletera(context) }) { Text("Reintentar") }
                    }
                }
            }
            is UiState.Error -> {
                Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("Error: ${state.mensaje}", color = Color(0xFFDC2626))
                        Button(onClick = { vm.cargarBilletera(context) }, modifier = Modifier.padding(top = 8.dp)) { Text("Reintentar") }
                    }
                }
            }
            is UiState.Exito -> {
                val billetera = state.datos
                val transacciones = billetera.transacciones ?: emptyList()
                val recargarCargando = recargarState is UiState.Cargando

                LazyColumn(
                    modifier = Modifier.fillMaxSize().padding(padding),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // Tarjeta de saldo disponible
                    item {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(
                                    Brush.linearGradient(listOf(Color(0xFF6366F1), Color(0xFF8B5CF6))),
                                    RoundedCornerShape(24.dp)
                                )
                                .padding(24.dp)
                        ) {
                            Column {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text("Saldo disponible", color = Color.White.copy(alpha = 0.85f), fontSize = 14.sp)
                                    Surface(
                                        shape = RoundedCornerShape(12.dp),
                                        color = Color.White.copy(alpha = 0.2f)
                                    ) {
                                        Text(
                                            "Comisión fija 12%",
                                            color = Color.White,
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.SemiBold,
                                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                        )
                                    }
                                }
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    "S/ ${String.format("%.2f", billetera.saldoActual)}",
                                    color = Color.White, fontSize = 36.sp, fontWeight = FontWeight.ExtraBold
                                )
                            }
                        }
                    }

                    // Propuesta 4: Analítica Gráfica, Semáforo y Chips de Recarga Rápida
                    item {
                        GraficoBilletera(
                            transacciones = transacciones,
                            saldoActual = billetera.saldoActual,
                            onSeleccionarMontoRecarga = { monto ->
                                vm.recargarSaldo(context, monto)
                            }
                        )
                    }

                    // Recarga personalizada de saldo
                    item {
                        Card(
                            shape = RoundedCornerShape(20.dp),
                            colors = CardDefaults.cardColors(containerColor = Color.White),
                            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                                Text("Recarga Manual de Saldo", fontSize = 15.sp, fontWeight = FontWeight.Bold, color = Color(0xFF0F172A))

                                OutlinedTextField(
                                    value = montoRecarga,
                                    onValueChange = { montoRecarga = it },
                                    label = { Text("Otro monto en Soles (S/)") },
                                    placeholder = { Text("Ej. 35.00") },
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                                    singleLine = true,
                                    shape = RoundedCornerShape(12.dp),
                                    modifier = Modifier.fillMaxWidth()
                                )

                                // Mensajes de error
                                if (recargarState is UiState.Error) {
                                    Text((recargarState as UiState.Error).mensaje, color = Color(0xFFDC2626), fontSize = 12.sp)
                                }
                                if (recargarState is UiState.SinConexion) {
                                    Text("Sin conexión a internet.", color = Color(0xFFDC2626), fontSize = 12.sp)
                                }

                                Button(
                                    onClick = {
                                        val monto = montoRecarga.toDoubleOrNull()
                                        if (monto != null && monto > 0) vm.recargarSaldo(context, monto)
                                    },
                                    enabled = !recargarCargando && montoRecarga.isNotBlank(),
                                    shape = RoundedCornerShape(12.dp),
                                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF6366F1)),
                                    modifier = Modifier.fillMaxWidth().height(48.dp)
                                ) {
                                    if (recargarCargando) CircularProgressIndicator(color = Color.White, modifier = Modifier.size(20.dp))
                                    else Text("Recargar Saldo", fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }

                    // Historial de Transacciones
                    item {
                        Text("Historial de Transacciones", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Color(0xFF0F172A))
                    }

                    if (transacciones.isEmpty()) {
                        item {
                            Surface(shape = RoundedCornerShape(16.dp), color = Color.White, modifier = Modifier.fillMaxWidth()) {
                                Text(
                                    "Aún no tienes movimientos en tu billetera.",
                                    color = Color(0xFF64748B), fontSize = 13.sp,
                                    textAlign = TextAlign.Center, modifier = Modifier.padding(24.dp)
                                )
                            }
                        }
                    } else {
                        items(transacciones) { tx ->
                            val esRecarga = tx.tipo.equals("RECARGA", ignoreCase = true)
                            Card(
                                shape = RoundedCornerShape(16.dp),
                                colors = CardDefaults.cardColors(containerColor = Color.White),
                                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth().padding(16.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                                        Box(
                                            modifier = Modifier.size(42.dp).background(
                                                if (esRecarga) Color(0xFFD1FAE5) else Color(0xFFFFE4E6), CircleShape
                                            ),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Text(
                                                if (esRecarga) "↑" else "↓",
                                                fontSize = 18.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = if (esRecarga) Color(0xFF059669) else Color(0xFFE11D48)
                                            )
                                        }
                                        Spacer(modifier = Modifier.width(12.dp))
                                        Column {
                                            Text(
                                                if (esRecarga) "Recarga de Saldo" else "Comisión Plataforma (12%)",
                                                fontWeight = FontWeight.Bold, fontSize = 14.sp, color = Color(0xFF0F172A)
                                            )
                                            Text(tx.fecha.take(16).replace("T", " "), fontSize = 11.sp, color = Color(0xFF64748B))
                                            if (!tx.descripcion.isNullOrBlank()) {
                                                Text(tx.descripcion, fontSize = 11.sp, color = Color(0xFF94A3B8))
                                            }
                                        }
                                    }
                                    Text(
                                        "${if (esRecarga) "+" else "-"} S/ ${String.format("%.2f", tx.monto)}",
                                        fontWeight = FontWeight.ExtraBold, fontSize = 15.sp,
                                        color = if (esRecarga) Color(0xFF059669) else Color(0xFFE11D48)
                                    )
                                }
                            }
                        }
                    }
                }
            }
            else -> {}
        }
    }
}
