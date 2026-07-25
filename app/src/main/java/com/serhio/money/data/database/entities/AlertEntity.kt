package com.serhio.money.data.database.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "alerts")
data class AlertEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val currencyCode: String,
    val targetRate: Double,
    val isAbove: Boolean,
    val isEnabled: Boolean = true,
    val createdAt: Long = System.currentTimeMillis(),
    val triggeredAt: Long? = null
)
