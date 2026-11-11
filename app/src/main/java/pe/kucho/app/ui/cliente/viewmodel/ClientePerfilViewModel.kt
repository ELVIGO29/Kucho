package pe.kucho.app.ui.cliente.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import pe.kucho.app.data.model.ActualizarClienteRequest
import pe.kucho.app.data.model.ClientePerfilResponse
import pe.kucho.app.data.model.UiState
import pe.kucho.app.data.network.NetworkChecker
import pe.kucho.app.data.repository.ClienteRepository

/**
 * ViewModel para ClientePerfilScreen.
 * Gestiona la carga y actualización del perfil del cliente autenticado.
 */
class ClientePerfilViewModel(
    private val clienteRepository: ClienteRepository = ClienteRepository()
) : ViewModel() {

    private val _perfilState = MutableStateFlow<UiState<ClientePerfilResponse>>(UiState.Cargando)
    val perfilState: StateFlow<UiState<ClientePerfilResponse>> = _perfilState.asStateFlow()

    private val _actualizarState = MutableStateFlow<UiState<ClientePerfilResponse>?>(null)
    val actualizarState: StateFlow<UiState<ClientePerfilResponse>?> = _actualizarState.asStateFlow()

    fun resetActualizarState() { _actualizarState.value = null }

    /** Carga el perfil del cliente. Emite SinConexion si no hay red. */
    fun cargarPerfil(context: Context) {
        if (!NetworkChecker.isConnected(context)) {
            _perfilState.value = UiState.SinConexion
            return
        }
        _perfilState.value = UiState.Cargando
        viewModelScope.launch {
            val result = clienteRepository.obtenerMiPerfil()
            _perfilState.value = result.fold(
                onSuccess = { UiState.Exito(it) },
                onFailure = { UiState.Error(it.message ?: "Error al cargar perfil") }
            )
        }
    }

    /** Actualiza el perfil. Valida conectividad antes de enviar. */
    fun actualizarPerfil(context: Context, request: ActualizarClienteRequest) {
        if (!NetworkChecker.isConnected(context)) {
            _actualizarState.value = UiState.SinConexion
            return
        }
        _actualizarState.value = UiState.Cargando
        viewModelScope.launch {
            val result = clienteRepository.actualizarMiPerfil(request)
            _actualizarState.value = result.fold(
                onSuccess = { UiState.Exito(it) },
                onFailure = { UiState.Error(it.message ?: "Error al actualizar perfil") }
            )
        }
    }
}
