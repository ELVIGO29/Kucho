package pe.kucho.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.navigation.compose.rememberNavController
import pe.kucho.app.data.local.SessionDataStore
import pe.kucho.app.navigation.KuchoNavGraph
import pe.kucho.app.navigation.Routes
import pe.kucho.app.ui.theme.KuchoTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            KuchoTheme {
                val sessionDataStore = remember { SessionDataStore(this) }
                val onboardingVisto by sessionDataStore.isOnboardingCompletado().collectAsState(initial = null)
                val userRol by sessionDataStore.getRol().collectAsState(initial = "CARGANDO")

                if (onboardingVisto == null || userRol == "CARGANDO") {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(Color.White),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(color = Color(0xFF6366F1))
                    }
                } else {
                    val navController = rememberNavController()
                    val tokenExistente = remember { sessionDataStore.getTokenBlocking() }
                    val startDest = when {
                        onboardingVisto == false -> Routes.Onboarding.route
                        !tokenExistente.isNullOrBlank() && userRol == "CLIENTE" -> Routes.ClienteHome.route
                        !tokenExistente.isNullOrBlank() && userRol == "TECNICO" -> Routes.TecnicoDashboard.route
                        !tokenExistente.isNullOrBlank() && userRol == "ADMIN" -> Routes.AdminDashboard.route
                        else -> Routes.Login.route
                    }
                    KuchoNavGraph(navController = navController, startDestination = startDest)
                }
            }
        }
    }
}
