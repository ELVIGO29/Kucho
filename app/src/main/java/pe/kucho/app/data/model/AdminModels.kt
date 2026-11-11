package pe.kucho.app.data.model

import com.google.gson.annotations.SerializedName

// --- DASHBOARD ADMIN ---
data class EstadisticasDashboard(
    @SerializedName("totalTecnicosActivos") val totalTecnicosActivos: Long,
    @SerializedName("totalClientesRegistrados") val totalClientesRegistrados: Long,
    @SerializedName("solicitudesActivas") val solicitudesActivas: Long,
    @SerializedName("solicitudesFinalizadas") val solicitudesFinalizadas: Long,
    @SerializedName("calificacionPromedio") val calificacionPromedio: Double?,
    @SerializedName("totalRecaudado") val totalRecaudado: Double?
)
