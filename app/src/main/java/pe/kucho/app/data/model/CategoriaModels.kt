package pe.kucho.app.data.model

import com.google.gson.annotations.SerializedName

// --- CATEGORÍAS ---
data class Categoria(
    @SerializedName("idCategoria") val idCategoria: String,
    @SerializedName("nombre") val nombre: String,
    @SerializedName("slug") val slug: String? = null,
    @SerializedName("icono") val icono: String? = null,
    @SerializedName("activo") val activo: Boolean = true
)

// DTO para crear o editar categoría (uso Admin)
data class CrearCategoriaRequest(
    @SerializedName("nombre") val nombre: String,
    @SerializedName("slug") val slug: String,
    @SerializedName("icono") val icono: String? = null
)
