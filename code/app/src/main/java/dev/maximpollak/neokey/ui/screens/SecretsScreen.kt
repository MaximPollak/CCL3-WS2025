package dev.maximpollak.neokey.ui.screens

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.os.Build
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.ArrowBackIosNew
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material.icons.outlined.Visibility
import androidx.compose.material.icons.outlined.VisibilityOff
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.FloatingActionButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.work.Worker
import androidx.work.WorkerParameters
import dev.maximpollak.neokey.domain.model.Secret
import dev.maximpollak.neokey.domain.model.SecretType
import dev.maximpollak.neokey.security.CryptoManager
import dev.maximpollak.neokey.utils.copySensitiveToClipboard
import dev.maximpollak.neokey.viewmodel.SecretsViewModel
import dev.maximpollak.neokey.viewmodel.SecretsViewModelFactory

@Composable
fun SecretsScreen(
    onAddClick: () -> Unit,
    onBackClick: () -> Unit,
    onSecretClick: (Int) -> Unit,
    onSecretLongPress: (Int) -> Unit, // EDIT route
    categoryFilter: String = "ALL"
) {
    val context = LocalContext.current
    val viewModel: SecretsViewModel = viewModel(factory = SecretsViewModelFactory(context))
    val secrets by viewModel.secrets.collectAsState(initial = emptyList())

    var query by remember { mutableStateOf("") }
    var backLocked by remember { mutableStateOf(false) }

    // Long-press options dialog state (stores the secret id)
    var optionsSecretId by remember { mutableStateOf<Int?>(null) }
    var showDeleteConfirm by remember { mutableStateOf(false) }

    // which entries are currently revealed
    val revealed = remember { mutableStateMapOf<Int, Boolean>() }

    // decrypted plaintext cached ONLY while revealed (removed on hide)
    val revealedPassword = remember { mutableStateMapOf<Int, String>() }

    val categoryLabel = remember(categoryFilter) {
        when (categoryFilter) {
            "ALL" -> "All"
            else -> categoryFilter.lowercase().replaceFirstChar { it.uppercase() }
        }
    }

    val filtered = remember(query, secrets, categoryFilter) {
        val q = query.trim().lowercase()

        val categoryFiltered =
            if (categoryFilter == "ALL") secrets
            else secrets.filter { it.category.name == categoryFilter }

        if (q.isEmpty()) categoryFiltered
        else categoryFiltered.filter { s ->
            s.title.lowercase().contains(q) ||
                    s.account.lowercase().contains(q) ||
                    s.category.name.lowercase().contains(q) ||
                    (s.note?.lowercase()?.contains(q) == true)
        }
    }

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
            Spacer(Modifier.height(12.dp))

            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = {
                        if (backLocked) return@IconButton
                        backLocked = true
                        onBackClick()
                    },
                    enabled = !backLocked
                ) {
                    Icon(
                        imageVector = Icons.Outlined.ArrowBackIosNew,
                        contentDescription = "Back",
                        tint = Color(0xFF38FBDB)
                    )
                }

                Spacer(Modifier.width(6.dp))

                Column {
                    Text(
                        text = "NEOKey",
                        style = MaterialTheme.typography.headlineLarge,
                        fontWeight = FontWeight.SemiBold,
                        color = Color.White
                    )
                    Spacer(Modifier.height(4.dp))
                    Text(
                        text = "$categoryLabel • ${filtered.size} secured entries",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.White.copy(alpha = 0.65f)
                    )
                }
            }

            Spacer(Modifier.height(14.dp))

            SearchPill(
                value = query,
                onValueChange = { query = it }
            )

            Spacer(Modifier.height(14.dp))

            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(bottom = 110.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(filtered, key = { it.id }) { secret ->
                    val isRevealed = revealed[secret.id] == true
                    val displayPassword = if (isRevealed) {
                        revealedPassword[secret.id] ?: MASK
                    } else MASK

                    SecretListCard(
                        secret = secret,
                        revealed = isRevealed,
                        displayPassword = displayPassword,
                        onCopyPassword = {
                            val plain = CryptoManager.decrypt(secret.password)
                            copySensitiveToClipboard(
                                context = context,
                                label = "password",
                                value = plain,
                                clearAfterMs = 30_000L
                            )
                        },
                        onToggleReveal = {
                            val currentlyRevealed = revealed[secret.id] == true
                            if (!currentlyRevealed) {
                                // reveal -> decrypt now, store plaintext only in RAM
                                revealedPassword[secret.id] = CryptoManager.decrypt(secret.password)
                                revealed[secret.id] = true
                            } else {
                                // hide -> forget plaintext
                                revealedPassword.remove(secret.id)
                                revealed[secret.id] = false
                            }
                        },
                        onClick = { onSecretClick(secret.id) },
                        onLongPress = { optionsSecretId = secret.id }
                    )
                }
            }
        }

        FloatingActionButton(
            onClick = onAddClick,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .navigationBarsPadding()
                .padding(bottom = 18.dp)
                .size(72.dp)
                .border(
                    width = 3.dp,
                    color = Color(0xFF38FBDB),
                    shape = CircleShape
                )
                .shadow(
                    elevation = 18.dp,
                    shape = CircleShape,
                    clip = false
                ),
            shape = CircleShape,
            containerColor = Color(0xFF0D121E),
            contentColor = Color(0xFF38FBDB),
            elevation = FloatingActionButtonDefaults.elevation(
                defaultElevation = 0.dp,
                pressedElevation = 0.dp
            )
        ) {
            Icon(
                imageVector = Icons.Outlined.Add,
                contentDescription = "Add secret",
                modifier = Modifier.size(34.dp)
            )
        }

        // Long-press options dialog (Edit / Delete / Cancel)
        if (optionsSecretId != null) {
            val id = optionsSecretId!!

            AlertDialog(
                onDismissRequest = { optionsSecretId = null },
                title = { Text("Entry options") },
                text = { Text("What do you want to do?") },
                confirmButton = {},
                dismissButton = {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 4.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        TextButton(
                            onClick = { optionsSecretId = null },
                            modifier = Modifier.weight(1f),
                            colors = ButtonDefaults.textButtonColors(
                                contentColor = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        ) {
                            Text("Cancel")
                        }

                        TextButton(
                            onClick = { showDeleteConfirm = true },
                            modifier = Modifier.weight(1f),
                            colors = ButtonDefaults.textButtonColors(
                                contentColor = MaterialTheme.colorScheme.error
                            )
                        ) {
                            Text("Delete")
                        }

                        TextButton(
                            onClick = {
                                optionsSecretId = null
                                onSecretLongPress(id)
                            },
                            modifier = Modifier.weight(1f),
                        ) {
                            Text("Edit")
                        }
                    }
                }
            )
        }
        if (showDeleteConfirm && optionsSecretId != null) {
            val id = optionsSecretId!!

            AlertDialog(
                onDismissRequest = { showDeleteConfirm = false },
                title = { Text("Delete entry?") },
                text = { Text("This cannot be undone.") },
                confirmButton = {
                    TextButton(
                        onClick = {
                            val secretToDelete = secrets.firstOrNull { it.id == id }
                            showDeleteConfirm = false
                            optionsSecretId = null
                            if (secretToDelete != null) {
                                viewModel.deleteSecret(secretToDelete)
                            }
                        },
                        colors = ButtonDefaults.textButtonColors(
                            contentColor = MaterialTheme.colorScheme.error
                        )
                    ) { Text("Delete") }
                },
                dismissButton = {
                    TextButton(
                        onClick = { showDeleteConfirm = false },
                        colors = ButtonDefaults.textButtonColors(
                            contentColor = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    ) { Text("Cancel") }
                }
            )
        }
    }
}

