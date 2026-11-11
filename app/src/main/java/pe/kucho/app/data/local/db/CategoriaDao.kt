package pe.kucho.app.data.local.db

import android.content.ContentValues
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import pe.kucho.app.data.model.Categoria

class CategoriaDao(private val dbHelper: KuchoDbHelper) {

    fun guardarCategorias(categorias: List<Categoria>) {
        val db = dbHelper.writableDatabase
        db.beginTransaction()
        try {
            db.delete("categorias", null, null)
            for (cat in categorias) {
                val values = ContentValues().apply {
                    put("idCategoria", cat.idCategoria)
                    put("nombre", cat.nombre)
                    put("slug", cat.slug)
                    put("icono", cat.icono)
                    put("activo", if (cat.activo) 1 else 0)
                }
                db.insertWithOnConflict("categorias", null, values, SQLiteDatabase.CONFLICT_REPLACE)
            }
            db.setTransactionSuccessful()
        } finally {
            db.endTransaction()
        }
    }

    fun obtenerCategorias(): List<Categoria> {
        val lista = mutableListOf<Categoria>()
        val db = dbHelper.readableDatabase
        val cursor: Cursor = db.query(
            "categorias",
            null,
            null,
            null,
            null,
            null,
            "nombre ASC"
        )
        cursor.use {
            while (it.moveToNext()) {
                val idCategoria = it.getString(it.getColumnIndexOrThrow("idCategoria"))
                val nombre = it.getString(it.getColumnIndexOrThrow("nombre"))
                val slug = if (it.isNull(it.getColumnIndexOrThrow("slug"))) null else it.getString(it.getColumnIndexOrThrow("slug"))
                val icono = if (it.isNull(it.getColumnIndexOrThrow("icono"))) null else it.getString(it.getColumnIndexOrThrow("icono"))
                val activo = it.getInt(it.getColumnIndexOrThrow("activo")) == 1
                lista.add(Categoria(idCategoria, nombre, slug, icono, activo))
            }
        }
        return lista
    }
}
