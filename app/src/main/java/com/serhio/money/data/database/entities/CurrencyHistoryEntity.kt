package com.serhio.money.data.database.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "currency_history")
data class CurrencyHistoryEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val timestamp: Long,
    val baseCurrency: String,
    val targetCurrency: String,
    val rate: Double,
    val source: String
)