@Composable
private fun SearchPill(
    value: String,
    onValueChange: (String) -> Unit
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        modifier = Modifier
            .fillMaxWidth()
            .height(54.dp),
        singleLine = true,
        placeholder = {
            Text(
                text = "Search entries...",
                color = Color.White.copy(alpha = 0.45f)
            )
        },
        leadingIcon = {
            Icon(
                imageVector = Icons.Outlined.Search,
                contentDescription = null,
                tint = Color.White.copy(alpha = 0.65f)
            )
        },
        shape = RoundedCornerShape(18.dp),
        colors = OutlinedTextFieldDefaults.colors(
            focusedContainerColor = Color.White.copy(alpha = 0.05f),
            unfocusedContainerColor = Color.White.copy(alpha = 0.05f),
            disabledContainerColor = Color.White.copy(alpha = 0.04f),
            focusedBorderColor = Color.White.copy(alpha = 0.10f),
            unfocusedBorderColor = Color.White.copy(alpha = 0.08f),
            cursorColor = Color(0xFF25E6C8),
            focusedTextColor = Color.White,
            unfocusedTextColor = Color.White
        )
    )
}

@Composable
private fun SecretListCard(
    secret: Secret,
    revealed: Boolean,
    displayPassword: String,
    onToggleReveal: () -> Unit,
    onCopyPassword: () -> Unit,
    onClick: () -> Unit,
    onLongPress: () -> Unit
) {
    val cardShape = RoundedCornerShape(22.dp)
    val surface = Color.White.copy(alpha = 0.05f)

    val accent = secret.category.accentColor()
    val border = accent.copy(alpha = 0.35f)

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clip(cardShape)
            .combinedClickable(
                indication = null,
                interactionSource = remember { MutableInteractionSource() },
                onClick = onClick,
                onLongClick = onLongPress
            )
            .border(1.dp, border, cardShape),
        shape = cardShape,
        colors = CardDefaults.cardColors(containerColor = surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 14.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = secret.title,
                    color = Color.White,
                    fontWeight = FontWeight.SemiBold,
                    style = MaterialTheme.typography.titleMedium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f)
                )

                CategoryChip(secret.category)
            }

            Spacer(Modifier.height(10.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = displayPassword,
                    color = Color.White.copy(alpha = 0.70f),
                    style = MaterialTheme.typography.bodyMedium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f)
                )

                IconButton(onClick = onCopyPassword) {
                    Icon(
                        imageVector = Icons.Filled.ContentCopy,
                        contentDescription = "Copy password",
                        tint = Color.White.copy(alpha = 0.65f)
                    )
                }

                IconButton(onClick = onToggleReveal) {
                    Icon(
                        imageVector = if (revealed) Icons.Outlined.VisibilityOff else Icons.Outlined.Visibility,
                        contentDescription = if (revealed) "Hide password" else "Reveal password",
                        tint = Color.White.copy(alpha = 0.65f)
                    )
                }
            }
        }
    }
}

