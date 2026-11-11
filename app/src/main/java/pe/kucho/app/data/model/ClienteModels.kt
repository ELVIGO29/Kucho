package pe.kucho.app.data.model

import com.google.gson.annotations.SerializedName

// --- CLIENTE PERFIL ---
data class ClientePerfilResponse(
    @SerializedName("idUsuario") val idUsuario: String,
    @SerializedName("nombre") val nombre: String,
    @SerializedName("telefono") val telefono: String,
    @SerializedName("correo") val correo: String? = null,
    @SerializedName("distrito") val distrito: String? = null,
    @SerializedName("tipoDocumento") val tipoDocumento: String? = null,
    @SerializedName("numeroDocumento") val numeroDocumento: String? = null,
    @SerializedName("urlAvatar") val urlAvatar: String? = null
)

data class ActualizarClienteRequest(
    @SerializedName("nombre") val nombre: String,
    @SerializedName("correo") val correo: String? = null,
    @SerializedName("distrito") val distrito: String? = null,
    @SerializedName("tipoDocumento") val tipoDocumento: String? = null,
    @SerializedName("numeroDocumento") val numeroDocumento: String? = null,
    @SerializedName("urlAvatar") val urlAvatar: String? = null
)
