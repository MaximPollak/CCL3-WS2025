// File: NavGraph.kt
package dev.maximpollak.neokey.ui.navigation

import android.os.SystemClock
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.navigation.NavHostController
import androidx.navigation.NavOptionsBuilder
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import dev.maximpollak.neokey.ui.main.MainScreen
import dev.maximpollak.neokey.ui.screens.AddEditSecretScreen
import dev.maximpollak.neokey.ui.screens.CategoriesScreen
import dev.maximpollak.neokey.ui.screens.SecretDetailScreen
import dev.maximpollak.neokey.ui.screens.SecretsScreen
import dev.maximpollak.neokey.ui.screens.EditSecretScreen

private class NavLock(private val lockMs: Long = 450L) {
    private var lastNavTime: Long = 0L

    fun canNavigateNow(): Boolean {
        val now = SystemClock.elapsedRealtime()
        if (now - lastNavTime < lockMs) return false
        lastNavTime = now
        return true
    }
}

private fun NavHostController.safeNavigate(
    route: String,
    navLock: NavLock,
    builder: (NavOptionsBuilder.() -> Unit)? = null
) {
    if (!navLock.canNavigateNow()) return

    if (builder != null) navigate(route, builder) else navigate(route)
}

private fun NavHostController.safePopBackStack(navLock: NavLock): Boolean {
    if (!navLock.canNavigateNow()) return false
    return popBackStack()
}

@Composable
fun NavGraph() {
    val navController = rememberNavController()
    val navLock = remember { NavLock(lockMs = 450L) } // slightly longer to cover pop + quick tap races

    NavHost(
        navController = navController,
        startDestination = "categories"
    ) {
        composable("main") {
            MainScreen(
                onUnlocked = {
                    navController.safeNavigate("categories", navLock) {
                        popUpTo("main") { inclusive = true }
                    }
                },
            )
        }

        composable("categories") {
            CategoriesScreen(
                onCategoryClick = { key -> navController.safeNavigate("secrets/$key", navLock) },
                onAddClick = { navController.safeNavigate("add", navLock) }
            )
        }

        composable(
            route = "secrets/{category}",
            arguments = listOf(navArgument("category") { type = NavType.StringType })
        ) { backStack ->
            val category = backStack.arguments?.getString("category") ?: "ALL"

            SecretsScreen(
                onAddClick = { navController.safeNavigate("add", navLock) },
                onSecretClick = { id -> navController.safeNavigate("detail/$id", navLock) },
                onSecretLongPress = { id -> navController.safeNavigate("edit/$id", navLock) },
                onBackClick = { navController.safePopBackStack(navLock) },
                categoryFilter = category
            )
        }

        composable("secrets") {
            SecretsScreen(
                onAddClick = { navController.safeNavigate("add", navLock) },
                onSecretClick = { id -> navController.safeNavigate("detail/$id", navLock) },
                onSecretLongPress = { id -> navController.safeNavigate("edit/$id", navLock) },
                onBackClick = { navController.safePopBackStack(navLock) },
            )
        }

        composable("add") {
            AddEditSecretScreen(
                onNavigateBack = { navController.safePopBackStack(navLock) }
            )
        }

        composable(
            route = "edit/{id}",
            arguments = listOf(navArgument("id") { type = NavType.IntType })
        ) { backStack ->
            val id = backStack.arguments?.getInt("id")!!
            EditSecretScreen(
                secretId = id,
                onSaved = { navController.safePopBackStack(navLock) },
                onNavigateBack = { navController.safePopBackStack(navLock) }
            )
        }

        composable(
            route = "detail/{id}",
            arguments = listOf(navArgument("id") { type = NavType.IntType })
        ) { backStack ->
            val id = backStack.arguments?.getInt("id")!!
            SecretDetailScreen(
                secretId = id,
                onEdit = { navController.safeNavigate("edit/$id", navLock) },
                onNavigateBack = { navController.safePopBackStack(navLock) }
            )
        }
    }
}
