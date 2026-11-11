package pe.kucho.app.ui.cliente.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import pe.kucho.app.data.model.Categoria
import pe.kucho.app.data.model.CrearSolicitudRequest
import pe.kucho.app.data.model.SolicitudResponse
import pe.kucho.app.data.model.UiState
import pe.kucho.app.data.network.NetworkChecker
import pe.kucho.app.data.repository.CategoriaRepository
import pe.kucho.app.data.repository.SolicitudRepository

/**
 * ViewModel para CrearSolicitudScreen.
 * Gestiona la carga de categorías y la creación de una nueva solicitud.
 */
class CrearSolicitudViewModel(
    private val categoriaRepository: CategoriaRepository = CategoriaRepository(),
    private val solicitudRepository: SolicitudRepository = SolicitudRepository()
) : ViewModel() {

    private val _categoriasState = MutableStateFlow<UiState<List<Categoria>>>(UiState.Cargando)
    val categoriasState: StateFlow<UiState<List<Categoria>>> = _categoriasState.asStateFlow()

    private val _crearState = MutableStateFlow<UiState<SolicitudResponse>?>(null)
    val crearState: StateFlow<UiState<SolicitudResponse>?> = _crearState.asStateFlow()

    fun resetCrearState() { _crearState.value = null }

    /** Carga categorías activas para el selector del formulario. */
    fun cargarCategorias(context: Context) {
        _categoriasState.value = UiState.Cargando
        viewModelScope.launch {
            val hayRed = NetworkChecker.isConnected(context)
            val result = categoriaRepository.listarCategorias()
            _categoriasState.value = result.fold(
                onSuccess = { UiState.Exito(it) },
                onFailure = {
                    if (!hayRed) UiState.SinConexion
                    else UiState.Error(it.message ?: "Error al cargar categorías")
                }
            )
        }
    }

    /**
     * Crea una nueva solicitud de servicio.
     * POST no es idempotente: deshabilitar el botón mientras isSuccessful = false
     * para evitar duplicados (el ViewModel emite Cargando durante la petición).
     */
    fun crearSolicitud(context: Context, request: CrearSolicitudRequest) {
        if (!NetworkChecker.isConnected(context)) {
            _crearState.value = UiState.SinConexion
            return
        }
        _crearState.value = UiState.Cargando
        viewModelScope.launch {
            val result = solicitudRepository.crearSolicitud(request)
            _crearState.value = result.fold(
                onSuccess = { UiState.Exito(it) },
                onFailure = { UiState.Error(it.message ?: "Error al crear solicitud") }
            )
        }
    }
}
