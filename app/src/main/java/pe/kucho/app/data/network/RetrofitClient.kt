package pe.kucho.app.data.network

import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

object RetrofitClient {

    /**
     * URL base del backend Spring Boot.
     *
     * EMULADOR Android Studio : http://10.0.2.2:8080/
     * DISPOSITIVO FISICO (mismo Wi-Fi que el PC): http://192.168.1.13:8080/
     *
     * Si cambias de red o el backend tiene otra IP, actualiza esta constante.
     */
    private const val BASE_URL = "http://10.0.2.2:8080/"

    private var tokenProvider: (() -> String?)? = null

    fun init(tokenProvider: () -> String?) {
        this.tokenProvider = tokenProvider
    }

    private val authInterceptor = Interceptor { chain ->
        val original = chain.request()
        val rawToken = tokenProvider?.invoke()

        val requestBuilder = original.newBuilder().header("ngrok-skip-browser-warning", "true")
        if (!rawToken.isNullOrBlank()) {
            // Elimina cualquier "Bearer " repetido y asegura el formato correcto
            val cleanToken = rawToken.removePrefix("Bearer ").trim()
            requestBuilder.header("Authorization", "Bearer $cleanToken")
        }

        chain.proceed(requestBuilder.build())
    }

    private val loggingInterceptor = HttpLoggingInterceptor().apply {
        // Solo loggea el body en debug — en release evita exponer tokens en Logcat
        level = if (pe.kucho.app.BuildConfig.DEBUG) HttpLoggingInterceptor.Level.BODY
                else HttpLoggingInterceptor.Level.NONE
    }

    private val okHttpClient = OkHttpClient.Builder()
        .addInterceptor(authInterceptor)
        .addInterceptor(loggingInterceptor)
        .connectTimeout(15, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .writeTimeout(15, TimeUnit.SECONDS)
        .build()

    val apiService: ApiService by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(ApiService::class.java)
    }
}

