package pe.kucho.app.data.repository

import java.io.IOException
import org.json.JSONObject
import pe.kucho.app.data.model.NegociacionResponse
import pe.kucho.app.data.model.OfertarPrecioRequest
import pe.kucho.app.data.network.RetrofitClient

class NegociacionRepository {
    private val api = RetrofitClient.apiService

    suspend fun ofertarComoTecnico(idSolicitud: String, monto: Double): Result<NegociacionResponse> {
        return try {
            val res = api.ofertarComoTecnico(idSolicitud, OfertarPrecioRequest(monto))
            if (res.isSuccessful && res.body() != null) Result.success(res.body()!!)
            else Result.failure(Exception(parseError(res.errorBody()?.string()) ?: "Error al enviar la oferta (${res.code()})"))
        } catch (e: IOException) {
            Result.failure(Exception("Sin conexión a internet. Verifica tu red."))
        } catch (e: Exception) { Result.failure(e) }
    }

    suspend fun contraofertarComoCliente(idNegociacion: String, monto: Double): Result<NegociacionResponse> {
        return try {
            val res = api.contraofertarComoCliente(idNegociacion, OfertarPrecioRequest(monto))
            if (res.isSuccessful && res.body() != null) Result.success(res.body()!!)
            else Result.failure(Exception(parseError(res.errorBody()?.string()) ?: "Error al enviar contraoferta (${res.code()})"))
        } catch (e: IOException) {
            Result.failure(Exception("Sin conexión a internet. Verifica tu red."))
        } catch (e: Exception) { Result.failure(e) }
    }

    suspend fun aceptarNegociacion(idNegociacion: String): Result<NegociacionResponse> {
        return try {
            val res = api.aceptarNegociacion(idNegociacion)
            if (res.isSuccessful && res.body() != null) Result.success(res.body()!!)
            else Result.failure(Exception(parseError(res.errorBody()?.string()) ?: "Error al aceptar negociación (${res.code()})"))
        } catch (e: IOException) {
            Result.failure(Exception("Sin conexión a internet. Verifica tu red."))
        } catch (e: Exception) { Result.failure(e) }
    }

    suspend fun rechazarNegociacion(idNegociacion: String): Result<NegociacionResponse> {
        return try {
            val res = api.rechazarNegociacion(idNegociacion)
            if (res.isSuccessful && res.body() != null) Result.success(res.body()!!)
            else Result.failure(Exception(parseError(res.errorBody()?.string()) ?: "Error al rechazar negociación (${res.code()})"))
        } catch (e: IOException) {
            Result.failure(Exception("Sin conexión a internet. Verifica tu red."))
        } catch (e: Exception) { Result.failure(e) }
    }

    suspend fun obtenerNegociacionesPorSolicitud(idSolicitud: String): Result<List<NegociacionResponse>> {
        return try {
            val res = api.obtenerNegociacionesPorSolicitud(idSolicitud)
            if (res.isSuccessful) Result.success(res.body() ?: emptyList())
            else Result.failure(Exception(parseError(res.errorBody()?.string()) ?: "Error al cargar negociaciones (${res.code()})"))
        } catch (e: IOException) {
            Result.failure(Exception("Sin conexión a internet. Verifica tu red."))
        } catch (e: Exception) { Result.failure(e) }
    }

    suspend fun obtenerMiNegociacion(idSolicitud: String): Result<NegociacionResponse?> {
        return try {
            val res = api.obtenerMiNegociacion(idSolicitud)
            if (res.isSuccessful) Result.success(res.body())
            else Result.success(null)
        } catch (e: IOException) {
            Result.failure(Exception("Sin conexión a internet. Verifica tu red."))
        } catch (e: Exception) { Result.failure(e) }
    }

    private fun parseError(json: String?): String? {
        if (json.isNullOrBlank()) return null
        return try {
            val obj = JSONObject(json)
            when {
                obj.has("message") && !obj.isNull("message") -> obj.getString("message")
                obj.has("error") && !obj.isNull("error") -> obj.getString("error")
                obj.has("data") && !obj.isNull("data") -> obj.getString("data")
                else -> null
            }
        } catch (e: Exception) { null }
    }
}
