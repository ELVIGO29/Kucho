package pe.kucho.app.data.location

import android.annotation.SuppressLint
import android.content.Context
import android.location.Geocoder
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.tasks.CancellationTokenSource
import kotlinx.coroutines.suspendCancellableCoroutine
import java.util.Locale
import kotlin.coroutines.resume

/**
 * Cliente de ubicacion que usa FusedLocationProviderClient para obtener
 * la ubicacion actual del dispositivo con alta precision y bajo consumo de bateria.
 *
 * Usa getCurrentLocation() con una sola lectura (no listener continuo)
 * para evitar consumo excesivo de bateria.
 */
class LocationClient(private val context: Context) {

    private val fusedClient = LocationServices.getFusedLocationProviderClient(context)

    /**
     * Obtiene la ubicacion actual del dispositivo (una sola lectura).
     * Requiere que el permiso ACCESS_FINE_LOCATION o ACCESS_COARSE_LOCATION ya este concedido.
     *
     * @return Par (latitud, longitud) o null si no pudo obtenerla
     */
    @SuppressLint("MissingPermission")
    suspend fun obtenerUbicacionActual(): Pair<Double, Double>? =
        suspendCancellableCoroutine { continuation ->
            val cts = CancellationTokenSource()

            fusedClient.getCurrentLocation(Priority.PRIORITY_HIGH_ACCURACY, cts.token)
                .addOnSuccessListener { location ->
                    if (location != null) {
                        continuation.resume(Pair(location.latitude, location.longitude))
                    } else {
                        continuation.resume(null)
                    }
                }
                .addOnFailureListener {
                    continuation.resume(null)
                }

            continuation.invokeOnCancellation { cts.cancel() }
        }

    /**
     * Convierte coordenadas GPS en una direccion legible (geocodificacion inversa).
     * Usa el Geocoder del sistema operativo, que puede fallar sin internet.
     *
     * @param lat Latitud
     * @param lng Longitud
     * @return Direccion completa o un texto de fallback con las coordenadas
     */
    fun geocodificarInversa(lat: Double, lng: Double): String {
        return try {
            val geocoder = Geocoder(context, Locale("es", "PE"))
            @Suppress("DEPRECATION")
            val addresses = geocoder.getFromLocation(lat, lng, 1)
            if (!addresses.isNullOrEmpty()) {
                val address = addresses[0]
                buildString {
                    if (!address.thoroughfare.isNullOrBlank()) append(address.thoroughfare)
                    if (!address.subLocality.isNullOrBlank()) {
                        if (isNotEmpty()) append(", ")
                        append(address.subLocality)
                    }
                    if (!address.subAdminArea.isNullOrBlank()) {
                        if (isNotEmpty()) append(", ")
                        append(address.subAdminArea)
                    }
                }.ifBlank { "Lat: %.5f, Lng: %.5f".format(lat, lng) }
            } else {
                "Lat: %.5f, Lng: %.5f".format(lat, lng)
            }
        } catch (e: Exception) {
            "Lat: %.5f, Lng: %.5f".format(lat, lng)
        }
    }

    /**
     * Extrae el nombre del distrito/subAdminArea de las coordenadas.
     * Util para auto-rellenar el campo "Distrito" en el formulario.
     */
    fun obtenerDistrito(lat: Double, lng: Double): String? {
        return try {
            val geocoder = Geocoder(context, Locale("es", "PE"))
            @Suppress("DEPRECATION")
            val addresses = geocoder.getFromLocation(lat, lng, 1)
            addresses?.firstOrNull()?.subAdminArea
                ?: addresses?.firstOrNull()?.locality
        } catch (e: Exception) {
            null
        }
    }
}