@Composable
private fun CategoryChip(type: SecretType) {
    val (bg, fg, label) = when (type) {
        SecretType.WORK -> Triple(Color(0xFF0E3D3A), Color(0xFF25E6C8), "Work")
        SecretType.EDUCATION -> Triple(Color(0xFF3A1041), Color(0xFFDA57FF), "Education")
        SecretType.WIFI -> Triple(Color(0xFF102A43), Color(0xFF6EC6FF), "WiFi")
        SecretType.PRIVATE -> Triple(Color(0xFF3A1A10), Color(0xFFFF9B6A), "Private")
        SecretType.ELSE -> Triple(Color(0xFF2A2F3A), Color(0xFFB9C2D3), "Other")
    }

    Box(
        modifier = Modifier
            .padding(start = 10.dp)
            .clip(RoundedCornerShape(999.dp))
            .background(bg.copy(alpha = 0.95f))
            .border(1.dp, fg.copy(alpha = 0.35f), RoundedCornerShape(999.dp))
            .padding(horizontal = 10.dp, vertical = 5.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = label,
            color = fg,
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.Medium
        )
    }
}

private fun SecretType.accentColor(): Color = when (this) {
    SecretType.WORK -> Color(0xFF25E6C8)
    SecretType.EDUCATION -> Color(0xFFDA57FF)
    SecretType.WIFI -> Color(0xFF6EC6FF)
    SecretType.PRIVATE -> Color(0xFFFF9B6A)
    SecretType.ELSE -> Color(0xFFB9C2D3)
}

private const val MASK = "••••••••"

// ------------------- WorkManager clipboard clearing -------------------

class ClearClipboardWorker(
    context: Context,
    params: WorkerParameters
) : Worker(context, params) {

    override fun doWork(): Result {
        val valueToCheck = inputData.getString(KEY_VALUE) ?: return Result.failure()

        val clipboard =
            applicationContext.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager

        val currentText = clipboard.primaryClip
            ?.takeIf { it.itemCount > 0 }
            ?.getItemAt(0)
            ?.coerceToText(applicationContext)
            ?.toString()

        if (currentText == valueToCheck) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                clipboard.clearPrimaryClip()
            } else {
                clipboard.setPrimaryClip(ClipData.newPlainText("", ""))
            }
        }
        return Result.success()
    }

    companion object {
        const val KEY_VALUE = "value"
    }
}