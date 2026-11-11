package pe.kucho.app.ui.cliente

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import pe.kucho.app.data.repository.ResenaRepository

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CrearResenaScreen(
    idSolicitud: String,
    resenaRepository: ResenaRepository = ResenaRepository(),
    onBack: () -> Unit
) {
    var puntuacion by remember { mutableStateOf(5) }
    var comentario by remember { mutableStateOf("") }
    var cargando by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Calificar Servicio") },
                navigationIcon = { TextButton(onClick = onBack) { Text("< Volver") } }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text("Puntuación (1 a 5 estrellas):", style = MaterialTheme.typography.titleMedium)

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                (1..5).forEach { estrella ->
                    Button(
                        onClick = { puntuacion = estrella },
                        colors = if (puntuacion >= estrella) ButtonDefaults.buttonColors() else ButtonDefaults.outlinedButtonColors()
                    ) {
                        Text("$estrella ★")
                    }
                }
            }

            OutlinedTextField(
                value = comentario,
                onValueChange = { comentario = it },
                label = { Text("Comentario u opinión sobre el trabajo realizado") },
                modifier = Modifier.fillMaxWidth().height(120.dp)
            )

            Button(
                onClick = {
                    cargando = true
                    scope.launch {
                        resenaRepository.crearResena(idSolicitud, puntuacion, comentario.ifBlank { null })
                            .onSuccess { onBack() }
                        cargando = false
                    }
                },
                enabled = !cargando,
                modifier = Modifier.fillMaxWidth()
            ) {
                if (cargando) CircularProgressIndicator(modifier = Modifier.size(24.dp))
                else Text("Enviar Calificación")
            }
        }
    }
}
