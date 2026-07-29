package com.serhio.money.data.database

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import androidx.room.migration.Migration
import com.serhio.money.data.database.entities.AlertEntity
import com.serhio.money.data.database.entities.CurrencyHistoryEntity

@Database(
    entities = [CurrencyHistoryEntity::class, AlertEntity::class],
    version = 5,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun currencyDao(): CurrencyDao
    abstract fun alertDao(): AlertDao

    companion object {
        const val DATABASE_NAME = "money_db"

        val MIGRATION_2_3 = object : Migration(2, 3) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("DELETE FROM currency_history")
            }
        }

        val MIGRATION_3_4 = object : Migration(3, 4) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("""
                    CREATE TABLE IF NOT EXISTS alerts (
                        id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        currencyCode TEXT NOT NULL,
                        targetRate REAL NOT NULL,
                        isAbove INTEGER NOT NULL DEFAULT 1,
                        isEnabled INTEGER NOT NULL DEFAULT 1,
                        createdAt INTEGER NOT NULL DEFAULT 0,
                        triggeredAt INTEGER DEFAULT NULL
                    )
                """.trimIndent())
            }
        }

        val MIGRATION_4_5 = object : Migration(4, 5) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("CREATE INDEX IF NOT EXISTS index_currency_history_baseCurrency ON currency_history(baseCurrency)")
                db.execSQL("CREATE INDEX IF NOT EXISTS index_currency_history_targetCurrency ON currency_history(targetCurrency)")
                db.execSQL("CREATE INDEX IF NOT EXISTS index_currency_history_baseCurrency_targetCurrency_timestamp ON currency_history(baseCurrency, targetCurrency, timestamp)")
            }
        }
    }
}
