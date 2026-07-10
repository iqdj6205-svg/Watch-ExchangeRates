package com.serhio.money.data.database

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import androidx.room.migration.Migration
import com.serhio.money.data.database.entities.CurrencyHistoryEntity

@Database(
    entities = [CurrencyHistoryEntity::class],
    version = 3,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun currencyDao(): CurrencyDao

    companion object {
        const val DATABASE_NAME = "money_db"

        val MIGRATION_2_3 = object : Migration(2, 3) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("DELETE FROM currency_history")
            }
        }
    }
}
