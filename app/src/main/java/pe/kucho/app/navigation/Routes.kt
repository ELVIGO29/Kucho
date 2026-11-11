package pe.kucho.app.navigation

sealed class Routes(val route: String) {
    object Onboarding : Routes("onboarding")
    object Login : Routes("auth/login")
    object Registro : Routes("auth/registro")

    // Cliente
    object ClienteHome : Routes("cliente/home")
    object ClienteCategorias : Routes("cliente/categorias")
    object CrearSolicitud : Routes("cliente/solicitudes/crear?categoriaId={categoriaId}") {
        fun createRoute(categoriaId: String? = null) =
            if (!categoriaId.isNullOrBlank()) "cliente/solicitudes/crear?categoriaId=$categoriaId"
            else "cliente/solicitudes/crear"
    }
    object MisSolicitudes : Routes("cliente/solicitudes")
    object ClientePerfil : Routes("cliente/perfil")
    object PerfilTecnicoPublico : Routes("cliente/tecnico/{idTecnico}?nombre={nombre}") {
        fun createRoute(idTecnico: String, nombre: String? = null) =
            if (!nombre.isNullOrBlank()) "cliente/tecnico/$idTecnico?nombre=${android.net.Uri.encode(nombre)}"
            else "cliente/tecnico/$idTecnico"
    }
    object CrearResena : Routes("cliente/resenas/crear/{idSolicitud}") {
        fun createRoute(idSolicitud: String) = "cliente/resenas/crear/$idSolicitud"
    }

    // Técnico
    object TecnicoDashboard : Routes("tecnico/dashboard")
    object BolsaTrabajos : Routes("tecnico/trabajos/bolsa")
    object MisTrabajos : Routes("tecnico/trabajos/mis-trabajos")
    object Billetera : Routes("tecnico/billetera")
    object TecnicoPerfil : Routes("tecnico/perfil")

    // Admin
    object AdminDashboard : Routes("admin/dashboard")
}
