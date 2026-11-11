package pe.kucho.app.ui.cliente

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import pe.kucho.app.data.model.UiState
import pe.kucho.app.ui.cliente.viewmodel.ClienteHomeViewModel

@Composable
fun ClienteHomeScreen(
    vm: ClienteHomeViewModel = viewModel(),
    onCrearSolicitud: (categoriaId: String?) -> Unit,
    onVerMisSolicitudes: () -> Unit,
    onVerCategorias: () -> Unit,
    onVerPerfil: () -> Unit
) {
    val context = LocalContext.current
    val categoriasState by vm.categoriasState.collectAsState()
    val solicitudesState by vm.solicitudesState.collectAsState()
    val perfilState by vm.perfilState.collectAsState()

    LaunchedEffect(Unit) { vm.cargarDatos(context) }

    val nombreUsuario = when (val s = perfilState) {
        is UiState.Exito -> s.datos.nombre.split(" ").firstOrNull() ?: "Usuario"
        else -> "Usuario"
    }

    val categoryColors = remember {
        listOf(
            Color(0xFFEDE9FE) to Color(0xFF6366F1),
            Color(0xFFFFE4E6) to Color(0xFFF43F5E),
            Color(0xFFD1FAE5) to Color(0xFF10B981),
            Color(0xFFFEF3C7) to Color(0xFFF59E0B),
            Color(0xFFDBEAFE) to Color(0xFF3B82F6),
            Color(0xFFFCE7F3) to Color(0xFFEC4899)
        )
    }

    Scaffold(
        bottomBar = {
            NavigationBar(containerColor = Color.White, tonalElevation = 8.dp, modifier = Modifier.shadow(12.dp)) {
                NavigationBarItem(selected = true, onClick = {}, icon = { Text("🏠", fontSize = 20.sp) }, label = { Text("Inicio") })
                NavigationBarItem(selected = false, onClick = onVerCategorias, icon = { Text("📋", fontSize = 20.sp) }, label = { Text("Categorias") })
                NavigationBarItem(selected = false, onClick = onVerMisSolicitudes, icon = { Text("📝", fontSize = 20.sp) }, label = { Text("Solicitudes") })
                NavigationBarItem(selected = false, onClick = onVerPerfil, icon = { Text("👤", fontSize = 20.sp) }, label = { Text("Perfil") })
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier.fillMaxSize().padding(padding).background(Color(0xFFF8FAFC)).verticalScroll(rememberScrollState())
        ) {
            // Header
            Box(
                modifier = Modifier.fillMaxWidth().background(
                    Brush.linearGradient(listOf(Color(0xFF6366F1), Color(0xFF8B5CF6))),
                    RoundedCornerShape(bottomStart = 28.dp, bottomEnd = 28.dp)
                ).padding(horizontal = 24.dp, vertical = 32.dp)
            ) {
                Column {
                    Text("Hola, $nombreUsuario 👋", fontSize = 22.sp, fontWeight = FontWeight.ExtraBold, color = Color.White)
                    Text("¿Que servicio necesitas hoy?", fontSize = 14.sp, color = Color.White.copy(alpha = 0.8f))
                    Spacer(modifier = Modifier.height(16.dp))
                    Button(
                        onClick = { onCrearSolicitud(null) },
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color.White.copy(alpha = 0.25f)),
                        modifier = Modifier.fillMaxWidth().height(48.dp)
                    ) {
                        Text("+ Nueva Solicitud de Servicio", fontWeight = FontWeight.Bold, color = Color.White)
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Categorias
            Column(modifier = Modifier.padding(horizontal = 16.dp)) {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    Text("Categorias", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Color(0xFF0F172A))
                    TextButton(onClick = onVerCategorias) { Text("Ver todas", color = Color(0xFF6366F1), fontSize = 13.sp) }
                }
                Spacer(modifier = Modifier.height(12.dp))

                when (val s = categoriasState) {
                    is UiState.Cargando -> Box(Modifier.fillMaxWidth().height(80.dp), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(color = Color(0xFF6366F1), modifier = Modifier.size(28.dp))
                    }
                    is UiState.SinConexion -> SinConexionBanner(onReintentar = { vm.cargarDatos(context) })
                    is UiState.Error -> Text(s.mensaje, color = Color(0xFFDC2626), fontSize = 12.sp)
                    is UiState.Exito -> {
                        LazyRow(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                            itemsIndexed(s.datos.take(6)) { idx, cat ->
                                val (bg, icon) = categoryColors[idx % categoryColors.size]
                                Column(
                                    modifier = Modifier.width(80.dp).clickable { onCrearSolicitud(cat.idCategoria) },
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Box(modifier = Modifier.size(56.dp).background(bg, CircleShape).clip(CircleShape), contentAlignment = Alignment.Center) {
                                        Text(cat.icono ?: "🔧", fontSize = 26.sp)
                                    }
                                    Spacer(modifier = Modifier.height(6.dp))
                                    Text(cat.nombre, fontSize = 11.sp, textAlign = TextAlign.Center, maxLines = 2, overflow = TextOverflow.Ellipsis, color = Color(0xFF334155))
                                }
                            }
                        }
                    }
                    else -> {}
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Solicitudes recientes
            Column(modifier = Modifier.padding(horizontal = 16.dp)) {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    Text("Solicitudes Recientes", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Color(0xFF0F172A))
                    TextButton(onClick = onVerMisSolicitudes) { Text("Ver todas", color = Color(0xFF6366F1), fontSize = 13.sp) }
                }
                Spacer(modifier = Modifier.height(12.dp))

                when (val s = solicitudesState) {
                    is UiState.Cargando -> Box(Modifier.fillMaxWidth().height(60.dp), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(color = Color(0xFF6366F1), modifier = Modifier.size(24.dp))
                    }
                    is UiState.SinConexion -> SinConexionBanner(onReintentar = { vm.cargarDatos(context) })
                    is UiState.Error -> Text(s.mensaje, color = Color(0xFFDC2626), fontSize = 12.sp)
                    is UiState.Exito -> {
                        if (s.datos.isEmpty()) {
                            Text("Aun no tienes solicitudes. ¡Crea una!", color = Color(0xFF64748B), fontSize = 13.sp)
                        } else {
                            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                s.datos.take(3).forEach { sol ->
                                    val (bgColor, textColor) = obtenerColoresEstado(sol.estado)
                                    Card(shape = RoundedCornerShape(12.dp), colors = CardDefaults.cardColors(containerColor = Color.White),
                                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp), modifier = Modifier.fillMaxWidth()) {
                                        Row(modifier = Modifier.fillMaxWidth().padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                                            Column(modifier = Modifier.weight(1f)) {
                                                Text(sol.nombreCategoria, fontWeight = FontWeight.Bold, fontSize = 14.sp, color = Color(0xFF0F172A))
                                                Text(sol.descripcion, maxLines = 1, overflow = TextOverflow.Ellipsis, fontSize = 12.sp, color = Color(0xFF64748B))
                                            }
                                            Surface(shape = RoundedCornerShape(8.dp), color = bgColor) {
                                                Text(sol.estado, color = textColor, fontSize = 10.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp))
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
            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

/** Banner reutilizable para estado sin conexion */
@Composable
fun SinConexionBanner(onReintentar: () -> Unit) {
    Surface(shape = RoundedCornerShape(12.dp), color = Color(0xFFFEF2F2), modifier = Modifier.fillMaxWidth()) {
        Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("📵", fontSize = 18.sp)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Sin conexion a internet", fontSize = 13.sp, color = Color(0xFFB91C1C), fontWeight = FontWeight.Medium)
            }
            TextButton(onClick = onReintentar) { Text("Reintentar", color = Color(0xFF6366F1), fontSize = 12.sp) }
        }
    }
}

/** Utilitario de colores por estado de solicitud */
fun obtenerColoresEstado(estado: String): Pair<Color, Color> = when (estado.uppercase()) {
    "ABIERTO" -> Color(0xFFEEF2FF) to Color(0xFF4F46E5)
    "EN_PROGRESO" -> Color(0xFFFEF3C7) to Color(0xFFD97706)
    "FINALIZADO" -> Color(0xFFD1FAE5) to Color(0xFF059669)
    "CANCELADO" -> Color(0xFFFEE2E2) to Color(0xFFDC2626)
    "ASIGNADO", "ACEPTADO" -> Color(0xFFDBEAFE) to Color(0xFF2563EB)
    else -> Color(0xFFF1F5F9) to Color(0xFF64748B)
}
