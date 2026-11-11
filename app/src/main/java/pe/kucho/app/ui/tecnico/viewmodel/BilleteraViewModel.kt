package pe.kucho.app.ui.tecnico.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import pe.kucho.app.data.model.BilleteraResponse
import pe.kucho.app.data.model.UiState
import pe.kucho.app.data.network.NetworkChecker
import pe.kucho.app.data.repository.BilleteraRepository

/**
 * ViewModel para BilleteraScreen.
 * Gestiona la carga del saldo y recarga de billetera del técnico.
 */
class BilleteraViewModel(
    private val billeteraRepository: BilleteraRepository = BilleteraRepository()
) : ViewModel() {

    private val _billeteraState = MutableStateFlow<UiState<BilleteraResponse>>(UiState.Cargando)
    val billeteraState: StateFlow<UiState<BilleteraResponse>> = _billeteraState.asStateFlow()

    private val _recargarState = MutableStateFlow<UiState<BilleteraResponse>?>(null)
    val recargarState: StateFlow<UiState<BilleteraResponse>?> = _recargarState.asStateFlow()

    fun resetRecargarState() { _recargarState.value = null }

    fun cargarBilletera(context: Context) {
        if (!NetworkChecker.isConnected(context)) {
            _billeteraState.value = UiState.SinConexion
            return
        }
        _billeteraState.value = UiState.Cargando
        viewModelScope.launch {
            val result = billeteraRepository.obtenerMiBilletera()
            _billeteraState.value = result.fold(
                onSuccess = { UiState.Exito(it) },
                onFailure = { UiState.Error(it.message ?: "Error al consultar billetera") }
            )
        }
    }

    fun recargarSaldo(context: Context, monto: Double) {
        if (!NetworkChecker.isConnected(context)) {
            _recargarState.value = UiState.SinConexion
            return
        }
        _recargarState.value = UiState.Cargando
        viewModelScope.launch {
            val result = billeteraRepository.recargarSaldo(monto)
            _recargarState.value = result.fold(
                onSuccess = { UiState.Exito(it) },
                onFailure = { UiState.Error(it.message ?: "Error al recargar billetera") }
            )
        }
    }
}
