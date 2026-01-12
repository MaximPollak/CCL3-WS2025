// File: NavGraph.kt
package dev.maximpollak.neokey.ui.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import dev.maximpollak.neokey.ui.main.MainScreen
import dev.maximpollak.neokey.ui.screens.AddEditSecretScreen
import dev.maximpollak.neokey.ui.screens.SecretDetailScreen
import dev.maximpollak.neokey.ui.screens.SecretsScreen

@Composable
fun NavGraph() {
    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = "main"
    ) {
        composable("main") {
            MainScreen(
                onUnlocked = { navController.navigate("secrets") { popUpTo("main") { inclusive = true } } },
                onUsePin = { navController.navigate("pin") } // optional
            )
        }

        composable("secrets") {
            SecretsScreen(
                onAddClick = { navController.navigate("add") },
                onSecretClick = { id -> navController.navigate("detail/$id") },
                onBackClick = { navController.popBackStack() } // kept; SecretsScreen doesn't show back in prototype
            )
        }

        composable("add") {
            AddEditSecretScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable(
            route = "edit/{id}",
            arguments = listOf(navArgument("id") { type = NavType.IntType })
        ) { backStack ->
            val id = backStack.arguments?.getInt("id")
            AddEditSecretScreen(
                secretId = id,
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable(
            route = "detail/{id}",
            arguments = listOf(navArgument("id") { type = NavType.IntType })
        ) { backStack ->
            val id = backStack.arguments?.getInt("id")!!
            SecretDetailScreen(
                secretId = id,
                onEdit = { navController.navigate("edit/$id") },
                onNavigateBack = { navController.popBackStack() }
            )
        }
    }
}
