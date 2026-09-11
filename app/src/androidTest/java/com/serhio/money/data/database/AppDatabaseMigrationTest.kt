package com.serhio.money.data.database

import android.content.Context
import androidx.sqlite.db.SupportSQLiteDatabase
import androidx.sqlite.db.SupportSQLiteOpenHelper
import androidx.sqlite.db.framework.FrameworkSQLiteOpenHelperFactory
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Migration tests run against a real SQLite database.
 *
 * `MigrationTestHelper` is not used because the app exports no Room schemas
 * (`exportSchema = false`), so the helper cannot load historical schemas.
 * Instead we create the old schema manually and invoke the migration objects
 * directly against a `SupportSQLiteDatabase`.
 */
@RunWith(AndroidJUnit4::class)
class AppDatabaseMigrationTest {

    private val context: Context
        get() = InstrumentationRegistry.getInstrumentation().targetContext

    private lateinit var dbName: String
    private lateinit var helper: SupportSQLiteOpenHelper
    private lateinit var db: SupportSQLiteDatabase

    @Before
    fun setup() {
        dbName = "migration-test-${System.nanoTime()}"
        val factory = FrameworkSQLiteOpenHelperFactory()
        val config = SupportSQLiteOpenHelper.Configuration.builder(context)
            .name(dbName)
            .callback(object : SupportSQLiteOpenHelper.Callback(2) {
                override fun onCreate(db: SupportSQLiteDatabase) = Unit
                override fun onUpgrade(db: SupportSQLiteDatabase, oldVersion: Int, newVersion: Int) = Unit
            })
            .build()
        helper = factory.create(config)
        db = helper.writableDatabase
    }

    @After
    fun tearDown() {
        db.close()
        context.deleteDatabase(dbName)
    }

    private fun createHistoryTable() {
        db.execSQL(
            """
            CREATE TABLE IF NOT EXISTS currency_history (
                id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                timestamp INTEGER NOT NULL,
                baseCurrency TEXT NOT NULL,
                targetCurrency TEXT NOT NULL,
                rate REAL NOT NULL,
                source TEXT NOT NULL
            )
            """.trimIndent()
        )
    }

    private fun createAlertsTable() {
        db.execSQL(
            """
            CREATE TABLE IF NOT EXISTS alerts (
                id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                currencyCode TEXT NOT NULL,
                targetRate REAL NOT NULL,
                isAbove INTEGER NOT NULL DEFAULT 1,
                isEnabled INTEGER NOT NULL DEFAULT 1,
                createdAt INTEGER NOT NULL DEFAULT 0,
                triggeredAt INTEGER DEFAULT NULL
            )
            """.trimIndent()
        )
    }

    private fun tableNames(): Set<String> {
        val names = mutableSetOf<String>()
        db.query("SELECT name FROM sqlite_master WHERE type='table'", emptyArray()).use { c ->
            while (c.moveToNext()) names.add(c.getString(0))
        }
        return names
    }

    private fun indexNames(table: String): Set<String> {
        val names = mutableSetOf<String>()
        db.query("SELECT name FROM sqlite_master WHERE type='index' AND tbl_name=?", arrayOf(table)).use { c ->
            while (c.moveToNext()) names.add(c.getString(0))
        }
        return names
    }

    @Test
    fun migration2To3_deletesAllHistory() {
        createHistoryTable()
        db.version = 2
        db.execSQL("INSERT INTO currency_history (timestamp, baseCurrency, targetCurrency, rate, source) VALUES (1000, 'USD', 'EUR', 0.92, 'test')")

        AppDatabase.MIGRATION_2_3.migrate(db)
        db.version = 3

        db.query("SELECT COUNT(*) FROM currency_history", emptyArray()).use { c ->
            c.moveToFirst()
            assertEquals(0, c.getInt(0))
        }
    }

    @Test
    fun migration3To4_createsAlertsTable() {
        createHistoryTable()
        db.version = 3
        db.execSQL("INSERT INTO currency_history (timestamp, baseCurrency, targetCurrency, rate, source) VALUES (1000, 'USD', 'EUR', 0.92, 'test')")

        AppDatabase.MIGRATION_3_4.migrate(db)
        db.version = 4

        assertTrue("alerts table should exist", tableNames().contains("alerts"))
        db.execSQL("INSERT INTO alerts (currencyCode, targetRate, isAbove, isEnabled, createdAt) VALUES ('EUR', 0.95, 1, 1, 1234)")
    }

    @Test
    fun migration4To5_createsIndexes() {
        createHistoryTable()
        createAlertsTable()
        db.version = 4

        AppDatabase.MIGRATION_4_5.migrate(db)
        db.version = 5

        val indexes = indexNames("currency_history")
        assertTrue(indexes.contains("index_currency_history_baseCurrency"))
        assertTrue(indexes.contains("index_currency_history_targetCurrency"))
        assertTrue(indexes.contains("index_currency_history_baseCurrency_targetCurrency_timestamp"))
    }
}