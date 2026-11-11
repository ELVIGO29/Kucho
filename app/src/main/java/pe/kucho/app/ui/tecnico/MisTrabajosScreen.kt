package pe.kucho.app.ui.tecnico

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.viewmodel.compose.viewModel
import pe.kucho.app.data.model.UiState
import pe.kucho.app.ui.cliente.obtenerColoresEstado
import pe.kucho.app.ui.shared.InAppNotificationData
import pe.kucho.app.ui.shared.NotificacionInAppOverlay
import pe.kucho.app.ui.shared.SeguimientoServicioCard
import pe.kucho.app.ui.shared.TipoNotificacionInApp
import pe.kucho.app.ui.shared.rememberInAppNotificationState
import pe.kucho.app.ui.tecnico.viewmodel.MisTrabajosViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MisTrabajosScreen(
    vm: MisTrabajosViewModel = viewModel(),
    onBack: (() -> Unit)? = null
) {
    val context = LocalContext.current
    val notificacionState = rememberInAppNotificationState()
    val trabajosState by vm.trabajosState.collectAsState()
    val accionState by vm.accionState.collectAsState()

    // Carga inicial al entrar por primera vez
    LaunchedEffect(Unit) { vm.cargarTrabajos(context) }

    // ON_RESUME: solo recarga si la pantalla vuelve de segundo plano (ej: volver de Flutter)
    // Usamos un flag para evitar la doble carga en el primer LaunchedEffect
    val lifecycleOwner = LocalLifecycleOwner.current
    var primeraVez by remember { mutableStateOf(true) }
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                if (primeraVez) {
                    primeraVez = false   // Ignorar la primera vez (ya cargó LaunchedEffect)
                } else {
                    vm.cargarTrabajos(context)   // Solo recarga al volver de otra app
                }
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

    // Reaccionar a acciones completadas
    LaunchedEffect(accionState) {
        when (val s = accionState) {
            is UiState.Exito -> {
                notificacionState.mostrar(
                    InAppNotificationData(
                        tipo = TipoNotificacionInApp.SERVICIO_FINALIZADO,
                        titulo = "¡Estado Actualizado!",
                        mensaje = "El avance del trabajo ha sido sincronizado exitosamente con el cliente.",
                        textoAccion = "Completado"
                    )
                )
                Toast.makeText(context, "Estado actualizado correctamente", Toast.LENGTH_SHORT).show()
                vm.resetAccionState()
                vm.cargarTrabajos(context)
            }
            is UiState.Error -> {
                Toast.makeText(context, s.mensaje, Toast.LENGTH_SHORT).show()
                vm.resetAccionState()
            }
            is UiState.SinConexion -> {
                Toast.makeText(context, "Sin conexión a internet.", Toast.LENGTH_SHORT).show()
                vm.resetAccionState()
            }
            else -> {}
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Mis Trabajos y Servicios", fontWeight = FontWeight.Bold, color = Color(0xFF0F172A)) },
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
        when (val state = trabajosState) {
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
                        Button(onClick = { vm.cargarTrabajos(context) }) { Text("Reintentar") }
                    }
                }
            }
            is UiState.Error -> {
                Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(state.mensaje, color = Color(0xFFDC2626), textAlign = TextAlign.Center)
                        Button(onClick = { vm.cargarTrabajos(context) }, modifier = Modifier.padding(top = 8.dp)) { Text("Reintentar") }
                    }
                }
            }
            is UiState.Exito -> {
                val solicitudes = state.datos
                val accionCargando = accionState is UiState.Cargando

                if (solicitudes.isEmpty()) {
                    Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("🔨", fontSize = 40.sp)
                            Spacer(modifier = Modifier.height(8.dp))
                            Text("Sin trabajos asignados aún", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = Color(0xFF0F172A))
                            Text("Postula a solicitudes abiertas en la Bolsa de Trabajos.", fontSize = 12.sp, color = Color(0xFF64748B))
                        }
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize().padding(padding),
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        items(solicitudes) { sol ->
                            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                                // 1. Tarjeta con detalle del trabajo (categoría, descripción, ubicación)
                                Card(
                                    shape = RoundedCornerShape(16.dp),
                                    colors = CardDefaults.cardColors(containerColor = Color.White),
                                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Column(modifier = Modifier.padding(16.dp)) {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Text(
                                                text = sol.nombreCategoria,
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 15.sp,
                                                color = Color(0xFF0F172A)
                                            )
                                            if (sol.precioPactado != null) {
                                                Surface(
                                                    shape = RoundedCornerShape(8.dp),
                                                    color = Color(0xFFDCFCE7)
                                                ) {
                                                    Text(
                                                        text = "S/ ${String.format("%.2f", sol.precioPactado)}",
                                                        fontWeight = FontWeight.Bold,
                                                        fontSize = 12.sp,
                                                        color = Color(0xFF166534),
                                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                                    )
                                                }
                                            }
                                        }

                                        Spacer(modifier = Modifier.height(6.dp))
                                        Text(
                                            text = sol.descripcion,
                                            fontSize = 13.sp,
                                            color = Color(0xFF475569),
                                            lineHeight = 18.sp
                                        )

                                        Spacer(modifier = Modifier.height(8.dp))
                                        Row(
                                            horizontalArrangement = Arrangement.spacedBy(10.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Text(
                                                text = "📍 ${sol.distrito}",
                                                fontSize = 11.sp,
                                                color = Color(0xFF64748B),
                                                fontWeight = FontWeight.Medium
                                            )
                                            if (!sol.referenciaDireccion.isNullOrBlank()) {
                                                Text(
                                                    text = "• ${sol.referenciaDireccion}",
                                                    fontSize = 11.sp,
                                                    color = Color(0xFF64748B)
                                                )
                                            }
                                        }
                                    }
                                }

                                // 2. Tarjeta de Seguimiento en Vivo con GPS para el técnico
                                SeguimientoServicioCard(
                                    solicitud = sol,
                                    esTecnico = true
                                )

                                // 3. Botones de acción de avance de estado
                                if (sol.estado.equals("EN_PROGRESO", ignoreCase = true)) {
                                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                        // Botón Principal: Lanzar Flutter + Firebase Acta Digital
                                        // El token se obtiene en IO thread dentro del ViewModel (sin bloquear UI)
                                        Button(
                                            onClick = {
                                                vm.lanzarActaDigital(
                                                    context = context,
                                                    idSolicitud = sol.idSolicitud,
                                                    nombreCliente = sol.nombreCliente ?: "Cliente",
                                                    categoria = sol.nombreCategoria,
                                                    distrito = sol.distrito ?: "Trujillo",
                                                    monto = sol.precioPactado ?: 0.0,
                                                    onFallback = {
                                                        vm.actualizarEstado(context, sol.idSolicitud, "FINALIZADO")
                                                    }
                                                )
                                            },
                                            enabled = !accionCargando,
                                            shape = RoundedCornerShape(12.dp),
                                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4F46E5)),
                                            modifier = Modifier.fillMaxWidth().height(48.dp)
                                        ) {
                                            Row(
                                                verticalAlignment = Alignment.CenterVertically,
                                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                                            ) {
                                                Text("✍️", fontSize = 16.sp)
                                                Text("Emitir Acta y Firma Digital", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = Color.White)
                                            }
                                        }

                                        // Opción alternativa rápida directa sin firma
                                        OutlinedButton(
                                            onClick = { vm.actualizarEstado(context, sol.idSolicitud, "FINALIZADO") },
                                            enabled = !accionCargando,
                                            shape = RoundedCornerShape(12.dp),
                                            colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFF10B981)),
                                            modifier = Modifier.fillMaxWidth().height(42.dp)
                                        ) {
                                            if (accionCargando) {
                                                CircularProgressIndicator(color = Color(0xFF10B981), modifier = Modifier.size(16.dp))
                                            } else {
                                                Text("✅ Finalizar directamente sin firma", fontWeight = FontWeight.Medium, fontSize = 12.sp)
                                            }
                                        }
                                    }
                                } else if (sol.estado.equals("ASIGNADO", ignoreCase = true) || sol.estado.equals("ACEPTADO", ignoreCase = true)) {
                                    Button(
                                        onClick = { vm.actualizarEstado(context, sol.idSolicitud, "EN_PROGRESO") },
                                        enabled = !accionCargando,
                                        shape = RoundedCornerShape(12.dp),
                                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF6366F1)),
                                        modifier = Modifier.fillMaxWidth().height(46.dp)
                                    ) {
                                        if (accionCargando) CircularProgressIndicator(color = Color.White, modifier = Modifier.size(18.dp))
                                        else Text("▶ Iniciar Trabajo (En Camino / En Curso)", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                    }
                                } else if (sol.estado.equals("FINALIZADO", ignoreCase = true)) {
                                    Surface(
                                        shape = RoundedCornerShape(12.dp),
                                        color = Color(0xFFF0FDF4),
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Row(
                                            modifier = Modifier.padding(12.dp),
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                                        ) {
                                            Text("✨", fontSize = 16.sp)
                                            Text(
                                                text = "Servicio concluido exitosamente con Acta Digital",
                                                fontSize = 12.sp,
                                                color = Color(0xFF166534),
                                                fontWeight = FontWeight.SemiBold
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
            else -> {}
        }
    }

    NotificacionInAppOverlay(state = notificacionState)
    }
}
