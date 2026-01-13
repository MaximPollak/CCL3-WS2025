// File: SecretDetailScreen.kt
package dev.maximpollak.neokey.ui.screens

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.outlined.ArrowBackIosNew
import androidx.compose.material.icons.outlined.Shield
import androidx.compose.material.icons.outlined.Visibility
import androidx.compose.material.icons.outlined.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
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
import dev.maximpollak.neokey.viewmodel.SecretsViewModel
import dev.maximpollak.neokey.viewmodel.SecretsViewModelFactory
import kotlin.math.max
import kotlin.math.min

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SecretDetailScreen(
    secretId: Int,
    onEdit: () -> Unit,          // kept for your NavGraph, not shown in this Figma screen
    onNavigateBack: () -> Unit
) {
    val context = LocalContext.current
    val viewModel: SecretsViewModel = viewModel(factory = SecretsViewModelFactory(context))
    val secrets by viewModel.secrets.collectAsState(initial = emptyList())

    val secretNullable = secrets.firstOrNull { it.id == secretId }
    val secret = secretNullable ?: run {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Brush.verticalGradient(listOf(Color(0xFF070A12), Color(0xFF0B1020)))),
            contentAlignment = Alignment.Center
        ) {
            Text("Entry not found", color = Color.White.copy(alpha = 0.7f))
        }
        return
    }

    // --- Figma-like colors ---
    val neoMint = Color(0xFF38FBDB)
    val bgTop = Color(0xFF070A12)
    val bgBottom = Color(0xFF0B1020)

    val cardFill = Color.White.copy(alpha = 0.05f)
    val cardBorder = Color.White.copy(alpha = 0.08f)

    val labelColor = Color.White.copy(alpha = 0.45f)
    val valueColor = Color.White.copy(alpha = 0.95f)

    var revealPassword by remember { mutableStateOf(false) }

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

            // Top bar: back left, title centered
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
            ) {
                IconButton(
                    onClick = onNavigateBack,
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

            // SERVICE card
            FigmaCard(
                title = "SERVICE",
                cardFill = cardFill,
                cardBorder = cardBorder
            ) {
                Text(
                    text = secret.title,
                    color = valueColor,
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Medium
                )
            }

            Spacer(Modifier.height(14.dp))

            // USERNAME card + copy
            FigmaCard(
                title = "USERNAME",
                trailing = {
                    IconButton(onClick = {
                        copyToClipboard(context, "username", secret.account)
                    }) {
                        Icon(
                            imageVector = Icons.Filled.ContentCopy,
                            contentDescription = "Copy username",
                            tint = neoMint
                        )
                    }
                },
                cardFill = cardFill,
                cardBorder = cardBorder
            ) {
                Text(
                    text = secret.account,
                    color = valueColor,
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Medium
                )
            }

            Spacer(Modifier.height(14.dp))

            // PASSWORD card: copy + eye + strength bar
            FigmaCard(
                title = "PASSWORD",
                trailing = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        IconButton(onClick = {
                            copyToClipboard(context, "password", secret.password)
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
                val masked = "•".repeat(max(8, min(secret.password.length, 14)))
                Text(
                    text = if (revealPassword) secret.password else masked,
                    color = valueColor,
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Medium
                )

                Spacer(Modifier.height(14.dp))

                val strength = passwordStrength(secret.password)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Shield,
                        contentDescription = null,
                        tint = neoMint,
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
                        color = neoMint,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                }

                Spacer(Modifier.height(10.dp))

                // progress bar like Figma
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(10.dp)
                        .clip(RoundedCornerShape(999.dp))
                        .background(Color.White.copy(alpha = 0.08f))
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth(strength.progress)
                            .fillMaxHeight()
                            .clip(RoundedCornerShape(999.dp))
                            .background(neoMint)
                    )
                }
            }

            Spacer(Modifier.height(14.dp))

            // CATEGORY card + chip
            FigmaCard(
                title = "CATEGORY",
                cardFill = cardFill,
                cardBorder = cardBorder
            ) {
                CategoryChipFigma(
                    type = secret.category,
                    neoMint = neoMint
                )
            }

            Spacer(Modifier.height(14.dp))

            // NOTES card
            FigmaCard(
                title = "NOTES",
                cardFill = cardFill,
                cardBorder = cardBorder
            ) {
                val noteText = secret.note?.takeIf { it.isNotBlank() } ?: "—"
                Text(
                    text = noteText,
                    color = Color.White.copy(alpha = 0.75f),
                    style = MaterialTheme.typography.bodyLarge
                )
            }

            Spacer(Modifier.weight(1f))
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

                if (trailing != null) trailing()
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
    // Figma shows mint chip; we keep mint for all categories for consistency
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

private fun copyToClipboard(context: Context, label: String, value: String) {
    val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
    clipboard.setPrimaryClip(ClipData.newPlainText(label, value))
}

private data class Strength(val label: String, val progress: Float)

private fun passwordStrength(pw: String): Strength {
    // Simple heuristic for UI only (works offline, no libs)
    var score = 0
    if (pw.length >= 8) score++
    if (pw.length >= 12) score++
    if (pw.any { it.isLowerCase() } && pw.any { it.isUpperCase() }) score++
    if (pw.any { it.isDigit() }) score++
    if (pw.any { !it.isLetterOrDigit() }) score++

    return when {
        score >= 4 -> Strength("Strong", 0.92f)
        score == 3 -> Strength("Good", 0.70f)
        score == 2 -> Strength("Okay", 0.50f)
        else -> Strength("Weak", 0.28f)
    }
}
