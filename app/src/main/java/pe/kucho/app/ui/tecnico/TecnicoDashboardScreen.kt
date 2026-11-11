package pe.kucho.app.ui.tecnico

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import pe.kucho.app.data.model.UiState
import pe.kucho.app.ui.tecnico.viewmodel.TecnicoDashboardViewModel

@Composable
fun TecnicoDashboardScreen(
    vm: TecnicoDashboardViewModel = viewModel(),
    onBolsa: () -> Unit,
    onMisTrabajos: () -> Unit,
    onBilletera: () -> Unit,
    onPerfil: () -> Unit
) {
    val context = LocalContext.current
    val perfilState by vm.perfilState.collectAsState()

    LaunchedEffect(Unit) { vm.cargarPerfil(context) }

    val primerNombre = when (val s = perfilState) {
        is UiState.Exito -> s.datos.nombre.split(" ").firstOrNull() ?: "Tecnico"
        else -> "Tecnico"
    }

    Scaffold(
        bottomBar = {
            NavigationBar(containerColor = Color.White, tonalElevation = 8.dp, modifier = Modifier.shadow(12.dp)) {
                NavigationBarItem(
                    selected = true, onClick = {},
                    icon = { Text("🏠", fontSize = 20.sp) },
                    label = { Text("Dashboard", fontWeight = FontWeight.Bold) },
                    colors = NavigationBarItemDefaults.colors(selectedIconColor = Color(0xFF6366F1), selectedTextColor = Color(0xFF6366F1))
                )
                NavigationBarItem(
                    selected = false, onClick = onBolsa,
                    icon = { Text("💼", fontSize = 20.sp) },
                    label = { Text("Bolsa") }
                )
                NavigationBarItem(
                    selected = false, onClick = onMisTrabajos,
                    icon = { Text("🔨", fontSize = 20.sp) },
                    label = { Text("Trabajos") }
                )
                NavigationBarItem(
                    selected = false, onClick = onBilletera,
                    icon = { Text("💳", fontSize = 20.sp) },
                    label = { Text("Billetera") }
                )
                NavigationBarItem(
                    selected = false, onClick = onPerfil,
                    icon = { Text("👤", fontSize = 20.sp) },
                    label = { Text("Perfil") }
                )
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(Color(0xFFF1F5F9))
                .verticalScroll(rememberScrollState())
        ) {
            // Header con gradiente
            Box(
                modifier = Modifier.fillMaxWidth().background(
                    Brush.linearGradient(listOf(Color(0xFF4F46E5), Color(0xFF7C3AED))),
                    RoundedCornerShape(bottomStart = 28.dp, bottomEnd = 28.dp)
                ).padding(horizontal = 24.dp, vertical = 32.dp)
            ) {
                Column {
                    when (val s = perfilState) {
                        is UiState.Cargando -> CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp))
                        is UiState.SinConexion -> Text("Sin conexion", color = Color.White.copy(alpha = 0.8f), fontSize = 13.sp)
                        else -> {}
                    }
                    Text("Hola, $primerNombre 👋", fontSize = 24.sp, fontWeight = FontWeight.ExtraBold, color = Color.White)
                    Text("Panel del Tecnico", fontSize = 14.sp, color = Color.White.copy(alpha = 0.8f))
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Tarjetas de acceso rapido
            Column(modifier = Modifier.padding(horizontal = 16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text("Acceso Rapido", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Color(0xFF0F172A))

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    DashCard(modifier = Modifier.weight(1f), emoji = "💼", titulo = "Bolsa de Trabajos",
                        subtitulo = "Ver disponibles", color = Color(0xFFEEF2FF), onClick = onBolsa)
                    DashCard(modifier = Modifier.weight(1f), emoji = "🔨", titulo = "Mis Trabajos",
                        subtitulo = "En progreso", color = Color(0xFFECFDF5), onClick = onMisTrabajos)
                }
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    DashCard(modifier = Modifier.weight(1f), emoji = "💳", titulo = "Mi Billetera",
                        subtitulo = "Saldo y recargas", color = Color(0xFFFFF7ED), onClick = onBilletera)
                    DashCard(modifier = Modifier.weight(1f), emoji = "👤", titulo = "Mi Perfil",
                        subtitulo = "Actualizar datos", color = Color(0xFFFDF4FF), onClick = onPerfil)
                }
            }
            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
private fun DashCard(modifier: Modifier, emoji: String, titulo: String, subtitulo: String, color: Color, onClick: () -> Unit) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = modifier.clickable(onClick = onClick)
    ) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Box(modifier = Modifier.size(44.dp).background(color, CircleShape), contentAlignment = Alignment.Center) {
                Text(emoji, fontSize = 20.sp)
            }
            Text(titulo, fontWeight = FontWeight.Bold, fontSize = 14.sp, color = Color(0xFF0F172A))
            Text(subtitulo, fontSize = 12.sp, color = Color(0xFF64748B))
        }
    }
}
