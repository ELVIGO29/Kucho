package pe.kucho.app.data.network

import pe.kucho.app.data.model.*
import retrofit2.Response
import retrofit2.http.*

interface ApiService {

    // --- FCM TOKEN ---
    @POST("api/v1/usuarios/fcm-token")
    suspend fun registrarFcmToken(@Body body: Map<String, String>): retrofit2.Response<Unit>


    // --- AUTENTICACIÓN ---
    @POST("api/v1/auth/otp/solicitar")
    suspend fun solicitarOtp(@Body request: AuthOtpRequest): Response<Unit>

    @POST("api/v1/auth/otp/verificar")
    suspend fun verificarOtp(@Body request: VerificarOtpRequest): Response<AuthResponse>

    @POST("api/v1/auth/registro")
    suspend fun registrarUsuario(@Body request: RegistroUsuarioRequest): Response<AuthResponse>

    // --- CLIENTE PERFIL ---
    @GET("api/v1/clientes/perfil")
    suspend fun obtenerMiPerfilCliente(): Response<ClientePerfilResponse>

    @PUT("api/v1/clientes/perfil")
    suspend fun actualizarMiPerfilCliente(@Body request: ActualizarClienteRequest): Response<ClientePerfilResponse>

    // --- CATEGORÍAS ---
    @GET("api/v1/categorias")
    suspend fun listarCategorias(): Response<List<Categoria>>

    @GET("api/v1/categorias/{slug}")
    suspend fun obtenerCategoriaPorSlug(@Path("slug") slug: String): Response<Categoria>

    // --- TÉCNICOS ---
    /**
     * Lista técnicos activos con paginación para evitar cargar toda la base de datos.
     * Usa @Query("page") y @Query("size") para controlar qué página y cuántos elementos.
     */
    @GET("api/v1/tecnicos")
    suspend fun obtenerTecnicosActivos(
        @Query("page") page: Int = 0,
        @Query("size") size: Int = 20
    ): Response<List<TecnicoResponse>>

    @GET("api/v1/tecnicos/perfil")
    suspend fun obtenerMiPerfilTecnico(): Response<TecnicoResponse>

    @PUT("api/v1/tecnicos/perfil")
    suspend fun actualizarMiPerfilTecnico(@Body request: ActualizarPerfilTecnicoRequest): Response<TecnicoResponse>

    @PUT("api/v1/tecnicos/documento")
    suspend fun registrarDocumentoTecnico(@Body request: RegistroDocumentoRequest): Response<TecnicoResponse>

    // --- SOLICITUDES (CLIENTE Y TÉCNICO) ---
    @POST("api/v1/solicitudes")
    suspend fun crearSolicitud(@Body request: CrearSolicitudRequest): Response<SolicitudResponse>

    /**
     * Lista las solicitudes del cliente autenticado con paginación.
     * Parámetros: page (0-based) y size (elementos por página).
     */
    @GET("api/v1/solicitudes/mis-solicitudes")
    suspend fun obtenerMisSolicitudesCliente(
        @Query("page") page: Int = 0,
        @Query("size") size: Int = 20
    ): Response<List<SolicitudResponse>>

    @DELETE("api/v1/solicitudes/{id}/cancelar")
    suspend fun cancelarSolicitud(@Path("id") idSolicitud: String): Response<Unit>

    /**
     * Lista solicitudes disponibles para técnicos con filtro opcional por distrito.
     * La paginación permite que el técnico cargue más resultados bajo demanda.
     */
    @GET("api/v1/solicitudes/disponibles")
    suspend fun obtenerSolicitudesDisponibles(
        @Query("distrito") distrito: String? = null,
        @Query("page") page: Int = 0,
        @Query("size") size: Int = 20
    ): Response<List<SolicitudResponse>>

    /**
     * Lista los trabajos activos/finalizados del técnico autenticado con paginación.
     */
    @GET("api/v1/solicitudes/mis-trabajos")
    suspend fun obtenerMisTrabajosTecnico(
        @Query("page") page: Int = 0,
        @Query("size") size: Int = 20
    ): Response<List<SolicitudResponse>>

