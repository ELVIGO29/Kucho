package pe.kucho.app.ui.tecnico

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.launch
import pe.kucho.app.data.local.SessionDataStore
import pe.kucho.app.data.model.ActualizarPerfilTecnicoRequest
import pe.kucho.app.data.model.RegistroDocumentoRequest
import pe.kucho.app.data.model.TecnicoResponse
import pe.kucho.app.data.repository.TecnicoRepository

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TecnicoPerfilScreen(
    tecnicoRepository: TecnicoRepository = remember { TecnicoRepository() },
    onBack: (() -> Unit)? = null,
    onLogout: () -> Unit
) {
    val context = LocalContext.current
    val sessionDataStore = remember { SessionDataStore(context) }
    val scope = rememberCoroutineScope()

    var perfil by remember { mutableStateOf<TecnicoResponse?>(null) }
    var biografia by remember { mutableStateOf("") }
    var zonasTexto by remember { mutableStateOf("") }
    var tipoDoc by remember { mutableStateOf("DNI") }
    var numDoc by remember { mutableStateOf("") }

    var cargando by remember { mutableStateOf(true) }
    var guardandoPerfil by remember { mutableStateOf(false) }
    var guardandoDoc by remember { mutableStateOf(false) }
    var tipoDocExpanded by remember { mutableStateOf(false) }

    val tiposDoc = listOf("DNI", "CE", "PASAPORTE")

    fun cargar() {
        cargando = true
        scope.launch {
            tecnicoRepository.obtenerMiPerfil().onSuccess { p ->
                perfil = p
                biografia = p.biografia ?: ""
                zonasTexto = p.zonas?.joinToString(", ") ?: ""
                tipoDoc = p.tipoDocumento ?: "DNI"
                numDoc = p.numeroDocumento ?: ""
                cargando = false
            }.onFailure {
                cargando = false
            }
        }
    }

    LaunchedEffect(Unit) {
        cargar()
    }

    Scaffold(
        topBar = {
            if (onBack != null) {
                TopAppBar(
                    title = { Text("Mi Perfil", fontWeight = FontWeight.Bold, color = Color(0xFF0F172A)) },
                    navigationIcon = {
                        IconButton(onClick = onBack) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver", tint = Color(0xFF0F172A))
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White)
                )
            }
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
            } else {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                        .padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // Header con Avatar
                    Card(
                        shape = RoundedCornerShape(24.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(20.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(80.dp)
                                    .background(
                                        Brush.linearGradient(listOf(Color(0xFF6366F1), Color(0xFF8B5CF6))),
                                        CircleShape
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    (perfil?.nombre ?: "T").take(1).uppercase(),
                                    color = Color.White,
                                    fontSize = 32.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                            Spacer(modifier = Modifier.height(10.dp))
                            Text(
                                text = perfil?.nombre ?: "Técnico",
                                fontSize = 20.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF0F172A)
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = if (perfil?.dniVerificado == true) Color(0xFFD1FAE5) else Color(0xFFFEF3C7)
                            ) {
                                Text(
                                    text = if (perfil?.dniVerificado == true) "✅ Identidad Verificada" else "⚠️ Identidad No Verificada",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (perfil?.dniVerificado == true) Color(0xFF059669) else Color(0xFFD97706),
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                                )
                            }
                        }
                    }

                    // Datos Personales (Solo Lectura)
                    Card(
                        shape = RoundedCornerShape(20.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(20.dp)) {
                            Text(
                                "Datos Personales",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF0F172A)
                            )
                            Spacer(modifier = Modifier.height(14.dp))

                            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                                Column {
                                    Text("NOMBRE COMPLETO", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color(0xFF64748B))
                                    Text(perfil?.nombre ?: "-", fontSize = 14.sp, fontWeight = FontWeight.Medium, color = Color(0xFF0F172A))
                                }
                                if (!perfil?.correo.isNullOrBlank()) {
                                    Column {
                                        Text("CORREO ELECTRÓNICO", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color(0xFF64748B))
                                        Text(perfil?.correo ?: "-", fontSize = 14.sp, fontWeight = FontWeight.Medium, color = Color(0xFF0F172A))
                                    }
                                }
                                if (!perfil?.telefono.isNullOrBlank()) {
                                    Column {
                                        Text("TELÉFONO", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color(0xFF64748B))
                                        Text(perfil?.telefono ?: "-", fontSize = 14.sp, fontWeight = FontWeight.Medium, color = Color(0xFF0F172A))
                                    }
                                }
                                if (!perfil?.numeroDocumento.isNullOrBlank()) {
                                    Column {
                                        Text("DOCUMENTO", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color(0xFF64748B))
                                        Text("${perfil?.tipoDocumento ?: "DNI"} - ${perfil?.numeroDocumento}", fontSize = 14.sp, fontWeight = FontWeight.Medium, color = Color(0xFF0F172A))
                                    }
                                }
                            }
                        }
                    }

                    // Información Pública
                    Card(
                        shape = RoundedCornerShape(20.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier.padding(20.dp),
                            verticalArrangement = Arrangement.spacedBy(14.dp)
                        ) {
                            Text(
                                "Información Pública",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF0F172A)
                            )

                            Column {
                                Text("Biografía Profesional", fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = Color(0xFF334155))
                                Spacer(modifier = Modifier.height(4.dp))
                                OutlinedTextField(
                                    value = biografia,
                                    onValueChange = { biografia = it },
                                    placeholder = { Text("Cuéntanos tu experiencia y especialidades...") },
                                    minLines = 3,
                                    maxLines = 5,
                                    shape = RoundedCornerShape(12.dp),
                                    modifier = Modifier.fillMaxWidth()
                                )
                            }

                            Column {
                                Text("Zonas de Cobertura (separadas por coma)", fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = Color(0xFF334155))
                                Spacer(modifier = Modifier.height(4.dp))
                                OutlinedTextField(
                                    value = zonasTexto,
                                    onValueChange = { zonasTexto = it },
                                    placeholder = { Text("Ej. Trujillo, Victor Larco, Huanchaco") },
                                    singleLine = true,
                                    shape = RoundedCornerShape(12.dp),
                                    modifier = Modifier.fillMaxWidth()
                                )
                            }

                            Button(
                                onClick = {
                                    guardandoPerfil = true
                                    scope.launch {
                                        val zonasList = zonasTexto.split(",").map { it.trim() }.filter { it.isNotBlank() }
                                        val req = ActualizarPerfilTecnicoRequest(
                                            biografia = biografia.trim().ifBlank { null },
                                            zonas = zonasList
                                        )
                                        tecnicoRepository.actualizarMiPerfil(req).onSuccess {
                                            Toast.makeText(context, "¡Información actualizada!", Toast.LENGTH_SHORT).show()
                                            cargar()
                                        }.onFailure {
                                            Toast.makeText(context, it.localizedMessage ?: "Error al actualizar", Toast.LENGTH_SHORT).show()
                                        }
                                        guardandoPerfil = false
                                    }
                                },
                                enabled = !guardandoPerfil,
                                shape = RoundedCornerShape(12.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF6366F1)),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                if (guardandoPerfil) {
                                    CircularProgressIndicator(color = Color.White, modifier = Modifier.size(20.dp))
                                } else {
                                    Text("Guardar Información", fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }

                    // Registro de Documento (si aún no está verificado)
                    if (perfil?.dniVerificado != true) {
                        Card(
                            shape = RoundedCornerShape(20.dp),
                            colors = CardDefaults.cardColors(containerColor = Color.White),
                            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(
                                modifier = Modifier.padding(20.dp),
                                verticalArrangement = Arrangement.spacedBy(14.dp)
                            ) {
                                Text(
                                    "Registro de Documento",
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF0F172A)
                                )

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text("Tipo Doc.", fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = Color(0xFF334155))
                                        Spacer(modifier = Modifier.height(4.dp))
                                        ExposedDropdownMenuBox(
                                            expanded = tipoDocExpanded,
                                            onExpandedChange = { tipoDocExpanded = it }
                                        ) {
                                            OutlinedTextField(
                                                value = tipoDoc,
                                                onValueChange = {},
                                                readOnly = true,
                                                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = tipoDocExpanded) },
                                                shape = RoundedCornerShape(12.dp),
                                                modifier = Modifier.menuAnchor().fillMaxWidth()
                                            )
                                            ExposedDropdownMenu(
                                                expanded = tipoDocExpanded,
                                                onDismissRequest = { tipoDocExpanded = false }
                                            ) {
                                                tiposDoc.forEach { t ->
                                                    DropdownMenuItem(
                                                        text = { Text(t) },
                                                        onClick = {
                                                            tipoDoc = t
                                                            tipoDocExpanded = false
                                                        }
                                                    )
                                                }
                                            }
                                        }
                                    }

                                    Column(modifier = Modifier.weight(1.5f)) {
                                        Text("Número Documento", fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = Color(0xFF334155))
                                        Spacer(modifier = Modifier.height(4.dp))
                                        OutlinedTextField(
                                            value = numDoc,
                                            onValueChange = { numDoc = it },
                                            placeholder = { Text("12345678") },
                                            singleLine = true,
                                            shape = RoundedCornerShape(12.dp),
                                            modifier = Modifier.fillMaxWidth()
                                        )
                                    }
                                }

                                Button(
                                    onClick = {
                                        if (numDoc.isBlank()) {
                                            Toast.makeText(context, "Ingresa el número de documento", Toast.LENGTH_SHORT).show()
                                            return@Button
                                        }
                                        guardandoDoc = true
                                        scope.launch {
                                            val req = RegistroDocumentoRequest(
                                                tipoDocumento = tipoDoc,
                                                numeroDocumento = numDoc.trim()
                                            )
                                            tecnicoRepository.registrarDocumento(req).onSuccess {
                                                Toast.makeText(context, "Documento enviado para validación", Toast.LENGTH_SHORT).show()
                                                cargar()
                                            }.onFailure {
                                                Toast.makeText(context, it.localizedMessage ?: "Error al registrar documento", Toast.LENGTH_SHORT).show()
                                            }
                                            guardandoDoc = false
                                        }
                                    },
                                    enabled = numDoc.isNotBlank() && !guardandoDoc,
                                    shape = RoundedCornerShape(12.dp),
                                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0F172A)),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    if (guardandoDoc) {
                                        CircularProgressIndicator(color = Color.White, modifier = Modifier.size(20.dp))
                                    } else {
                                        Text("Solicitar Verificación", fontWeight = FontWeight.Bold)
                                    }
                                }
                            }
                        }
                    }

                    // Cerrar Sesión
                    OutlinedButton(
                        onClick = {
                            scope.launch {
                                sessionDataStore.cerrarSesion()
                                onLogout()
                            }
                        },
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.outlinedButtonColors(
                            containerColor = Color(0xFFFFF1F2),
                            contentColor = Color(0xFFE11D48)
                        ),
                        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFFECDD3)),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp)
                    ) {
                        Text("Cerrar Sesión", fontWeight = FontWeight.Bold)
                    }

                    Spacer(modifier = Modifier.height(20.dp))
                }
            }
        }
    }
}
