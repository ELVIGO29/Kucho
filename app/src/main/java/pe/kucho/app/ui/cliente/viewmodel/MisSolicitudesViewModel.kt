package pe.kucho.app.ui.cliente.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import pe.kucho.app.data.model.SolicitudResponse
import pe.kucho.app.data.model.UiState
import pe.kucho.app.data.network.NetworkChecker
import pe.kucho.app.data.repository.NegociacionRepository
import pe.kucho.app.data.repository.SolicitudRepository

/**
 * ViewModel para MisSolicitudesScreen.
 * Gestiona la lista de solicitudes del cliente y las acciones de cancelar/negociar.
 */
class MisSolicitudesViewModel(
    private val solicitudRepository: SolicitudRepository = SolicitudRepository(),
    private val negociacionRepository: NegociacionRepository = NegociacionRepository()
) : ViewModel() {

    private val _solicitudesState = MutableStateFlow<UiState<List<SolicitudResponse>>>(UiState.Cargando)
    val solicitudesState: StateFlow<UiState<List<SolicitudResponse>>> = _solicitudesState.asStateFlow()

    private val _accionState = MutableStateFlow<UiState<Unit>?>(null)
    val accionState: StateFlow<UiState<Unit>?> = _accionState.asStateFlow()

    fun resetAccionState() { _accionState.value = null }

    /** Carga las solicitudes del cliente. Si no hay red, usa caché SQLite. */
    fun cargarSolicitudes(context: Context, page: Int = 0, size: Int = 20) {
        _solicitudesState.value = UiState.Cargando
        viewModelScope.launch {
            val hayRed = NetworkChecker.isConnected(context)
            val result = solicitudRepository.obtenerMisSolicitudesCliente(page, size)
            _solicitudesState.value = result.fold(
                onSuccess = { UiState.Exito(it) },
                onFailure = {
                    if (!hayRed) UiState.SinConexion
                    else UiState.Error(it.message ?: "Error al cargar solicitudes")
                }
            )
        }
    }

    /** Cancela una solicitud verificando conectividad primero. */
    fun cancelarSolicitud(context: Context, idSolicitud: String) {
        if (!NetworkChecker.isConnected(context)) {
            _accionState.value = UiState.SinConexion
            return
        }
        _accionState.value = UiState.Cargando
        viewModelScope.launch {
            val result = solicitudRepository.cancelarSolicitud(idSolicitud)
            _accionState.value = result.fold(
                onSuccess = { UiState.EliminadoExitosamente },
                onFailure = { UiState.Error(it.message ?: "Error al cancelar solicitud") }
            )
        }
    }

    /** Acepta una negociación verificando conectividad primero. */
    fun aceptarNegociacion(context: Context, idNegociacion: String) {
        if (!NetworkChecker.isConnected(context)) {
            _accionState.value = UiState.SinConexion
            return
        }
        _accionState.value = UiState.Cargando
        viewModelScope.launch {
            val result = negociacionRepository.aceptarNegociacion(idNegociacion)
            _accionState.value = result.fold(
                onSuccess = { UiState.EliminadoExitosamente },
                onFailure = { UiState.Error(it.message ?: "Error al aceptar oferta") }
            )
        }
    }
}
