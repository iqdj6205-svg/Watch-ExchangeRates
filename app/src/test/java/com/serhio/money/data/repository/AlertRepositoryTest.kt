package com.serhio.money.data.repository

import com.serhio.money.data.database.AlertDao
import com.serhio.money.data.database.entities.AlertEntity
import com.serhio.money.domain.model.Alert
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class AlertRepositoryTest {

    private lateinit var alertDao: AlertDao
    private lateinit var repository: AlertRepository

    @Before
    fun setup() {
        alertDao = mockk(relaxed = true)
        repository = AlertRepository(alertDao)
    }

    @Test
    fun `getAllAlerts returns mapped domain alerts`() = runTest {
        val entities = listOf(
            AlertEntity(id = 1, currencyCode = "EUR", targetRate = 0.95, isAbove = true, isEnabled = true),
            AlertEntity(id = 2, currencyCode = "PLN", targetRate = 4.10, isAbove = false, isEnabled = false)
        )
        every { alertDao.getAllAlerts() } returns flowOf(entities)

        val result = repository.getAllAlerts().first()

        assertEquals(2, result.size)
        assertEquals("EUR", result[0].currencyCode)
        assertTrue(result[0].isAbove)
        assertEquals("PLN", result[1].currencyCode)
        assertFalse(result[1].isEnabled)
    }

    @Test
    fun `getEnabledAlerts returns only enabled alerts`() = runTest {
        val entities = listOf(
            AlertEntity(id = 1, currencyCode = "EUR", targetRate = 0.95, isAbove = true, isEnabled = true)
        )
        coEvery { alertDao.getEnabledAlerts() } returns entities

        val result = repository.getEnabledAlerts()

        assertEquals(1, result.size)
        assertEquals(1L, result[0].id)
        assertEquals("EUR", result[0].currencyCode)
    }

    @Test
    fun `addAlert delegates to dao and returns id`() = runTest {
        val alert = Alert(currencyCode = "EUR", targetRate = 0.95, isAbove = true)
        coEvery { alertDao.insertAlert(any()) } returns 42L

        val result = repository.addAlert(alert)

        assertEquals(42L, result)
        coVerify { alertDao.insertAlert(any()) }
    }

    @Test
    fun `updateAlert delegates to dao with mapped entity`() = runTest {
        val alert = Alert(id = 5, currencyCode = "GBP", targetRate = 1.30, isAbove = false)
        coEvery { alertDao.updateAlert(any()) } returns Unit

        repository.updateAlert(alert)

        coVerify { alertDao.updateAlert(match { it.id == 5L && it.currencyCode == "GBP" }) }
    }

    @Test
    fun `deleteAlert delegates to dao`() = runTest {
        val alert = Alert(id = 3, currencyCode = "CHF", targetRate = 1.10, isAbove = true)
        coEvery { alertDao.deleteAlert(any()) } returns Unit

        repository.deleteAlert(alert)

        coVerify { alertDao.deleteAlert(match { it.id == 3L }) }
    }

    @Test
    fun `toggleAlert calls setEnabled with correct params`() = runTest {
        coEvery { alertDao.setEnabled(any(), any()) } returns Unit

        repository.toggleAlert(7L, false)

        coVerify { alertDao.setEnabled(7L, false) }
    }

    @Test
    fun `markTriggered calls dao with timestamp`() = runTest {
        coEvery { alertDao.markTriggered(any(), any()) } returns Unit

        repository.markTriggered(1L)

        coVerify { alertDao.markTriggered(1L, any()) }
    }
}
