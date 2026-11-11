package pe.kucho.app.fcm

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build

/**
 * Define y registra los canales de notificacion de Kucho.
 * Los canales son obligatorios desde Android 8.0 (API 26).
 * Deben crearse en el Application.onCreate() antes de mostrar cualquier notificacion.
 */
object KuchoNotificationChannels {

    const val CANAL_GENERAL = "kucho_general"
    const val CANAL_SOLICITUDES = "kucho_solicitudes"
    const val CANAL_OFERTAS = "kucho_ofertas"

    fun crearCanales(context: Context) {
        val notificationManager =
            context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        val canales = listOf(
            NotificationChannel(
                CANAL_GENERAL,
                "Notificaciones generales",
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = "Avisos generales de la aplicacion Kucho"
            },
            NotificationChannel(
                CANAL_SOLICITUDES,
                "Mis solicitudes",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Actualizaciones sobre tus solicitudes de servicio tecnico"
                enableVibration(true)
            },
            NotificationChannel(
                CANAL_OFERTAS,
                "Ofertas recibidas",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Ofertas de tecnicos para tus solicitudes"
                enableVibration(true)
            }
        )

        canales.forEach { notificationManager.createNotificationChannel(it) }
    }
}
