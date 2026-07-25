package com.serhio.money.data.database.mappers

import com.serhio.money.data.database.entities.AlertEntity
import com.serhio.money.domain.model.Alert

fun AlertEntity.toDomain() = Alert(
    id = id,
    currencyCode = currencyCode,
    targetRate = targetRate,
    isAbove = isAbove,
    isEnabled = isEnabled,
    createdAt = createdAt,
    triggeredAt = triggeredAt
)

fun Alert.toEntity() = AlertEntity(
    id = id,
    currencyCode = currencyCode,
    targetRate = targetRate,
    isAbove = isAbove,
    isEnabled = isEnabled,
    createdAt = createdAt,
    triggeredAt = triggeredAt
)
