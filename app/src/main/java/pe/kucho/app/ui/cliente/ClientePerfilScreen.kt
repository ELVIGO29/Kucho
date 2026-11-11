package pe.kucho.app.ui.cliente

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
import androidx.lifecycle.viewmodel.compose.viewModel
import kotlinx.coroutines.launch
import pe.kucho.app.data.local.SessionDataStore
import pe.kucho.app.data.model.ActualizarClienteRequest
import pe.kucho.app.data.model.UiState
import pe.kucho.app.ui.cliente.viewmodel.ClientePerfilViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ClientePerfilScreen(
    vm: ClientePerfilViewModel = viewModel(),
    onBack: (() -> Unit)? = null,
    onLogout: () -> Unit
) {
    val context = LocalContext.current
    val sessionDataStore = remember { SessionDataStore(context) }
    val scope = rememberCoroutineScope()

    var nombre by remember { mutableStateOf("") }
    var telefono by remember { mutableStateOf("") }
    var correo by remember { mutableStateOf("") }
    var distrito by remember { mutableStateOf("") }
    var tipoDocumento by remember { mutableStateOf("DNI") }
    var numeroDocumento by remember { mutableStateOf("") }
    var tipoDocExpanded by remember { mutableStateOf(false) }
    val tiposDoc = listOf("DNI", "CE", "PASAPORTE", "RUC")

    val perfilState by vm.perfilState.collectAsState()
    val actualizarState by vm.actualizarState.collectAsState()

    LaunchedEffect(Unit) { vm.cargarPerfil(context) }

    LaunchedEffect(perfilState) {
        if (perfilState is UiState.Exito) {
            val p = (perfilState as UiState.Exito).datos
            nombre = p.nombre; telefono = p.telefono
            correo = p.correo ?: ""; distrito = p.distrito ?: ""
            tipoDocumento = p.tipoDocumento ?: "DNI"; numeroDocumento = p.numeroDocumento ?: ""
        }
    }

    LaunchedEffect(actualizarState) {
        when (val s = actualizarState) {
            is UiState.Exito -> { Toast.makeText(context, "Perfil actualizado", Toast.LENGTH_SHORT).show(); vm.resetActualizarState() }
            is UiState.Error -> { Toast.makeText(context, s.mensaje, Toast.LENGTH_SHORT).show(); vm.resetActualizarState() }
            is UiState.SinConexion -> { Toast.makeText(context, "Sin conexion a internet.", Toast.LENGTH_SHORT).show(); vm.resetActualizarState() }
            else -> {}
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Mi Perfil", fontWeight = FontWeight.Bold, color = Color(0xFF0F172A)) },
                navigationIcon = { if (onBack != null) IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver", tint = Color(0xFF0F172A)) } },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White)
            )
        },
        containerColor = Color(0xFFF8FAFC)
    ) { padding ->
        when (val state = perfilState) {
            is UiState.Cargando -> Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) { CircularProgressIndicator(color = Color(0xFF6366F1)) }
            is UiState.SinConexion -> Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text("📵", fontSize = 40.sp)
                    Text("Sin conexion a internet", fontWeight = FontWeight.Bold)
                    Button(onClick = { vm.cargarPerfil(context) }) { Text("Reintentar") }
                }
            }
            is UiState.Error -> Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(state.mensaje, color = Color(0xFFDC2626))
                    Button(onClick = { vm.cargarPerfil(context) }, modifier = Modifier.padding(top = 8.dp)) { Text("Reintentar") }
                }
            }
            is UiState.Exito -> {
                val guardando = actualizarState is UiState.Cargando
                Column(
                    modifier = Modifier.fillMaxSize().padding(padding).verticalScroll(rememberScrollState()).padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Box(Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                        Box(modifier = Modifier.size(80.dp).background(Brush.linearGradient(listOf(Color(0xFF6366F1), Color(0xFF8B5CF6))), CircleShape), contentAlignment = Alignment.Center) {
                            Text(nombre.firstOrNull()?.uppercase() ?: "U", fontSize = 32.sp, fontWeight = FontWeight.ExtraBold, color = Color.White)
                        }
                    }
                    Text("Informacion Personal", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color(0xFF0F172A))

                    OutlinedTextField(value = nombre, onValueChange = { nombre = it }, label = { Text("Nombre completo") }, shape = RoundedCornerShape(12.dp), modifier = Modifier.fillMaxWidth())
                    OutlinedTextField(value = telefono, onValueChange = {}, label = { Text("Telefono") }, enabled = false, shape = RoundedCornerShape(12.dp), modifier = Modifier.fillMaxWidth())
                    OutlinedTextField(value = correo, onValueChange = { correo = it }, label = { Text("Correo electronico") }, shape = RoundedCornerShape(12.dp), modifier = Modifier.fillMaxWidth())
                    OutlinedTextField(value = distrito, onValueChange = { distrito = it }, label = { Text("Distrito") }, shape = RoundedCornerShape(12.dp), modifier = Modifier.fillMaxWidth())

                    ExposedDropdownMenuBox(expanded = tipoDocExpanded, onExpandedChange = { tipoDocExpanded = it }) {
                        OutlinedTextField(value = tipoDocumento, onValueChange = {}, label = { Text("Tipo documento") }, readOnly = true,
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(tipoDocExpanded) }, shape = RoundedCornerShape(12.dp), modifier = Modifier.fillMaxWidth().menuAnchor())
                        ExposedDropdownMenu(expanded = tipoDocExpanded, onDismissRequest = { tipoDocExpanded = false }) {
                            tiposDoc.forEach { tipo -> DropdownMenuItem(text = { Text(tipo) }, onClick = { tipoDocumento = tipo; tipoDocExpanded = false }) }
                        }
                    }
                    OutlinedTextField(value = numeroDocumento, onValueChange = { numeroDocumento = it }, label = { Text("Numero de documento") }, shape = RoundedCornerShape(12.dp), modifier = Modifier.fillMaxWidth())

                    Button(
                        onClick = { vm.actualizarPerfil(context, ActualizarClienteRequest(nombre = nombre, correo = correo.ifBlank { null }, distrito = distrito.ifBlank { null }, tipoDocumento = tipoDocumento.ifBlank { null }, numeroDocumento = numeroDocumento.ifBlank { null })) },
                        enabled = !guardando && nombre.isNotBlank(),
                        shape = RoundedCornerShape(14.dp), colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF6366F1)),
                        modifier = Modifier.fillMaxWidth().height(50.dp)
                    ) {
                        if (guardando) CircularProgressIndicator(color = Color.White, modifier = Modifier.size(22.dp))
                        else Text("Guardar Cambios", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                    }

                    OutlinedButton(
                        onClick = { scope.launch { sessionDataStore.cerrarSesion(); onLogout() } },
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFFE11D48)),
                        shape = RoundedCornerShape(14.dp), modifier = Modifier.fillMaxWidth()
                    ) { Text("Cerrar Sesion", fontWeight = FontWeight.Bold) }
                }
            }
            else -> {}
        }
    }
}
