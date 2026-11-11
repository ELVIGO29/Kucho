package pe.kucho.app.data.local

import android.content.Context
import android.content.SharedPreferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.runBlocking

/**
 * Gestiona la sesion del usuario con dos capas de almacenamiento:
 *
 * 1. EncryptedSharedPreferences (AES-256-GCM via Android Keystore):
 *    - JWT token: dato mas sensible, cifrado en hardware si el dispositivo lo soporta
 *
 * 2. DataStore Preferences (no cifrado pero no sensible):
 *    - rol, idUsuario, nombre: datos de UI, no son secretos de autenticacion
 */
private val Context.dataStore by preferencesDataStore(name = "kucho_session")

class SessionDataStore(private val context: Context) {

    companion object {
        // Claves DataStore (datos no sensibles de UI)
        val ROL_KEY = stringPreferencesKey("user_rol")
        val ID_USUARIO_KEY = stringPreferencesKey("user_id")
        val NOMBRE_KEY = stringPreferencesKey("user_nombre")
        val ONBOARDING_COMPLETADO_KEY = booleanPreferencesKey("onboarding_completado")

        // Clave SharedPreferences cifrado (token sensible)
        private const val PREF_NAME = "kucho_secure"
        private const val KEY_TOKEN = "jwt_token"
    }

    /**
     * EncryptedSharedPreferences con MasterKey.Builder (API correcta, no deprecada).
     * Usa AES256_GCM para valores y AES256_SIV para claves.
     * El MasterKey vive en Android Keystore Hardware (TEE/StrongBox si disponible).
     */
    private val encryptedPrefs: SharedPreferences by lazy {
        val masterKey = MasterKey.Builder(context)
            .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
            .build()

        EncryptedSharedPreferences.create(
            context,
            PREF_NAME,
            masterKey,
            EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
            EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
        )
    }

    /**
     * Guarda la sesion completa:
     * - token JWT en almacenamiento cifrado (EncryptedSharedPreferences)
     * - rol, id y nombre en DataStore (datos de UI no sensibles)
     */
    suspend fun guardarSesion(token: String, rol: String, idUsuario: String, nombre: String? = null) {
        // Guardar token cifrado (operacion sincrona pero rapida con Keystore)
        encryptedPrefs.edit()
            .putString(KEY_TOKEN, token)
            .apply()

        // Guardar metadatos de sesion en DataStore
        context.dataStore.edit { prefs ->
            prefs[ROL_KEY] = rol
            prefs[ID_USUARIO_KEY] = idUsuario
            prefs[NOMBRE_KEY] = nombre ?: ""
        }
    }

    /**
     * Obtiene el token JWT desde EncryptedSharedPreferences.
     * Bloqueante porque RetrofitClient lo necesita de forma sincrona en el interceptor.
     */
    fun getTokenBlocking(): String? {
        return try {
            encryptedPrefs.getString(KEY_TOKEN, null)
        } catch (e: Exception) {
            null
        }
    }

    /**
     * Flow reactivo del token (para observar cambios desde Compose si se necesita).
     */
    fun getToken(): Flow<String?> = context.dataStore.data.map {
        try { encryptedPrefs.getString(KEY_TOKEN, null) } catch (e: Exception) { null }
    }

    fun getNombre(): Flow<String?> = context.dataStore.data.map { it[NOMBRE_KEY] }
    fun getRol(): Flow<String?> = context.dataStore.data.map { it[ROL_KEY] }
    fun getIdUsuario(): Flow<String?> = context.dataStore.data.map { it[ID_USUARIO_KEY] }
    fun isOnboardingCompletado(): Flow<Boolean> = context.dataStore.data.map { it[ONBOARDING_COMPLETADO_KEY] ?: false }

    suspend fun setOnboardingCompletado(completado: Boolean = true) {
        context.dataStore.edit { prefs ->
            prefs[ONBOARDING_COMPLETADO_KEY] = completado
        }
    }

    /**
     * Cierra la sesion: borra el token cifrado y los datos de DataStore.
     */
    suspend fun cerrarSesion() {
        encryptedPrefs.edit().clear().apply()
        context.dataStore.edit { it.clear() }
    }
}
