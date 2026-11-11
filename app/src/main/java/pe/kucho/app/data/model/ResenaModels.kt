package pe.kucho.app.data.model

import com.google.gson.annotations.SerializedName

// --- RESEÑAS ---
data class CrearResenaRequest(
    @SerializedName("idSolicitud") val idSolicitud: String,
    @SerializedName("puntuacion") val puntuacion: Int,
    @SerializedName("comentario") val comentario: String? = null
)

data class ResenaResponse(
    @SerializedName("idResena") val idResena: String,
    @SerializedName("idSolicitud") val idSolicitud: String,
    @SerializedName("idEvaluador") val idEvaluador: String,
    @SerializedName("nombreEvaluador") val nombreEvaluador: String?,
    @SerializedName("puntuacion") val puntuacion: Int,
    @SerializedName("comentario") val comentario: String?,
    @SerializedName("fechaCreacion") val fechaCreacion: String
)
