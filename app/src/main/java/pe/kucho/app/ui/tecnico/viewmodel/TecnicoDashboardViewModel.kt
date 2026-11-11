package pe.kucho.app.ui.tecnico.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import pe.kucho.app.data.model.TecnicoResponse
import pe.kucho.app.data.model.UiState
import pe.kucho.app.data.network.NetworkChecker
import pe.kucho.app.data.repository.TecnicoRepository

/**
 * ViewModel para TecnicoDashboardScreen.
 * Carga el perfil del técnico para mostrar saludo y estadísticas.
 */
class TecnicoDashboardViewModel(
    private val tecnicoRepository: TecnicoRepository = TecnicoRepository()
) : ViewModel() {

    private val _perfilState = MutableStateFlow<UiState<TecnicoResponse>>(UiState.Cargando)
    val perfilState: StateFlow<UiState<TecnicoResponse>> = _perfilState.asStateFlow()

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
}
