package pe.kucho.app.ui.tecnico.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import pe.kucho.app.data.model.BilleteraResponse
import pe.kucho.app.data.model.NegociacionResponse
import pe.kucho.app.data.model.SolicitudResponse
import pe.kucho.app.data.model.TecnicoResponse
import pe.kucho.app.data.model.UiState
import pe.kucho.app.data.network.NetworkChecker
import pe.kucho.app.data.repository.BilleteraRepository
import pe.kucho.app.data.repository.NegociacionRepository
import pe.kucho.app.data.repository.SolicitudRepository
import pe.kucho.app.data.repository.TecnicoRepository

/**
 * ViewModel para BolsaTrabajosScreen.
 * Gestiona solicitudes disponibles, billetera, perfil técnico y negociaciones.
 */
class BolsaTrabajosViewModel(
    private val solicitudRepository: SolicitudRepository = SolicitudRepository(),
    private val tecnicoRepository: TecnicoRepository = TecnicoRepository(),
    private val negociacionRepository: NegociacionRepository = NegociacionRepository(),
    private val billeteraRepository: BilleteraRepository = BilleteraRepository()
) : ViewModel() {

    private val _solicitudesState = MutableStateFlow<UiState<List<SolicitudResponse>>>(UiState.Cargando)
    val solicitudesState: StateFlow<UiState<List<SolicitudResponse>>> = _solicitudesState.asStateFlow()

    private val _billeteraState = MutableStateFlow<UiState<BilleteraResponse>>(UiState.Cargando)
    val billeteraState: StateFlow<UiState<BilleteraResponse>> = _billeteraState.asStateFlow()

    private val _perfilState = MutableStateFlow<UiState<TecnicoResponse>>(UiState.Cargando)
    val perfilState: StateFlow<UiState<TecnicoResponse>> = _perfilState.asStateFlow()

    private val _accionState = MutableStateFlow<UiState<NegociacionResponse>?>(null)
    val accionState: StateFlow<UiState<NegociacionResponse>?> = _accionState.asStateFlow()

    fun resetAccionState() { _accionState.value = null }

    /**
     * Carga datos iniciales: perfil técnico, billetera y solicitudes disponibles.
     * Si no hay red, los repositorios intentan devolver caché local.
     */
    fun cargarDatos(context: Context, distrito: String? = null, page: Int = 0, size: Int = 20) {
        viewModelScope.launch {
            val hayRed = NetworkChecker.isConnected(context)

            tecnicoRepository.obtenerMiPerfil().fold(
                onSuccess = { _perfilState.value = UiState.Exito(it) },
                onFailure = { _perfilState.value = if (!hayRed) UiState.SinConexion else UiState.Error(it.message ?: "") }
            )

            billeteraRepository.obtenerMiBilletera().fold(
                onSuccess = { _billeteraState.value = UiState.Exito(it) },
                onFailure = { _billeteraState.value = if (!hayRed) UiState.SinConexion else UiState.Error(it.message ?: "") }
            )

            _solicitudesState.value = UiState.Cargando
            solicitudRepository.obtenerSolicitudesDisponibles(distrito, page, size).fold(
                onSuccess = { _solicitudesState.value = UiState.Exito(it) },
                onFailure = { _solicitudesState.value = if (!hayRed) UiState.SinConexion else UiState.Error(it.message ?: "Error al cargar trabajos") }
            )
        }
    }

    fun ofertarPrecio(context: Context, idSolicitud: String, monto: Double) {
        if (!NetworkChecker.isConnected(context)) {
            _accionState.value = UiState.SinConexion
            return
        }
        _accionState.value = UiState.Cargando
        viewModelScope.launch {
            val result = negociacionRepository.ofertarComoTecnico(idSolicitud, monto)
            _accionState.value = result.fold(
                onSuccess = { UiState.Exito(it) },
                onFailure = { UiState.Error(it.message ?: "Error al enviar oferta") }
            )
        }
    }

    fun aceptarSolicitudDirecta(context: Context, idSolicitud: String) {
        if (!NetworkChecker.isConnected(context)) {
            _accionState.value = UiState.SinConexion
            return
        }
        _accionState.value = UiState.Cargando
        viewModelScope.launch {
            // Aceptar directamente sin negociar
            solicitudRepository.aceptarSolicitud(idSolicitud).fold(
                onSuccess = { _accionState.value = UiState.EliminadoExitosamente },
                onFailure = { _accionState.value = UiState.Error(it.message ?: "Error al aceptar") }
            )
        }
    }
}
