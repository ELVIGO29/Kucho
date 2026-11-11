package pe.kucho.app.data.model

sealed class UiState<out T> {
    /** La petición está en curso: mostrar indicador de progreso */
    object Cargando : UiState<Nothing>()

    /** La petición fue exitosa: contiene los datos obtenidos */
    data class Exito<T>(val datos: T) : UiState<T>()

    /** La petición falló con código 4xx/5xx: contiene el mensaje de error */
    data class Error(val mensaje: String) : UiState<Nothing>()

    /** No hay conexión a internet (IOException): mostrar aviso de sin internet */
    object SinConexion : UiState<Nothing>()

    /** El recurso fue eliminado exitosamente (para operaciones DELETE) */
    object EliminadoExitosamente : UiState<Nothing>()
}
