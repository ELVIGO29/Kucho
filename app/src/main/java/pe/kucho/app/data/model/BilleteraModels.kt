package pe.kucho.app.data.model

import com.google.gson.annotations.SerializedName

// --- BILLETERA ---
data class TransaccionBilleteraResponse(
    @SerializedName("idTransaccion") val idTransaccion: String,
    @SerializedName("monto") val monto: Double,
    @SerializedName("tipo") val tipo: String,
    @SerializedName("fecha") val fecha: String,
    @SerializedName("descripcion") val descripcion: String? = null
)

data class BilleteraResponse(
    @SerializedName("saldoActual") val saldoActual: Double = 0.0,
    @SerializedName("transacciones") val transacciones: List<TransaccionBilleteraResponse>? = emptyList()
)

data class RecargarBilleteraRequest(
    @SerializedName("monto") val monto: Double
)
