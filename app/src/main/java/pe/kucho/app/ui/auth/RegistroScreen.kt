package pe.kucho.app.ui.auth

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
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
import kotlinx.coroutines.launch
import pe.kucho.app.data.model.RegistroUsuarioRequest
import pe.kucho.app.data.repository.AuthRepository

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RegistroScreen(
    authRepository: AuthRepository = AuthRepository(),
    onBackToLogin: () -> Unit,
    onRegisterSuccess: () -> Unit
) {
    val scope = rememberCoroutineScope()

    var nombre by remember { mutableStateOf("") }
    var telefono by remember { mutableStateOf("") }
    var rolSeleccionado by remember { mutableStateOf("CLIENTE") }
    var tipoDocumento by remember { mutableStateOf("DNI") }
    var numeroDocumento by remember { mutableStateOf("") }
    var correo by remember { mutableStateOf("") }
    var distrito by remember { mutableStateOf("") }
    var biografia by remember { mutableStateOf("") }

    var cargando by remember { mutableStateOf(false) }
    var mensajeError by remember { mutableStateOf<String?>(null) }
    var aceptoPoliticas by remember { mutableStateOf(false) }
    var docDropdownExpanded by remember { mutableStateOf(false) }
    val opcionesDoc = listOf("DNI", "CE", "PASAPORTE", "RUC")

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF8FAFC))
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(16.dp))

            // Brand Header
            Box(
                modifier = Modifier
                    .size(56.dp)
                    .background(
                        Brush.linearGradient(listOf(Color(0xFF6366F1), Color(0xFF8B5CF6))),
                        CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text("✨", fontSize = 26.sp)
            }
            Spacer(modifier = Modifier.height(10.dp))
            Text("Kucho", fontSize = 28.sp, fontWeight = FontWeight.ExtraBold, color = Color(0xFF1E293B))
            Text("Únete como vecino o técnico", fontSize = 13.sp, color = Color(0xFF64748B))

            Spacer(modifier = Modifier.height(24.dp))

            // Card Formulario
            Card(
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, Color(0xFFE2E8F0), RoundedCornerShape(24.dp))
            ) {
                Column(modifier = Modifier.padding(24.dp)) {
                    Text("Registro", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = Color(0xFF0F172A))
                    Spacer(modifier = Modifier.height(16.dp))

                    // Selector de Rol (Cliente vs Técnico)
                    Text("Tipo de usuario", fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = Color(0xFF475569))
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        RoleSelectorCard(
                            modifier = Modifier.weight(1f),
                            title = "Cliente",
                            icon = "👤",
                            selected = rolSeleccionado == "CLIENTE",
                            onClick = { rolSeleccionado = "CLIENTE" }
                        )
                        RoleSelectorCard(
                            modifier = Modifier.weight(1f),
                            title = "Técnico",
                            icon = "👷",
                            selected = rolSeleccionado == "TECNICO",
                            onClick = { rolSeleccionado = "TECNICO" }
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Nombre Completo
                    OutlinedTextField(
                        value = nombre,
                        onValueChange = { nombre = it },
                        label = { Text("Nombre Completo") },
                        placeholder = { Text("Ej: Juan Pérez") },
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    // Teléfono
                    OutlinedTextField(
                        value = telefono,
                        onValueChange = { if (it.length <= 9) telefono = it },
                        label = { Text("Número de Celular") },
                        placeholder = { Text("987 654 321") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    // Tipo de Documento y Número
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        ExposedDropdownMenuBox(
                            expanded = docDropdownExpanded,
                            onExpandedChange = { docDropdownExpanded = !docDropdownExpanded },
                            modifier = Modifier.width(110.dp)
                        ) {
                            OutlinedTextField(
                                value = tipoDocumento,
                                onValueChange = {},
                                readOnly = true,
                                label = { Text("Tipo") },
                                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = docDropdownExpanded) },
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier.menuAnchor()
                            )
                            ExposedDropdownMenu(
                                expanded = docDropdownExpanded,
                                onDismissRequest = { docDropdownExpanded = false }
                            ) {
                                opcionesDoc.forEach { doc ->
                                    DropdownMenuItem(
                                        text = { Text(doc) },
                                        onClick = {
                                            tipoDocumento = doc
                                            docDropdownExpanded = false
                                        }
                                    )
                                }
                            }
                        }

                        OutlinedTextField(
                            value = numeroDocumento,
                            onValueChange = { numeroDocumento = it },
                            label = { Text("N° de Documento") },
                            placeholder = { Text("12345678") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            singleLine = true,
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.weight(1f)
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Correo y Distrito
                    OutlinedTextField(
                        value = correo,
                        onValueChange = { correo = it },
                        label = { Text("Correo Electrónico (Opcional)") },
                        placeholder = { Text("juan@email.com") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    OutlinedTextField(
                        value = distrito,
                        onValueChange = { distrito = it },
                        label = { Text("Distrito (Opcional)") },
                        placeholder = { Text("Ej: Trujillo, Miraflores") },
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    )

                    // Biografía solo para Técnicos
                    if (rolSeleccionado == "TECNICO") {
                        Spacer(modifier = Modifier.height(12.dp))
                        OutlinedTextField(
                            value = biografia,
                            onValueChange = { biografia = it },
                            label = { Text("Biografía / Especialidad") },
                            placeholder = { Text("Cuéntanos sobre tu experiencia y trabajos...") },
                            maxLines = 3,
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.fillMaxWidth()
                        )
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    // Botón Crear Cuenta
                    Button(
                        onClick = {
                            if (nombre.isBlank() || telefono.isBlank() || numeroDocumento.isBlank()) {
                                mensajeError = "Por favor completa los campos obligatorios."
                                return@Button
                            }
                            cargando = true
                            mensajeError = null
                            scope.launch {
                                val telFormateado = telefono.trim().removePrefix("+51").trim()
                                val req = RegistroUsuarioRequest(
                                    telefono = telFormateado,
                                    nombre = nombre.trim(),
                                    rol = rolSeleccionado,
                                    tipoDocumento = tipoDocumento,
                                    numeroDocumento = numeroDocumento.trim(),
                                    correo = correo.trim().ifBlank { null },
                                    distrito = distrito.trim().ifBlank { null },
                                    biografia = if (rolSeleccionado == "TECNICO") biografia.trim().ifBlank { null } else null
                                )
                                val res = authRepository.registrarUsuario(req)
                                cargando = false
                                res.onSuccess {
                                    onRegisterSuccess()
                                }.onFailure {
                                    mensajeError = it.localizedMessage ?: "Error al registrar usuario."
                                }
                            }
                        },
                        enabled = !cargando && aceptoPoliticas && nombre.isNotBlank() && telefono.isNotBlank() && numeroDocumento.isNotBlank(),
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF6366F1)),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp)
                    ) {
                        if (cargando) {
                            CircularProgressIndicator(color = Color.White, modifier = Modifier.size(22.dp))
                        } else {
                            Text("Crear Cuenta", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                        }
                    }

                    // Mensaje de Error
                    AnimatedVisibility(visible = mensajeError != null, enter = fadeIn(), exit = fadeOut()) {
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = Color(0xFFFEF2F2),
                            border = BorderStroke(1.dp, Color(0xFFFECACA)),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 16.dp)
                        ) {
                            Row(
                                modifier = Modifier.padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text("⚠️", fontSize = 14.sp)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    mensajeError ?: "",
                                    color = Color(0xFFDC2626),
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Footer Iniciar Sesión
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("¿Ya tienes cuenta?", color = Color(0xFF64748B), fontSize = 14.sp)
                Spacer(modifier = Modifier.width(6.dp))
                TextButton(onClick = onBackToLogin) {
                    Text("Inicia sesión", color = Color(0xFF6366F1), fontWeight = FontWeight.Bold, fontSize = 14.sp)
                }
            }
        }
    }
}

@Composable
fun RoleSelectorCard(
    modifier: Modifier = Modifier,
    title: String,
    icon: String,
    selected: Boolean,
    onClick: () -> Unit
) {
    Surface(
        shape = RoundedCornerShape(14.dp),
        color = if (selected) Color(0xFFEEF2FF) else Color(0xFFF8FAFC),
        border = BorderStroke(
            1.5.dp,
            if (selected) Color(0xFF6366F1) else Color(0xFFE2E8F0)
        ),
        modifier = modifier
            .height(64.dp)
            .clickable { onClick() }
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Text(icon, fontSize = 20.sp)
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                title,
                fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
                color = if (selected) Color(0xFF4F46E5) else Color(0xFF475569),
                fontSize = 14.sp
            )
        }
    }
}



