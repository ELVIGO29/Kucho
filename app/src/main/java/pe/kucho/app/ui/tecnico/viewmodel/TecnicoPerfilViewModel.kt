package pe.kucho.app.ui.tecnico.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import pe.kucho.app.data.model.ActualizarPerfilTecnicoRequest
import pe.kucho.app.data.model.RegistroDocumentoRequest
import pe.kucho.app.data.model.TecnicoResponse
import pe.kucho.app.data.model.UiState
import pe.kucho.app.data.network.NetworkChecker
import pe.kucho.app.data.repository.TecnicoRepository

/**
 * ViewModel para TecnicoPerfilScreen.
 * Gestiona carga, actualización de perfil y registro de documento.
 */
class TecnicoPerfilViewModel(
    private val tecnicoRepository: TecnicoRepository = TecnicoRepository()
) : ViewModel() {

    private val _perfilState = MutableStateFlow<UiState<TecnicoResponse>>(UiState.Cargando)
    val perfilState: StateFlow<UiState<TecnicoResponse>> = _perfilState.asStateFlow()

    private val _guardandoState = MutableStateFlow<UiState<TecnicoResponse>?>(null)
    val guardandoState: StateFlow<UiState<TecnicoResponse>?> = _guardandoState.asStateFlow()

    fun resetGuardandoState() { _guardandoState.value = null }

    fun cargarPerfil(context: Context) {
        if (!NetworkChecker.isConnected(context)) {
            _perfilState.value = UiState.SinConexion
            return
        }
        _perfilState.value = UiState.Cargando
        viewModelScope.launch {
            val result = tecnicoRepository.obtenerMiPerfil()
            _perfilState.value = result.fold(
                onSuccess = { UiState.Exito(it) },
                onFailure = { UiState.Error(it.message ?: "Error al cargar perfil") }
            )
        }
    }

    fun actualizarPerfil(context: Context, request: ActualizarPerfilTecnicoRequest) {
        if (!NetworkChecker.isConnected(context)) {
            _guardandoState.value = UiState.SinConexion
            return
        }
        _guardandoState.value = UiState.Cargando
        viewModelScope.launch {
            val result = tecnicoRepository.actualizarMiPerfil(request)
            _guardandoState.value = result.fold(
                onSuccess = { UiState.Exito(it) },
                onFailure = { UiState.Error(it.message ?: "Error al actualizar perfil") }
            )
        }
    }

    fun registrarDocumento(context: Context, request: RegistroDocumentoRequest) {
        if (!NetworkChecker.isConnected(context)) {
            _guardandoState.value = UiState.SinConexion
            return
        }
        _guardandoState.value = UiState.Cargando
        viewModelScope.launch {
            val result = tecnicoRepository.registrarDocumento(request)
            _guardandoState.value = result.fold(
                onSuccess = { UiState.Exito(it) },
                onFailure = { UiState.Error(it.message ?: "Error al registrar documento") }
            )
        }
    }
}
