package pe.kucho.app.ui.admin.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import pe.kucho.app.data.model.Categoria
import pe.kucho.app.data.model.CrearCategoriaRequest
import pe.kucho.app.data.model.EstadisticasDashboard
import pe.kucho.app.data.model.PageResponse
import pe.kucho.app.data.model.SolicitudResponse
import pe.kucho.app.data.model.TecnicoResponse
import pe.kucho.app.data.model.UiState
import pe.kucho.app.data.model.VerificarTecnicoRequest
import pe.kucho.app.data.network.NetworkChecker
import pe.kucho.app.data.network.RetrofitClient
import pe.kucho.app.data.repository.AdminRepository
import pe.kucho.app.data.repository.CategoriaRepository

/**
 * ViewModel para AdminDashboardScreen.
 * Centraliza estadísticas, gestión de técnicos, categorías e historial de solicitudes.
 */
class AdminDashboardViewModel(
    private val adminRepository: AdminRepository = AdminRepository(),
    private val categoriaRepository: CategoriaRepository = CategoriaRepository()
) : ViewModel() {

    private val _statsState = MutableStateFlow<UiState<EstadisticasDashboard>>(UiState.Cargando)
    val statsState: StateFlow<UiState<EstadisticasDashboard>> = _statsState.asStateFlow()

    private val _tecnicosState = MutableStateFlow<UiState<List<TecnicoResponse>>>(UiState.Cargando)
    val tecnicosState: StateFlow<UiState<List<TecnicoResponse>>> = _tecnicosState.asStateFlow()

    private val _categoriasState = MutableStateFlow<UiState<List<Categoria>>>(UiState.Cargando)
    val categoriasState: StateFlow<UiState<List<Categoria>>> = _categoriasState.asStateFlow()

    private val _solicitudesState = MutableStateFlow<UiState<PageResponse<SolicitudResponse>>>(UiState.Cargando)
    val solicitudesState: StateFlow<UiState<PageResponse<SolicitudResponse>>> = _solicitudesState.asStateFlow()

    private val _accionState = MutableStateFlow<UiState<Any>?>(null)
    val accionState: StateFlow<UiState<Any>?> = _accionState.asStateFlow()

    fun resetAccionState() { _accionState.value = null }

    /**
     * Carga todos los datos del dashboard admin con verificación de conectividad.
     */
    fun cargarTodo(context: Context, page: Int = 0, size: Int = 20) {
        if (!NetworkChecker.isConnected(context)) {
            _statsState.value = UiState.SinConexion
            _tecnicosState.value = UiState.SinConexion
            _categoriasState.value = UiState.SinConexion
            _solicitudesState.value = UiState.SinConexion
            return
        }
        viewModelScope.launch {
            val api = RetrofitClient.apiService

            // Estadísticas
            _statsState.value = UiState.Cargando
            adminRepository.obtenerEstadisticas().fold(
                onSuccess = { _statsState.value = UiState.Exito(it) },
                onFailure = { _statsState.value = UiState.Error(it.message ?: "Error estadísticas") }
            )

            // Técnicos activos
            _tecnicosState.value = UiState.Cargando
            try {
                val res = api.obtenerTecnicosActivos()
                _tecnicosState.value = if (res.isSuccessful) UiState.Exito(res.body() ?: emptyList())
                else UiState.Error("Error al cargar técnicos (${res.code()})")
            } catch (e: Exception) {
                _tecnicosState.value = UiState.Error(e.message ?: "Error")
            }

            // Categorías
            _categoriasState.value = UiState.Cargando
            categoriaRepository.listarCategorias().fold(
                onSuccess = { _categoriasState.value = UiState.Exito(it) },
                onFailure = { _categoriasState.value = UiState.Error(it.message ?: "Error categorías") }
            )

            // Solicitudes admin con paginación
            _solicitudesState.value = UiState.Cargando
            try {
                val res = api.obtenerSolicitudesAdmin(page, size)
                _solicitudesState.value = if (res.isSuccessful) UiState.Exito(res.body()!!)
                else UiState.Error("Error al cargar solicitudes (${res.code()})")
            } catch (e: Exception) {
                _solicitudesState.value = UiState.Error(e.message ?: "Error")
            }
        }
    }

    fun verificarTecnico(context: Context, idTecnico: String, verificado: Boolean) {
        if (!NetworkChecker.isConnected(context)) {
            _accionState.value = UiState.SinConexion
            return
        }
        _accionState.value = UiState.Cargando
        viewModelScope.launch {
            try {
                val api = RetrofitClient.apiService
                val res = api.verificarTecnico(idTecnico, VerificarTecnicoRequest(verificado))
                _accionState.value = if (res.isSuccessful && res.body() != null) UiState.Exito(res.body()!!)
                else UiState.Error("Error al verificar técnico (${res.code()})")
            } catch (e: Exception) {
                _accionState.value = UiState.Error(e.message ?: "Error")
            }
        }
    }

    fun crearCategoria(context: Context, request: CrearCategoriaRequest) {
        if (!NetworkChecker.isConnected(context)) {
            _accionState.value = UiState.SinConexion
            return
        }
        _accionState.value = UiState.Cargando
        viewModelScope.launch {
            try {
                val api = RetrofitClient.apiService
                val res = api.crearCategoria(request)
                _accionState.value = if (res.isSuccessful && res.body() != null) UiState.Exito(res.body()!!)
                else UiState.Error("Error al crear categoría (${res.code()})")
            } catch (e: Exception) {
                _accionState.value = UiState.Error(e.message ?: "Error")
            }
        }
    }

    fun actualizarCategoria(context: Context, idCategoria: String, request: CrearCategoriaRequest) {
        if (!NetworkChecker.isConnected(context)) {
            _accionState.value = UiState.SinConexion
            return
        }
        _accionState.value = UiState.Cargando
        viewModelScope.launch {
            try {
                val api = RetrofitClient.apiService
                val res = api.actualizarCategoria(idCategoria, request)
                _accionState.value = if (res.isSuccessful && res.body() != null) UiState.Exito(res.body()!!)
                else UiState.Error("Error al actualizar categoría (${res.code()})")
            } catch (e: Exception) {
                _accionState.value = UiState.Error(e.message ?: "Error")
            }
        }
    }

    fun cambiarEstadoCategoria(context: Context, idCategoria: String, activo: Boolean) {
        if (!NetworkChecker.isConnected(context)) {
            _accionState.value = UiState.SinConexion
            return
        }
        _accionState.value = UiState.Cargando
        viewModelScope.launch {
            try {
                val api = RetrofitClient.apiService
                val res = api.cambiarEstadoCategoria(idCategoria, activo)
                _accionState.value = if (res.isSuccessful) UiState.EliminadoExitosamente
                else UiState.Error("Error al cambiar estado (${res.code()})")
            } catch (e: Exception) {
                _accionState.value = UiState.Error(e.message ?: "Error")
            }
        }
    }
}
