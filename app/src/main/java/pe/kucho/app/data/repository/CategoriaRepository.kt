package pe.kucho.app.data.repository

import java.io.IOException
import pe.kucho.app.KuchoApplication
import pe.kucho.app.data.local.db.CategoriaDao
import pe.kucho.app.data.local.db.KuchoDbHelper
import pe.kucho.app.data.model.Categoria
import pe.kucho.app.data.network.RetrofitClient

class CategoriaRepository {
    private val api = RetrofitClient.apiService
    private val categoriaDao by lazy {
        try {
            CategoriaDao(KuchoDbHelper.getInstance(KuchoApplication.instance))
        } catch (_: Exception) {
            null
        }
    }

    /**
     * Lista todas las categorías disponibles.
     * En caso de error o sin conexión (IOException), usa el caché local SQLite.
     */
    suspend fun listarCategorias(): Result<List<Categoria>> {
        return try {
            val res = api.listarCategorias()
            if (res.isSuccessful && res.body() != null) {
                val categorias = res.body()!!
                // Persistencia local en SQLite
                categoriaDao?.guardarCategorias(categorias)
                Result.success(categorias)
            } else {
                // Respaldo de persistencia local SQLite si el backend responde con error
                val cached = categoriaDao?.obtenerCategorias() ?: emptyList()
                if (cached.isNotEmpty()) Result.success(cached)
                else Result.failure(Exception("Error al obtener categorías (${res.code()})"))
            }
        } catch (e: IOException) {
            // Sin conexión: usar caché local SQLite
            val cached = categoriaDao?.obtenerCategorias() ?: emptyList()
            if (cached.isNotEmpty()) Result.success(cached)
            else Result.failure(Exception("Sin conexión y sin datos en caché."))
        } catch (e: Exception) {
            // Respaldo de persistencia local SQLite en caso de otro error
            val cached = categoriaDao?.obtenerCategorias() ?: emptyList()
            if (cached.isNotEmpty()) Result.success(cached)
            else Result.failure(e)
        }
    }
}
