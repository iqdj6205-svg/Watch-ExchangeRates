package com.serhio.money.domain.model

data class Alert(
    val id: Long = 0,
    val currencyCode: String,
    val targetRate: Double,
    val isAbove: Boolean,
    val isEnabled: Boolean = true,
    val createdAt: Long = System.currentTimeMillis(),
    val triggeredAt: Long? = null
) {
    val direction: String get() = if (isAbove) "≥" else "≤"
    val description: String get() = "1 $currencyCode $direction $targetRate"
}
