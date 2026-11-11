package pe.kucho.app.data.repository

import java.io.IOException
import pe.kucho.app.data.model.ActualizarClienteRequest
import pe.kucho.app.data.model.ClientePerfilResponse
import pe.kucho.app.data.network.RetrofitClient

class ClienteRepository {
    private val api = RetrofitClient.apiService

    suspend fun obtenerMiPerfil(): Result<ClientePerfilResponse> {
        return try {
            val res = api.obtenerMiPerfilCliente()
            if (res.isSuccessful && res.body() != null) Result.success(res.body()!!)
            else Result.failure(Exception("Error al obtener perfil del cliente (${res.code()})"))
        } catch (e: IOException) {
            Result.failure(Exception("Sin conexión a internet. Verifica tu red."))
        } catch (e: Exception) { Result.failure(e) }
    }

    suspend fun actualizarMiPerfil(request: ActualizarClienteRequest): Result<ClientePerfilResponse> {
        return try {
            val res = api.actualizarMiPerfilCliente(request)
            if (res.isSuccessful && res.body() != null) Result.success(res.body()!!)
            else Result.failure(Exception("Error al actualizar perfil (${res.code()})"))
        } catch (e: IOException) {
            Result.failure(Exception("Sin conexión a internet. Verifica tu red."))
        } catch (e: Exception) { Result.failure(e) }
    }
}
