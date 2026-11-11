package pe.kucho.app.data.repository

import java.io.IOException
import pe.kucho.app.data.model.BilleteraResponse
import pe.kucho.app.data.model.RecargarBilleteraRequest
import pe.kucho.app.data.network.RetrofitClient

class BilleteraRepository {
    private val api = RetrofitClient.apiService

    suspend fun obtenerMiBilletera(): Result<BilleteraResponse> {
        return try {
            val res = api.obtenerMiBilletera()
            if (res.isSuccessful && res.body() != null) Result.success(res.body()!!)
            else Result.failure(Exception("Error al consultar billetera (${res.code()})"))
        } catch (e: IOException) {
            Result.failure(Exception("Sin conexión a internet. Verifica tu red."))
        } catch (e: Exception) { Result.failure(e) }
    }

    suspend fun recargarSaldo(monto: Double): Result<BilleteraResponse> {
        return try {
            val res = api.recargarBilletera(RecargarBilleteraRequest(monto))
            if (res.isSuccessful && res.body() != null) Result.success(res.body()!!)
            else Result.failure(Exception("Error al recargar billetera (${res.code()})"))
        } catch (e: IOException) {
            Result.failure(Exception("Sin conexión a internet. Verifica tu red."))
        } catch (e: Exception) { Result.failure(e) }
    }
}
