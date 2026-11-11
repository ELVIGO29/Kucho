package pe.kucho.app.ui.admin.dashboard

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch
import pe.kucho.app.data.local.SessionDataStore
import pe.kucho.app.data.model.*
import pe.kucho.app.data.network.RetrofitClient
import java.util.Locale

enum class AdminTab(val title: String, val icon: ImageVector) {
    RESUMEN("Resumen", Icons.Default.Home),
    VALIDAR("Validar Técnicos", Icons.Default.Person),
    CATEGORIAS("Categorías", Icons.Default.Menu),
    SOLICITUDES("Historial Solicitudes", Icons.Default.Info)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminDashboardScreen(
    onLogout: () -> Unit
) {
    val context = LocalContext.current
    val sessionDataStore = remember { SessionDataStore(context) }
    val scope = rememberCoroutineScope()

    var selectedTab by remember { mutableStateOf(AdminTab.RESUMEN) }
    var isLoading by remember { mutableStateOf(true) }

    // Estados de Datos
    var stats by remember { mutableStateOf<EstadisticasDashboard?>(null) }
    var tecnicos by remember { mutableStateOf<List<TecnicoResponse>>(emptyList()) }
    var categorias by remember { mutableStateOf<List<Categoria>>(emptyList()) }
    var solicitudes by remember { mutableStateOf<List<SolicitudResponse>>(emptyList()) }

    // Modal Crear/Editar Categoría
    var showCategoryDialog by remember { mutableStateOf(false) }
    var editingCategory by remember { mutableStateOf<Categoria?>(null) }
    var catNombre by remember { mutableStateOf("") }
    var catSlug by remember { mutableStateOf("") }
    var catIcono by remember { mutableStateOf("") }

    fun reloadAllData() {
        scope.launch {
            isLoading = true
            try {
                // 1. Obtener y configurar el token en RetrofitClient
                val savedToken = sessionDataStore.getToken().firstOrNull() ?: ""
                if (savedToken.isBlank()) {
                    Toast.makeText(context, "No hay sesión activa. Por favor inicia sesión.", Toast.LENGTH_SHORT).show()
                    isLoading = false
                    return@launch
                }

                RetrofitClient.init { savedToken }
                val api = RetrofitClient.apiService

                // 2. Cargar Estadísticas Admin
                val statsRes = api.obtenerEstadisticasDashboard()
                if (statsRes.isSuccessful) {
                    stats = statsRes.body()
                } else {
                    Toast.makeText(context, "Stats: Error ${statsRes.code()}", Toast.LENGTH_SHORT).show()
                }

                // 3. Cargar Técnicos
                val tecRes = api.obtenerTecnicosActivos()
                if (tecRes.isSuccessful) {
                    tecnicos = tecRes.body() ?: emptyList()
                }

                // 4. Cargar Categorías
                val catRes = api.listarCategorias()
                if (catRes.isSuccessful) {
                    categorias = catRes.body() ?: emptyList()
                }

                // 5. Cargar Solicitudes Admin
                val solRes = api.obtenerSolicitudesAdmin(page = 0, size = 100)
                if (solRes.isSuccessful) {
                    solicitudes = solRes.body()?.content ?: emptyList()
                } else {
                    Toast.makeText(context, "Solicitudes: Error ${solRes.code()}", Toast.LENGTH_SHORT).show()
                }

            } catch (e: Exception) {
                Toast.makeText(context, "Error: ${e.localizedMessage}", Toast.LENGTH_LONG).show()
            } finally {
                isLoading = false
            }
        }
    }

    LaunchedEffect(Unit) {
        reloadAllData()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("✨ Kucho", fontWeight = FontWeight.Bold, color = Color(0xFF6366F1), fontSize = 20.sp)
                        Spacer(modifier = Modifier.width(8.dp))
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = Color(0xFF6366F1).copy(alpha = 0.15f)
                        ) {
                            Text(
                                "ADMIN",
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                color = Color(0xFF6366F1),
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                },
                actions = {
                    IconButton(onClick = { reloadAllData() }) {
                        Icon(Icons.Default.Refresh, contentDescription = "Recargar")
                    }
                    IconButton(onClick = {
                        scope.launch {
                            sessionDataStore.cerrarSesion()
                            onLogout()
                        }
                    }) {
                        Icon(Icons.AutoMirrored.Filled.ExitToApp, contentDescription = "Cerrar sesión", tint = Color(0xFFEF4444))
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.surface)
            )
        },
        bottomBar = {
            NavigationBar(containerColor = MaterialTheme.colorScheme.surface) {
                AdminTab.entries.forEach { tab ->
                    NavigationBarItem(
                        icon = { Icon(tab.icon, contentDescription = tab.title) },
                        label = { Text(tab.title, maxLines = 1, overflow = TextOverflow.Ellipsis, fontSize = 11.sp) },
                        selected = selectedTab == tab,
                        onClick = { selectedTab = tab },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = Color(0xFF6366F1),
                            selectedTextColor = Color(0xFF6366F1),
                            indicatorColor = Color(0xFF6366F1).copy(alpha = 0.12f)
                        )
                    )
                }
            }
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(MaterialTheme.colorScheme.background)
        ) {
            if (isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.align(Alignment.Center),
                    color = Color(0xFF6366F1)
                )
            } else {
                when (selectedTab) {
                    AdminTab.RESUMEN -> AdminResumenTab(
                        stats = stats,
                        tecnicos = tecnicos.filter { it.dniVerificado != true },
                        solicitudes = solicitudes.take(5),
                        onVerificarTecnicoClick = { selectedTab = AdminTab.VALIDAR },
                        onVerSolicitudesClick = { selectedTab = AdminTab.SOLICITUDES }
                    )
                    AdminTab.VALIDAR -> AdminValidarTecnicosTab(
                        tecnicos = tecnicos,
                        onToggleVerificado = { id, nuevoEstado ->
                            scope.launch {
                                try {
                                    val token = sessionDataStore.getToken().firstOrNull() ?: ""
                                    RetrofitClient.init { token }

                                    val res = RetrofitClient.apiService.verificarTecnico(
                                        idTecnico = id,
                                        request = VerificarTecnicoRequest(verificado = nuevoEstado)
                                    )
                                    if (res.isSuccessful) {
                                        Toast.makeText(context, if (nuevoEstado) "✅ Técnico aprobado" else "🚫 Verificación revocada", Toast.LENGTH_SHORT).show()
                                        reloadAllData()
                                    } else {
                                        Toast.makeText(context, "Error: ${res.code()}", Toast.LENGTH_SHORT).show()
                                    }
                                } catch (e: Exception) {
                                    Toast.makeText(context, "Error: ${e.localizedMessage}", Toast.LENGTH_SHORT).show()
                                }
                            }
                        }
                    )
                    AdminTab.CATEGORIAS -> AdminCategoriasTab(
                        categorias = categorias,
                        onNuevaCategoria = {
                            editingCategory = null
                            catNombre = ""
                            catSlug = ""
                            catIcono = ""
                            showCategoryDialog = true
                        },
                        onEditarCategoria = { cat ->
                            editingCategory = cat
                            catNombre = cat.nombre
                            catSlug = cat.slug ?: ""
                            catIcono = cat.icono ?: ""
                            showCategoryDialog = true
                        },
                        onToggleEstado = { cat ->
                            scope.launch {
                                try {
                                    val token = sessionDataStore.getToken().firstOrNull() ?: ""
                                    RetrofitClient.init { token }

                                    val res = RetrofitClient.apiService.cambiarEstadoCategoria(
                                        idCategoria = cat.idCategoria,
                                        activo = !cat.activo
                                    )
                                    if (res.isSuccessful) {
                                        Toast.makeText(context, "Estado actualizado", Toast.LENGTH_SHORT).show()
                                        reloadAllData()
                                    } else {
                                        Toast.makeText(context, "Error: ${res.code()}", Toast.LENGTH_SHORT).show()
                                    }
                                } catch (e: Exception) {
                                    Toast.makeText(context, "Error: ${e.localizedMessage}", Toast.LENGTH_SHORT).show()
                                }
                            }
                        }
                    )
                    AdminTab.SOLICITUDES -> AdminSolicitudesTab(solicitudes = solicitudes)
                }
            }
        }
    }

    // Modal Crear/Editar Categoría
    if (showCategoryDialog) {
        Dialog(onDismissRequest = { showCategoryDialog = false }) {
            Surface(
                shape = RoundedCornerShape(20.dp),
                color = MaterialTheme.colorScheme.surface,
                modifier = Modifier.fillMaxWidth().padding(16.dp),
                tonalElevation = 6.dp
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Text(
                        if (editingCategory != null) "Editar Categoría" else "Nueva Categoría",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(16.dp))

                    OutlinedTextField(
                        value = catNombre,
                        onValueChange = {
                            catNombre = it
                            if (editingCategory == null) {
                                catSlug = it.lowercase(Locale.ROOT).replace(" ", "-").replace(Regex("[^a-z0-9-]"), "")
                            }
                        },
                        label = { Text("Nombre") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(10.dp))

                    OutlinedTextField(
                        value = catSlug,
                        onValueChange = { catSlug = it },
                        label = { Text("Slug (identificador)") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(10.dp))

                    OutlinedTextField(
                        value = catIcono,
                        onValueChange = { catIcono = it },
                        label = { Text("Icono / Emoji (ej. 🛠️, 🧹, ⚡)") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(20.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End
                    ) {
                        TextButton(onClick = { showCategoryDialog = false }) {
                            Text("Cancelar")
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Button(
                            onClick = {
                                if (catNombre.isBlank() || catSlug.isBlank()) {
                                    Toast.makeText(context, "Completa los campos obligatorios", Toast.LENGTH_SHORT).show()
                                    return@Button
                                }
                                scope.launch {
                                    try {
                                        val token = sessionDataStore.getToken().firstOrNull() ?: ""
                                        RetrofitClient.init { token }

                                        val req = CrearCategoriaRequest(
                                            nombre = catNombre.trim(),
                                            slug = catSlug.trim(),
                                            icono = catIcono.trim().ifBlank { null }
                                        )
                                        val res = if (editingCategory != null) {
                                            RetrofitClient.apiService.actualizarCategoria(editingCategory!!.idCategoria, req)
                                        } else {
                                            RetrofitClient.apiService.crearCategoria(req)
                                        }

                                        if (res.isSuccessful) {
                                            showCategoryDialog = false
                                            reloadAllData()
                                            Toast.makeText(context, "Categoría guardada", Toast.LENGTH_SHORT).show()
                                        } else {
                                            Toast.makeText(context, "Error: ${res.code()}", Toast.LENGTH_SHORT).show()
                                        }
                                    } catch (e: Exception) {
                                        Toast.makeText(context, "Error: ${e.localizedMessage}", Toast.LENGTH_SHORT).show()
                                    }
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF6366F1))
                        ) {
                            Text("Guardar")
                        }
                    }
                }
            }
        }
    }
}

// -------------------------------------------------------------
// PESTAÑA 1: RESUMEN (KPIs)
// -------------------------------------------------------------
@Composable
fun AdminResumenTab(
    stats: EstadisticasDashboard?,
    tecnicos: List<TecnicoResponse>,
    solicitudes: List<SolicitudResponse>,
    onVerificarTecnicoClick: () -> Unit,
    onVerSolicitudesClick: () -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = Color.Transparent),
                modifier = Modifier.fillMaxWidth()
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            Brush.horizontalGradient(
                                colors = listOf(Color(0xFF6366F1), Color(0xFF8B5CF6), Color(0xFFD946EF))
                            )
                        )
                        .padding(20.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text("Dashboard", color = Color.White, fontSize = 22.sp, fontWeight = FontWeight.Bold)
                            Text("Bienvenido al centro de control de Kucho", color = Color.White.copy(alpha = 0.85f), fontSize = 13.sp)
                        }
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = Color.White.copy(alpha = 0.2f)
                        ) {
                            Text("📅 Hoy", modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp), color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }

        // Fila 1 KPIs
        item {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                KpiCard(
                    modifier = Modifier.weight(1f),
                    title = "Técnicos activos",
                    value = "${stats?.totalTecnicosActivos ?: 0}",
                    trend = "Registrados",
                    trendColor = Color(0xFF10B981),
                    iconEmoji = "👨‍🔧",
                    bgColor = Color(0xFFEDE9FE)
                )
                KpiCard(
                    modifier = Modifier.weight(1f),
                    title = "Clientes registrados",
                    value = "${stats?.totalClientesRegistrados ?: 0}",
                    trend = "Usuarios en App",
                    trendColor = Color(0xFF3B82F6),
                    iconEmoji = "👥",
                    bgColor = Color(0xFFDBEAFE)
                )
            }
        }

        // Fila 2 KPIs
        item {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                KpiCard(
                    modifier = Modifier.weight(1f),
                    title = "Solicitudes activas",
                    value = "${stats?.solicitudesActivas ?: 0}",
                    trend = "En proceso",
                    trendColor = Color(0xFFF59E0B),
                    iconEmoji = "📋",
                    bgColor = Color(0xFFFEF3C7)
                )
                KpiCard(
                    modifier = Modifier.weight(1f),
                    title = "Solicitudes finalizadas",
                    value = "${stats?.solicitudesFinalizadas ?: 0}",
                    trend = "Completadas",
                    trendColor = Color(0xFF10B981),
                    iconEmoji = "✅",
                    bgColor = Color(0xFFD1FAE5)
                )
            }
        }

        // Fila 3 KPIs
        item {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                KpiCard(
                    modifier = Modifier.weight(1f),
                    title = "Calificación Prom.",
                    value = String.format(Locale.US, "%.1f ⭐", stats?.calificacionPromedio ?: 0.0),
                    trend = "Satisfacción",
                    trendColor = Color(0xFFF59E0B),
                    iconEmoji = "⭐",
                    bgColor = Color(0xFFFEF9C3)
                )
                KpiCard(
                    modifier = Modifier.weight(1f),
                    title = "Total Recaudado",
                    value = String.format(Locale.US, "S/ %.2f", stats?.totalRecaudado ?: 0.0),
                    trend = "Comisiones",
                    trendColor = Color(0xFF10B981),
                    iconEmoji = "💰",
                    bgColor = Color(0xFFDCFCE7)
                )
            }
        }

        // Card Rápido: Técnicos por Validar
        item {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Verificación de técnicos (Por Revisar)", fontWeight = FontWeight.Bold, fontSize = 15.sp)
                        TextButton(onClick = onVerificarTecnicoClick) {
                            Text("Ver todos", fontSize = 12.sp, color = Color(0xFF6366F1))
                        }
                    }
                    if (tecnicos.isEmpty()) {
                        Text("No hay técnicos pendientes de revisión.", color = Color.Gray, fontSize = 13.sp, modifier = Modifier.padding(vertical = 8.dp))
                    } else {
                        tecnicos.take(3).forEach { tec ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(38.dp)
                                        .background(Color(0xFF6366F1).copy(alpha = 0.15f), CircleShape),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(tec.nombre.take(1).uppercase(Locale.ROOT), color = Color(0xFF6366F1), fontWeight = FontWeight.Bold)
                                }
                                Spacer(modifier = Modifier.width(12.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(tec.nombre, fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
                                    Text(tec.biografia ?: "Registrado recientemente", color = Color.Gray, fontSize = 12.sp, maxLines = 1)
                                }
                                Button(
                                    onClick = onVerificarTecnicoClick,
                                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF10B981)),
                                    shape = RoundedCornerShape(8.dp),
                                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp)
                                ) {
                                    Text("Revisar", fontSize = 12.sp)
                                }
                            }
                        }
                    }
                }
            }
        }

        // Card Rápido: Últimas Solicitudes
        item {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Historial de solicitudes (Últimas)", fontWeight = FontWeight.Bold, fontSize = 15.sp)
                        TextButton(onClick = onVerSolicitudesClick) {
                            Text("Ver completo", fontSize = 12.sp, color = Color(0xFF6366F1))
                        }
                    }
                    if (solicitudes.isEmpty()) {
                        Text("No hay solicitudes registradas.", color = Color.Gray, fontSize = 13.sp, modifier = Modifier.padding(vertical = 8.dp))
                    } else {
                        solicitudes.forEach { sol ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text("#${sol.idSolicitud.take(6).uppercase(Locale.ROOT)} • ${sol.distrito}", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                    Text(sol.nombreCategoria, color = Color.Gray, fontSize = 12.sp)
                                }
                                StatusBadge(estado = sol.estado)
                            }
                            HorizontalDivider(color = Color.LightGray.copy(alpha = 0.3f))
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun KpiCard(
    modifier: Modifier = Modifier,
    title: String,
    value: String,
    trend: String,
    trendColor: Color,
    iconEmoji: String,
    bgColor: Color
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(title, color = Color.Gray, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                Spacer(modifier = Modifier.height(4.dp))
                Text(value, fontSize = 20.sp, fontWeight = FontWeight.ExtraBold)
                Spacer(modifier = Modifier.height(2.dp))
                Text(trend, color = trendColor, fontSize = 11.sp, fontWeight = FontWeight.Bold)
            }
            Box(
                modifier = Modifier
                    .size(42.dp)
                    .background(bgColor, RoundedCornerShape(12.dp)),
                contentAlignment = Alignment.Center
            ) {
                Text(iconEmoji, fontSize = 20.sp)
            }
        }
    }
}

