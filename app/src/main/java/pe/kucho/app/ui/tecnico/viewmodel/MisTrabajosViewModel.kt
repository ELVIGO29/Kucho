package pe.kucho.app.ui.tecnico.viewmodel

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import pe.kucho.app.data.local.SessionDataStore
import pe.kucho.app.data.model.SolicitudResponse
import pe.kucho.app.data.model.UiState
import pe.kucho.app.data.network.NetworkChecker
import pe.kucho.app.data.repository.TecnicoRepository

/**
 * ViewModel para MisTrabajosScreen.
 * Lista los trabajos del técnico y permite actualizar su estado.
 *
 * IMPORTANTE: Toda operación de I/O (red, almacenamiento) se ejecuta en
 * viewModelScope con Dispatchers.IO para nunca bloquear el hilo principal.
 */
class MisTrabajosViewModel(
    private val tecnicoRepository: TecnicoRepository = TecnicoRepository()
) : ViewModel() {

    private val _trabajosState = MutableStateFlow<UiState<List<SolicitudResponse>>>(UiState.Cargando)
    val trabajosState: StateFlow<UiState<List<SolicitudResponse>>> = _trabajosState.asStateFlow()

    private val _accionState = MutableStateFlow<UiState<SolicitudResponse>?>(null)
    val accionState: StateFlow<UiState<SolicitudResponse>?> = _accionState.asStateFlow()

    // Flag para evitar cargas concurrentes en ON_RESUME rápidos
    private var cargando = false

    fun resetAccionState() { _accionState.value = null }

    fun cargarTrabajos(context: Context, page: Int = 0, size: Int = 20) {
        if (cargando) return   // Ignorar si ya hay una carga en curso
        cargando = true
        viewModelScope.launch {
            val hayRed = withContext(Dispatchers.IO) { NetworkChecker.isConnected(context) }
            if (!hayRed) {
                _trabajosState.value = UiState.SinConexion
                cargando = false
                return@launch
            }
            _trabajosState.value = UiState.Cargando
            val result = withContext(Dispatchers.IO) {
                tecnicoRepository.obtenerMisTrabajos(page, size)
            }
            _trabajosState.value = result.fold(
                onSuccess = { UiState.Exito(it) },
                onFailure = { UiState.Error(it.message ?: "Error al obtener trabajos") }
            )
            cargando = false
        }
    }

    fun actualizarEstado(context: Context, idSolicitud: String, nuevoEstado: String) {
        if (!NetworkChecker.isConnected(context)) {
            _accionState.value = UiState.SinConexion
            return
        }
        _accionState.value = UiState.Cargando
        viewModelScope.launch {
            val result = withContext(Dispatchers.IO) {
                tecnicoRepository.actualizarEstado(idSolicitud, nuevoEstado)
            }
            _accionState.value = result.fold(
                onSuccess = { UiState.Exito(it) },
                onFailure = { UiState.Error(it.message ?: "Error al actualizar estado") }
            )
        }
    }

    /**
     * Lanza la app de Acta Digital Flutter en un hilo de IO para obtener el token
     * sin bloquear el hilo principal (evita ANR).
     */
    fun lanzarActaDigital(
        context: Context,
        idSolicitud: String,
        nombreCliente: String,
        categoria: String,
        distrito: String,
        monto: Double,
        onFallback: () -> Unit
    ) {
        viewModelScope.launch {
            // Leer el token en Dispatchers.IO para no bloquear el hilo UI
            val token = withContext(Dispatchers.IO) {
                try { SessionDataStore(context).getTokenBlocking() ?: "" }
                catch (e: Exception) { "" }
            }

            // Volver al hilo principal para lanzar la actividad
            try {
                val intent = Intent("pe.kucho.acta.EMITIR_ACTA").apply {
                    setPackage("pe.kucho.acta")
                    putExtra("idSolicitud", idSolicitud)
                    putExtra("nombreCliente", nombreCliente)
                    putExtra("categoria", categoria)
                    putExtra("distrito", distrito)
                    putExtra("monto", monto)
                    putExtra("token", token)
                    if (context !is android.app.Activity) {
                        flags = Intent.FLAG_ACTIVITY_NEW_TASK
                    }
                }
                context.startActivity(intent)
            } catch (e: Exception) {
                // Fallback: Deep Link
                try {
                    val deepLink = Uri.parse(
                        "kucho://acta?idSolicitud=$idSolicitud" +
                        "&nombreCliente=${Uri.encode(nombreCliente)}" +
                        "&categoria=${Uri.encode(categoria)}" +
                        "&distrito=${Uri.encode(distrito)}" +
                        "&monto=$monto" +
                        "&token=${Uri.encode(token)}"
                    )
                    val dlIntent = Intent(Intent.ACTION_VIEW, deepLink).apply {
                        if (context !is android.app.Activity) {
                            flags = Intent.FLAG_ACTIVITY_NEW_TASK
                        }
                    }
                    context.startActivity(dlIntent)
                } catch (e2: Exception) {
                    // Última opción: actualización directa sin firma
                    onFallback()
                }
            }
        }
    }
}
