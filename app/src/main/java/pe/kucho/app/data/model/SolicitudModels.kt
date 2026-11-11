package pe.kucho.app.data.model

import com.google.gson.annotations.SerializedName

// --- SOLICITUDES ---
data class CrearSolicitudRequest(
    @SerializedName("idCategoria") val idCategoria: String,
    @SerializedName("descripcion") val descripcion: String,
    @SerializedName("distrito") val distrito: String,
    @SerializedName("referenciaDireccion") val referenciaDireccion: String? = null,
    @SerializedName("latitud") val latitud: Double? = null,
    @SerializedName("longitud") val longitud: Double? = null,
    @SerializedName("fotoUrl") val fotoUrl: String? = null,
    @SerializedName("fechaProgramada") val fechaProgramada: String? = null,
    @SerializedName("precioPactado") val precioPactado: Double? = null
)

data class ActualizarEstadoSolicitudRequest(
    @SerializedName("estado") val estado: String // "EN_PROGRESO", "FINALIZADO"
)

data class SolicitudResponse(
    @SerializedName("idSolicitud") val idSolicitud: String,
    @SerializedName("descripcion") val descripcion: String,
    @SerializedName("distrito") val distrito: String,
    @SerializedName("referenciaDireccion") val referenciaDireccion: String? = null,
    @SerializedName("latitud") val latitud: Double? = null,
    @SerializedName("longitud") val longitud: Double? = null,
    @SerializedName("fotoUrl") val fotoUrl: String? = null,
    @SerializedName("estado") val estado: String,
    @SerializedName("precioPactado") val precioPactado: Double? = null,
    @SerializedName("fechaCreacion") val fechaCreacion: String,
    @SerializedName("fechaProgramada") val fechaProgramada: String? = null,
    @SerializedName("idCliente") val idCliente: String,
    @SerializedName("nombreCliente") val nombreCliente: String?,
    @SerializedName("telefonoCliente") val telefonoCliente: String?,
    @SerializedName("idTecnico") val idTecnico: String? = null,
    @SerializedName("nombreTecnico") val nombreTecnico: String? = null,
    @SerializedName("telefonoTecnico") val telefonoTecnico: String? = null,
    @SerializedName("nombreCategoria") val nombreCategoria: String,
    @SerializedName("puntuacionResena") val puntuacionResena: Int? = null,
    @SerializedName("comentarioResena") val comentarioResena: String? = null
)
