package pe.kucho.app.data.repository

import java.io.IOException
import pe.kucho.app.data.model.CrearResenaRequest
import pe.kucho.app.data.model.ResenaResponse
import pe.kucho.app.data.network.RetrofitClient

class ResenaRepository {
    private val api = RetrofitClient.apiService

    suspend fun crearResena(idSolicitud: String, puntuacion: Int, comentario: String?): Result<ResenaResponse> {
        return try {
            val res = api.crearResena(CrearResenaRequest(idSolicitud, puntuacion, comentario))
            if (res.isSuccessful && res.body() != null) Result.success(res.body()!!)
            else Result.failure(Exception("Error al publicar la reseña (${res.code()})"))
        } catch (e: IOException) {
            Result.failure(Exception("Sin conexión a internet. Verifica tu red."))
        } catch (e: Exception) { Result.failure(e) }
    }

    /**
     * Obtiene reseñas de un técnico con paginación.
     * Por defecto carga 10 reseñas por página.
     */
    suspend fun obtenerResenasPorTecnico(
        idTecnico: String,
        page: Int = 0,
        size: Int = 10
    ): Result<List<ResenaResponse>> {
        return try {
            val res = api.obtenerResenasPorTecnico(idTecnico, page, size)
            if (res.isSuccessful) Result.success(res.body() ?: emptyList())
            else Result.failure(Exception("Error al cargar reseñas (${res.code()})"))
        } catch (e: IOException) {
            Result.failure(Exception("Sin conexión a internet. Verifica tu red."))
        } catch (e: Exception) { Result.failure(e) }
    }
}
