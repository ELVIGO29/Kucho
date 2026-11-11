package pe.kucho.app.ui.cliente.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import pe.kucho.app.data.model.Categoria
import pe.kucho.app.data.model.ClientePerfilResponse
import pe.kucho.app.data.model.SolicitudResponse
import pe.kucho.app.data.model.UiState
import pe.kucho.app.data.network.NetworkChecker
import pe.kucho.app.data.repository.CategoriaRepository
import pe.kucho.app.data.repository.ClienteRepository
import pe.kucho.app.data.repository.SolicitudRepository

/**
 * ViewModel para ClienteHomeScreen.
 * Carga categorías, solicitudes recientes y perfil del cliente.
 * Cancela automáticamente las corrutinas si la pantalla se destruye (viewModelScope).
 */
class ClienteHomeViewModel(
    private val categoriaRepository: CategoriaRepository = CategoriaRepository(),
    private val solicitudRepository: SolicitudRepository = SolicitudRepository(),
    private val clienteRepository: ClienteRepository = ClienteRepository()
) : ViewModel() {

    private val _categoriasState = MutableStateFlow<UiState<List<Categoria>>>(UiState.Cargando)
    val categoriasState: StateFlow<UiState<List<Categoria>>> = _categoriasState.asStateFlow()

    private val _solicitudesState = MutableStateFlow<UiState<List<SolicitudResponse>>>(UiState.Cargando)
    val solicitudesState: StateFlow<UiState<List<SolicitudResponse>>> = _solicitudesState.asStateFlow()

    private val _perfilState = MutableStateFlow<UiState<ClientePerfilResponse>>(UiState.Cargando)
    val perfilState: StateFlow<UiState<ClientePerfilResponse>> = _perfilState.asStateFlow()

    /**
     * Carga todos los datos de la pantalla de inicio del cliente.
     * Verifica conectividad antes de cada petición. Si no hay red,
     * los repositorios devolverán el caché local (SQLite) si está disponible.
     */
    fun cargarDatos(context: Context) {
        viewModelScope.launch {
            val hayRed = NetworkChecker.isConnected(context)

            // Categorías (con caché offline)
            _categoriasState.value = UiState.Cargando
            val catResult = categoriaRepository.listarCategorias()
            _categoriasState.value = catResult.fold(
                onSuccess = { UiState.Exito(it.filter { c -> c.activo }) },
                onFailure = {
                    if (!hayRed) UiState.SinConexion
                    else UiState.Error(it.message ?: "Error al cargar categorías")
                }
            )

            // Solicitudes recientes (con caché offline)
            _solicitudesState.value = UiState.Cargando
            val solResult = solicitudRepository.obtenerMisSolicitudesCliente(page = 0, size = 5)
            _solicitudesState.value = solResult.fold(
                onSuccess = { UiState.Exito(it) },
                onFailure = {
                    if (!hayRed) UiState.SinConexion
                    else UiState.Error(it.message ?: "Error al cargar solicitudes")
                }
            )

            // Perfil del cliente
            val perfilResult = clienteRepository.obtenerMiPerfil()
            _perfilState.value = perfilResult.fold(
                onSuccess = { UiState.Exito(it) },
                onFailure = {
                    if (!hayRed) UiState.SinConexion
                    else UiState.Error(it.message ?: "Error al cargar perfil")
                }
            )
        }
    }
}
