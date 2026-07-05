package com.serhio.money.data.database

import androidx.room.Database
import androidx.room.RoomDatabase
import com.serhio.money.data.database.entities.CurrencyHistoryEntity

@Database(
    entities = [CurrencyHistoryEntity::class],
    version = 2,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun currencyDao(): CurrencyDao

    companion object {
        const val DATABASE_NAME = "money_db"
    }
}
