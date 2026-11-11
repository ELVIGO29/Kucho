package pe.kucho.app

import android.app.Application
import pe.kucho.app.data.local.SessionDataStore
import pe.kucho.app.data.local.db.KuchoDbHelper
import pe.kucho.app.data.network.RetrofitClient
import pe.kucho.app.fcm.KuchoNotificationChannels

class KuchoApplication : Application() {

    companion object {
        lateinit var instance: KuchoApplication
            private set
    }

    override fun onCreate() {
        super.onCreate()
        instance = this

        // Inicializar persistencia local SQLite (cache offline)
        KuchoDbHelper.getInstance(this)

        // Inicializar Retrofit con el proveedor de Token cifrado de la sesion
        val sessionDataStore = SessionDataStore(this)
        RetrofitClient.init {
            sessionDataStore.getTokenBlocking()
        }

        // Crear canales de notificacion push (obligatorio Android 8+)
        KuchoNotificationChannels.crearCanales(this)
    }
}
