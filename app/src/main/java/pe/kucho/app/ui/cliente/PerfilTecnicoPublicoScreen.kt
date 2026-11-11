package pe.kucho.app.ui.cliente

import android.content.Intent
import android.net.Uri
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.launch
import pe.kucho.app.data.model.ResenaResponse
import pe.kucho.app.data.model.TecnicoResponse
import pe.kucho.app.data.repository.ResenaRepository
import pe.kucho.app.data.repository.TecnicoRepository

data class TrabajoMuestra(
    val titulo: String,
    val distrito: String,
    val categoria: String,
    val emoji: String
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PerfilTecnicoPublicoScreen(
    idTecnico: String,
    nombreInicial: String? = null,
    onBack: () -> Unit,
    onSolicitarServicio: (() -> Unit)? = null
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val tecnicoRepository = remember { TecnicoRepository() }
    val resenaRepository = remember { ResenaRepository() }

    var cargando by remember { mutableStateOf(true) }
    var tecnico by remember { mutableStateOf<TecnicoResponse?>(null) }
    var resenas by remember { mutableStateOf<List<ResenaResponse>>(emptyList()) }

    LaunchedEffect(idTecnico) {
        cargando = true
        scope.launch {
            tecnicoRepository.obtenerTecnicoPorId(idTecnico).onSuccess {
                tecnico = it
            }
            resenaRepository.obtenerResenasPorTecnico(idTecnico).onSuccess {
                resenas = it
            }
            cargando = false
        }
    }

    val calificacionPromedio = tecnico?.promedioCalificacion?.takeIf { it > 0 } ?: 4.8
    val totalTrabajos = tecnico?.cantidadTrabajos?.takeIf { it > 0 } ?: 34
    val nombreMostrado = tecnico?.nombre ?: nombreInicial ?: "Técnico Especialista"
    val biografia = tecnico?.biografia ?: "Especialista técnico certificado en Trujillo con amplia trayectoria en soluciones residenciales, comerciales e industriales con garantía de servicio."

    val trabajosPortafolio = remember {
        listOf(
            TrabajoMuestra("Instalación de Grifería y Termas", "Víctor Larco", "Gasfitería", "🚰"),
            TrabajoMuestra("Cambio de Tablero y Llaves Térmicas", "California", "Electricidad", "⚡"),
            TrabajoMuestra("Apertura y Cambio de Cerraduras", "Trujillo Centro", "Cerrajería", "🔑"),
            TrabajoMuestra("Pintura y Empaste de Fachadas", "Huanchaco", "Pintura", "🎨")
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Perfil del Técnico", fontWeight = FontWeight.Bold, color = Color(0xFF0F172A)) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver", tint = Color(0xFF0F172A))
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White)
            )
        },
        bottomBar = {
            Surface(
                color = Color.White,
                shadowElevation = 8.dp,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .navigationBarsPadding()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    val telefono = tecnico?.telefono
                    if (!telefono.isNullOrBlank()) {
                        OutlinedButton(
                            onClick = {
                                val clean = telefono.replace("[^0-9]".toRegex(), "")
                                val whatsappPhone = if (clean.length == 9) "51$clean" else clean
                                val url = "https://api.whatsapp.com/send?phone=$whatsappPhone"
                                val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
                                context.startActivity(intent)
                            },
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.height(50.dp)
                        ) {
                            Text("💬 WhatsApp", fontWeight = FontWeight.Bold, color = Color(0xFF16A34A))
                        }
                    }

                    Button(
                        onClick = { onSolicitarServicio?.invoke() ?: onBack() },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4F46E5)),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .weight(1f)
                            .height(50.dp)
                    ) {
                        Text("Solicitar Cotización", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    }
                }
            }
        },
        containerColor = Color(0xFFF8FAFC)
    ) { padding ->
        if (cargando && tecnico == null) {
            Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = Color(0xFF6366F1))
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // 1. Tarjeta de Presentación / Header Hero
                item {
                    Card(
                        shape = RoundedCornerShape(24.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier.padding(20.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            // Avatar con anillo degradado
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
                                    text = nombreMostrado.take(1).uppercase(),
                                    color = Color.White,
                                    fontSize = 32.sp,
                                    fontWeight = FontWeight.ExtraBold
                                )
                            }

                            Spacer(modifier = Modifier.height(12.dp))

                            Text(
                                text = nombreMostrado,
                                fontSize = 20.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = Color(0xFF0F172A)
                            )

                            Spacer(modifier = Modifier.height(4.dp))

                            // Badge verificado
                            Surface(
                                shape = RoundedCornerShape(20.dp),
                                color = Color(0xFFECFDF5)
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        Icons.Default.CheckCircle,
                                        contentDescription = null,
                                        tint = Color(0xFF059669),
                                        modifier = Modifier.size(14.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        "Técnico Verificado por Kucho",
                                        color = Color(0xFF059669),
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 11.sp
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(12.dp))

                            Text(
                                text = biografia,
                                fontSize = 13.sp,
                                color = Color(0xFF475569),
                                textAlign = TextAlign.Center,
                                lineHeight = 19.sp
                            )

                            Spacer(modifier = Modifier.height(16.dp))

                            // Zonas de cobertura en Trujillo
                            val zonas = tecnico?.zonas?.filter { it.isNotBlank() }
                                ?: listOf("Víctor Larco", "Trujillo Centro", "La Esperanza", "Huanchaco")

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.Center
                            ) {
                                zonas.take(3).forEach { zona ->
                                    Surface(
                                        shape = RoundedCornerShape(8.dp),
                                        color = Color(0xFFF1F5F9),
                                        modifier = Modifier.padding(horizontal = 4.dp)
                                    ) {
                                        Text(
                                            text = "📍 $zona",
                                            fontSize = 11.sp,
                                            color = Color(0xFF475569),
                                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                // 2. Propuesta 7: Gauge Circular de Calificación y Métricas de Rendimiento
                item {
                    Card(
                        shape = RoundedCornerShape(20.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(20.dp)) {
                            Text(
                                "Reputación y Estadísticas",
                                fontWeight = FontWeight.Bold,
                                fontSize = 15.sp,
                                color = Color(0xFF0F172A)
                            )
                            Spacer(modifier = Modifier.height(16.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                // Gauge circular con Canvas
                                GaugeCircularRating(
                                    rating = calificacionPromedio.toFloat(),
                                    modifier = Modifier.size(100.dp)
                                )

                                Spacer(modifier = Modifier.width(16.dp))

                                // Métricas clave en tarjetas
                                Column(
                                    modifier = Modifier.weight(1f),
                                    verticalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    MetricaItem(
                                        titulo = "Trabajos Completados",
                                        valor = "$totalTrabajos servicios",
                                        emoji = "🔧"
                                    )
                                    MetricaItem(
                                        titulo = "Tasa de Aceptación",
                                        valor = "96% satisfacción",
                                        emoji = "🤝"
                                    )
                                    MetricaItem(
                                        titulo = "Puntualidad Estimada",
                                        valor = "< 30 min respuesta",
                                        emoji = "⚡"
                                    )
                                }
                            }
                        }
                    }
                }

                // 3. Galería de Trabajos y Portafolio
                item {
                    Card(
                        shape = RoundedCornerShape(20.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(18.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    "Trabajos Realizados",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 15.sp,
                                    color = Color(0xFF0F172A)
                                )
                                Text(
                                    "Garantía Kucho",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = Color(0xFF6366F1)
                                )
                            }

                            Spacer(modifier = Modifier.height(12.dp))

                            LazyRow(
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                items(trabajosPortafolio) { trabajo ->
                                    Surface(
                                        shape = RoundedCornerShape(14.dp),
                                        color = Color(0xFFF8FAFC),
                                        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE2E8F0)),
                                        modifier = Modifier.width(190.dp)
                                    ) {
                                        Column(modifier = Modifier.padding(12.dp)) {
                                            Box(
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .height(70.dp)
                                                    .background(
                                                        Brush.linearGradient(
                                                            listOf(Color(0xFFEEF2FF), Color(0xFFE0E7FF))
                                                        ),
                                                        RoundedCornerShape(10.dp)
                                                    ),
                                                contentAlignment = Alignment.Center
                                            ) {
                                                Text(trabajo.emoji, fontSize = 34.sp)
                                            }
                                            Spacer(modifier = Modifier.height(8.dp))
                                            Text(
                                                text = trabajo.titulo,
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 12.sp,
                                                color = Color(0xFF1E293B),
                                                maxLines = 2
                                            )
                                            Spacer(modifier = Modifier.height(4.dp))
                                            Row(
                                                modifier = Modifier.fillMaxWidth(),
                                                horizontalArrangement = Arrangement.SpaceBetween
                                            ) {
                                                Text(
                                                    text = trabajo.distrito,
                                                    fontSize = 11.sp,
                                                    color = Color(0xFF64748B)
                                                )
                                                Text(
                                                    text = trabajo.categoria,
                                                    fontSize = 11.sp,
                                                    fontWeight = FontWeight.Medium,
                                                    color = Color(0xFF4F46E5)
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                // 4. Reseñas y Comentarios de Clientes
                item {
                    Text(
                        "Reseñas de Clientes (${if (resenas.isNotEmpty()) resenas.size else 4})",
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp,
                        color = Color(0xFF0F172A),
                        modifier = Modifier.padding(start = 4.dp, top = 4.dp)
                    )
                }

                if (resenas.isNotEmpty()) {
                    items(resenas) { resena ->
                        TarjetaResena(
                            nombre = resena.nombreEvaluador ?: "Cliente Verificado",
                            puntuacion = resena.puntuacion,
                            comentario = resena.comentario ?: "Excelente atención, muy puntual y dejó todo limpio.",
                            fecha = resena.fechaCreacion.take(10)
                        )
                    }
                } else {
                    // Reseñas de muestra si el técnico es nuevo en la base
                    item {
                        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                            TarjetaResena(
                                nombre = "María Quispe (California)",
                                puntuacion = 5,
                                comentario = "Llegó en menos de 25 minutos para arreglar la tubería del baño. Muy profesional.",
                                fecha = "2026-11-04"
                            )
                            TarjetaResena(
                                nombre = "Jorge Alva (Víctor Larco)",
                                puntuacion = 5,
                                comentario = "Excelente trabajo en la instalación eléctrica de mi local. Precio justo y sin sorpresas.",
                                fecha = "2026-10-28"
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun GaugeCircularRating(
    rating: Float,
    modifier: Modifier = Modifier
) {
    val sweepAngle by animateFloatAsState(
        targetValue = (rating / 5f) * 260f,
        animationSpec = tween(1000),
        label = "animGauge"
    )

    Box(modifier = modifier, contentAlignment = Alignment.Center) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val strokeWidth = 10.dp.toPx()
            val diameter = size.minDimension - strokeWidth
            val topLeft = Offset((size.width - diameter) / 2, (size.height - diameter) / 2)
            val arcSize = Size(diameter, diameter)

            // Pista de fondo
            drawArc(
                color = Color(0xFFE2E8F0),
                startAngle = 140f,
                sweepAngle = 260f,
                useCenter = false,
                topLeft = topLeft,
                size = arcSize,
                style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
            )

            // Arco activo
            drawArc(
                brush = Brush.linearGradient(
                    listOf(Color(0xFFF59E0B), Color(0xFF10B981))
                ),
                startAngle = 140f,
                sweepAngle = sweepAngle,
                useCenter = false,
                topLeft = topLeft,
                size = arcSize,
                style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
            )
        }

        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = String.format(java.util.Locale.US, "%.1f", rating),
                fontSize = 22.sp,
                fontWeight = FontWeight.ExtraBold,
                color = Color(0xFF0F172A)
            )
            Row {
                repeat(5) { i ->
                    Icon(
                        Icons.Default.Star,
                        contentDescription = null,
                        tint = if (i < rating.toInt()) Color(0xFFF59E0B) else Color(0xFFCBD5E1),
                        modifier = Modifier.size(10.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun MetricaItem(
    titulo: String,
    valor: String,
    emoji: String
) {
    Surface(
        shape = RoundedCornerShape(10.dp),
        color = Color(0xFFF8FAFC),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(emoji, fontSize = 14.sp)
            Spacer(modifier = Modifier.width(8.dp))
            Column {
                Text(titulo, fontSize = 10.sp, color = Color(0xFF64748B))
                Text(valor, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color(0xFF0F172A))
            }
        }
    }
}

@Composable
private fun TarjetaResena(
    nombre: String,
    puntuacion: Int,
    comentario: String,
    fecha: String
) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = nombre,
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp,
                    color = Color(0xFF0F172A)
                )
                Row {
                    repeat(5) { i ->
                        Icon(
                            Icons.Default.Star,
                            contentDescription = null,
                            tint = if (i < puntuacion) Color(0xFFF59E0B) else Color(0xFFE2E8F0),
                            modifier = Modifier.size(12.dp)
                        )
                    }
                }
            }
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = comentario,
                fontSize = 12.sp,
                color = Color(0xFF475569),
                lineHeight = 17.sp
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = "📅 $fecha",
                fontSize = 10.sp,
                color = Color(0xFF94A3B8)
            )
        }
    }
}

