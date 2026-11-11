package pe.kucho.app.ui.auth.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import pe.kucho.app.data.model.AuthResponse
import pe.kucho.app.data.model.UiState
import pe.kucho.app.data.network.NetworkChecker
import pe.kucho.app.data.repository.AuthRepository

/**
 * ViewModel para LoginScreen.
 * Expone UiState<AuthResponse> mediante StateFlow para que la UI observe
 * los cambios de forma reactiva sin manejar lógica de red directamente.
 *
 * Usa viewModelScope para que las corrutinas se cancelen automáticamente
 * si la vista se destruye (ciclo de vida seguro).
 */
class LoginViewModel(
    private val authRepository: AuthRepository = AuthRepository()
) : ViewModel() {

    private val _otpState = MutableStateFlow<UiState<Unit>>(UiState.Cargando.also { /* no emitir al inicio */ })
    val otpState: StateFlow<UiState<Unit>> get() = _otpState.asStateFlow()

    private val _loginState = MutableStateFlow<UiState<AuthResponse>?>(null)
    val loginState: StateFlow<UiState<AuthResponse>?> = _loginState.asStateFlow()

    /** Reinicia el estado para evitar que la UI reaccione a estados anteriores. */
    fun resetOtpState() { _otpState.value = UiState.Cargando }
    fun resetLoginState() { _loginState.value = null }

    /**
     * Paso 1: solicitar OTP por SMS.
     * Verifica conectividad antes de lanzar la petición.
     */
    fun solicitarOtp(context: Context, telefono: String) {
        if (!NetworkChecker.isConnected(context)) {
            _otpState.value = UiState.SinConexion
            return
        }
        _otpState.value = UiState.Cargando
        viewModelScope.launch {
            val result = authRepository.solicitarOtp(telefono)
            _otpState.value = result.fold(
                onSuccess = { UiState.Exito(Unit) },
                onFailure = { UiState.Error(it.message ?: "Error al solicitar código") }
            )
        }
    }

    /**
     * Paso 2: verificar OTP y obtener token de sesión.
     * Verifica conectividad antes de lanzar la petición.
     */
    fun verificarOtp(context: Context, telefono: String, codigo: String) {
        if (!NetworkChecker.isConnected(context)) {
            _loginState.value = UiState.SinConexion
            return
        }
        _loginState.value = UiState.Cargando
        viewModelScope.launch {
            val result = authRepository.verificarOtp(telefono, codigo)
            _loginState.value = result.fold(
                onSuccess = { UiState.Exito(it) },
                onFailure = { UiState.Error(it.message ?: "Código incorrecto o expirado") }
            )
        }
    }
}
