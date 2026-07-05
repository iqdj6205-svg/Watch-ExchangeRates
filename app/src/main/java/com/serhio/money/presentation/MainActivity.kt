package com.serhio.money.presentation

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavType
import androidx.navigation.navArgument
import androidx.wear.compose.material3.MaterialTheme
import androidx.wear.compose.navigation.SwipeDismissableNavHost
import androidx.wear.compose.navigation.composable
import androidx.wear.compose.navigation.rememberSwipeDismissableNavController
import com.serhio.money.data.settings.SettingsManager
import com.serhio.money.presentation.components.MainScreen
import com.serhio.money.presentation.config.TileComplicationConfigScreen
import com.serhio.money.presentation.config.TileComplicationConfigViewModel
import com.serhio.money.presentation.graphs.GraphsScreen
import com.serhio.money.presentation.navigation.Screen
import com.serhio.money.presentation.settings.SettingsScreen
import com.serhio.money.presentation.settings.SettingsViewModel
import com.serhio.money.utils.WorkerUtils
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @Inject
    lateinit var settingsManager: SettingsManager

    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        super.onCreate(savedInstanceState)

        CoroutineScope(Dispatchers.IO).launch {
            val interval = settingsManager.updateIntervalFlow.first()
            WorkerUtils.schedulePeriodicUpdate(this@MainActivity, interval)
        }

        setContent {
            MaterialTheme {
                val navController = rememberSwipeDismissableNavController()

                SwipeDismissableNavHost(
                    navController = navController,
                    startDestination = Screen.Main.route
                ) {
                    composable(Screen.Main.route) {
                        val mainViewModel: MainViewModel = hiltViewModel()
                        val uiState by mainViewModel.uiState.collectAsState()
                        val isOnline by mainViewModel.isOnline.collectAsState()
                        MainScreen(
                            uiState = uiState,
                            isOnline = isOnline,
                            onRefresh = { mainViewModel.refreshRates() },
                            onOpenSettings = { navController.navigate(Screen.Settings.route) },
                            onOpenTileConfig = { navController.navigate(Screen.TileConfig.route) },
                            onOpenGraphs = { base, target ->
                                navController.navigate(Screen.Graphs.createRoute(base, target))
                            }
                        )
                    }
                    composable(Screen.Settings.route) {
                        val settingsViewModel: SettingsViewModel = hiltViewModel()
                        SettingsScreen(viewModel = settingsViewModel)
                    }
                    composable(Screen.TileConfig.route) {
                        val configViewModel: TileComplicationConfigViewModel = hiltViewModel()
                        val tileCurrency by configViewModel.tileDisplayCurrency.collectAsState()
                        val tileMode by configViewModel.tileDisplayMode.collectAsState()
                        val compCurrency by configViewModel.complicationDisplayCurrency.collectAsState()
                        val compMode by configViewModel.complicationDisplayMode.collectAsState()
                        TileComplicationConfigScreen(
                            tileCurrency = tileCurrency,
                            tileMode = tileMode,
                            complicationCurrency = compCurrency,
                            complicationMode = compMode,
                            onTileCurrencyChange = { configViewModel.setTileCurrency(it) },
                            onTileModeChange = { configViewModel.setTileMode(it) },
                            onComplicationCurrencyChange = { configViewModel.setComplicationCurrency(it) },
                            onComplicationModeChange = { configViewModel.setComplicationMode(it) },
                            onBack = { navController.popBackStack() }
                        )
                    }
                    composable(
                        route = Screen.Graphs.route,
                        arguments = listOf(
                            navArgument("base") { type = NavType.StringType },
                            navArgument("target") { type = NavType.StringType }
                        )
                    ) { backStackEntry ->
                        val base = backStackEntry.arguments?.getString("base") ?: "USD"
                        val target = backStackEntry.arguments?.getString("target") ?: "EUR"
                        val mainViewModel: MainViewModel = hiltViewModel()
                        val uiState by mainViewModel.uiState.collectAsState()
                        when (uiState) {
                            is MainUiState.Success -> {
                                val history = (uiState as MainUiState.Success).history[target] ?: emptyList()
                                val dataPoints = history.mapIndexed { index, value ->
                                    com.serhio.money.presentation.graphs.ChartDataPoint(
                                        timestamp = System.currentTimeMillis() - (history.size - 1 - index) * 3600000L,
                                        value = value
                                    )
                                }
                                GraphsScreen(
                                    baseCurrency = base,
                                    targetCurrency = target,
                                    history = dataPoints,
                                    onBack = { navController.popBackStack() }
                                )
                            }
                            else -> {
                                GraphsScreen(
                                    baseCurrency = base,
                                    targetCurrency = target,
                                    history = emptyList(),
                                    onBack = { navController.popBackStack() }
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}