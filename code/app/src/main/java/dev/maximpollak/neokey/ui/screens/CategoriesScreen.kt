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
import androidx.compose.material3.*
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
    val key: String,              // "ALL" or SecretType.name
    val label: String,
    val icon: androidx.compose.ui.graphics.vector.ImageVector,
    val iconBg: Color
)

@Composable
fun CategoriesScreen(
    onCategoryClick: (String) -> Unit // passes "ALL" or "WORK"/"WIFI"/...
) {
    val context = LocalContext.current
    val viewModel: SecretsViewModel = viewModel(factory = SecretsViewModelFactory(context))
    val secrets by viewModel.secrets.collectAsState(initial = emptyList())

    val tiles = listOf(
        CategoryTile("ALL", "All", Icons.Outlined.Key, Color(0xFF1F8EF1)),
        CategoryTile(SecretType.WORK.name, "Work", Icons.Outlined.PeopleAlt, Color(0xFF33C759)),
        CategoryTile(SecretType.EDUCATION.name, "Codes", Icons.Outlined.QrCode2, Color(0xFFFFCC00)),
        CategoryTile(SecretType.WIFI.name, "Wi-Fi", Icons.Outlined.Wifi, Color(0xFF64D2FF)),
        CategoryTile(SecretType.PRIVATE.name, "Security", Icons.Outlined.Security, Color(0xFFFF3B30)),
        CategoryTile(SecretType.ELSE.name, "Other", Icons.Outlined.Folder, Color(0xFFFF9500)),
    )

    val counts = secretsCountMap(secrets)

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(Color(0xFF070A12), Color(0xFF0B1020))
                )
            )
            .statusBarsPadding()
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 18.dp)
        ) {
            Spacer(Modifier.height(10.dp))

            Text(
                text = "Categories",
                color = Color.White,
                style = MaterialTheme.typography.headlineLarge,
                fontWeight = FontWeight.SemiBold
            )

            Spacer(Modifier.height(16.dp))

            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                contentPadding = PaddingValues(bottom = 24.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                items(tiles, key = { it.key }) { tile ->
                    CategoryCard(
                        tile = tile,
                        count = counts[tile.key] ?: 0,
                        onClick = { onCategoryClick(tile.key) }
                    )
                }
            }
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
    onClick: () -> Unit
) {
    val shape = RoundedCornerShape(18.dp)
    val cardFill = Color.White.copy(alpha = 0.05f)
    val cardBorder = Color.White.copy(alpha = 0.08f)

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(92.dp)
            .clip(shape)
            .background(cardFill)
            .border(1.dp, cardBorder, shape)
            .clickable(onClick = onClick)
            .padding(14.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxSize(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(34.dp)
                    .clip(CircleShape)
                    .background(tile.iconBg),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = tile.icon,
                    contentDescription = null,
                    tint = Color.Black,
                    modifier = Modifier.size(18.dp)
                )
            }

            Spacer(Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = tile.label,
                    color = Color.White,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Medium
                )
            }

            Text(
                text = count.toString(),
                color = Color.White.copy(alpha = 0.55f),
                style = MaterialTheme.typography.titleMedium
            )

            Spacer(Modifier.width(6.dp))

            Icon(
                imageVector = Icons.Outlined.ChevronRight,
                contentDescription = null,
                tint = Color.White.copy(alpha = 0.45f)
            )
        }
    }
}
