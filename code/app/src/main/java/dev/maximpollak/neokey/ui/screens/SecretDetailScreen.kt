// File: SecretDetailScreen.kt
package dev.maximpollak.neokey.ui.screens

import android.content.ClipData
import android.content.ClipDescription
import android.content.ClipboardManager
import android.content.Context
import android.os.Build
import android.os.Handler
import android.os.Looper
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.outlined.ArrowBackIosNew
import androidx.compose.material.icons.outlined.DeleteOutline
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.outlined.Shield
import androidx.compose.material.icons.outlined.Visibility
import androidx.compose.material.icons.outlined.VisibilityOff
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import dev.maximpollak.neokey.domain.model.SecretType
import dev.maximpollak.neokey.security.CryptoManager
import dev.maximpollak.neokey.utils.calculatePasswordStrength
import dev.maximpollak.neokey.viewmodel.SecretsViewModel
import dev.maximpollak.neokey.viewmodel.SecretsViewModelFactory
import kotlin.math.max
import kotlin.math.min

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SecretDetailScreen(
    secretId: Int,
    onEdit: () -> Unit,
    onNavigateBack: () -> Unit
) {
    val context = LocalContext.current
    val viewModel: SecretsViewModel = viewModel(factory = SecretsViewModelFactory(context))

    val secret by viewModel.selectedSecret.collectAsState()

    LaunchedEffect(secretId) {
        viewModel.loadSecretById(secretId)
    }

    // --- Colors ---
    val neoMint = Color(0xFF38FBDB)
    val bgTop = Color(0xFF070A12)
    val bgBottom = Color(0xFF0B1020)
    val cardFill = Color.White.copy(alpha = 0.05f)
    val cardBorder = Color.White.copy(alpha = 0.08f)

    var revealPassword by remember { mutableStateOf(false) }
    var showDeleteDialog by remember { mutableStateOf(false) }

    val secretValue = secret
    if (secretValue == null) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Brush.verticalGradient(listOf(bgTop, bgBottom))),
            contentAlignment = Alignment.Center
        ) {
            Text("Loading…", color = Color.White.copy(alpha = 0.70f))
        }
        return
    }

    // ✅ Decrypt immediately in detail screen
    val decryptedUsername = remember(secretValue.id, secretValue.account) {
        CryptoManager.decrypt(secretValue.account).trim()
    }
    val decryptedPassword = remember(secretValue.id, secretValue.password) {
        CryptoManager.decrypt(secretValue.password)
    }
    val decryptedNote = remember(secretValue.id, secretValue.note) {
        secretValue.note?.let { CryptoManager.decrypt(it) }
    }

    val hasUsername = decryptedUsername.isNotBlank()

    val strength = calculatePasswordStrength(decryptedPassword)

    // ✅ SAME progress logic as AddEditSecretScreen
    val maxScore = 6f
    val progress = (strength.score.toFloat() / maxScore).coerceIn(0f, 1f)

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Brush.verticalGradient(listOf(bgTop, bgBottom)))
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 18.dp)
                .verticalScroll(rememberScrollState())
                .padding(bottom = 140.dp)
        ) {
            Spacer(Modifier.height(10.dp))

            // Top bar
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
            ) {
                IconButton(
                    onClick = {
                        viewModel.clearSelectedSecret()
                        onNavigateBack()
                    },
                    modifier = Modifier.align(Alignment.CenterStart)
                ) {
                    Icon(
                        imageVector = Icons.Outlined.ArrowBackIosNew,
                        contentDescription = "Back",
                        tint = neoMint
                    )
                }

                Text(
                    text = "Entry Details",
                    color = Color.White,
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.align(Alignment.Center)
                )
            }

            Spacer(Modifier.height(16.dp))

            // SERVICE
            FigmaCard("SERVICE", cardFill = cardFill, cardBorder = cardBorder) {
                Text(
                    text = secretValue.title,
                    color = Color.White.copy(alpha = 0.95f),
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Medium
                )
            }

            Spacer(Modifier.height(14.dp))

            // USERNAME
            FigmaCard(
                title = "USERNAME",
                trailing = {
                    IconButton(
                        onClick = {
                            if (hasUsername) {
                                copySensitiveToClipboard(
                                    context = context,
                                    label = "username",
                                    value = decryptedUsername,
                                    clearAfterMs = 30_000L
                                )
                            }
                        },
                        enabled = hasUsername
                    ) {
                        Icon(
                            imageVector = Icons.Filled.ContentCopy,
                            contentDescription = "Copy username",
                            tint = neoMint.copy(alpha = if (hasUsername) 1f else 0.25f)
                        )
                    }
                },
                cardFill = cardFill,
                cardBorder = cardBorder
            ) {
                Text(
                    text = if (hasUsername) decryptedUsername else "No username assigned",
                    color = Color.White.copy(alpha = if (hasUsername) 0.95f else 0.55f),
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Medium
                )
            }

            Spacer(Modifier.height(14.dp))

            // PASSWORD
            FigmaCard(
                title = "PASSWORD",
                trailing = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        IconButton(onClick = {
                            copySensitiveToClipboard(
                                context = context,
                                label = "password",
                                value = decryptedPassword,
                                clearAfterMs = 30_000L
                            )
                        }) {
                            Icon(
                                imageVector = Icons.Filled.ContentCopy,
                                contentDescription = "Copy password",
                                tint = neoMint
                            )
                        }
                        IconButton(onClick = { revealPassword = !revealPassword }) {
                            Icon(
                                imageVector = if (revealPassword) Icons.Outlined.VisibilityOff else Icons.Outlined.Visibility,
                                contentDescription = "Toggle password",
                                tint = Color.White.copy(alpha = 0.55f)
                            )
                        }
                    }
                },
                cardFill = cardFill,
                cardBorder = cardBorder
            ) {
                val masked = "•".repeat(max(8, min(decryptedPassword.length, 14)))

                Text(
                    text = if (revealPassword) decryptedPassword else masked,
                    color = Color.White.copy(alpha = 0.95f),
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Medium
                )

                Spacer(Modifier.height(14.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Shield,
                        contentDescription = null,
                        tint = strength.color,
                        modifier = Modifier.size(18.dp)
                    )

                    Spacer(Modifier.width(8.dp))

                    Text(
                        text = "Password Strength",
                        color = Color.White.copy(alpha = 0.55f),
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.weight(1f)
                    )

                    Text(
                        text = strength.label,
                        color = strength.color,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                }

                Spacer(Modifier.height(10.dp))

                val barShape = RoundedCornerShape(999.dp)

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(10.dp)
                        .clip(barShape)
                        .background(Color.White.copy(alpha = 0.08f))
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxHeight()
                            .fillMaxWidth(progress)
                            .clip(barShape)
                            .background(strength.color)
                    )
                }
            }

            Spacer(Modifier.height(14.dp))

            // CATEGORY
            FigmaCard("CATEGORY", cardFill = cardFill, cardBorder = cardBorder) {
                CategoryChipFigma(type = secretValue.category, neoMint = neoMint)
            }

            Spacer(Modifier.height(14.dp))

            // NOTES
            FigmaCard("NOTES", cardFill = cardFill, cardBorder = cardBorder) {
                val noteText = decryptedNote?.takeIf { it.isNotBlank() } ?: "—"
                Text(
                    text = noteText,
                    color = Color.White.copy(alpha = 0.75f),
                    style = MaterialTheme.typography.bodyLarge
                )
            }

            Spacer(Modifier.height(10.dp))
        }

        BottomActionBar(
            neoMint = neoMint,
            cardFill = cardFill,
            cardBorder = cardBorder,
            onEdit = onEdit,
            onDelete = { showDeleteDialog = true },
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .navigationBarsPadding()
                .padding(horizontal = 18.dp, vertical = 14.dp)
        )

        if (showDeleteDialog) {
            AlertDialog(
                onDismissRequest = { showDeleteDialog = false },
                title = { Text("Delete entry?") },
                text = { Text("This cannot be undone.") },
                confirmButton = {
                    TextButton(
                        onClick = {
                            showDeleteDialog = false
                            viewModel.deleteSecret(secretValue)
                            viewModel.clearSelectedSecret()
                            onNavigateBack()
                        },
                        colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.error)
                    ) { Text("Delete", fontWeight = FontWeight.SemiBold) }
                },
                dismissButton = {
                    TextButton(onClick = { showDeleteDialog = false }) { Text("Cancel") }
                }
            )
        }
    }
}

