package pe.kucho.app.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import pe.kucho.app.ui.admin.dashboard.AdminDashboardScreen
import pe.kucho.app.ui.auth.LoginScreen
import pe.kucho.app.ui.auth.RegistroScreen
import pe.kucho.app.ui.cliente.ClienteCategoriasScreen
import pe.kucho.app.ui.cliente.ClienteHomeScreen
import pe.kucho.app.ui.cliente.ClientePerfilScreen
import pe.kucho.app.ui.cliente.CrearResenaScreen
import pe.kucho.app.ui.cliente.CrearSolicitudScreen
import pe.kucho.app.ui.cliente.MisSolicitudesScreen
import pe.kucho.app.ui.cliente.PerfilTecnicoPublicoScreen
import pe.kucho.app.ui.onboarding.OnboardingScreen
import pe.kucho.app.ui.tecnico.BilleteraScreen
import pe.kucho.app.ui.tecnico.BolsaTrabajosScreen
import pe.kucho.app.ui.tecnico.MisTrabajosScreen
import pe.kucho.app.ui.tecnico.TecnicoDashboardScreen
import pe.kucho.app.ui.tecnico.TecnicoPerfilScreen

@Composable
fun KuchoNavGraph(
    navController: NavHostController,
    startDestination: String = Routes.Login.route
) {
    NavHost(
        navController = navController,
        startDestination = startDestination
    ) {
        // --- ONBOARDING INTERACTIVO ---
        composable(Routes.Onboarding.route) {
            OnboardingScreen(
                onCompletado = {
                    navController.navigate(Routes.Login.route) {
                        popUpTo(Routes.Onboarding.route) { inclusive = true }
                    }
                }
            )
        }

        // --- AUTH ---
        composable(Routes.Login.route) {
            LoginScreen(
                onLoginSuccess = { rol, _ ->
                    when (rol) {
                        "CLIENTE" -> navController.navigate(Routes.ClienteHome.route) {
                            popUpTo(Routes.Login.route) { inclusive = true }
                        }
                        "TECNICO" -> navController.navigate(Routes.TecnicoDashboard.route) {
                            popUpTo(Routes.Login.route) { inclusive = true }
                        }
                        "ADMIN" -> navController.navigate(Routes.AdminDashboard.route) {
                            popUpTo(Routes.Login.route) { inclusive = true }
                        }
                    }
                },
                onNavigateToRegister = {
                    navController.navigate(Routes.Registro.route)
                },
                onVerGuia = {
                    navController.navigate(Routes.Onboarding.route)
                }
            )
        }

        composable(Routes.Registro.route) {
            RegistroScreen(
                onBackToLogin = { navController.popBackStack() },
                onRegisterSuccess = {
                    navController.navigate(Routes.Login.route) {
                        popUpTo(Routes.Registro.route) { inclusive = true }
                    }
                }
            )
        }

        // --- CLIENTE ---
        composable(Routes.ClienteHome.route) {
            ClienteHomeScreen(
                onCrearSolicitud = { categoriaId ->
                    navController.navigate(Routes.CrearSolicitud.createRoute(categoriaId))
                },
                onVerMisSolicitudes = { navController.navigate(Routes.MisSolicitudes.route) },
                onVerCategorias = { navController.navigate(Routes.ClienteCategorias.route) },
                onVerPerfil = { navController.navigate(Routes.ClientePerfil.route) }
            )
        }

        composable(Routes.ClienteCategorias.route) {
            ClienteCategoriasScreen(
                onBack = { navController.popBackStack() },
                onPedirServicio = { categoriaId ->
                    navController.navigate(Routes.CrearSolicitud.createRoute(categoriaId))
                }
            )
        }

        composable(
            route = Routes.CrearSolicitud.route,
            arguments = listOf(
                navArgument("categoriaId") {
                    type = NavType.StringType
                    nullable = true
                    defaultValue = null
                }
            )
        ) { backStackEntry ->
            val categoriaId = backStackEntry.arguments?.getString("categoriaId")
            CrearSolicitudScreen(
                categoriaIdInicial = categoriaId,
                onBack = { navController.popBackStack() }
            )
        }

        composable(Routes.MisSolicitudes.route) {
            MisSolicitudesScreen(
                onBack = { navController.popBackStack() },
                onNuevaSolicitud = {
                    navController.navigate(Routes.CrearSolicitud.createRoute(null))
                },
                onCrearResena = { idSolicitud, _ ->
                    navController.navigate(Routes.CrearResena.createRoute(idSolicitud))
                },
                onVerPerfilTecnico = { idTecnico, nombre ->
                    navController.navigate(Routes.PerfilTecnicoPublico.createRoute(idTecnico, nombre))
                }
            )
        }

        composable(
            route = Routes.PerfilTecnicoPublico.route,
            arguments = listOf(
                navArgument("idTecnico") { type = NavType.StringType },
                navArgument("nombre") {
                    type = NavType.StringType
                    nullable = true
                    defaultValue = null
                }
            )
        ) { backStackEntry ->
            val idTecnico = backStackEntry.arguments?.getString("idTecnico") ?: ""
            val nombre = backStackEntry.arguments?.getString("nombre")
            PerfilTecnicoPublicoScreen(
                idTecnico = idTecnico,
                nombreInicial = nombre,
                onBack = { navController.popBackStack() },
                onSolicitarServicio = {
                    navController.navigate(Routes.CrearSolicitud.createRoute(null))
                }
            )
        }

        composable(Routes.ClientePerfil.route) {
            ClientePerfilScreen(
                onBack = { navController.popBackStack() },
                onLogout = {
                    navController.navigate(Routes.Login.route) {
                        popUpTo(0) { inclusive = true }
                    }
                }
            )
        }

        composable(
            route = Routes.CrearResena.route,
            arguments = listOf(navArgument("idSolicitud") { type = NavType.StringType })
        ) { backStackEntry ->
            val idSolicitud = backStackEntry.arguments?.getString("idSolicitud") ?: ""
            CrearResenaScreen(
                idSolicitud = idSolicitud,
                onBack = { navController.popBackStack() }
            )
        }

        // --- TECNICO ---
        composable(Routes.TecnicoDashboard.route) {
            TecnicoDashboardScreen(
                onBilletera = { navController.navigate(Routes.Billetera.route) },
                onBolsa = { navController.navigate(Routes.BolsaTrabajos.route) },
                onMisTrabajos = { navController.navigate(Routes.MisTrabajos.route) },
                onPerfil = { navController.navigate(Routes.TecnicoPerfil.route) }
            )
        }

        composable(Routes.BolsaTrabajos.route) {
            BolsaTrabajosScreen(
                onBack = { navController.popBackStack() },
                onIrBilletera = { navController.navigate(Routes.Billetera.route) },
                onAceptarExito = { navController.popBackStack() }
            )
        }

        composable(Routes.MisTrabajos.route) {
            MisTrabajosScreen(
                onBack = { navController.popBackStack() }
            )
        }

        composable(Routes.Billetera.route) {
            BilleteraScreen(
                onBack = { navController.popBackStack() }
            )
        }

        composable(Routes.TecnicoPerfil.route) {
            TecnicoPerfilScreen(
                onBack = { navController.popBackStack() },
                onLogout = {
                    navController.navigate(Routes.Login.route) {
                        popUpTo(0) { inclusive = true }
                    }
                }
            )
        }

        // --- ADMIN ---
        composable(Routes.AdminDashboard.route) {
            AdminDashboardScreen(
                onLogout = {
                    navController.navigate(Routes.Login.route) {
                        popUpTo(0) { inclusive = true }
                    }
                }
            )
        }
    }
}
