package pe.kucho.app.ui.auth

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Phone
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
import pe.kucho.app.data.local.SessionDataStore
import pe.kucho.app.data.model.UiState
import pe.kucho.app.data.network.RetrofitClient
import pe.kucho.app.ui.auth.viewmodel.LoginViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LoginScreen(
    vm: LoginViewModel = viewModel(),
    onLoginSuccess: (rol: String, token: String) -> Unit,
    onNavigateToRegister: () -> Unit,
    onVerGuia: (() -> Unit)? = null
) {
    val context = LocalContext.current
    val sessionDataStore = remember { SessionDataStore(context) }

    var telefono by remember { mutableStateOf("") }
    var codigoOtp by remember { mutableStateOf("") }
    var pasoOtpEnviado by remember { mutableStateOf(false) }
    var mensajeError by remember { mutableStateOf<String?>(null) }

    // Observar estados del ViewModel con StateFlow
    val otpState by vm.otpState.collectAsState()
    val loginState by vm.loginState.collectAsState()

    val cargandoOtp = otpState is UiState.Cargando && pasoOtpEnviado == false
    val cargandoLogin = loginState is UiState.Cargando

    // Reaccionar al resultado del OTP
    LaunchedEffect(otpState) {
        when (val s = otpState) {
            is UiState.Exito -> { pasoOtpEnviado = true; mensajeError = null }
            is UiState.Error -> mensajeError = s.mensaje
            is UiState.SinConexion -> mensajeError = "Sin conexion a internet. Verifica tu red."
            else -> {}
        }
    }

    // Reaccionar al resultado del login
    LaunchedEffect(loginState) {
        when (val s = loginState) {
            is UiState.Exito -> {
                val response = s.datos
                sessionDataStore.guardarSesion(
                    token = response.token,
                    rol = response.rol,
                    idUsuario = response.idUsuario,
                    nombre = response.nombre
                )
                RetrofitClient.init { response.token }
                onLoginSuccess(response.rol, response.token)
                vm.resetLoginState()
            }
            is UiState.Error -> mensajeError = s.mensaje
            is UiState.SinConexion -> mensajeError = "Sin conexion a internet. Verifica tu red."
            else -> {}
        }
    }

    fun obtenerTelefonoNormalizado(): String =
        telefono.trim().removePrefix("+51").trim()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF8FAFC))
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp, vertical = 40.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Logo
            Box(
                modifier = Modifier
                    .size(72.dp)
                    .background(
                        Brush.linearGradient(listOf(Color(0xFF6366F1), Color(0xFF8B5CF6))),
                        CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) { Text("🔧", fontSize = 32.sp) }

            Spacer(modifier = Modifier.height(16.dp))
            Text("Kucho", fontSize = 28.sp, fontWeight = FontWeight.ExtraBold, color = Color(0xFF0F172A))
            Text("Inicia sesion en tu cuenta", fontSize = 14.sp, color = Color(0xFF64748B))
            Spacer(modifier = Modifier.height(32.dp))

            Surface(
                shape = RoundedCornerShape(20.dp),
                color = Color.White,
                shadowElevation = 4.dp,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(24.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    if (!pasoOtpEnviado) {
                        // PASO 1: Ingresar teléfono
                        Text("Ingresar", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = Color(0xFF0F172A))

                        OutlinedTextField(
                            value = telefono,
                            onValueChange = { telefono = it },
                            label = { Text("Numero de telefono") },
                            placeholder = { Text("999 888 777") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                            singleLine = true,
                            shape = RoundedCornerShape(12.dp),
                            leadingIcon = { Icon(Icons.Default.Phone, contentDescription = null, tint = Color(0xFF6366F1)) },
                            prefix = { Text("+51 ", color = Color(0xFF64748B)) },
                            modifier = Modifier.fillMaxWidth()
                        )

                        Button(
                            onClick = {
                                mensajeError = null
                                vm.solicitarOtp(context, obtenerTelefonoNormalizado())
                            },
                            enabled = obtenerTelefonoNormalizado().length >= 9 && !cargandoLogin,
                            shape = RoundedCornerShape(14.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF6366F1)),
                            modifier = Modifier.fillMaxWidth().height(50.dp)
                        ) {
                            if (cargandoLogin) CircularProgressIndicator(color = Color.White, modifier = Modifier.size(22.dp))
                            else Text("Enviar codigo", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                        }

                        Text(
                            "Te enviaremos un codigo de verificacion (OTP) por SMS",
                            fontSize = 11.sp, color = Color(0xFF94A3B8), textAlign = TextAlign.Center
                        )
                    } else {
                        // PASO 2: Verificar OTP
                        Text("Verificar Codigo", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = Color(0xFF0F172A))
                        Text(
                            "Enviado al +51 ${obtenerTelefonoNormalizado()}",
                            fontSize = 13.sp, color = Color(0xFF64748B),
                            modifier = Modifier.padding(top = 4.dp, bottom = 20.dp)
                        )

                        OutlinedTextField(
                            value = codigoOtp,
                            onValueChange = { if (it.length <= 6) codigoOtp = it },
                            placeholder = { Text("000000") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            singleLine = true,
                            shape = RoundedCornerShape(12.dp),
                            leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null, tint = Color(0xFF6366F1)) },
                            modifier = Modifier.fillMaxWidth()
                        )

                        Button(
                            onClick = {
                                mensajeError = null
                                vm.verificarOtp(context, obtenerTelefonoNormalizado(), codigoOtp.trim())
                            },
                            enabled = codigoOtp.trim().length >= 4 && !cargandoLogin,
                            shape = RoundedCornerShape(14.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF6366F1)),
                            modifier = Modifier.fillMaxWidth().height(50.dp)
                        ) {
                            if (cargandoLogin) CircularProgressIndicator(color = Color.White, modifier = Modifier.size(22.dp))
                            else Text("Ingresar", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                        }

                        TextButton(onClick = { pasoOtpEnviado = false; vm.resetOtpState() }) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Cambiar numero de telefono", color = Color(0xFF6366F1), fontSize = 13.sp)
                        }
                    }

                    // Mensaje de error / sin conexion
                    AnimatedVisibility(visible = mensajeError != null, enter = fadeIn(), exit = fadeOut()) {
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = Color(0xFFFEF2F2),
                            border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFFECACA)),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                                Text("⚠️", fontSize = 14.sp)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(mensajeError ?: "", color = Color(0xFFDC2626), fontSize = 12.sp, fontWeight = FontWeight.Medium)
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("No tienes cuenta?", color = Color(0xFF64748B), fontSize = 14.sp)
                Spacer(modifier = Modifier.width(6.dp))
                TextButton(onClick = onNavigateToRegister) {
                    Text("Registrate", color = Color(0xFF6366F1), fontWeight = FontWeight.Bold, fontSize = 14.sp)
                }
            }

            if (onVerGuia != null) {
                Spacer(modifier = Modifier.height(4.dp))
                TextButton(onClick = onVerGuia) {
                    Text("💡 ¿Cómo funciona Kucho? Ver guía rápida", color = Color(0xFF4F46E5), fontSize = 13.sp, fontWeight = FontWeight.Medium)
                }
            }
        }
    }
}