/**
 * Same "copy" logic as SecretsScreen:
 * - mark as sensitive (Android 13+)
 * - auto-clear after [clearAfterMs] if clipboard still contains the same value
 */
private fun copySensitiveToClipboard(
    context: Context,
    label: String,
    value: String,
    clearAfterMs: Long = 30_000L
) {
    val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
    val clip = ClipData.newPlainText(label, value)

    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        val extras = android.os.PersistableBundle().apply {
            putBoolean(ClipDescription.EXTRA_IS_SENSITIVE, true)
        }
        clip.description.extras = extras
    }

    clipboard.setPrimaryClip(clip)

    Handler(Looper.getMainLooper()).postDelayed({
        val currentText = clipboard.primaryClip
            ?.takeIf { it.itemCount > 0 }
            ?.getItemAt(0)
            ?.coerceToText(context)
            ?.toString()

        if (currentText == value) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                clipboard.clearPrimaryClip()
            } else {
                clipboard.setPrimaryClip(ClipData.newPlainText("", ""))
            }
        }
    }, clearAfterMs)
}

// --- helpers unchanged below ---

@Composable
private fun BottomActionBar(
    neoMint: Color,
    cardFill: Color,
    cardBorder: Color,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier
) {
    val shape = RoundedCornerShape(22.dp)

    Card(
        modifier = modifier
            .fillMaxWidth()
            .border(1.dp, cardBorder, shape),
        shape = shape,
        colors = CardDefaults.cardColors(containerColor = cardFill),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Button(
                onClick = onEdit,
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(containerColor = neoMint, contentColor = Color.Black)
            ) {
                Icon(Icons.Outlined.Edit, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(Modifier.width(8.dp))
                Text("Edit", fontWeight = FontWeight.SemiBold)
            }

            OutlinedButton(
                onClick = onDelete,
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(16.dp),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.error.copy(alpha = 0.6f)),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.error)
            ) {
                Icon(Icons.Outlined.DeleteOutline, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(Modifier.width(8.dp))
                Text("Delete", fontWeight = FontWeight.SemiBold)
            }
        }
    }
}

