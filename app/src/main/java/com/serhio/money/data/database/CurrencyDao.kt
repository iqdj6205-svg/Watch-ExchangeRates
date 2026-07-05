package com.serhio.money.data.database

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.serhio.money.data.database.entities.CurrencyHistoryEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface CurrencyDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertHistoryRecord(record: CurrencyHistoryEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertHistoryRecords(records: List<CurrencyHistoryEntity>)

    @Query("SELECT * FROM currency_history WHERE baseCurrency = :base AND targetCurrency = :target ORDER BY timestamp DESC")
    fun getHistoryForPair(base: String, target: String): Flow<List<CurrencyHistoryEntity>>

    @Query("SELECT * FROM currency_history WHERE baseCurrency = :base ORDER BY timestamp DESC LIMIT 1")
    suspend fun getLatestTimestampForBase(base: String): CurrencyHistoryEntity?

    @Query("SELECT * FROM currency_history WHERE baseCurrency = :base AND timestamp = (SELECT MAX(timestamp) FROM currency_history WHERE baseCurrency = :base)")
    suspend fun getCachedRates(base: String): List<CurrencyHistoryEntity>

    @Query("SELECT * FROM currency_history WHERE baseCurrency = :base AND targetCurrency = :target ORDER BY timestamp DESC LIMIT :limit")
    fun getRecentHistory(base: String, target: String, limit: Int): Flow<List<CurrencyHistoryEntity>>

    @Query("SELECT * FROM currency_history ORDER BY timestamp DESC LIMIT 100")
    fun getLatestHistory(): Flow<List<CurrencyHistoryEntity>>

    @Query("DELETE FROM currency_history WHERE timestamp < :threshold")
    suspend fun deleteOldRecords(threshold: Long)
}
