// File: CategoriesScreen.kt
package dev.maximpollak.neokey.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.FloatingActionButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import dev.maximpollak.neokey.domain.model.Secret
import dev.maximpollak.neokey.domain.model.SecretType
import dev.maximpollak.neokey.viewmodel.SecretsViewModel
import dev.maximpollak.neokey.viewmodel.SecretsViewModelFactory

data class CategoryTile(
    val key: String, // "ALL" or SecretType.name
    val label: String,
    val icon: androidx.compose.ui.graphics.vector.ImageVector
)

@Composable
fun CategoriesScreen(
    onCategoryClick: (String) -> Unit,
    onAddClick: () -> Unit
) {
    val context = LocalContext.current
    val viewModel: SecretsViewModel = viewModel(factory = SecretsViewModelFactory(context))
    val secrets by viewModel.secrets.collectAsState(initial = emptyList())

    val accent = Color(0xFF25E6C8)
    val bgTop = Color(0xFF070A12)
    val bgBottom = Color(0xFF0B1020)

    val tiles = listOf(
        CategoryTile("ALL", "All", Icons.Outlined.Key),
        CategoryTile(SecretType.WORK.name, "Work", Icons.Outlined.WorkOutline),
        CategoryTile(SecretType.EDUCATION.name, "Education", Icons.Outlined.School),
        CategoryTile(SecretType.WIFI.name, "Wi-Fi", Icons.Outlined.Wifi),
        CategoryTile(SecretType.PRIVATE.name, "Private", Icons.Outlined.Security),
        CategoryTile(SecretType.ELSE.name, "Other", Icons.Outlined.FolderOpen),
    )

    val counts = secretsCountMap(secrets)

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Brush.verticalGradient(listOf(bgTop, bgBottom)))
            .statusBarsPadding()
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 18.dp)
        ) {
            Spacer(Modifier.height(10.dp))

            // Header (manual, stable)
            Text(
                text = "Categories",
                color = Color.White,
                style = MaterialTheme.typography.headlineLarge,
                fontWeight = FontWeight.SemiBold
            )
            Spacer(Modifier.height(4.dp))
            Text(
                text = "${counts["ALL"] ?: 0} entries",
                color = Color.White.copy(alpha = 0.55f),
                style = MaterialTheme.typography.bodyMedium
            )

            Spacer(Modifier.height(16.dp))

            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                contentPadding = PaddingValues(bottom = 140.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                items(tiles, key = { it.key }) { tile ->
                    CategoryCard(
                        tile = tile,
                        count = counts[tile.key] ?: 0,
                        accent = accent,
                        onClick = { onCategoryClick(tile.key) }
                    )
                }
            }
        }

        // ✅ ORIGINAL STYLE FAB (center-bottom, bordered)
        FloatingActionButton(
            onClick = onAddClick,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .navigationBarsPadding()
                .padding(bottom = 18.dp)
                .size(72.dp)
                .border(3.dp, accent, CircleShape),
            shape = CircleShape,
            containerColor = Color(0xFF0D121E),
            contentColor = accent,
            elevation = FloatingActionButtonDefaults.elevation(
                defaultElevation = 12.dp,
                pressedElevation = 16.dp
            )
        ) {
            Icon(
                imageVector = Icons.Outlined.Add,
                contentDescription = "Add secret",
                modifier = Modifier.size(34.dp)
            )
        }
    }
}

private fun secretsCountMap(secrets: List<Secret>): Map<String, Int> {
    val map = mutableMapOf<String, Int>()
    map["ALL"] = secrets.size
    SecretType.values().forEach { type ->
        map[type.name] = secrets.count { it.category == type }
    }
    return map
}

@Composable
private fun CategoryCard(
    tile: CategoryTile,
    count: Int,
    accent: Color,
    onClick: () -> Unit
) {
    val shape = RoundedCornerShape(20.dp)
    val cardFill = Color.White.copy(alpha = 0.05f)
    val cardBorder = Color.White.copy(alpha = 0.08f)

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .height(118.dp)
            .clip(shape)
            .background(cardFill)
            .border(1.dp, cardBorder, shape)
            .clickable(onClick = onClick)
            .padding(14.dp),
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        Surface(
            color = accent.copy(alpha = 0.14f),
            contentColor = accent,
            shape = RoundedCornerShape(14.dp)
        ) {
            Icon(
                imageVector = tile.icon,
                contentDescription = null,
                modifier = Modifier.padding(10.dp)
            )
        }

        Column {
            Text(
                text = tile.label,
                color = Color.White,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )
            Text(
                text = "$count items",
                color = Color.White.copy(alpha = 0.55f),
                style = MaterialTheme.typography.bodyMedium
            )
        }
    }
}
