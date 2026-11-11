package pe.kucho.app.data.local.db

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

class KuchoDbHelper private constructor(context: Context) :
    SQLiteOpenHelper(context.applicationContext, DATABASE_NAME, null, DATABASE_VERSION) {

    companion object {
        const val DATABASE_NAME = "kucho_local.db"
        const val DATABASE_VERSION = 1

        @Volatile
        private var instance: KuchoDbHelper? = null

        fun getInstance(context: Context): KuchoDbHelper {
            return instance ?: synchronized(this) {
                instance ?: KuchoDbHelper(context).also { instance = it }
            }
        }
    }

    override fun onCreate(db: SQLiteDatabase) {
        // Tabla de Categorías (Cache local SQLite)
        db.execSQL(
            """
            CREATE TABLE IF NOT EXISTS categorias (
                idCategoria TEXT PRIMARY KEY,
                nombre TEXT NOT NULL,
                slug TEXT,
                icono TEXT,
                activo INTEGER DEFAULT 1
            )
            """.trimIndent()
        )

        // Tabla de Solicitudes / Bolsa de Trabajos (Cache local SQLite)
        db.execSQL(
            """
            CREATE TABLE IF NOT EXISTS solicitudes_cache (
                idSolicitud TEXT PRIMARY KEY,
                descripcion TEXT,
                distrito TEXT,
                referenciaDireccion TEXT,
                estado TEXT,
                precioPactado REAL,
                fechaCreacion TEXT,
                fechaProgramada TEXT,
                idCliente TEXT,
                nombreCliente TEXT,
                telefonoCliente TEXT,
                idTecnico TEXT,
                nombreTecnico TEXT,
                telefonoTecnico TEXT,
                nombreCategoria TEXT
            )
            """.trimIndent()
        )
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        db.execSQL("DROP TABLE IF EXISTS categorias")
        db.execSQL("DROP TABLE IF EXISTS solicitudes_cache")
        onCreate(db)
    }
}
