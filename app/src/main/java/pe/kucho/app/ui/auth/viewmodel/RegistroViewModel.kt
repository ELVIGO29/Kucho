package pe.kucho.app.ui.auth.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import pe.kucho.app.data.model.AuthResponse
import pe.kucho.app.data.model.RegistroUsuarioRequest
import pe.kucho.app.data.model.UiState
import pe.kucho.app.data.network.NetworkChecker
import pe.kucho.app.data.repository.AuthRepository

/**
 * ViewModel para RegistroScreen.
 * Gestiona el registro de nuevo usuario (CLIENTE o TECNICO).
 */
class RegistroViewModel(
    private val authRepository: AuthRepository = AuthRepository()
) : ViewModel() {

    private val _state = MutableStateFlow<UiState<AuthResponse>?>(null)
    val state: StateFlow<UiState<AuthResponse>?> = _state.asStateFlow()

    fun resetState() { _state.value = null }

    /**
     * Registra un usuario nuevo verificando conectividad primero.
     * Si no hay red, emite UiState.SinConexion para que la UI muestre el aviso.
     */
    fun registrarUsuario(context: Context, request: RegistroUsuarioRequest) {
        if (!NetworkChecker.isConnected(context)) {
            _state.value = UiState.SinConexion
            return
        }
        _state.value = UiState.Cargando
        viewModelScope.launch {
            val result = authRepository.registrarUsuario(request)
            _state.value = result.fold(
                onSuccess = { UiState.Exito(it) },
                onFailure = { UiState.Error(it.message ?: "Error al crear cuenta") }
            )
        }
    }
}
