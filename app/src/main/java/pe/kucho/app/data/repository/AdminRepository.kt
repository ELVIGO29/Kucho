package pe.kucho.app.data.repository

import java.io.IOException
import pe.kucho.app.data.model.EstadisticasDashboard
import pe.kucho.app.data.network.RetrofitClient

class AdminRepository {
    private val api = RetrofitClient.apiService

    suspend fun obtenerEstadisticas(): Result<EstadisticasDashboard> {
        return try {
            val res = api.obtenerEstadisticasDashboard()
            if (res.isSuccessful && res.body() != null) Result.success(res.body()!!)
            else Result.failure(Exception("Error al cargar estadísticas (${res.code()})"))
        } catch (e: IOException) {
            Result.failure(Exception("Sin conexión a internet. Verifica tu red."))
        } catch (e: Exception) { Result.failure(e) }
    }
}
