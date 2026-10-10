package com.example.cajita.data.local.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.cajita.data.local.dao.CajitaDao
import com.example.cajita.data.local.entity.CierreLocal
import com.example.cajita.data.local.entity.ConteoLocal
import com.example.cajita.data.local.entity.EgresoLocal
import com.example.cajita.data.local.entity.MovimientoLocal
import com.example.cajita.data.local.entity.ProveedorLocal
import com.example.cajita.data.local.entity.TurnoLocal
import com.example.cajita.data.local.entity.VentaLocal

@Database(
    entities = [
        TurnoLocal::class,
        ConteoLocal::class,
        MovimientoLocal::class,
        CierreLocal::class,
        VentaLocal::class,
        EgresoLocal::class,
        ProveedorLocal::class
    ],
    version = 2,
    exportSchema = false
)
abstract class CajitaDatabase : RoomDatabase() {

    abstract fun cajitaDao(): CajitaDao

    companion object {
        @Volatile
        private var INSTANCE: CajitaDatabase? = null

        val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("""
                    CREATE TABLE IF NOT EXISTS `ventas` (
                        `id` TEXT NOT NULL,
                        `ventaId` TEXT NOT NULL,
                        `version` INTEGER NOT NULL,
                        `eliminada` INTEGER NOT NULL,
                        `turnoId` TEXT NOT NULL,
                        `ocurridoEn` INTEGER NOT NULL,
                        `registradoEn` INTEGER NOT NULL,
                        `medio` TEXT NOT NULL,
                        `totalVenta` INTEGER NOT NULL,
                        `montoDigital` INTEGER NOT NULL,
                        `efectivoEntrada` INTEGER NOT NULL,
                        `efectivoSalida` INTEGER NOT NULL,
                        `billetesEntrada` TEXT NOT NULL,
                        `billetesSalida` TEXT NOT NULL,
                        `nota` TEXT,
                        `sincronizado` INTEGER NOT NULL,
                        PRIMARY KEY(`id`)
                    )
                """.trimIndent())

                db.execSQL("""
                    CREATE TABLE IF NOT EXISTS `egresos` (
                        `id` TEXT NOT NULL,
                        `egresoId` TEXT NOT NULL,
                        `version` INTEGER NOT NULL,
                        `eliminado` INTEGER NOT NULL,
                        `turnoId` TEXT NOT NULL,
                        `ocurridoEn` INTEGER NOT NULL,
                        `registradoEn` INTEGER NOT NULL,
                        `tipo` TEXT NOT NULL,
                        `origen` TEXT NOT NULL,
                        `monto` INTEGER NOT NULL,
                        `billetes` TEXT NOT NULL,
                        `proveedor` TEXT,
                        `nota` TEXT,
                        `sincronizado` INTEGER NOT NULL,
                        PRIMARY KEY(`id`)
                    )
                """.trimIndent())

                db.execSQL("""
                    CREATE TABLE IF NOT EXISTS `proveedores` (
                        `nombre` TEXT NOT NULL,
                        `ultimoUso` INTEGER NOT NULL,
                        PRIMARY KEY(`nombre`)
                    )
                """.trimIndent())

                db.execSQL("ALTER TABLE `cierres` ADD COLUMN `digitalCargado` TEXT NOT NULL DEFAULT '{}'")
                db.execSQL("ALTER TABLE `cierres` ADD COLUMN `digitalEditado` TEXT NOT NULL DEFAULT '{}'")
                db.execSQL("ALTER TABLE `cierres` ADD COLUMN `ventasDigitalesCargadas` INTEGER NOT NULL DEFAULT 0")
                db.execSQL("ALTER TABLE `cierres` ADD COLUMN `ventasDigitales` INTEGER NOT NULL DEFAULT 0")
                db.execSQL("ALTER TABLE `cierres` ADD COLUMN `totalDigitalCargado` INTEGER NOT NULL DEFAULT 0")
                db.execSQL("ALTER TABLE `cierres` ADD COLUMN `totalDigital` INTEGER NOT NULL DEFAULT 0")
                db.execSQL("ALTER TABLE `cierres` ADD COLUMN `diferenciaDigital` INTEGER NOT NULL DEFAULT 0")
                db.execSQL("ALTER TABLE `cierres` ADD COLUMN `ventasEfectivo` INTEGER NOT NULL DEFAULT 0")
                db.execSQL("ALTER TABLE `cierres` ADD COLUMN `totalVentasEfectivo` INTEGER NOT NULL DEFAULT 0")
                db.execSQL("ALTER TABLE `cierres` ADD COLUMN `totalRetiros` INTEGER NOT NULL DEFAULT 0")
                db.execSQL("ALTER TABLE `cierres` ADD COLUMN `totalProveedores` INTEGER NOT NULL DEFAULT 0")
            }
        }

        fun getDatabase(context: Context): CajitaDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    CajitaDatabase::class.java,
                    "cajita_database"
                )
                .addMigrations(MIGRATION_1_2)
                .fallbackToDestructiveMigration()
                .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