// -------------------------------------------------------------
// PESTAÑA 2: VALIDAR TÉCNICOS
// -------------------------------------------------------------
@Composable
fun AdminValidarTecnicosTab(
    tecnicos: List<TecnicoResponse>,
    onToggleVerificado: (String, Boolean) -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Column {
                Text("Validar Técnicos", fontSize = 20.sp, fontWeight = FontWeight.Bold)
                Text("Aprueba o revoca a los técnicos registrados en el sistema.", color = Color.Gray, fontSize = 13.sp)
            }
        }

        if (tecnicos.isEmpty()) {
            item {
                Box(modifier = Modifier.fillMaxWidth().padding(40.dp), contentAlignment = Alignment.Center) {
                    Text("No hay técnicos registrados", color = Color.Gray)
                }
            }
        } else {
            items(tecnicos) { tec ->
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(44.dp)
                                    .background(Color(0xFF6366F1).copy(alpha = 0.15f), CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(tec.nombre.take(1).uppercase(Locale.ROOT), color = Color(0xFF6366F1), fontWeight = FontWeight.Bold, fontSize = 16.sp)
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(tec.nombre, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                                Text(tec.biografia ?: "Sin biografía", color = Color.Gray, fontSize = 12.sp, maxLines = 2)
                            }
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = if (tec.dniVerificado == true) Color(0xFF10B981).copy(alpha = 0.15f) else Color(0xFFF59E0B).copy(alpha = 0.15f)
                            ) {
                                Text(
                                    if (tec.dniVerificado == true) "VERIFICADO" else "PENDIENTE",
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                    color = if (tec.dniVerificado == true) Color(0xFF10B981) else Color(0xFFF59E0B),
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }

                        if (!tec.zonas.isNullOrEmpty()) {
                            Spacer(modifier = Modifier.height(8.dp))
                            Text("Zonas: ${tec.zonas.joinToString(", ")}", fontSize = 12.sp, color = Color.Gray)
                        }

                        Spacer(modifier = Modifier.height(12.dp))
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                            if (tec.dniVerificado != true) {
                                Button(
                                    onClick = { onToggleVerificado(tec.idUsuario, true) },
                                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF10B981)),
                                    shape = RoundedCornerShape(8.dp)
                                ) {
                                    Text("Aprobar")
                                }
                            } else {
                                OutlinedButton(
                                    onClick = { onToggleVerificado(tec.idUsuario, false) },
                                    colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFFEF4444)),
                                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFEF4444)),
                                    shape = RoundedCornerShape(8.dp)
                                ) {
                                    Text("Revocar")
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

// -------------------------------------------------------------
// PESTAÑA 3: CATÁLOGO DE CATEGORÍAS
// -------------------------------------------------------------
@Composable
fun AdminCategoriasTab(
    categorias: List<Categoria>,
    onNuevaCategoria: () -> Unit,
    onEditarCategoria: (Categoria) -> Unit,
    onToggleEstado: (Categoria) -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text("Catálogo de Categorías", fontSize = 20.sp, fontWeight = FontWeight.Bold)
                    Text("Gestiona especialidades y servicios", color = Color.Gray, fontSize = 13.sp)
                }
                Button(
                    onClick = onNuevaCategoria,
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF6366F1)),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Text("+ Nueva")
                }
            }
        }

        items(categorias) { cat ->
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(cat.icono ?: "🛠️", fontSize = 24.sp)
                    Spacer(modifier = Modifier.width(12.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(cat.nombre, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                        Text(cat.slug ?: "", color = Color.Gray, fontSize = 12.sp)
                    }
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = if (cat.activo) Color(0xFF10B981).copy(alpha = 0.15f) else Color(0xFFEF4444).copy(alpha = 0.15f)
                    ) {
                        Text(
                            if (cat.activo) "ACTIVO" else "INACTIVO",
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                            color = if (cat.activo) Color(0xFF10B981) else Color(0xFFEF4444),
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    IconButton(onClick = { onEditarCategoria(cat) }) {
                        Icon(Icons.Default.Edit, contentDescription = "Editar", tint = Color.Gray)
                    }
                    IconButton(onClick = { onToggleEstado(cat) }) {
                        Icon(
                            if (cat.activo) Icons.Default.Close else Icons.Default.Check,
                            contentDescription = "Cambiar estado",
                            tint = if (cat.activo) Color(0xFFEF4444) else Color(0xFF10B981)
                        )
                    }
                }
            }
        }
    }
}

