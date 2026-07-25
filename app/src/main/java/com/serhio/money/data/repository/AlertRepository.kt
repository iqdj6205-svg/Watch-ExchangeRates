package com.serhio.money.data.repository

import com.serhio.money.data.database.AlertDao
import com.serhio.money.data.database.mappers.toDomain
import com.serhio.money.data.database.mappers.toEntity
import com.serhio.money.domain.model.Alert
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AlertRepository @Inject constructor(
    private val alertDao: AlertDao
) {
    fun getAllAlerts(): Flow<List<Alert>> {
        return alertDao.getAllAlerts().map { entities ->
            entities.map { it.toDomain() }
        }
    }

    suspend fun getEnabledAlerts(): List<Alert> {
        return alertDao.getEnabledAlerts().map { it.toDomain() }
    }

    suspend fun addAlert(alert: Alert): Long {
        return alertDao.insertAlert(alert.toEntity())
    }

    suspend fun updateAlert(alert: Alert) {
        alertDao.updateAlert(alert.toEntity())
    }

    suspend fun deleteAlert(alert: Alert) {
        alertDao.deleteAlert(alert.toEntity())
    }

    suspend fun toggleAlert(id: Long, enabled: Boolean) {
        alertDao.setEnabled(id, enabled)
    }

    suspend fun markTriggered(id: Long) {
        alertDao.markTriggered(id, System.currentTimeMillis())
    }
}
