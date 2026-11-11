package pe.kucho.app.data.repository

import java.io.IOException
import pe.kucho.app.KuchoApplication
import pe.kucho.app.data.local.db.KuchoDbHelper
import pe.kucho.app.data.local.db.SolicitudDao
import pe.kucho.app.data.model.*
import pe.kucho.app.data.network.RetrofitClient

class SolicitudRepository {
    private val api = RetrofitClient.apiService
    private val solicitudDao by lazy {
        try {
            SolicitudDao(KuchoDbHelper.getInstance(KuchoApplication.instance))
        } catch (_: Exception) {
            null
        }
    }

    suspend fun crearSolicitud(request: CrearSolicitudRequest): Result<SolicitudResponse> {
        return try {
            val res = api.crearSolicitud(request)
            if (res.isSuccessful && res.body() != null) Result.success(res.body()!!)
            else Result.failure(Exception("Error al crear solicitud (${res.code()})"))
        } catch (e: IOException) {
            Result.failure(Exception("Sin conexión a internet. Verifica tu red."))
        } catch (e: Exception) { Result.failure(e) }
    }

    /**
     * Obtiene las solicitudes del cliente autenticado.
     * Con paginación: por defecto carga la primera página de 20 elementos.
     * Si no hay conexión, devuelve el caché local SQLite.
     */
    suspend fun obtenerMisSolicitudesCliente(page: Int = 0, size: Int = 20): Result<List<SolicitudResponse>> {
        return try {
            val res = api.obtenerMisSolicitudesCliente(page, size)
            if (res.isSuccessful) {
                val lista = res.body() ?: emptyList()
                // Solo guarda en caché la primera página para no mezclar páginas
                if (lista.isNotEmpty() && page == 0) {
                    solicitudDao?.guardarSolicitudes(lista)
                }
                Result.success(lista)
            } else {
                val cached = solicitudDao?.obtenerSolicitudes() ?: emptyList()
                if (cached.isNotEmpty()) Result.success(cached)
                else Result.failure(Exception("Error al obtener solicitudes (${res.code()})"))
            }
        } catch (e: IOException) {
            // Sin conexión: usar caché local
            val cached = solicitudDao?.obtenerSolicitudes() ?: emptyList()
            if (cached.isNotEmpty()) Result.success(cached)
            else Result.failure(Exception("Sin conexión y sin datos en caché."))
        } catch (e: Exception) {
            val cached = solicitudDao?.obtenerSolicitudes() ?: emptyList()
            if (cached.isNotEmpty()) Result.success(cached)
            else Result.failure(e)
        }
    }

    suspend fun cancelarSolicitud(idSolicitud: String): Result<Unit> {
        return try {
            val res = api.cancelarSolicitud(idSolicitud)
            if (res.isSuccessful) Result.success(Unit)
            else Result.failure(Exception("Error al cancelar solicitud (${res.code()})"))
        } catch (e: IOException) {
            Result.failure(Exception("Sin conexión a internet. Verifica tu red."))
        } catch (e: Exception) { Result.failure(e) }
    }

    /**
     * Obtiene solicitudes disponibles para técnicos con filtro de distrito y paginación.
     * Si no hay conexión, devuelve el caché local SQLite.
     */
    suspend fun obtenerSolicitudesDisponibles(
        distrito: String? = null,
        page: Int = 0,
        size: Int = 20
    ): Result<List<SolicitudResponse>> {
        return try {
            val res = api.obtenerSolicitudesDisponibles(distrito, page, size)
            if (res.isSuccessful) {
                val lista = res.body() ?: emptyList()
                if (lista.isNotEmpty() && page == 0) {
                    solicitudDao?.guardarSolicitudes(lista)
                }
                Result.success(lista)
            } else {
                val cached = solicitudDao?.obtenerSolicitudes() ?: emptyList()
                if (cached.isNotEmpty()) Result.success(cached)
                else Result.failure(Exception("Error al obtener trabajos disponibles (${res.code()})"))
            }
        } catch (e: IOException) {
            val cached = solicitudDao?.obtenerSolicitudes() ?: emptyList()
            if (cached.isNotEmpty()) Result.success(cached)
            else Result.failure(Exception("Sin conexión y sin datos en caché."))
        } catch (e: Exception) {
            val cached = solicitudDao?.obtenerSolicitudes() ?: emptyList()
            if (cached.isNotEmpty()) Result.success(cached)
            else Result.failure(e)
        }
    }

    suspend fun aceptarSolicitud(idSolicitud: String): Result<SolicitudResponse> {
        return try {
            val res = api.aceptarSolicitud(idSolicitud)
            if (res.isSuccessful && res.body() != null) Result.success(res.body()!!)
            else Result.failure(Exception("No se pudo aceptar el trabajo (${res.code()})"))
        } catch (e: IOException) {
            Result.failure(Exception("Sin conexión a internet. Verifica tu red."))
        } catch (e: Exception) { Result.failure(e) }
    }

    suspend fun obtenerSolicitudPorId(idSolicitud: String): Result<SolicitudResponse> {
        return try {
            val res = api.obtenerSolicitudPorId(idSolicitud)
            if (res.isSuccessful && res.body() != null) Result.success(res.body()!!)
            else Result.failure(Exception("No se pudo obtener la solicitud (${res.code()})"))
        } catch (e: IOException) {
            Result.failure(Exception("Sin conexión a internet. Verifica tu red."))
        } catch (e: Exception) { Result.failure(e) }
    }
}