@Composable
private fun FigmaCard(
    title: String,
    trailing: @Composable (() -> Unit)? = null,
    cardFill: Color,
    cardBorder: Color,
    content: @Composable ColumnScope.() -> Unit
) {
    val shape = RoundedCornerShape(26.dp)

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, cardBorder, shape),
        shape = shape,
        colors = CardDefaults.cardColors(containerColor = cardFill),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(modifier = Modifier.padding(18.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.Top
            ) {
                Text(
                    text = title,
                    color = Color.White.copy(alpha = 0.35f),
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.weight(1f)
                )
                trailing?.invoke()
            }

            Spacer(Modifier.height(10.dp))
            content()
        }
    }
}

@Composable
private fun CategoryChipFigma(
    type: SecretType,
    neoMint: Color
) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(14.dp))
            .background(neoMint.copy(alpha = 0.12f))
            .border(1.dp, neoMint.copy(alpha = 0.35f), RoundedCornerShape(14.dp))
            .padding(horizontal = 14.dp, vertical = 10.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = when (type) {
                SecretType.WORK -> "Work"
                SecretType.EDUCATION -> "Education"
                SecretType.WIFI -> "WiFi"
                SecretType.PRIVATE -> "Private"
                SecretType.ELSE -> "Else"
            },
            color = neoMint,
            style = MaterialTheme.typography.bodyLarge,
            fontWeight = FontWeight.Medium
        )
    }
}