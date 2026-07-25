package com.serhio.money.data.database

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.serhio.money.data.database.entities.AlertEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface AlertDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAlert(alert: AlertEntity): Long

    @Update
    suspend fun updateAlert(alert: AlertEntity)

    @Delete
    suspend fun deleteAlert(alert: AlertEntity)

    @Query("SELECT * FROM alerts ORDER BY createdAt DESC")
    fun getAllAlerts(): Flow<List<AlertEntity>>

    @Query("SELECT * FROM alerts WHERE isEnabled = 1")
    suspend fun getEnabledAlerts(): List<AlertEntity>

    @Query("SELECT * FROM alerts WHERE id = :id")
    suspend fun getAlertById(id: Long): AlertEntity?

    @Query("UPDATE alerts SET triggeredAt = :timestamp WHERE id = :id")
    suspend fun markTriggered(id: Long, timestamp: Long)

    @Query("UPDATE alerts SET isEnabled = :enabled WHERE id = :id")
    suspend fun setEnabled(id: Long, enabled: Boolean)
}
