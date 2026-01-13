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
import dev.maximpollak.neokey.ui.screens.CategoriesScreen


@Composable
fun NavGraph() {
    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = "secrets"
    ) {
        composable("main") {
            MainScreen(
                onUnlocked = { navController.navigate("categories") { popUpTo("main") { inclusive = true } } },
            )
        }

        composable("categories") {
            CategoriesScreen(
                onCategoryClick = { key -> navController.navigate("secrets/$key") },
                onAddClick = { navController.navigate("add") }
            )
        }

        composable(
            route = "secrets/{category}",
            arguments = listOf(navArgument("category") { type = NavType.StringType })
        ) { backStack ->
            val category = backStack.arguments?.getString("category") ?: "ALL"

            SecretsScreen(
                onAddClick = { navController.navigate("add") },
                onSecretClick = { id -> navController.navigate("detail/$id") },
                onBackClick = { navController.popBackStack() },
                categoryFilter = category
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
