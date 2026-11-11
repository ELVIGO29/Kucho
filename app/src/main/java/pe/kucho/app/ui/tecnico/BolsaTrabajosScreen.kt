package pe.kucho.app.ui.tecnico

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.launch
import pe.kucho.app.data.model.BilleteraResponse
import pe.kucho.app.data.model.NegociacionResponse
import pe.kucho.app.data.model.SolicitudResponse
import pe.kucho.app.data.model.TecnicoResponse
import pe.kucho.app.data.repository.BilleteraRepository
import pe.kucho.app.data.repository.NegociacionRepository
import pe.kucho.app.data.repository.SolicitudRepository
import pe.kucho.app.data.repository.TecnicoRepository

data class DatosConfirmacionOferta(
    val idSolicitud: String,
    val monto: Double,
    val tituloModal: String,
    val subtituloModal: String,
    val tipoOperacion: String, // "OFERTA", "BASE", "CONTRAOFERTA", "ACEPTAR_CLIENTE"
    val onConfirmar: () -> Unit
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BolsaTrabajosScreen(
    solicitudRepository: SolicitudRepository = remember { SolicitudRepository() },
    tecnicoRepository: TecnicoRepository = remember { TecnicoRepository() },
    negociacionRepository: NegociacionRepository = remember { NegociacionRepository() },
    billeteraRepository: BilleteraRepository = remember { BilleteraRepository() },
    onBack: (() -> Unit)? = null,
    onIrBilletera: () -> Unit = {},
    onAceptarExito: () -> Unit = {}
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    // Lista completa descargada del servidor (sin filtrar)
    var todasLasSolicitudes by remember { mutableStateOf<List<SolicitudResponse>>(emptyList()) }
    var billetera by remember { mutableStateOf<BilleteraResponse?>(null) }
    var perfilTecnico by remember { mutableStateOf<TecnicoResponse?>(null) }
    var filtroDistrito by remember { mutableStateOf("") }
    var cargando by remember { mutableStateOf(true) }
    var procesando by remember { mutableStateOf(false) }

    val ofertasInput = remember { mutableStateMapOf<String, String>() }
    val negociaciones = remember { mutableStateMapOf<String, NegociacionResponse?>() }

    // Estado del diálogo de confirmación de descuento de billetera
    var confirmacionDialog by remember { mutableStateOf<DatosConfirmacionOferta?>(null) }

    // Filtrado local — no recarga del servidor al escribir
    val solicitudes = remember(todasLasSolicitudes, filtroDistrito) {
        if (filtroDistrito.isBlank()) todasLasSolicitudes
        else todasLasSolicitudes.filter {
            it.distrito.contains(filtroDistrito.trim(), ignoreCase = true)
        }
    }

    fun cargarDatos() {
        cargando = true
        scope.launch {
            tecnicoRepository.obtenerMiPerfil().onSuccess { p ->
                perfilTecnico = p
            }
            billeteraRepository.obtenerMiBilletera().onSuccess { b ->
                billetera = b
            }
            solicitudRepository.obtenerSolicitudesDisponibles().onSuccess { lista ->
                todasLasSolicitudes = lista
                cargando = false

                lista.forEach { sol ->
                    negociacionRepository.obtenerMiNegociacion(sol.idSolicitud).onSuccess { neg ->
                        negociaciones[sol.idSolicitud] = neg
                    }
                }
            }.onFailure {
                todasLasSolicitudes = emptyList()
                cargando = false
            }
        }
    }

    LaunchedEffect(Unit) {
        cargarDatos()
    }

    val dniVerificado = perfilTecnico?.dniVerificado == true
    val saldoActual = billetera?.saldoActual ?: 0.0

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Bolsa de Trabajos", fontWeight = FontWeight.Bold, color = Color(0xFF0F172A)) },
                navigationIcon = {
                    if (onBack != null) {
                        IconButton(onClick = onBack) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver", tint = Color(0xFF0F172A))
                        }
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
            LazyColumn(
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                // Barra de Búsqueda
                item {
                    Card(
                        shape = RoundedCornerShape(18.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            OutlinedTextField(
                                value = filtroDistrito,
                                onValueChange = { filtroDistrito = it },
                                placeholder = { Text("Filtrar por distrito...") },
                                singleLine = true,
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier.weight(1f)
                            )
                            // "Buscar" recarga del servidor; el filtro local ya es reactivo al escribir
                            Button(
                                onClick = { cargarDatos() },
                                shape = RoundedCornerShape(12.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF6366F1))
                            ) {
                                Text("↺ Actualizar", fontWeight = FontWeight.Bold, maxLines = 1, softWrap = false)
                            }
                        }
                    }
                }

                // Alerta de DNI no verificado
                if (!dniVerificado && !cargando) {
                    item {
                        Surface(
                            shape = RoundedCornerShape(14.dp),
                            color = Color(0xFFFEF2F2),
                            border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFFECACA)),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = "⚠️ Debes verificar tu documento en tu perfil antes de poder ofertar o aceptar trabajos.",
                                color = Color(0xFFDC2626),
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold,
                                modifier = Modifier.padding(14.dp)
                            )
                        }
                    }
                }

                // Saldo Disponible Banner
                if (dniVerificado && billetera != null) {
                    item {
                        Surface(
                            shape = RoundedCornerShape(16.dp),
                            color = Color(0xFF6366F1),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 16.dp, vertical = 12.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "💰 Saldo Disponible: S/ ${String.format("%.2f", saldoActual)}",
                                    color = Color.White,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Button(
                                    onClick = onIrBilletera,
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = Color.White,
                                        contentColor = Color(0xFF6366F1)
                                    ),
                                    shape = RoundedCornerShape(10.dp),
                                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp)
                                ) {
                                    Text("Recargar", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                }

                // Loading
                if (cargando) {
                    item {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(160.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator(color = Color(0xFF6366F1))
                        }
                    }
                } else if (solicitudes.isEmpty()) {
                    item {
                        Surface(
                            shape = RoundedCornerShape(18.dp),
                            color = Color.White,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                "No hay trabajos disponibles con esos criterios en este momento.",
                                color = Color(0xFF64748B),
                                fontSize = 14.sp,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.padding(32.dp)
                            )
                        }
                    }
                } else {
                    // Lista de Solicitudes Disponibles
                    items(solicitudes, key = { it.idSolicitud }) { sol ->
                        val negActiva = negociaciones[sol.idSolicitud]

                        Card(
                            shape = RoundedCornerShape(20.dp),
                            colors = CardDefaults.cardColors(containerColor = Color.White),
                            elevation = CardDefaults.cardElevation(defaultElevation = 3.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(18.dp)) {
                                // Encabezado: Categoría y Fecha
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Surface(
                                        shape = RoundedCornerShape(8.dp),
                                        color = Color(0xFFEEF2FF)
                                    ) {
                                        Text(
                                            text = sol.nombreCategoria,
                                            color = Color(0xFF4F46E5),
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.Bold,
                                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                                        )
                                    }
                                    Text(
                                        text = sol.fechaCreacion.take(16).replace("T", " "),
                                        fontSize = 11.sp,
                                        color = Color(0xFF64748B)
                                    )
                                }

                                Spacer(modifier = Modifier.height(10.dp))

                                // Descripción
                                Text(
                                    text = sol.descripcion,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = Color(0xFF1E293B),
                                    lineHeight = 20.sp
                                )

                                Spacer(modifier = Modifier.height(10.dp))

                                // Metadatos
                                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                    Text("📍 ${sol.distrito}", fontSize = 12.sp, color = Color(0xFF64748B))
                                    if (!sol.referenciaDireccion.isNullOrBlank()) {
                                        Text("🏠 Ref: ${sol.referenciaDireccion}", fontSize = 12.sp, color = Color(0xFF64748B))
                                    }
                                    if (sol.precioPactado != null && sol.precioPactado > 0) {
                                        Text(
                                            "💵 Precio base: S/ ${String.format("%.2f", sol.precioPactado)}",
                                            fontSize = 13.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = Color(0xFF059669)
                                        )
                                    } else {
                                        Text("💵 Sin precio definido", fontSize = 12.sp, color = Color(0xFF94A3B8))
                                    }
                                }

                                Spacer(modifier = Modifier.height(14.dp))
                                HorizontalDivider(color = Color(0xFFF1F5F9))
                                Spacer(modifier = Modifier.height(12.dp))

                                // Si NO hay negociación previa
                                if (negActiva == null) {
                                    Column {
                                        Text("Tu oferta (S/):", fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = Color(0xFF334155))
                                        Spacer(modifier = Modifier.height(4.dp))
                                        OutlinedTextField(
                                            value = ofertasInput[sol.idSolicitud] ?: "",
                                            onValueChange = { ofertasInput[sol.idSolicitud] = it },
                                            placeholder = {
                                                Text(
                                                    if (sol.precioPactado != null && sol.precioPactado > 0) String.format("%.2f", sol.precioPactado)
                                                    else "0.00"
                                                )
                                            },
                                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                                            singleLine = true,
                                            prefix = { Text("S/ ", fontWeight = FontWeight.Bold) },
                                            shape = RoundedCornerShape(10.dp),
                                            modifier = Modifier.fillMaxWidth()
                                        )

                                        Spacer(modifier = Modifier.height(10.dp))

                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                                        ) {
                                            // Botón Hacer Oferta con Alerta de Billetera
                                            Button(
                                                onClick = {
                                                    val monto = ofertasInput[sol.idSolicitud]?.toDoubleOrNull()
                                                    if (monto == null || monto <= 0) {
                                                        Toast.makeText(context, "Ingresa un monto válido mayor a 0", Toast.LENGTH_SHORT).show()
                                                        return@Button
                                                    }
                                                    // Mostrar diálogo de confirmación y advertencia sobre descuento en billetera digital
                                                    confirmacionDialog = DatosConfirmacionOferta(
                                                        idSolicitud = sol.idSolicitud,
                                                        monto = monto,
                                                        tituloModal = "Enviar Oferta",
                                                        subtituloModal = "Estás a punto de enviar una oferta por S/ ${String.format("%.2f", monto)} al cliente.",
                                                        tipoOperacion = "OFERTA",
                                                        onConfirmar = {
                                                            confirmacionDialog = null
                                                            procesando = true
                                                            scope.launch {
                                                                negociacionRepository.ofertarComoTecnico(sol.idSolicitud, monto).onSuccess { neg ->
                                                                    Toast.makeText(context, "¡Oferta enviada! El cliente será notificado.", Toast.LENGTH_SHORT).show()
                                                                    negociaciones[sol.idSolicitud] = neg
                                                                    cargarDatos()
                                                                }.onFailure {
                                                                    Toast.makeText(context, it.localizedMessage ?: "Error al enviar oferta", Toast.LENGTH_SHORT).show()
                                                                }
                                                                procesando = false
                                                            }
                                                        }
                                                    )
                                                },
                                                enabled = dniVerificado && !procesando,
                                                shape = RoundedCornerShape(10.dp),
                                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF10B981)),
                                                modifier = Modifier.weight(1f)
                                            ) {
                                                Text("💬 Ofertar", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                            }

                                            // Botón Aceptar Precio Base con Alerta de Billetera
                                            if (sol.precioPactado != null && sol.precioPactado > 0) {
                                                Button(
                                                    onClick = {
                                                        val monto = sol.precioPactado
                                                        // Mostrar diálogo de confirmación y advertencia sobre descuento en billetera digital
                                                        confirmacionDialog = DatosConfirmacionOferta(
                                                            idSolicitud = sol.idSolicitud,
                                                            monto = monto,
                                                            tituloModal = "Aceptar Precio Base",
                                                            subtituloModal = "Estás a punto de aceptar directamente el precio base de S/ ${String.format("%.2f", monto)} propuesto por el cliente.",
                                                            tipoOperacion = "BASE",
                                                            onConfirmar = {
                                                                confirmacionDialog = null
                                                                procesando = true
                                                                scope.launch {
                                                                    negociacionRepository.ofertarComoTecnico(sol.idSolicitud, monto).onSuccess { neg ->
                                                                        Toast.makeText(context, "¡Oferta enviada al precio base! El cliente decidirá.", Toast.LENGTH_SHORT).show()
                                                                        negociaciones[sol.idSolicitud] = neg
                                                                        cargarDatos()
                                                                    }.onFailure {
                                                                        Toast.makeText(context, it.localizedMessage ?: "Error al ofertar precio base", Toast.LENGTH_SHORT).show()
                                                                    }
                                                                    procesando = false
                                                                }
                                                            }
                                                        )
                                                    },
                                                    enabled = dniVerificado && !procesando,
                                                    shape = RoundedCornerShape(10.dp),
                                                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF6366F1)),
                                                    modifier = Modifier.weight(1.3f)
                                                ) {
                                                    Text("✅ Aceptar Base", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                                }
                                            }
                                        }
                                    }
                                } else {
                                    // Si YA hay negociación
                                    Surface(
                                        shape = RoundedCornerShape(12.dp),
                                        color = when (negActiva.estado.uppercase()) {
                                            "ACEPTADO" -> Color(0xFFECFDF5)
                                            "RECHAZADO" -> Color(0xFFFFF1F2)
                                            else -> Color(0xFFFFFBEB)
                                        },
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Column(modifier = Modifier.padding(12.dp)) {
                                            Row(
                                                modifier = Modifier.fillMaxWidth(),
                                                horizontalArrangement = Arrangement.SpaceBetween,
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Text(
                                                    text = "S/ ${String.format("%.2f", negActiva.precioActual)}",
                                                    fontSize = 20.sp,
                                                    fontWeight = FontWeight.ExtraBold,
                                                    color = Color(0xFF0F172A)
                                                )
                                                Text(
                                                    text = when {
                                                        negActiva.estado.equals("ACEPTADO", ignoreCase = true) -> "✅ ¡Aceptado!"
                                                        negActiva.estado.equals("RECHAZADO", ignoreCase = true) -> "❌ Rechazado"
                                                        negActiva.ultimoEnOfertar.equals("TECNICO", ignoreCase = true) -> "⏳ Esperando al cliente"
                                                        else -> "🔔 ¡Cliente contraofertó!"
                                                    },
                                                    fontSize = 11.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    color = when {
                                                        negActiva.estado.equals("ACEPTADO", ignoreCase = true) -> Color(0xFF059669)
                                                        negActiva.estado.equals("RECHAZADO", ignoreCase = true) -> Color(0xFFE11D48)
                                                        negActiva.ultimoEnOfertar.equals("TECNICO", ignoreCase = true) -> Color(0xFFD97706)
                                                        else -> Color(0xFF6366F1)
                                                    }
                                                )
                                            }

                                            // Si el cliente contraofertó y está pendiente
                                            if (negActiva.estado.equals("PENDIENTE", ignoreCase = true) &&
                                                negActiva.ultimoEnOfertar.equals("CLIENTE", ignoreCase = true)
                                            ) {
                                                Spacer(modifier = Modifier.height(10.dp))

                                                // Campo para contraofertar de vuelta
                                                Text("Tu contraoferta (S/):", fontSize = 11.sp, fontWeight = FontWeight.SemiBold, color = Color(0xFF475569))
                                                Spacer(modifier = Modifier.height(4.dp))
                                                OutlinedTextField(
                                                    value = ofertasInput[sol.idSolicitud] ?: "",
                                                    onValueChange = { ofertasInput[sol.idSolicitud] = it },
                                                    placeholder = { Text(String.format("%.2f", negActiva.precioActual)) },
                                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                                                    singleLine = true,
                                                    prefix = { Text("S/ ", fontWeight = FontWeight.Bold) },
                                                    shape = RoundedCornerShape(10.dp),
                                                    modifier = Modifier.fillMaxWidth()
                                                )

                                                Spacer(modifier = Modifier.height(8.dp))

                                                Row(
                                                    modifier = Modifier.fillMaxWidth(),
                                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                                ) {
                                                    // Botón Contraofertar con alerta
                                                    Button(
                                                        onClick = {
                                                            val nuevoMonto = ofertasInput[sol.idSolicitud]?.toDoubleOrNull()
                                                            if (nuevoMonto == null || nuevoMonto <= 0) {
                                                                Toast.makeText(context, "Ingresa un monto válido para contraofertar", Toast.LENGTH_SHORT).show()
                                                                return@Button
                                                            }
                                                            confirmacionDialog = DatosConfirmacionOferta(
                                                                idSolicitud = sol.idSolicitud,
                                                                monto = nuevoMonto,
                                                                tituloModal = "Enviar Contraoferta",
                                                                subtituloModal = "Estás a punto de enviar una contraoferta por S/ ${String.format("%.2f", nuevoMonto)} al cliente.",
                                                                tipoOperacion = "CONTRAOFERTA",
                                                                onConfirmar = {
                                                                    confirmacionDialog = null
                                                                    procesando = true
                                                                    scope.launch {
                                                                        negociacionRepository.ofertarComoTecnico(sol.idSolicitud, nuevoMonto).onSuccess { neg ->
                                                                            Toast.makeText(context, "¡Contraoferta enviada al cliente!", Toast.LENGTH_SHORT).show()
                                                                            negociaciones[sol.idSolicitud] = neg
                                                                            cargarDatos()
                                                                        }.onFailure {
                                                                            Toast.makeText(context, it.localizedMessage ?: "Error al contraofertar", Toast.LENGTH_SHORT).show()
                                                                        }
                                                                        procesando = false
                                                                    }
                                                                }
                                                            )
                                                        },
                                                        enabled = dniVerificado && !procesando,
                                                        shape = RoundedCornerShape(10.dp),
                                                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF3B82F6)),
                                                        modifier = Modifier.weight(1f),
                                                        contentPadding = PaddingValues(horizontal = 6.dp, vertical = 10.dp)
                                                    ) {
                                                        Text("💬 Contraofertar", fontSize = 11.sp, fontWeight = FontWeight.Bold, maxLines = 1, softWrap = false)
                                                    }

                                                    // Botón Aceptar Precio con alerta
                                                    Button(
                                                        onClick = {
                                                            confirmacionDialog = DatosConfirmacionOferta(
                                                                idSolicitud = sol.idSolicitud,
                                                                monto = negActiva.precioActual,
                                                                tituloModal = "Aceptar Precio del Cliente",
                                                                subtituloModal = "Estás aceptando el precio de S/ ${String.format("%.2f", negActiva.precioActual)} propuesto por el cliente.",
                                                                tipoOperacion = "ACEPTAR_CLIENTE",
                                                                onConfirmar = {
                                                                    confirmacionDialog = null
                                                                    procesando = true
                                                                    scope.launch {
                                                                        negociacionRepository.ofertarComoTecnico(sol.idSolicitud, negActiva.precioActual).onSuccess {
                                                                            Toast.makeText(context, "Has aceptado el precio del cliente", Toast.LENGTH_SHORT).show()
                                                                            cargarDatos()
                                                                        }.onFailure {
                                                                            Toast.makeText(context, it.localizedMessage ?: "Error al aceptar precio", Toast.LENGTH_SHORT).show()
                                                                        }
                                                                        procesando = false
                                                                    }
                                                                }
                                                            )
                                                        },
                                                        enabled = !procesando,
                                                        shape = RoundedCornerShape(10.dp),
                                                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF10B981)),
                                                        modifier = Modifier.weight(1.2f),
                                                        contentPadding = PaddingValues(horizontal = 6.dp, vertical = 10.dp)
                                                    ) {
                                                        Text("✅ Aceptar S/ ${String.format("%.2f", negActiva.precioActual)}", fontSize = 10.sp, fontWeight = FontWeight.Bold, maxLines = 1, softWrap = false)
                                                    }

                                                    // Botón Rechazar
                                                    OutlinedButton(
                                                        onClick = {
                                                            procesando = true
                                                            scope.launch {
                                                                negociacionRepository.rechazarNegociacion(negActiva.idNegociacion).onSuccess {
                                                                    Toast.makeText(context, "Negociación rechazada", Toast.LENGTH_SHORT).show()
                                                                    cargarDatos()
                                                                }.onFailure {
                                                                    Toast.makeText(context, it.localizedMessage ?: "Error al rechazar", Toast.LENGTH_SHORT).show()
                                                                }
                                                                procesando = false
                                                            }
                                                        },
                                                        enabled = !procesando,
                                                        shape = RoundedCornerShape(10.dp),
                                                        colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFFE11D48)),
                                                        modifier = Modifier.weight(0.8f),
                                                        contentPadding = PaddingValues(horizontal = 4.dp, vertical = 10.dp)
                                                    ) {
                                                        Text("Rechazar", fontSize = 10.sp, fontWeight = FontWeight.Bold, maxLines = 1, softWrap = false)
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
            }
        }
    }

    // DIÁLOGO AMIGABLE Y SERIO DE DESCUENTO DE COMISIÓN DE BILLETERA DIGITAL
    confirmacionDialog?.let { datos ->
        AlertaComisionBilleteraDialog(
            datos = datos,
            saldoBilletera = saldoActual,
            onDismiss = { confirmacionDialog = null },
            onIrBilletera = {
                confirmacionDialog = null
                onIrBilletera()
            }
        )
    }
}

