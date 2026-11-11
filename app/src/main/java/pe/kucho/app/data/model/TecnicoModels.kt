package pe.kucho.app.data.model

import com.google.gson.annotations.SerializedName

// --- TÉCNICOS ---
data class TecnicoResponse(
    @SerializedName("idUsuario") val idUsuario: String,
    @SerializedName("nombre") val nombre: String,
    @SerializedName("telefono") val telefono: String? = null,
    @SerializedName("correo") val correo: String? = null,
    @SerializedName("tipoDocumento") val tipoDocumento: String? = null,
    @SerializedName("numeroDocumento") val numeroDocumento: String? = null,
    @SerializedName("biografia") val biografia: String? = null,
    @SerializedName("urlAvatar", alternate = ["fotoUrl"]) val fotoUrl: String? = null,
    @SerializedName("dniVerificado") val dniVerificado: Boolean = false,
    @SerializedName("activo") val activo: Boolean = true,
    @SerializedName("categorias") val categorias: List<String>? = emptyList(),
    @SerializedName("zonas") val zonas: List<String>? = emptyList(),
    @SerializedName("promedioCalificacion") val promedioCalificacion: Double? = 0.0,
    @SerializedName("cantidadTrabajos") val cantidadTrabajos: Int? = 0
)

data class ActualizarPerfilTecnicoRequest(
    @SerializedName("biografia") val biografia: String? = null,
    @SerializedName("zonas") val zonas: List<String>? = emptyList()
)

data class RegistroDocumentoRequest(
    @SerializedName("tipoDocumento") val tipoDocumento: String,
    @SerializedName("numeroDocumento") val numeroDocumento: String
)

// DTO para verificar/revocar técnico (uso Admin)
data class VerificarTecnicoRequest(
    @SerializedName("verificado") val verificado: Boolean
)
