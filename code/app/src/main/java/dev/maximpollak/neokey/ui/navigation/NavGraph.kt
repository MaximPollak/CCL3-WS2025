package dev.maximpollak.neokey.ui.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavType
import androidx.navigation.compose.*
import androidx.navigation.navArgument
import dev.maximpollak.secretum.ui.screens.*

@Composable
fun NavGraph() {
    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = "main"
    ) {

        composable("main") {
            MainScreen(
                onNavigateToSecrets = { navController.navigate("secrets") }
            )
        }

        composable("secrets") {
            SecretsScreen(
                onAddClick = { navController.navigate("add") },
                onSecretClick = { id -> navController.navigate("detail/$id") },
                onBackClick = { navController.popBackStack() }
            )
        }

        composable("add") {
            AddEditSecretScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable(
            "edit/{id}",
            arguments = listOf(navArgument("id") { type = NavType.IntType })
        ) { backStack ->
            val id = backStack.arguments?.getInt("id")
            AddEditSecretScreen(
                secretId = id,
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable(
            "detail/{id}",
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