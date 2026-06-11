package com.safepath.app.ui.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.safepath.app.ui.home.HomeScreen
import com.safepath.app.ui.navigation_screen.NavigationScreen
import com.safepath.app.ui.routes.RouteResultScreen

object Routes {
    const val HOME       = "home"
    const val ROUTE_RESULTS = "route_results?source={source}&destination={destination}"
    const val NAVIGATION = "navigation/{routeId}"

    fun routeResults(source: String, destination: String) =
        "route_results?source=${source}&destination=${destination}"

    fun navigation(routeId: Int) = "navigation/$routeId"
}

@Composable
fun AppNavigation() {
    val navController = rememberNavController()

    NavHost(
        navController    = navController,
        startDestination = Routes.HOME
    ) {
        // ── Home ─────────────────────────────────────────────────────────────
        composable(Routes.HOME) {
            HomeScreen(
                onSearch = { source, destination ->
                    navController.navigate(Routes.routeResults(source, destination))
                }
            )
        }

        // ── Route Results ─────────────────────────────────────────────────────
        composable(
            route = Routes.ROUTE_RESULTS,
            arguments = listOf(
                navArgument("source")      { type = NavType.StringType; defaultValue = "" },
                navArgument("destination") { type = NavType.StringType; defaultValue = "" }
            )
        ) { backStackEntry ->
            val source      = backStackEntry.arguments?.getString("source")      ?: ""
            val destination = backStackEntry.arguments?.getString("destination") ?: ""
            RouteResultScreen(
                source      = source,
                destination = destination,
                onStartNavigation = { routeId ->
                    navController.navigate(Routes.navigation(routeId))
                },
                onBack = { navController.popBackStack() }
            )
        }

        // ── Navigation ────────────────────────────────────────────────────────
        composable(
            route = Routes.NAVIGATION,
            arguments = listOf(
                navArgument("routeId") { type = NavType.IntType; defaultValue = 0 }
            )
        ) { backStackEntry ->
            val routeId = backStackEntry.arguments?.getInt("routeId") ?: 0
            NavigationScreen(
                routeId = routeId,
                onBack  = { navController.popBackStack() }
            )
        }
    }
}
