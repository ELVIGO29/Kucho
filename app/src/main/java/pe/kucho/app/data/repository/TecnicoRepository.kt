package pe.kucho.app.data.repository

import java.io.IOException
import org.json.JSONObject
import pe.kucho.app.data.model.*
import pe.kucho.app.data.network.RetrofitClient

class TecnicoRepository {
    private val api = RetrofitClient.apiService

    suspend fun obtenerMiPerfil(): Result<TecnicoResponse> {
        return try {
            val res = api.obtenerMiPerfilTecnico()
            if (res.isSuccessful && res.body() != null) Result.success(res.body()!!)
            else Result.failure(Exception(parseError(res.errorBody()?.string()) ?: "Error al obtener perfil (${res.code()})"))
        } catch (e: IOException) {
            Result.failure(Exception("Sin conexión a internet. Verifica tu red."))
        } catch (e: Exception) { Result.failure(e) }
    }

    suspend fun actualizarMiPerfil(request: ActualizarPerfilTecnicoRequest): Result<TecnicoResponse> {
        return try {
            val res = api.actualizarMiPerfilTecnico(request)
            if (res.isSuccessful && res.body() != null) Result.success(res.body()!!)
            else Result.failure(Exception(parseError(res.errorBody()?.string()) ?: "Error al actualizar perfil (${res.code()})"))
        } catch (e: IOException) {
            Result.failure(Exception("Sin conexión a internet. Verifica tu red."))
        } catch (e: Exception) { Result.failure(e) }
    }

    suspend fun registrarDocumento(request: RegistroDocumentoRequest): Result<TecnicoResponse> {
        return try {
            val res = api.registrarDocumentoTecnico(request)
            if (res.isSuccessful && res.body() != null) Result.success(res.body()!!)
            else Result.failure(Exception(parseError(res.errorBody()?.string()) ?: "Error al registrar documento (${res.code()})"))
        } catch (e: IOException) {
            Result.failure(Exception("Sin conexión a internet. Verifica tu red."))
        } catch (e: Exception) { Result.failure(e) }
    }

    /**
     * Obtiene los trabajos del técnico autenticado con paginación.
     * Por defecto carga la primera página de 20 elementos.
     */
    suspend fun obtenerMisTrabajos(page: Int = 0, size: Int = 20): Result<List<SolicitudResponse>> {
        return try {
            val res = api.obtenerMisTrabajosTecnico(page, size)
            if (res.isSuccessful) Result.success(res.body() ?: emptyList())
            else Result.failure(Exception(parseError(res.errorBody()?.string()) ?: "Error al obtener mis trabajos (${res.code()})"))
        } catch (e: IOException) {
            Result.failure(Exception("Sin conexión a internet. Verifica tu red."))
        } catch (e: Exception) { Result.failure(e) }
    }

    suspend fun actualizarEstado(idSolicitud: String, nuevoEstado: String): Result<SolicitudResponse> {
        return try {
            val res = api.actualizarEstadoSolicitud(idSolicitud, ActualizarEstadoSolicitudRequest(nuevoEstado))
            if (res.isSuccessful && res.body() != null) Result.success(res.body()!!)
            else Result.failure(Exception(parseError(res.errorBody()?.string()) ?: "Error al actualizar estado (${res.code()})"))
        } catch (e: IOException) {
            Result.failure(Exception("Sin conexión a internet. Verifica tu red."))
        } catch (e: Exception) { Result.failure(e) }
    }

    suspend fun obtenerTecnicosActivos(): Result<List<TecnicoResponse>> {
        return try {
            val res = api.obtenerTecnicosActivos()
            if (res.isSuccessful) Result.success(res.body() ?: emptyList())
            else Result.failure(Exception(parseError(res.errorBody()?.string()) ?: "Error al obtener técnicos (${res.code()})"))
        } catch (e: IOException) {
            Result.failure(Exception("Sin conexión a internet. Verifica tu red."))
        } catch (e: Exception) { Result.failure(e) }
    }

    suspend fun obtenerTecnicoPorId(idTecnico: String): Result<TecnicoResponse> {
        return try {
            val res = api.obtenerTecnicosActivos()
            if (res.isSuccessful) {
                val tec = res.body()?.find { it.idUsuario == idTecnico }
                if (tec != null) Result.success(tec)
                else Result.failure(Exception("Técnico no encontrado"))
            } else {
                Result.failure(Exception(parseError(res.errorBody()?.string()) ?: "Error (${res.code()})"))
            }
        } catch (e: IOException) {
            Result.failure(Exception("Sin conexión a internet. Verifica tu red."))
        } catch (e: Exception) { Result.failure(e) }
    }

    private fun parseError(json: String?): String? {
        if (json.isNullOrBlank()) return null
        return try {
            val obj = JSONObject(json)
            if (obj.has("error")) obj.getString("error")
            else if (obj.has("message")) obj.getString("message")
            else null
        } catch (e: Exception) { null }
    }
}
