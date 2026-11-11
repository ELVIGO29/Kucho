package pe.kucho.app.ui.cliente

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
import kotlinx.coroutines.launch
import pe.kucho.app.data.model.NegociacionResponse
import pe.kucho.app.data.model.SolicitudResponse
import pe.kucho.app.data.network.NetworkChecker
import pe.kucho.app.data.repository.NegociacionRepository
import pe.kucho.app.data.repository.SolicitudRepository
import pe.kucho.app.ui.shared.InAppNotificationData
import pe.kucho.app.ui.shared.NegociacionDinamicaSheet
import pe.kucho.app.ui.shared.NotificacionInAppOverlay
import pe.kucho.app.ui.shared.SeguimientoServicioCard
import pe.kucho.app.ui.shared.TipoNotificacionInApp
import pe.kucho.app.ui.shared.rememberInAppNotificationState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MisSolicitudesScreen(
    solicitudRepository: SolicitudRepository = remember { SolicitudRepository() },
    negociacionRepository: NegociacionRepository = remember { NegociacionRepository() },
    onBack: () -> Unit,
    onNuevaSolicitud: () -> Unit,
    onCrearResena: (idSolicitud: String, idTecnico: String) -> Unit,
    onVerPerfilTecnico: ((idTecnico: String, nombre: String?) -> Unit)? = null
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val notificacionState = rememberInAppNotificationState()

    var solicitudes by remember { mutableStateOf<List<SolicitudResponse>>(emptyList()) }
    var cargando by remember { mutableStateOf(true) }

    val negociacionesPorSolicitud = remember { mutableStateMapOf<String, List<NegociacionResponse>>() }
    val procesandoNegociacion = remember { mutableStateMapOf<String, Boolean>() }

    fun cargarDatos() {
        cargando = true
        if (!NetworkChecker.isConnected(context)) {
            Toast.makeText(context, "Sin conexión. Mostrando datos en caché.", Toast.LENGTH_SHORT).show()
        }
        scope.launch {
            solicitudRepository.obtenerMisSolicitudesCliente().onSuccess { lista ->
                solicitudes = lista
                cargando = false

                lista.filter { it.estado.equals("ABIERTO", ignoreCase = true) }.forEach { sol ->
                    negociacionRepository.obtenerNegociacionesPorSolicitud(sol.idSolicitud).onSuccess { negs ->
                        negociacionesPorSolicitud[sol.idSolicitud] = negs
                    }
                }
            }.onFailure {
                solicitudes = emptyList()
                cargando = false
            }
        }
    }

    LaunchedEffect(Unit) {
        cargarDatos()
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = {
                        Text(
                            "Mis Solicitudes y Servicios",
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF0F172A)
                        )
                    },
                    navigationIcon = {
                        IconButton(onClick = onBack) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver", tint = Color(0xFF0F172A))
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White)
                )
            }
        ) { padding ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .background(Color(0xFFF8FAFC))
            ) {
            if (cargando) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = Color(0xFF6366F1))
                }
            } else if (solicitudes.isEmpty()) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(32.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Text("📋", fontSize = 56.sp)
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        "No tienes solicitudes",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF0F172A)
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        "Aún no has solicitado ningún servicio técnico. ¡Pide uno ahora!",
                        fontSize = 14.sp,
                        color = Color(0xFF64748B),
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(20.dp))
                    Button(
                        onClick = onNuevaSolicitud,
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF6366F1)),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("+ Nueva Solicitud", fontWeight = FontWeight.Bold)
                    }
                }
            } else {
                LazyColumn(
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // Header Card
                    item {
                        Card(
                            shape = RoundedCornerShape(20.dp),
                            colors = CardDefaults.cardColors(containerColor = Color.White),
                            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(20.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(
                                    modifier = Modifier
                                        .weight(1f)
                                        .padding(end = 12.dp)
                                ) {
                                    Text(
                                        "Seguimiento y Cotizaciones",
                                        fontSize = 18.sp,
                                        fontWeight = FontWeight.ExtraBold,
                                        color = Color(0xFF0F172A)
                                    )
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Text(
                                        "Negocia ofertas y rastrea a tu técnico en tiempo real",
                                        fontSize = 12.sp,
                                        color = Color(0xFF64748B)
                                    )
                                }
                                Button(
                                    onClick = onNuevaSolicitud,
                                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF6366F1)),
                                    shape = RoundedCornerShape(12.dp),
                                    contentPadding = PaddingValues(horizontal = 14.dp, vertical = 8.dp)
                                ) {
                                    Text(
                                        "+ Nueva",
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Bold,
                                        maxLines = 1,
                                        softWrap = false
                                    )
                                }
                            }
                        }
                    }

                    items(solicitudes, key = { it.idSolicitud }) { sol ->
                        val (badgeBg, badgeTextColor) = obtenerColoresEstado(sol.estado)
                        val negociaciones = negociacionesPorSolicitud[sol.idSolicitud] ?: emptyList()
                        val esAsignadoOEnCurso = sol.estado.uppercase() in listOf("ASIGNADO", "EN_PROGRESO", "FINALIZADO")

                        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                            // 1. Tarjeta principal de la Solicitud
                            Card(
                                shape = RoundedCornerShape(20.dp),
                                colors = CardDefaults.cardColors(containerColor = Color.White),
                                elevation = CardDefaults.cardElevation(defaultElevation = 3.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(modifier = Modifier.padding(18.dp)) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = sol.nombreCategoria,
                                            fontSize = 16.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = Color(0xFF0F172A)
                                        )
                                        Surface(
                                            shape = RoundedCornerShape(8.dp),
                                            color = badgeBg
                                        ) {
                                            Text(
                                                text = sol.estado,
                                                fontSize = 11.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = badgeTextColor,
                                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                                            )
                                        }
                                    }

                                    Spacer(modifier = Modifier.height(10.dp))

                                    Text(
                                        text = sol.descripcion,
                                        fontSize = 14.sp,
                                        color = Color(0xFF334155),
                                        lineHeight = 20.sp
                                    )

                                    Spacer(modifier = Modifier.height(12.dp))

                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                                    ) {
                                        Surface(shape = RoundedCornerShape(8.dp), color = Color(0xFFF1F5F9)) {
                                            Text(
                                                text = "📍 ${sol.distrito}",
                                                fontSize = 12.sp,
                                                color = Color(0xFF475569),
                                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                            )
                                        }
                                        Surface(shape = RoundedCornerShape(8.dp), color = Color(0xFFF1F5F9)) {
                                            Text(
                                                text = "📅 ${sol.fechaCreacion.take(10)}",
                                                fontSize = 12.sp,
                                                color = Color(0xFF475569),
                                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                            )
                                        }
                                        if (sol.precioPactado != null && sol.precioPactado > 0) {
                                            Surface(shape = RoundedCornerShape(8.dp), color = Color(0xFFD1FAE5)) {
                                                Text(
                                                    text = "💰 S/ ${String.format("%.2f", sol.precioPactado)}",
                                                    fontSize = 12.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    color = Color(0xFF059669),
                                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                                )
                                            }
                                        }
                                    }

                                    // Si la solicitud está ABIERTA, botón para cancelar si el cliente lo desea
                                    if (sol.estado.equals("ABIERTO", ignoreCase = true)) {
                                        Spacer(modifier = Modifier.height(14.dp))
                                        OutlinedButton(
                                            onClick = {
                                                scope.launch {
                                                    solicitudRepository.cancelarSolicitud(sol.idSolicitud).onSuccess {
                                                        Toast.makeText(context, "Solicitud cancelada", Toast.LENGTH_SHORT).show()
                                                        cargarDatos()
                                                    }.onFailure {
                                                        Toast.makeText(context, "Error al cancelar solicitud", Toast.LENGTH_SHORT).show()
                                                    }
                                                }
                                            },
                                            colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFFE11D48)),
                                            shape = RoundedCornerShape(10.dp),
                                            modifier = Modifier.fillMaxWidth()
                                        ) {
                                            Text("Cancelar Solicitud", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                        }
                                    }
                                }
                            }

                            // 2. Propuesta 2 (B): Live Tracking Card para servicios Asignados o en Progreso
                            if (esAsignadoOEnCurso) {
                                SeguimientoServicioCard(
                                    solicitud = sol,
                                    esTecnico = false,
                                    onCalificar = {
                                        if (!sol.idTecnico.isNullOrBlank()) {
                                            onCrearResena(sol.idSolicitud, sol.idTecnico)
                                        }
                                    }
                                )
                            }

                            // 3. Propuesta 2 (A): Centro de Negociación y Contraofertas si la solicitud está ABIERTA
                            if (sol.estado.equals("ABIERTO", ignoreCase = true)) {
                                Text(
                                    text = "🤝 Ofertas Recibidas (${negociaciones.size})",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 15.sp,
                                    color = Color(0xFF0F172A),
                                    modifier = Modifier.padding(start = 4.dp, top = 4.dp)
                                )

                                if (negociaciones.isEmpty()) {
                                    Surface(
                                        shape = RoundedCornerShape(14.dp),
                                        color = Color.White,
                                        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE2E8F0)),
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Row(
                                            modifier = Modifier.padding(16.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Text("⏳", fontSize = 24.sp)
                                            Spacer(modifier = Modifier.width(12.dp))
                                            Column {
                                                Text(
                                                    "Buscando técnicos en Trujillo...",
                                                    fontWeight = FontWeight.Bold,
                                                    fontSize = 13.sp,
                                                    color = Color(0xFF0F172A)
                                                )
                                                Text(
                                                    "Tu solicitud es visible en la bolsa de trabajo. Te notificaremos al recibir cotizaciones.",
                                                    fontSize = 11.sp,
                                                    color = Color(0xFF64748B)
                                                )
                                            }
                                        }
                                    }
                                } else {
                                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                                        negociaciones.forEach { neg ->
                                            val estaProcesando = procesandoNegociacion[neg.idNegociacion] == true

                                            NegociacionDinamicaSheet(
                                                negociacion = neg,
                                                esCliente = true,
                                                precioInicial = sol.precioPactado,
                                                procesando = estaProcesando,
                                                onVerPerfilTecnico = {
                                                    onVerPerfilTecnico?.invoke(neg.idTecnico, neg.nombreTecnico)
                                                },
                                                onAceptar = {
                                                    procesandoNegociacion[neg.idNegociacion] = true
                                                    scope.launch {
                                                        negociacionRepository.aceptarNegociacion(neg.idNegociacion).onSuccess {
                                                            notificacionState.mostrar(
                                                                InAppNotificationData(
                                                                    tipo = TipoNotificacionInApp.TECNICO_EN_CAMINO,
                                                                    titulo = "¡Técnico Asignado!",
                                                                    mensaje = "Servicio coordinado con ${neg.nombreTecnico ?: "el técnico"}.",
                                                                    textoAccion = "Ver estado"
                                                                )
                                                            )
                                                            Toast.makeText(context, "¡Técnico asignado exitosamente!", Toast.LENGTH_SHORT).show()
                                                            cargarDatos()
                                                        }.onFailure {
                                                            Toast.makeText(context, it.localizedMessage ?: "Error al aceptar oferta", Toast.LENGTH_LONG).show()
                                                        }
                                                        procesandoNegociacion[neg.idNegociacion] = false
                                                    }
                                                },
                                                onContraofertar = { nuevoPrecio ->
                                                    procesandoNegociacion[neg.idNegociacion] = true
                                                    scope.launch {
                                                        negociacionRepository.contraofertarComoCliente(neg.idNegociacion, nuevoPrecio).onSuccess {
                                                            notificacionState.mostrar(
                                                                InAppNotificationData(
                                                                    tipo = TipoNotificacionInApp.NUEVA_OFERTA,
                                                                    titulo = "Contraoferta Enviada",
                                                                    mensaje = "Propuesta de S/ ${String.format(java.util.Locale.US, "%.2f", nuevoPrecio)} enviada.",
                                                                    textoAccion = "En espera"
                                                                )
                                                            )
                                                            Toast.makeText(context, "¡Contraoferta enviada al técnico!", Toast.LENGTH_SHORT).show()
                                                            cargarDatos()
                                                        }.onFailure {
                                                            Toast.makeText(context, it.localizedMessage ?: "Error al enviar contraoferta", Toast.LENGTH_SHORT).show()
                                                        }
                                                        procesandoNegociacion[neg.idNegociacion] = false
                                                    }
                                                },
                                                onRechazar = {
                                                    procesandoNegociacion[neg.idNegociacion] = true
                                                    scope.launch {
                                                        negociacionRepository.rechazarNegociacion(neg.idNegociacion).onSuccess {
                                                            Toast.makeText(context, "Oferta rechazada", Toast.LENGTH_SHORT).show()
                                                            cargarDatos()
                                                        }.onFailure {
                                                            Toast.makeText(context, "Error al rechazar oferta", Toast.LENGTH_SHORT).show()
                                                        }
                                                        procesandoNegociacion[neg.idNegociacion] = false
                                                    }
                                                }
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        // Overlay flotante interactivo de notificaciones in-app
        NotificacionInAppOverlay(state = notificacionState)
    }
}
}
