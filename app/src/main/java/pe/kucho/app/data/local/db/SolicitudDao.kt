package pe.kucho.app.data.local.db

import android.content.ContentValues
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import pe.kucho.app.data.model.SolicitudResponse

class SolicitudDao(private val dbHelper: KuchoDbHelper) {

    fun guardarSolicitudes(solicitudes: List<SolicitudResponse>) {
        val db = dbHelper.writableDatabase
        db.beginTransaction()
        try {
            for (sol in solicitudes) {
                val values = ContentValues().apply {
                    put("idSolicitud", sol.idSolicitud)
                    put("descripcion", sol.descripcion)
                    put("distrito", sol.distrito)
                    put("referenciaDireccion", sol.referenciaDireccion)
                    put("estado", sol.estado)
                    put("precioPactado", sol.precioPactado)
                    put("fechaCreacion", sol.fechaCreacion)
                    put("fechaProgramada", sol.fechaProgramada)
                    put("idCliente", sol.idCliente)
                    put("nombreCliente", sol.nombreCliente)
                    put("telefonoCliente", sol.telefonoCliente)
                    put("idTecnico", sol.idTecnico)
                    put("nombreTecnico", sol.nombreTecnico)
                    put("telefonoTecnico", sol.telefonoTecnico)
                    put("nombreCategoria", sol.nombreCategoria)
                }
                db.insertWithOnConflict("solicitudes_cache", null, values, SQLiteDatabase.CONFLICT_REPLACE)
            }
            db.setTransactionSuccessful()
        } finally {
            db.endTransaction()
        }
    }

    fun obtenerSolicitudes(): List<SolicitudResponse> {
        val lista = mutableListOf<SolicitudResponse>()
        val db = dbHelper.readableDatabase
        val cursor: Cursor = db.query(
            "solicitudes_cache",
            null,
            null,
            null,
            null,
            null,
            "fechaCreacion DESC"
        )
        cursor.use {
            while (it.moveToNext()) {
                val idSolicitud = it.getString(it.getColumnIndexOrThrow("idSolicitud"))
                val descripcion = it.getString(it.getColumnIndexOrThrow("descripcion"))
                val distrito = it.getString(it.getColumnIndexOrThrow("distrito"))
                val referenciaDireccion = if (it.isNull(it.getColumnIndexOrThrow("referenciaDireccion"))) null else it.getString(it.getColumnIndexOrThrow("referenciaDireccion"))
                val estado = it.getString(it.getColumnIndexOrThrow("estado"))
                val precioPactado = if (it.isNull(it.getColumnIndexOrThrow("precioPactado"))) null else it.getDouble(it.getColumnIndexOrThrow("precioPactado"))
                val fechaCreacion = it.getString(it.getColumnIndexOrThrow("fechaCreacion"))
                val fechaProgramada = if (it.isNull(it.getColumnIndexOrThrow("fechaProgramada"))) null else it.getString(it.getColumnIndexOrThrow("fechaProgramada"))
                val idCliente = it.getString(it.getColumnIndexOrThrow("idCliente"))
                val nombreCliente = if (it.isNull(it.getColumnIndexOrThrow("nombreCliente"))) null else it.getString(it.getColumnIndexOrThrow("nombreCliente"))
                val telefonoCliente = if (it.isNull(it.getColumnIndexOrThrow("telefonoCliente"))) null else it.getString(it.getColumnIndexOrThrow("telefonoCliente"))
                val idTecnico = if (it.isNull(it.getColumnIndexOrThrow("idTecnico"))) null else it.getString(it.getColumnIndexOrThrow("idTecnico"))
                val nombreTecnico = if (it.isNull(it.getColumnIndexOrThrow("nombreTecnico"))) null else it.getString(it.getColumnIndexOrThrow("nombreTecnico"))
                val telefonoTecnico = if (it.isNull(it.getColumnIndexOrThrow("telefonoTecnico"))) null else it.getString(it.getColumnIndexOrThrow("telefonoTecnico"))
                val nombreCategoria = it.getString(it.getColumnIndexOrThrow("nombreCategoria"))

                lista.add(
                    SolicitudResponse(
                        idSolicitud = idSolicitud,
                        descripcion = descripcion,
                        distrito = distrito,
                        referenciaDireccion = referenciaDireccion,
                        estado = estado,
                        precioPactado = precioPactado,
                        fechaCreacion = fechaCreacion,
                        fechaProgramada = fechaProgramada,
                        idCliente = idCliente,
                        nombreCliente = nombreCliente,
                        telefonoCliente = telefonoCliente,
                        idTecnico = idTecnico,
                        nombreTecnico = nombreTecnico,
                        telefonoTecnico = telefonoTecnico,
                        nombreCategoria = nombreCategoria
                    )
                )
            }
        }
        return lista
    }
}
