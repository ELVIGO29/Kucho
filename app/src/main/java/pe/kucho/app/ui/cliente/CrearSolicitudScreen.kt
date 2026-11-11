package pe.kucho.app.ui.cliente

import android.Manifest
import android.content.Intent
import android.net.Uri
import android.provider.Settings
import android.widget.Toast
import android.app.DatePickerDialog
import java.util.Calendar
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import kotlinx.coroutines.launch
import pe.kucho.app.data.location.LocationClient
import pe.kucho.app.data.model.CrearSolicitudRequest
import pe.kucho.app.data.model.UiState
import pe.kucho.app.ui.cliente.viewmodel.CrearSolicitudViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CrearSolicitudScreen(
    categoriaIdInicial: String? = null,
    onBack: () -> Unit,
    vm: CrearSolicitudViewModel = viewModel()
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val locationClient = remember { LocationClient(context) }

    var descripcion by remember { mutableStateOf("") }
    var distrito by remember { mutableStateOf("") }
    var referencia by remember { mutableStateOf("") }
    var precioSugerido by remember { mutableStateOf("") }
    var fechaProgramada by remember { mutableStateOf("") }
    var menuExpanded by remember { mutableStateOf(false) }

    // Estado GPS
    var latitud by remember { mutableStateOf<Double?>(null) }
    var longitud by remember { mutableStateOf<Double?>(null) }
    var direccionDetectada by remember { mutableStateOf("") }
    var buscandoUbicacion by remember { mutableStateOf(false) }
    var mostrarDialogPermisos by remember { mutableStateOf(false) }

    val categoriasState by vm.categoriasState.collectAsState()
    val crearState by vm.crearState.collectAsState()

    var categoriaSeleccionadaId by remember { mutableStateOf(categoriaIdInicial) }

    LaunchedEffect(Unit) { vm.cargarCategorias(context) }

    LaunchedEffect(categoriasState) {
        if (categoriasState is UiState.Exito) {
            val lista = (categoriasState as UiState.Exito).datos
            if (categoriaSeleccionadaId == null) categoriaSeleccionadaId = lista.firstOrNull()?.idCategoria
        }
    }

    LaunchedEffect(crearState) {
        when (val s = crearState) {
            is UiState.Exito -> {
                Toast.makeText(context, "Solicitud creada exitosamente!", Toast.LENGTH_SHORT).show()
                vm.resetCrearState(); onBack()
            }
            is UiState.Error -> {
                Toast.makeText(context, s.mensaje, Toast.LENGTH_SHORT).show()
                vm.resetCrearState()
            }
            is UiState.SinConexion -> {
                Toast.makeText(context, "Sin conexion a internet.", Toast.LENGTH_SHORT).show()
                vm.resetCrearState()
            }
            else -> {}
        }
    }

    // Launcher de permisos de ubicacion
    val permisosLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permisos ->
        val concedido = permisos[Manifest.permission.ACCESS_FINE_LOCATION] == true ||
                        permisos[Manifest.permission.ACCESS_COARSE_LOCATION] == true
        if (concedido) {
            buscandoUbicacion = true
            scope.launch {
                val coords = locationClient.obtenerUbicacionActual()
                buscandoUbicacion = false
                if (coords != null) {
                    latitud = coords.first
                    longitud = coords.second
                    direccionDetectada = locationClient.geocodificarInversa(coords.first, coords.second)
                    val distritoDetectado = locationClient.obtenerDistrito(coords.first, coords.second)
                    if (!distritoDetectado.isNullOrBlank() && distrito.isBlank()) {
                        distrito = distritoDetectado
                    }
                    if (referencia.isBlank()) referencia = direccionDetectada
                } else {
                    Toast.makeText(context, "No se pudo obtener la ubicacion.", Toast.LENGTH_SHORT).show()
                }
            }
        } else {
            mostrarDialogPermisos = true
        }
    }

    val categorias = if (categoriasState is UiState.Exito) (categoriasState as UiState.Exito).datos else emptyList()
    val categoriaSeleccionada = categorias.find { it.idCategoria == categoriaSeleccionadaId }
    val creando = crearState is UiState.Cargando

    // Dialog permiso denegado
    if (mostrarDialogPermisos) {
        AlertDialog(
            onDismissRequest = { mostrarDialogPermisos = false },
            title = { Text("Permiso de Ubicacion", fontWeight = FontWeight.Bold) },
            text = { Text("Para detectar tu ubicacion, activa el permiso desde los ajustes de la app.") },
            confirmButton = {
                TextButton(onClick = {
                    mostrarDialogPermisos = false
                    val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                        data = Uri.fromParts("package", context.packageName, null)
                    }
                    context.startActivity(intent)
                }) { Text("Abrir Ajustes", color = Color(0xFF6366F1), fontWeight = FontWeight.Bold) }
            },
            dismissButton = {
                TextButton(onClick = { mostrarDialogPermisos = false }) {
                    Text("Cancelar", color = Color(0xFF64748B))
                }
            },
            shape = RoundedCornerShape(18.dp)
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Nueva Solicitud", fontWeight = FontWeight.Bold, color = Color(0xFF0F172A)) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver", tint = Color(0xFF0F172A))
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White)
            )
        },
        containerColor = Color(0xFFF8FAFC)
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Selector de categoria
            when (categoriasState) {
                is UiState.Cargando -> Box(
                    Modifier.fillMaxWidth().height(56.dp),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = Color(0xFF6366F1), modifier = Modifier.size(24.dp))
                }
                is UiState.SinConexion -> SinConexionBanner(onReintentar = { vm.cargarCategorias(context) })
                else -> {
                    ExposedDropdownMenuBox(expanded = menuExpanded, onExpandedChange = { menuExpanded = it }) {
                        OutlinedTextField(
                            value = categoriaSeleccionada?.nombre ?: "Selecciona una categoria",
                            onValueChange = {}, readOnly = true,
                            label = { Text("Categoria de servicio") },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(menuExpanded) },
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.fillMaxWidth().menuAnchor()
                        )
                        ExposedDropdownMenu(expanded = menuExpanded, onDismissRequest = { menuExpanded = false }) {
                            categorias.forEach { cat ->
                                DropdownMenuItem(
                                    text = { Text(cat.nombre) },
                                    onClick = { categoriaSeleccionadaId = cat.idCategoria; menuExpanded = false }
                                )
                            }
                        }
                    }
                }
            }

            // Descripcion
            OutlinedTextField(
                value = descripcion, onValueChange = { descripcion = it },
                label = { Text("Descripcion del problema") }, minLines = 3, maxLines = 5,
                shape = RoundedCornerShape(12.dp), modifier = Modifier.fillMaxWidth()
            )

            // Seccion GPS
            Surface(
                shape = RoundedCornerShape(14.dp),
                color = Color(0xFFEEF2FF),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(
                        "Ubicacion del servicio",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color(0xFF4F46E5)
                    )
                    Button(
                        onClick = {
                            permisosLauncher.launch(arrayOf(
                                Manifest.permission.ACCESS_FINE_LOCATION,
                                Manifest.permission.ACCESS_COARSE_LOCATION
                            ))
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF6366F1)),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.fillMaxWidth(),
                        enabled = !buscandoUbicacion
                    ) {
                        if (buscandoUbicacion) {
                            CircularProgressIndicator(
                                color = Color.White,
                                modifier = Modifier.size(18.dp),
                                strokeWidth = 2.dp
                            )
                            Spacer(Modifier.width(8.dp))
                            Text("Detectando ubicacion...")
                        } else {
                            Icon(Icons.Default.LocationOn, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(Modifier.width(8.dp))
                            Text(if (latitud != null) "Ubicacion detectada - Actualizar" else "Detectar mi ubicacion actual")
                        }
                    }

                    AnimatedVisibility(visible = latitud != null, enter = fadeIn(), exit = fadeOut()) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(Color(0xFFDDD6FE), RoundedCornerShape(8.dp))
                                .padding(10.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text("\uD83D\uDCCD", fontSize = 16.sp)
                            Column {
                                Text(
                                    direccionDetectada,
                                    fontSize = 12.sp,
                                    color = Color(0xFF3730A3),
                                    fontWeight = FontWeight.Medium,
                                    lineHeight = 16.sp
                                )
                                if (latitud != null && longitud != null) {
                                    Text(
                                        "%.5f, %.5f".format(latitud, longitud),
                                        fontSize = 10.sp,
                                        color = Color(0xFF6366F1)
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // Distrito
            OutlinedTextField(
                value = distrito, onValueChange = { distrito = it },
                label = { Text("Distrito") },
                shape = RoundedCornerShape(12.dp), modifier = Modifier.fillMaxWidth()
            )

            // Referencia
            OutlinedTextField(
                value = referencia, onValueChange = { referencia = it },
                label = { Text("Referencia de direccion (opcional)") },
                shape = RoundedCornerShape(12.dp), modifier = Modifier.fillMaxWidth()
            )

            // Precio sugerido
            OutlinedTextField(
                value = precioSugerido, onValueChange = { precioSugerido = it },
                label = { Text("Precio sugerido en S/ (opcional)") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                shape = RoundedCornerShape(12.dp), modifier = Modifier.fillMaxWidth()
            )

            // Fecha programada con DatePicker
            val calendar = remember { Calendar.getInstance() }
            val datePickerDialog = remember {
                DatePickerDialog(
                    context,
                    { _, year, month, dayOfMonth ->
                        val m = (month + 1).toString().padStart(2, '0')
                        val d = dayOfMonth.toString().padStart(2, '0')
                        fechaProgramada = "$year-$m-$d"
                    },
                    calendar.get(Calendar.YEAR),
                    calendar.get(Calendar.MONTH),
                    calendar.get(Calendar.DAY_OF_MONTH)
                )
            }

            OutlinedTextField(
                value = fechaProgramada,
                onValueChange = { fechaProgramada = it },
                label = { Text("Fecha programada (opcional)") },
                trailingIcon = {
                    IconButton(onClick = { datePickerDialog.show() }) {
                        Icon(Icons.Default.DateRange, contentDescription = "Seleccionar fecha")
                    }
                },
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth()
            )

            // Boton publicar
            Button(
                onClick = {
                    val idCat = categoriaSeleccionadaId ?: return@Button
                    val fechaFormateada = fechaProgramada.trim().ifBlank { null }?.let { raw ->
                        val clean = raw.replace("/", "-")
                        if (clean.matches(Regex("""^\d{4}-\d{2}-\d{2}$"""))) "${clean}T00:00:00-05:00" else clean
                    }
                    vm.crearSolicitud(
                        context, CrearSolicitudRequest(
                            idCategoria = idCat,
                            descripcion = descripcion,
                            distrito = distrito,
                            referenciaDireccion = referencia.ifBlank { null },
                            precioPactado = precioSugerido.toDoubleOrNull(),
                            fechaProgramada = fechaFormateada,
                            latitud = latitud,
                            longitud = longitud
                        )
                    )
                },
                enabled = !creando && descripcion.isNotBlank() && distrito.isNotBlank() && categoriaSeleccionadaId != null,
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF6366F1)),
                modifier = Modifier.fillMaxWidth().height(52.dp)
            ) {
                if (creando) CircularProgressIndicator(color = Color.White, modifier = Modifier.size(22.dp))
                else Text("Publicar Solicitud", fontSize = 16.sp, fontWeight = FontWeight.Bold)
            }

            Spacer(modifier = Modifier.height(8.dp))
        }
    }
}

