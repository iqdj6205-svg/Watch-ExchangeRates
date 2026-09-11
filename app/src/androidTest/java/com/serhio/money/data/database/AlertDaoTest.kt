package com.serhio.money.data.database

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.serhio.money.data.database.entities.AlertEntity
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class AlertDaoTest {

    private lateinit var database: AppDatabase
    private lateinit var dao: AlertDao

    @Before
    fun setup() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        database = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        dao = database.alertDao()
    }

    @After
    fun tearDown() {
        database.close()
    }

    @Test
    fun insertAlert_generatesId() = runBlocking {
        val id = dao.insertAlert(
            AlertEntity(currencyCode = "EUR", targetRate = 0.95, isAbove = true, createdAt = 1000L)
        )

        assertTrue(id > 0)
        val alerts = dao.getAllAlerts().first()
        assertEquals(1, alerts.size)
        assertEquals(id, alerts[0].id)
    }

    @Test
    fun getAllAlerts_orderedByCreatedAtDesc() = runBlocking {
        dao.insertAlert(AlertEntity(currencyCode = "EUR", targetRate = 0.95, isAbove = true, createdAt = 1000L))
        dao.insertAlert(AlertEntity(currencyCode = "GBP", targetRate = 1.30, isAbove = false, createdAt = 2000L))
        dao.insertAlert(AlertEntity(currencyCode = "PLN", targetRate = 4.10, isAbove = true, createdAt = 3000L))

        val alerts = dao.getAllAlerts().first()

        assertEquals(3, alerts.size)
        assertEquals("PLN", alerts[0].currencyCode)
        assertEquals("GBP", alerts[1].currencyCode)
        assertEquals("EUR", alerts[2].currencyCode)
    }

    @Test
    fun updateAlert_changesTargetRate() = runBlocking {
        val id = dao.insertAlert(AlertEntity(currencyCode = "EUR", targetRate = 0.95, isAbove = true))
        val alert = dao.getAlertById(id)!!

        dao.updateAlert(alert.copy(targetRate = 1.05))

        val updated = dao.getAlertById(id)!!
        assertEquals(1.05, updated.targetRate, 0.001)
    }

    @Test
    fun deleteAlert_removesFromDatabase() = runBlocking {
        val id = dao.insertAlert(AlertEntity(currencyCode = "EUR", targetRate = 0.95, isAbove = true))
        val alert = dao.getAlertById(id)!!

        dao.deleteAlert(alert)

        assertNull(dao.getAlertById(id))
    }

    @Test
    fun getEnabledAlerts_returnsOnlyEnabled() = runBlocking {
        dao.insertAlert(AlertEntity(currencyCode = "EUR", targetRate = 0.95, isAbove = true, isEnabled = true))
        dao.insertAlert(AlertEntity(currencyCode = "GBP", targetRate = 1.30, isAbove = false, isEnabled = false))

        val enabled = dao.getEnabledAlerts()

        assertEquals(1, enabled.size)
        assertEquals("EUR", enabled[0].currencyCode)
    }

    @Test
    fun setEnabled_togglesAlertState() = runBlocking {
        val id = dao.insertAlert(AlertEntity(currencyCode = "EUR", targetRate = 0.95, isAbove = true))

        dao.setEnabled(id, false)
        assertFalse(dao.getAlertById(id)!!.isEnabled)

        dao.setEnabled(id, true)
        assertTrue(dao.getAlertById(id)!!.isEnabled)
    }

    @Test
    fun markTriggered_setsTriggeredAtTimestamp() = runBlocking {
        val id = dao.insertAlert(AlertEntity(currencyCode = "EUR", targetRate = 0.95, isAbove = true))
        val timestamp = System.currentTimeMillis()

        dao.markTriggered(id, timestamp)

        val alert = dao.getAlertById(id)!!
        assertEquals(timestamp, alert.triggeredAt)
    }
}