@Composable
fun AlertaComisionBilleteraDialog(
    datos: DatosConfirmacionOferta,
    saldoBilletera: Double,
    onDismiss: () -> Unit,
    onIrBilletera: () -> Unit
) {
    val comision = datos.monto * 0.12
    val saldoSuficiente = saldoBilletera >= comision
    val saldoRestante = (saldoBilletera - comision).coerceAtLeast(0.0)

    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            if (saldoSuficiente) {
                Button(
                    onClick = datos.onConfirmar,
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF10B981)),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("Confirmar y Enviar", fontWeight = FontWeight.Bold)
                }
            } else {
                Button(
                    onClick = onIrBilletera,
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF6366F1)),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("Recargar Billetera", fontWeight = FontWeight.Bold)
                }
            }
        },
        dismissButton = {
            OutlinedButton(
                onClick = onDismiss,
                shape = RoundedCornerShape(12.dp)
            ) {
                Text(if (saldoSuficiente) "Cancelar" else "Cerrar", color = Color(0xFF64748B))
            }
        },
        title = {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = Color(0xFFEEF2FF),
                    modifier = Modifier.size(40.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Text("💼", fontSize = 20.sp)
                    }
                }
                Column {
                    Text(
                        text = datos.tituloModal,
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF0F172A)
                    )
                    Text(
                        text = "Políticas y deducción de billetera",
                        fontSize = 11.sp,
                        color = Color(0xFF64748B)
                    )
                }
            }
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(
                    text = datos.subtituloModal,
                    fontSize = 13.sp,
                    color = Color(0xFF334155),
                    lineHeight = 18.sp
                )

                // Tarjeta de desglose financiero
                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = Color(0xFFF8FAFC),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE2E8F0)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Monto del servicio:", fontSize = 13.sp, color = Color(0xFF64748B))
                            Text("S/ ${String.format("%.2f", datos.monto)}", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Color(0xFF0F172A))
                        }
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Comisión Kucho (12%):", fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = Color(0xFF4F46E5))
                            Text("S/ ${String.format("%.2f", comision)}", fontSize = 13.sp, fontWeight = FontWeight.ExtraBold, color = Color(0xFF4F46E5))
                        }
                        HorizontalDivider(color = Color(0xFFE2E8F0), modifier = Modifier.padding(vertical = 4.dp))
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Tu saldo actual en billetera:", fontSize = 12.sp, color = Color(0xFF64748B))
                            Text(
                                "S/ ${String.format("%.2f", saldoBilletera)}",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (saldoSuficiente) Color(0xFF059669) else Color(0xFFDC2626)
                            )
                        }
                        if (saldoSuficiente) {
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text("Saldo estimado posterior:", fontSize = 12.sp, color = Color(0xFF64748B))
                                Text("S/ ${String.format("%.2f", saldoRestante)}", fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = Color(0xFF334155))
                            }
                        }
                    }
                }

                // Cuadro informativo con tono amigable pero serio
                if (saldoSuficiente) {
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = Color(0xFFEFF6FF),
                        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFBFDBFE)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(modifier = Modifier.padding(10.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Text("ℹ️", fontSize = 14.sp)
                            Text(
                                text = "El 12% (S/ ${String.format("%.2f", comision)}) solo será descontado de tu billetera digital si el cliente acepta y se concreta la asignación. No hay cobro previo si el cliente no te selecciona.",
                                fontSize = 11.sp,
                                color = Color(0xFF1E40AF),
                                lineHeight = 16.sp
                            )
                        }
                    }
                } else {
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = Color(0xFFFEF2F2),
                        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFFECACA)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(modifier = Modifier.padding(10.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Text("⚠️", fontSize = 14.sp)
                            Text(
                                text = "Tu saldo disponible es insuficiente para respaldar la comisión requerida de S/ ${String.format("%.2f", comision)}. Por favor recarga tu billetera digital para continuar.",
                                fontSize = 11.sp,
                                color = Color(0xFFB91C1C),
                                fontWeight = FontWeight.Medium,
                                lineHeight = 16.sp
                            )
                        }
                    }
                }
            }
        },
        shape = RoundedCornerShape(22.dp),
        containerColor = Color.White
    )
}
