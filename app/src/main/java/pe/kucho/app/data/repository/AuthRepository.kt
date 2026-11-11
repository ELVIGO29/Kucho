package pe.kucho.app.data.repository

import java.io.IOException
import org.json.JSONObject
import pe.kucho.app.data.model.*
import pe.kucho.app.data.network.RetrofitClient

class AuthRepository {
    private val api = RetrofitClient.apiService

    private fun extraerError(rawJson: String?, fallback: String): String {
        if (rawJson.isNullOrBlank()) return fallback
        return try {
            val json = JSONObject(rawJson)
            if (json.has("data") && !json.isNull("data")) {
                val data = json.get("data")
                if (data is String) return data
                if (data is JSONObject) {
                    val it = data.keys()
                    if (it.hasNext()) return data.getString(it.next())
                }
            }
            if (json.has("message")) json.getString("message") else fallback
        } catch (_: Exception) {
            fallback
        }
    }

    suspend fun solicitarOtp(telefono: String): Result<Unit> {
        return try {
            val response = api.solicitarOtp(AuthOtpRequest(telefono))
            if (response.isSuccessful) Result.success(Unit)
            else {
                val msg = extraerError(response.errorBody()?.string(), "Error al solicitar SMS de verificación (${response.code()})")
                Result.failure(Exception(msg))
            }
        } catch (e: IOException) {
            Result.failure(Exception("Sin conexión a internet. Verifica tu red."))
        } catch (e: Exception) { Result.failure(e) }
    }

    suspend fun verificarOtp(telefono: String, codigo: String): Result<AuthResponse> {
        return try {
            val response = api.verificarOtp(VerificarOtpRequest(telefono, codigo))
            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!)
            } else {
                val msg = extraerError(response.errorBody()?.string(), "Código OTP incorrecto o expirado")
                Result.failure(Exception(msg))
            }
        } catch (e: IOException) {
            Result.failure(Exception("Sin conexión a internet. Verifica tu red."))
        } catch (e: Exception) { Result.failure(e) }
    }

    suspend fun registrarUsuario(request: RegistroUsuarioRequest): Result<AuthResponse> {
        return try {
            val response = api.registrarUsuario(request)
            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!)
            } else {
                val msg = extraerError(response.errorBody()?.string(), "Error al crear cuenta (${response.code()})")
                Result.failure(Exception(msg))
            }
        } catch (e: IOException) {
            Result.failure(Exception("Sin conexión a internet. Verifica tu red."))
        } catch (e: Exception) { Result.failure(e) }
    }
}