    @POST("api/v1/solicitudes/{id}/aceptar")
    suspend fun aceptarSolicitud(@Path("id") idSolicitud: String): Response<SolicitudResponse>

    @PUT("api/v1/solicitudes/{id}/estado")
    suspend fun actualizarEstadoSolicitud(
        @Path("id") idSolicitud: String,
        @Body request: ActualizarEstadoSolicitudRequest
    ): Response<SolicitudResponse>

    @GET("api/v1/solicitudes/{id}")
    suspend fun obtenerSolicitudPorId(@Path("id") idSolicitud: String): Response<SolicitudResponse>

    // --- NEGOCIACIONES ---
    @POST("api/v1/negociaciones/solicitud/{idSolicitud}/ofertar")
    suspend fun ofertarComoTecnico(
        @Path("idSolicitud") idSolicitud: String,
        @Body request: OfertarPrecioRequest
    ): Response<NegociacionResponse>

    @PUT("api/v1/negociaciones/{idNegociacion}/contraofertar")
    suspend fun contraofertarComoCliente(
        @Path("idNegociacion") idNegociacion: String,
        @Body request: OfertarPrecioRequest
    ): Response<NegociacionResponse>

    @PUT("api/v1/negociaciones/{idNegociacion}/aceptar")
    suspend fun aceptarNegociacion(@Path("idNegociacion") idNegociacion: String): Response<NegociacionResponse>

    @PUT("api/v1/negociaciones/{idNegociacion}/rechazar")
    suspend fun rechazarNegociacion(@Path("idNegociacion") idNegociacion: String): Response<NegociacionResponse>

    @GET("api/v1/negociaciones/solicitud/{idSolicitud}")
    suspend fun obtenerNegociacionesPorSolicitud(@Path("idSolicitud") idSolicitud: String): Response<List<NegociacionResponse>>

    @GET("api/v1/negociaciones/solicitud/{idSolicitud}/mi-negociacion")
    suspend fun obtenerMiNegociacion(@Path("idSolicitud") idSolicitud: String): Response<NegociacionResponse>

    // --- BILLETERA ---
    @GET("api/v1/billetera")
    suspend fun obtenerMiBilletera(): Response<BilleteraResponse>

    @POST("api/v1/billetera/recargar")
    suspend fun recargarBilletera(@Body request: RecargarBilleteraRequest): Response<BilleteraResponse>

    // --- RESEÑAS ---
    @POST("api/v1/resenas")
    suspend fun crearResena(@Body request: CrearResenaRequest): Response<ResenaResponse>

    /**
     * Lista reseñas de un técnico con paginación para no cargar todas de golpe.
     */
    @GET("api/v1/resenas/tecnico/{idTecnico}")
    suspend fun obtenerResenasPorTecnico(
        @Path("idTecnico") idTecnico: String,
        @Query("page") page: Int = 0,
        @Query("size") size: Int = 10
    ): Response<List<ResenaResponse>>

    // --- ADMIN ---
    @GET("api/v1/admin/estadisticas")
    suspend fun obtenerEstadisticasDashboard(): Response<EstadisticasDashboard>

    @PUT("api/v1/admin/tecnicos/{id}/verificar")
    suspend fun verificarTecnico(
        @Path("id") idTecnico: String,
        @Body request: VerificarTecnicoRequest
    ): Response<TecnicoResponse>

    @POST("api/v1/admin/categorias")
    suspend fun crearCategoria(
        @Body request: CrearCategoriaRequest
    ): Response<Categoria>

    @PUT("api/v1/admin/categorias/{id}")
    suspend fun actualizarCategoria(
        @Path("id") idCategoria: String,
        @Body request: CrearCategoriaRequest
    ): Response<Categoria>

    @PUT("api/v1/admin/categorias/{id}/estado")
    suspend fun cambiarEstadoCategoria(
        @Path("id") idCategoria: String,
        @Query("activo") activo: Boolean
    ): Response<Unit>

    @GET("api/v1/admin/solicitudes")
    suspend fun obtenerSolicitudesAdmin(
        @Query("page") page: Int = 0,
        @Query("size") size: Int = 20
    ): Response<PageResponse<SolicitudResponse>>
}

