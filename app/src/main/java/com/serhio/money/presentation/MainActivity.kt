package com.serhio.money.presentation

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.navigation.NavType
import androidx.navigation.navArgument
import androidx.wear.compose.navigation.SwipeDismissableNavHost
import com.serhio.money.presentation.theme.MoneyTheme
import androidx.wear.compose.navigation.composable
import androidx.wear.compose.navigation.rememberSwipeDismissableNavController
import com.serhio.money.data.settings.SettingsManager
import com.serhio.money.presentation.alerts.AlertsScreen
import com.serhio.money.presentation.alerts.AlertsViewModel
import com.serhio.money.presentation.components.MainScreen
import com.serhio.money.presentation.graphs.ChartDataPoint
import com.serhio.money.presentation.config.TileComplicationConfigScreen
import com.serhio.money.presentation.config.TileComplicationConfigViewModel
import com.serhio.money.presentation.graphs.GraphsScreen
import com.serhio.money.presentation.navigation.Screen
import com.serhio.money.presentation.settings.SettingsScreen
import com.serhio.money.presentation.settings.SettingsViewModel
import com.serhio.money.utils.WorkerUtils
import androidx.lifecycle.lifecycleScope
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @Inject lateinit var settingsManager: SettingsManager

    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        super.onCreate(savedInstanceState)
        requestNotificationPermissionIfNeeded()

        lifecycleScope.launch {
            val interval = settingsManager.updateIntervalFlow.first()
            val alertInterval = settingsManager.alertIntervalFlow.first()
            WorkerUtils.schedulePeriodicUpdate(this@MainActivity, interval)
            WorkerUtils.scheduleAlertCheck(this@MainActivity, alertInterval)
        }

        setContent {
            MoneyTheme {
                val navController = rememberSwipeDismissableNavController()
                SwipeDismissableNavHost(navController = navController, startDestination = Screen.Main.route) {
                    composable(Screen.Main.route) {
                        val mainViewModel: MainViewModel = hiltViewModel()
                        val settingsViewModel: SettingsViewModel = hiltViewModel()
                        val uiState by mainViewModel.uiState.collectAsState()
                        val isOnline by mainViewModel.isOnline.collectAsState()
                        MainScreen(
                            uiState = uiState,
                            isOnline = isOnline,
                            onRefresh = { mainViewModel.refreshRates() },
                            onOpenSettings = { navController.navigate(Screen.Settings.route) },
                            onOpenTileConfig = { navController.navigate(Screen.TileConfig.route) },
                            onOpenAlerts = { navController.navigate(Screen.Alerts.route) },
                            onOpenGraphs = { base, target -> navController.navigate(Screen.Graphs.createRoute(base, target)) },
                            onReorderFavorites = { newOrder -> settingsViewModel.reorderCurrencies(newOrder) }
                        )
                    }
                    composable(Screen.Settings.route) { SettingsScreen(viewModel = hiltViewModel()) }
                    composable(Screen.Alerts.route) { AlertsScreen(viewModel = hiltViewModel<AlertsViewModel>()) }
                    composable(Screen.TileConfig.route) {
                        val configViewModel: TileComplicationConfigViewModel = hiltViewModel()
                        val tileCurrency by configViewModel.tileDisplayCurrency.collectAsState()
                        val tileMode by configViewModel.tileDisplayMode.collectAsState()
                        val compCurrency by configViewModel.complicationDisplayCurrency.collectAsState()
                        val compMode by configViewModel.complicationDisplayMode.collectAsState()
                        TileComplicationConfigScreen(tileCurrency, tileMode, compCurrency, compMode, configViewModel::setTileCurrency, configViewModel::setTileMode, configViewModel::setComplicationCurrency, configViewModel::setComplicationMode) { navController.popBackStack() }
                    }
                    composable(route = Screen.Graphs.route, arguments = listOf(navArgument("base") { type = NavType.StringType }, navArgument("target") { type = NavType.StringType })) { backStackEntry ->
                        val base = backStackEntry.arguments?.getString("base") ?: "USD"
                        val target = backStackEntry.arguments?.getString("target") ?: "EUR"
                        val mainViewModel: MainViewModel = hiltViewModel()
                        val uiState by mainViewModel.uiState.collectAsState()
                        val history = (uiState as? MainUiState.Success)?.history?.get(target).orEmpty().map { ChartDataPoint(it.timestamp, it.rate) }
                        GraphsScreen(baseCurrency = base, targetCurrency = target, history = history, onBack = { navController.popBackStack() })
                    }
                }
            }
        }
    }

    private fun requestNotificationPermissionIfNeeded() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU && ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.POST_NOTIFICATIONS), 1001)
        }
    }
}
