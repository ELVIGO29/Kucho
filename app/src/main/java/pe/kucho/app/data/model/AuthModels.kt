package pe.kucho.app.data.model

import com.google.gson.annotations.SerializedName

// --- AUTENTICACIÓN ---
data class AuthOtpRequest(
    @SerializedName("telefono") val telefono: String
)

data class VerificarOtpRequest(
    @SerializedName("telefono") val telefono: String,
    @SerializedName("codigo") val codigo: String
)

data class RegistroUsuarioRequest(
    @SerializedName("telefono") val telefono: String,
    @SerializedName("nombre") val nombre: String,
    @SerializedName("rol") val rol: String,             // "CLIENTE" o "TECNICO"
    @SerializedName("correo") val correo: String? = null,
    @SerializedName("tipoDocumento") val tipoDocumento: String? = null,
    @SerializedName("numeroDocumento") val numeroDocumento: String? = null,
    @SerializedName("distrito") val distrito: String? = null,
    @SerializedName("biografia") val biografia: String? = null
)

data class AuthResponse(
    @SerializedName("token") val token: String,
    @SerializedName("tipo") val tipo: String = "Bearer",
    @SerializedName("idUsuario") val idUsuario: String,
    @SerializedName("nombre") val nombre: String?,
    @SerializedName("rol") val rol: String,
    @SerializedName("expiraEn") val expiraEn: Long
)
