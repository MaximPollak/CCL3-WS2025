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
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.FloatingActionButtonDefaults

data class CategoryTile(
    val key: String,              // "ALL" or SecretType.name
    val label: String,
    val icon: androidx.compose.ui.graphics.vector.ImageVector,
    val iconBg: Color
)

@Composable
fun CategoriesScreen(
    onCategoryClick: (String) -> Unit, // passes "ALL" or "WORK"/"WIFI"/...
    onAddClick: () -> Unit
) {
    val context = LocalContext.current
    val viewModel: SecretsViewModel = viewModel(factory = SecretsViewModelFactory(context))
    val secrets by viewModel.secrets.collectAsState(initial = emptyList())

    // ✅ Categories now match your enum exactly
    val tiles = listOf(
        CategoryTile("ALL", "All", Icons.Outlined.Key, Color(0xFF1F8EF1)),
        CategoryTile(SecretType.WORK.name, "Work", Icons.Outlined.PeopleAlt, Color(0xFF33C759)),
        CategoryTile(SecretType.EDUCATION.name, "Education", Icons.Outlined.QrCode2, Color(0xFFFFCC00)),
        CategoryTile(SecretType.WIFI.name, "Wi-Fi", Icons.Outlined.Wifi, Color(0xFF64D2FF)),
        CategoryTile(SecretType.PRIVATE.name, "Private", Icons.Outlined.Security, Color(0xFFFF3B30)),
        CategoryTile(SecretType.ELSE.name, "Else", Icons.Outlined.Folder, Color(0xFFFF9500)),
    )

    // ✅ "ALL" shows all entries
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

        // MAIN CONTENT
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
                contentPadding = PaddingValues(bottom = 120.dp),
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

        // FLOATING ADD BUTTON
        FloatingActionButton(
            onClick = onAddClick,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .navigationBarsPadding()
                .padding(bottom = 18.dp)
                .size(72.dp)
                .border(3.dp, Color(0xFF38FBDB), CircleShape),
            shape = CircleShape,
            containerColor = Color(0xFF0D121E),
            contentColor = Color(0xFF38FBDB),
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

            Spacer(Modifier.width(6.dp))

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

            Spacer(Modifier.width(0.dp))

            Icon(
                imageVector = Icons.Outlined.ChevronRight,
                contentDescription = null,
                tint = Color.White.copy(alpha = 0.45f)
            )
        }
    }
}