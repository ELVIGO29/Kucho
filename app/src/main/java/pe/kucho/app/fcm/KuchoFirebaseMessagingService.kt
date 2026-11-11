package pe.kucho.app.fcm

import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Intent
import androidx.core.app.NotificationCompat
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import pe.kucho.app.MainActivity
import pe.kucho.app.R
import pe.kucho.app.data.local.SessionDataStore
import pe.kucho.app.data.network.RetrofitClient

/**
 * Servicio de Firebase Cloud Messaging para Kucho.
 *
 * Gestiona dos eventos:
 * 1. onNewToken: cuando Firebase asigna/renueva el token del dispositivo.
 *    -> Registra el token en el backend para que pueda enviarnos notificaciones.
 *
 * 2. onMessageReceived: cuando llega una notificacion push.
 *    -> Muestra una notificacion en la bandeja del sistema con el canal correcto.
 */
class KuchoFirebaseMessagingService : FirebaseMessagingService() {

    /**
     * Se llama cuando Firebase genera o renueva el token del dispositivo.
     * Registra el nuevo token en el backend via POST /api/v1/usuarios/fcm-token.
     */
    override fun onNewToken(token: String) {
        super.onNewToken(token)
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val sessionDataStore = SessionDataStore(applicationContext)
                val jwtToken = sessionDataStore.getTokenBlocking()

                // Solo registrar si hay sesion activa
                if (!jwtToken.isNullOrBlank()) {
                    RetrofitClient.apiService.registrarFcmToken(mapOf("fcmToken" to token))
                }
            } catch (e: Exception) {
                // Silenciar: el token se registrara en el proximo login
            }
        }
    }

    /**
     * Se llama cuando la app esta en primer plano y llega una notificacion push.
     * Cuando la app esta en background, FCM muestra la notificacion automaticamente.
     */
    override fun onMessageReceived(message: RemoteMessage) {
        super.onMessageReceived(message)

        val titulo = message.notification?.title ?: message.data["title"] ?: "Kucho"
        val cuerpo = message.notification?.body ?: message.data["body"] ?: ""
        val tipo = message.data["tipo"] ?: "general"

        mostrarNotificacion(titulo, cuerpo, tipo)
    }

    private fun mostrarNotificacion(titulo: String, cuerpo: String, tipo: String) {
        // Intent para abrir la app al tocar la notificacion
        val intent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            putExtra("notification_tipo", tipo)
        }

        val pendingIntent = PendingIntent.getActivity(
            this,
            System.currentTimeMillis().toInt(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        // Seleccionar canal segun el tipo de notificacion
        val channelId = when (tipo) {
            "nueva_oferta" -> KuchoNotificationChannels.CANAL_OFERTAS
            "solicitud_aceptada" -> KuchoNotificationChannels.CANAL_SOLICITUDES
            "nueva_solicitud" -> KuchoNotificationChannels.CANAL_SOLICITUDES
            else -> KuchoNotificationChannels.CANAL_GENERAL
        }

        val notification = NotificationCompat.Builder(this, channelId)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentTitle(titulo)
            .setContentText(cuerpo)
            .setStyle(NotificationCompat.BigTextStyle().bigText(cuerpo))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .build()

        val notificationManager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.notify(System.currentTimeMillis().toInt(), notification)
    }
}
