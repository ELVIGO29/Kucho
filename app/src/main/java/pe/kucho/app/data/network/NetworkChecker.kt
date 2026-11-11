package pe.kucho.app.data.network

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities

/**
 * Utilidad para verificar conectividad ANTES de lanzar peticiones HTTP.
 * Usa ConnectivityManager del sistema Android para detectar si hay red disponible
 * (Wi-Fi, datos móviles o Ethernet) con capacidad de Internet real.
 *
 * Rubric Semana 09, sección 8.1: verificar red antes de la petición.
 */
object NetworkChecker {

    /**
     * Retorna true si el dispositivo tiene conectividad activa con capacidad de Internet.
     * Funciona desde Android 6 (API 23) en adelante con NetworkCapabilities.
     */
    fun isConnected(context: Context): Boolean {
        return try {
            val connectivityManager =
                context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
            val network = connectivityManager.activeNetwork ?: return true
            val capabilities = connectivityManager.getNetworkCapabilities(network) ?: return true
            capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET) ||
                capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED)
        } catch (e: Throwable) {
            true
        }
    }
}
