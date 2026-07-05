package com.serhio.money.presentation.navigation

sealed class Screen(val route: String) {
    object Main : Screen("main")
    object Settings : Screen("settings")
    object TileConfig : Screen("tile_config")
    object Graphs : Screen("graphs/{base}/{target}") {
        fun createRoute(base: String, target: String) = "graphs/$base/$target"
    }
}