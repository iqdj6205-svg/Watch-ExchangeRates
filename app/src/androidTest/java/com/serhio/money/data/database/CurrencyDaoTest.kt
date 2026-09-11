package com.serhio.money.data.database

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.serhio.money.data.database.entities.CurrencyHistoryEntity
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class CurrencyDaoTest {

    private lateinit var database: AppDatabase
    private lateinit var dao: CurrencyDao

    @Before
    fun setup() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        database = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        dao = database.currencyDao()
    }

    @After
    fun tearDown() {
        database.close()
    }

    @Test
    fun insertRecord_thenQueryHistory_returnsRecord() = runBlocking {
        dao.insertHistoryRecord(
            CurrencyHistoryEntity(timestamp = 1000L, baseCurrency = "USD", targetCurrency = "EUR", rate = 0.92, source = "test")
        )

        val history = dao.getHistoryForPair("USD", "EUR").first()

        assertEquals(1, history.size)
        assertEquals("USD", history[0].baseCurrency)
        assertEquals("EUR", history[0].targetCurrency)
        assertEquals(0.92, history[0].rate, 0.001)
    }

    @Test
    fun insertMultipleRecords_orderedByTimestampDesc() = runBlocking {
        dao.insertHistoryRecords(
            listOf(
                CurrencyHistoryEntity(timestamp = 1000L, baseCurrency = "USD", targetCurrency = "EUR", rate = 0.90, source = "test"),
                CurrencyHistoryEntity(timestamp = 2000L, baseCurrency = "USD", targetCurrency = "EUR", rate = 0.92, source = "test"),
                CurrencyHistoryEntity(timestamp = 3000L, baseCurrency = "USD", targetCurrency = "EUR", rate = 0.94, source = "test")
            )
        )

        val history = dao.getHistoryForPair("USD", "EUR").first()

        assertEquals(3, history.size)
        assertEquals(3000L, history[0].timestamp)
        assertEquals(0.94, history[0].rate, 0.001)
    }

    @Test
    fun getCachedRates_returnsLatestSnapshotPerCurrency() = runBlocking {
        dao.insertHistoryRecord(
            CurrencyHistoryEntity(timestamp = 1000L, baseCurrency = "USD", targetCurrency = "EUR", rate = 0.90, source = "test")
        )
        dao.insertHistoryRecord(
            CurrencyHistoryEntity(timestamp = 2000L, baseCurrency = "USD", targetCurrency = "EUR", rate = 0.92, source = "test")
        )
        dao.insertHistoryRecord(
            CurrencyHistoryEntity(timestamp = 2000L, baseCurrency = "USD", targetCurrency = "GBP", rate = 0.79, source = "test")
        )

        val cached = dao.getCachedRates("USD")

        assertEquals(2, cached.size)
        val eurRate = cached.first { it.targetCurrency == "EUR" }
        assertEquals(0.92, eurRate.rate, 0.001)
    }

    @Test
    fun getCachedRates_emptyBase_returnsEmpty() = runBlocking {
        val cached = dao.getCachedRates("RUB")
        assertTrue(cached.isEmpty())
    }

    @Test
    fun getRecentHistory_limitsResults() = runBlocking {
        dao.insertHistoryRecords(
            listOf(
                CurrencyHistoryEntity(timestamp = 1000L, baseCurrency = "USD", targetCurrency = "EUR", rate = 0.90, source = "test"),
                CurrencyHistoryEntity(timestamp = 2000L, baseCurrency = "USD", targetCurrency = "EUR", rate = 0.92, source = "test"),
                CurrencyHistoryEntity(timestamp = 3000L, baseCurrency = "USD", targetCurrency = "EUR", rate = 0.94, source = "test")
            )
        )

        val recent = dao.getRecentHistory("USD", "EUR", 2).first()

        assertEquals(2, recent.size)
        assertEquals(3000L, recent[0].timestamp)
        assertEquals(2000L, recent[1].timestamp)
    }

    @Test
    fun deleteOldRecords_removesOnlyRowsOlderThanThreshold() = runBlocking {
        dao.insertHistoryRecords(
            listOf(
                CurrencyHistoryEntity(timestamp = 1000L, baseCurrency = "USD", targetCurrency = "EUR", rate = 0.90, source = "test"),
                CurrencyHistoryEntity(timestamp = 2000L, baseCurrency = "USD", targetCurrency = "EUR", rate = 0.92, source = "test")
            )
        )

        dao.deleteOldRecords(1500L)

        val history = dao.getHistoryForPair("USD", "EUR").first()
        assertEquals(1, history.size)
        assertEquals(2000L, history[0].timestamp)
    }

    @Test
    fun insertRecord_withSamePrimaryKey_replaces() = runBlocking {
        val record = CurrencyHistoryEntity(id = 5, timestamp = 1000L, baseCurrency = "USD", targetCurrency = "EUR", rate = 0.90, source = "test")
        dao.insertHistoryRecord(record)

        val updated = record.copy(rate = 0.95)
        dao.insertHistoryRecord(updated)

        val history = dao.getHistoryForPair("USD", "EUR").first()
        assertEquals(1, history.size)
        assertEquals(0.95, history[0].rate, 0.001)
    }
}