// -------------------------------------------------------------
// PESTAÑA 4: HISTORIAL DE SOLICITUDES
// -------------------------------------------------------------
@Composable
fun AdminSolicitudesTab(solicitudes: List<SolicitudResponse>) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Column {
                Text("Historial de Solicitudes", fontSize = 20.sp, fontWeight = FontWeight.Bold)
                Text("Visualiza y audita todas las solicitudes registradas", color = Color.Gray, fontSize = 13.sp)
            }
        }

        if (solicitudes.isEmpty()) {
            item {
                Box(modifier = Modifier.fillMaxWidth().padding(40.dp), contentAlignment = Alignment.Center) {
                    Text("No hay solicitudes registradas", color = Color.Gray)
                }
            }
        } else {
            items(solicitudes) { sol ->
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("#${sol.idSolicitud.take(6).uppercase(Locale.ROOT)}", fontWeight = FontWeight.ExtraBold, fontSize = 15.sp, color = Color(0xFF6366F1))
                            StatusBadge(estado = sol.estado)
                        }

                        Spacer(modifier = Modifier.height(8.dp))
                        Text(sol.descripcion, fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
                        Text("📍 ${sol.distrito} • Categoría: ${sol.nombreCategoria}", color = Color.Gray, fontSize = 12.sp)

                        Spacer(modifier = Modifier.height(10.dp))
                        HorizontalDivider(color = Color.LightGray.copy(alpha = 0.3f))
                        Spacer(modifier = Modifier.height(8.dp))

                        // Cliente y Técnico
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Column {
                                Text("Cliente:", color = Color.Gray, fontSize = 11.sp)
                                Text(sol.nombreCliente ?: "N/A", fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                                Text(sol.telefonoCliente ?: "", color = Color.Gray, fontSize = 11.sp)
                            }
                            Column(horizontalAlignment = Alignment.End) {
                                Text("Técnico Asignado:", color = Color.Gray, fontSize = 11.sp)
                                Text(sol.nombreTecnico ?: "Sin asignar", fontWeight = FontWeight.SemiBold, fontSize = 13.sp, color = if (sol.nombreTecnico != null) Color(0xFF10B981) else Color.Gray)
                                if (sol.telefonoTecnico != null) {
                                    Text(sol.telefonoTecnico, color = Color.Gray, fontSize = 11.sp)
                                }
                            }
                        }

                        // Reseña si existe
                        if (sol.puntuacionResena != null) {
                            Spacer(modifier = Modifier.height(10.dp))
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = Color(0xFFFEF9C3),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(modifier = Modifier.padding(8.dp), verticalAlignment = Alignment.CenterVertically) {
                                    Text("⭐".repeat(sol.puntuacionResena), fontSize = 12.sp)
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        "\"${sol.comentarioResena ?: "Sin comentario"}\"",
                                        fontSize = 12.sp,
                                        fontStyle = androidx.compose.ui.text.font.FontStyle.Italic,
                                        color = Color(0xFF78350F)
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

@Composable
fun StatusBadge(estado: String) {
    val (bgColor, textColor) = when (estado.uppercase(Locale.ROOT)) {
        "ASIGNADO" -> Color(0xFFDBEAFE) to Color(0xFF1D4ED8)
        "EN_PROGRESO" -> Color(0xFFFEF3C7) to Color(0xFFB45309)
        "FINALIZADO" -> Color(0xFFD1FAE5) to Color(0xFF047857)
        "CANCELADO" -> Color(0xFFFEE2E2) to Color(0xFFB91C1C)
        else -> Color(0xFFF3F4F6) to Color(0xFF4B5563) // ABIERTO
    }

    Surface(shape = RoundedCornerShape(6.dp), color = bgColor) {
        Text(
            text = estado.uppercase(Locale.ROOT),
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
            color = textColor,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold
        )
    }
}
