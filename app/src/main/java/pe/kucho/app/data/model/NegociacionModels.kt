package pe.kucho.app.data.model

import com.google.gson.annotations.SerializedName

// --- NEGOCIACIONES ---
data class OfertarPrecioRequest(
    @SerializedName("precio") val precio: Double
)

data class NegociacionResponse(
    @SerializedName("idNegociacion") val idNegociacion: String,
    @SerializedName("idSolicitud") val idSolicitud: String,
    @SerializedName("idTecnico") val idTecnico: String,
    @SerializedName("nombreTecnico") val nombreTecnico: String?,
    @SerializedName("precioActual") val precioActual: Double,
    @SerializedName("ultimoEnOfertar") val ultimoEnOfertar: String,
    @SerializedName("estado") val estado: String,
    @SerializedName("fechaActualizacion") val fechaActualizacion: String
)
