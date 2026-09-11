package com.serhio.money.presentation.components

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.serhio.money.domain.model.HistoryPoint
import com.serhio.money.presentation.MainUiState
import com.serhio.money.presentation.theme.MoneyTheme
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class MainScreenTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    private fun setContent(state: MainUiState, isOnline: Boolean = true) {
        composeTestRule.setContent {
            MoneyTheme {
                MainScreen(
                    uiState = state,
                    isOnline = isOnline,
                    onRefresh = {},
                    onOpenSettings = {},
                    onOpenTileConfig = {},
                    onOpenGraphs = { _, _ -> },
                    onOpenAlerts = {},
                    onReorderFavorites = {}
                )
            }
        }
    }

    @Test
    fun success_state_showsFavoritesPage() {
        setContent(
            MainUiState.Success(
                baseCurrency = "USD",
                rates = mapOf("EUR" to 0.92, "PLN" to 4.05),
                interestedCurrencies = listOf("EUR", "PLN"),
                lastUpdate = System.currentTimeMillis(),
                history = mapOf(
                    "EUR" to listOf(HistoryPoint(1000L, 0.90), HistoryPoint(2000L, 0.92)),
                    "PLN" to listOf(HistoryPoint(1000L, 4.00), HistoryPoint(2000L, 4.05))
                )
            )
        )

        composeTestRule.onNodeWithText("EUR", substring = true).assertExists()
        composeTestRule.onNodeWithText("PLN", substring = true).assertExists()
    }

    @Test
    fun error_state_showsErrorMessage() {
        setContent(MainUiState.Error("Network error"))

        composeTestRule.onNodeWithText("Network error").assertIsDisplayed()
    }

    @Test
    fun empty_interested_list_shows_empty_placeholder() {
        setContent(
            MainUiState.Success(
                baseCurrency = "USD",
                rates = mapOf("EUR" to 0.92),
                interestedCurrencies = emptyList(),
                lastUpdate = System.currentTimeMillis()
            )
        )

        composeTestRule.onNodeWithText("Add currencies in Settings").assertExists()
    }
}