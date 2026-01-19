// File: CategoriesScreen.kt
package dev.maximpollak.neokey.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.FloatingActionButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import dev.maximpollak.neokey.domain.model.Secret
import dev.maximpollak.neokey.domain.model.SecretType
import dev.maximpollak.neokey.viewmodel.SecretsViewModel
import dev.maximpollak.neokey.viewmodel.SecretsViewModelFactory

data class CategoryTile(
    val key: String,
    val label: String,
    val icon: androidx.compose.ui.graphics.vector.ImageVector
)

@Composable
fun CategoriesScreen(
    onCategoryClick: (String) -> Unit,
    onAddClick: () -> Unit,
    onSecretClick: (Int) -> Unit
) {
    val context = LocalContext.current
    val viewModel: SecretsViewModel = viewModel(factory = SecretsViewModelFactory(context))
    val secrets by viewModel.secrets.collectAsState(initial = emptyList())

    val accent = Color(0xFF25E6C8)
    val bgTop = Color(0xFF070A12)
    val bgBottom = Color(0xFF0B1020)

    val tiles = listOf(
        CategoryTile("ALL", "All", Icons.Outlined.Key),
        CategoryTile(SecretType.WORK.name, "Work", Icons.Outlined.PeopleAlt),
        CategoryTile(SecretType.EDUCATION.name, "Education", Icons.Outlined.QrCode2),
        CategoryTile(SecretType.WIFI.name, "Wi-Fi", Icons.Outlined.Wifi),
        CategoryTile(SecretType.PRIVATE.name, "Private", Icons.Outlined.Security),
        CategoryTile(SecretType.ELSE.name, "Other", Icons.Outlined.Folder),
    )

    val counts = secretsCountMap(secrets)

    // --- Search state ---
    var query by remember { mutableStateOf("") }
    val q = query.trim()

    // Search over ALL secrets: title, account, category, and note (optional)
    val results = remember(secrets, q) {
        if (q.isBlank()) emptyList()
        else {
            val needle = q.lowercase()
            secrets.filter { s ->
                s.title.lowercase().contains(needle) ||
                        s.account.lowercase().contains(needle) ||
                        s.category.prettyLabel().lowercase().contains(needle) ||
                        (s.note?.lowercase()?.contains(needle) == true)
            }.sortedBy { it.title.lowercase() }
        }
    }

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

            // Header
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

            Spacer(Modifier.height(14.dp))

            // Inline search field
            NeoSearchField(
                value = query,
                onValueChange = { query = it },
                accent = accent,
                placeholder = "Search all secrets…"
            )

            Spacer(Modifier.height(14.dp))

            if (q.isBlank()) {
                // Categories grid (normal view)
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
            } else {
                // Search results view
                Text(
                    text = "${results.size} result(s)",
                    color = Color.White.copy(alpha = 0.60f),
                    style = MaterialTheme.typography.bodyMedium
                )
                Spacer(Modifier.height(8.dp))

                if (results.isEmpty()) {
                    Text(
                        text = "No matching secrets found.",
                        color = Color.White.copy(alpha = 0.55f),
                        style = MaterialTheme.typography.bodyMedium
                    )
                }

                LazyColumn(
                    contentPadding = PaddingValues(bottom = 140.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier.fillMaxSize()
                ) {
                    items(results, key = { it.id }) { secret ->
                        SearchResultCard(
                            secret = secret,
                            accent = accent,
                            onClick = { onSecretClick(secret.id) }
                        )
                    }
                }
            }
        }

        // Keep your original FAB
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

@Composable
private fun NeoSearchField(
    value: String,
    onValueChange: (String) -> Unit,
    accent: Color,
    placeholder: String
) {
    val shape = RoundedCornerShape(16.dp)
    val bg = Color.White.copy(alpha = 0.06f)
    val border = Color.White.copy(alpha = 0.10f)

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(52.dp)
            .clip(shape)
            .background(bg)
            .border(1.dp, border, shape)
            .padding(horizontal = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = Icons.Outlined.Search,
            contentDescription = null,
            tint = Color.White.copy(alpha = 0.65f)
        )
        Spacer(Modifier.width(10.dp))

        Box(modifier = Modifier.weight(1f)) {
            if (value.isBlank()) {
                Text(
                    text = placeholder,
                    color = Color.White.copy(alpha = 0.45f),
                    style = MaterialTheme.typography.bodyMedium
                )
            }

            BasicTextField(
                value = value,
                onValueChange = onValueChange,
                singleLine = true,
                textStyle = MaterialTheme.typography.bodyMedium.copy(color = Color.White),
                modifier = Modifier.fillMaxWidth()
            )
        }

        if (value.isNotBlank()) {
            Spacer(Modifier.width(10.dp))
            Surface(
                color = accent.copy(alpha = 0.14f),
                contentColor = accent,
                shape = RoundedCornerShape(999.dp),
                modifier = Modifier
                    .clip(RoundedCornerShape(999.dp))
                    .clickable { onValueChange("") }
            ) {
                Icon(
                    imageVector = Icons.Outlined.Close,
                    contentDescription = "Clear",
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                )
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

@Composable
private fun SearchResultCard(
    secret: Secret,
    accent: Color,
    onClick: () -> Unit
) {
    val shape = RoundedCornerShape(18.dp)
    val fill = Color.White.copy(alpha = 0.05f)
    val border = Color.White.copy(alpha = 0.08f)

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(shape)
            .background(fill)
            .border(1.dp, border, shape)
            .clickable(onClick = onClick)
            .padding(14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Surface(
            color = accent.copy(alpha = 0.14f),
            contentColor = accent,
            shape = RoundedCornerShape(14.dp)
        ) {
            Icon(
                imageVector = Icons.Outlined.VpnKey,
                contentDescription = null,
                modifier = Modifier.padding(10.dp)
            )
        }

        Spacer(Modifier.width(12.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = secret.title,
                color = Color.White,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            Text(
                text = secret.category.prettyLabel(),
                color = Color.White.copy(alpha = 0.55f),
                style = MaterialTheme.typography.bodyMedium,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }

        Icon(
            imageVector = Icons.Outlined.ChevronRight,
            contentDescription = null,
            tint = Color.White.copy(alpha = 0.35f)
        )
    }
}

private fun SecretType.prettyLabel(): String = when (this) {
    SecretType.WORK -> "Work"
    SecretType.WIFI -> "WiFi"
    SecretType.EDUCATION -> "Education"
    SecretType.PRIVATE -> "Private"
    SecretType.ELSE -> "Other"
}
