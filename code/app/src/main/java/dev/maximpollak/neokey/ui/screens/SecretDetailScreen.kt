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
import androidx.compose.material.icons.outlined.ArrowBack
import androidx.compose.material.icons.outlined.DeleteOutline
import androidx.compose.material.icons.outlined.Edit
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import dev.maximpollak.neokey.domain.model.SecretType
import dev.maximpollak.neokey.viewmodel.SecretsViewModel
import dev.maximpollak.neokey.viewmodel.SecretsViewModelFactory
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SecretDetailScreen(
    secretId: Int,
    onEdit: () -> Unit,
    onNavigateBack: () -> Unit
) {
    val context = LocalContext.current
    val viewModel: SecretsViewModel = viewModel(factory = SecretsViewModelFactory(context))
    val secrets by viewModel.secrets.collectAsState()

    // ✅ Bind to a nullable first
    val secretNullable = secrets.firstOrNull { it.id == secretId }

    // ✅ Then bind to a NON-null local once (prevents all Secret? errors)
    val secret = secretNullable ?: run {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(Color(0xFF070A12), Color(0xFF0B1020))
                    )
                )
                .statusBarsPadding(),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "Secret not found.",
                color = Color.White.copy(alpha = 0.75f),
                style = MaterialTheme.typography.bodyLarge
            )
        }
        return
    }

    var revealPassword by remember { mutableStateOf(false) }
    var showDeleteDialog by remember { mutableStateOf(false) }

    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

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
        Scaffold(
            containerColor = Color.Transparent,
            contentColor = Color.White,
            contentWindowInsets = WindowInsets(0, 0, 0, 0),
            snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
            topBar = {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.Outlined.ArrowBack,
                            contentDescription = "Back",
                            tint = Color.White
                        )
                    }

                    Spacer(Modifier.width(6.dp))

                    Text(
                        text = "Details",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.SemiBold,
                        color = Color.White,
                        modifier = Modifier.weight(1f)
                    )

                    IconButton(onClick = onEdit) {
                        Icon(
                            imageVector = Icons.Outlined.Edit,
                            contentDescription = "Edit",
                            tint = Color.White.copy(alpha = 0.9f)
                        )
                    }

                    IconButton(onClick = { showDeleteDialog = true }) {
                        Icon(
                            imageVector = Icons.Outlined.DeleteOutline,
                            contentDescription = "Delete",
                            tint = Color(0xFFFF6B6B).copy(alpha = 0.9f)
                        )
                    }
                }
            }
        ) { padding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(horizontal = 18.dp)
                    .navigationBarsPadding()
            ) {
                Spacer(Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.Top
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = secret.title,
                            color = Color.White,
                            style = MaterialTheme.typography.headlineMedium,
                            fontWeight = FontWeight.SemiBold,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis
                        )
                        Spacer(Modifier.height(6.dp))
                        Text(
                            text = "Created: ${formatDate(secret.createdAt)}",
                            color = Color.White.copy(alpha = 0.55f),
                            style = MaterialTheme.typography.bodySmall
                        )
                    }

                    CategoryChip(type = secret.category)
                }

                Spacer(Modifier.height(18.dp))

                InfoCard(
                    label = "Account",
                    value = secret.account,
                    onCopy = {
                        copyToClipboard(context, "account", secret.account)
                        showSnack(scope, snackbarHostState, "Account copied")
                    }
                )

                Spacer(Modifier.height(12.dp))

                PasswordCard(
                    password = secret.password,
                    revealed = revealPassword,
                    onToggleReveal = { revealPassword = !revealPassword },
                    onCopy = {
                        copyToClipboard(context, "password", secret.password)
                        showSnack(scope, snackbarHostState, "Password copied")
                    }
                )

                Spacer(Modifier.height(12.dp))

                // ✅ No !! needed: bind note to local first
                val noteText = secret.note
                if (!noteText.isNullOrBlank()) {
                    NoteCard(note = noteText)
                    Spacer(Modifier.height(16.dp))
                }

                Spacer(Modifier.weight(1f))

                Button(
                    onClick = onEdit,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(54.dp),
                    shape = RoundedCornerShape(18.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF0D121E),
                        contentColor = Color(0xFF25E6C8)
                    )
                ) {
                    Text("Edit entry", fontWeight = FontWeight.Medium)
                }

                Spacer(Modifier.height(18.dp))
            }
        }

        if (showDeleteDialog) {
            AlertDialog(
                onDismissRequest = { showDeleteDialog = false },
                title = { Text("Delete entry?") },
                text = { Text("This cannot be undone.") },
                confirmButton = {
                    TextButton(
                        onClick = {
                            showDeleteDialog = false
                            // ✅ secret is non-null here
                            viewModel.deleteSecret(secret)
                            onNavigateBack()
                        }
                    ) {
                        Text("Delete", color = Color(0xFFFF6B6B))
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showDeleteDialog = false }) {
                        Text("Cancel")
                    }
                }
            )
        }
    }
}

@Composable
private fun InfoCard(
    label: String,
    value: String,
    onCopy: () -> Unit
) {
    val shape = RoundedCornerShape(22.dp)
    val surface = Color.White.copy(alpha = 0.05f)
    val border = Color.White.copy(alpha = 0.08f)

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clip(shape)
            .border(1.dp, border, shape),
        shape = shape,
        colors = CardDefaults.cardColors(containerColor = surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = label,
                    color = Color.White.copy(alpha = 0.65f),
                    style = MaterialTheme.typography.labelLarge,
                    modifier = Modifier.weight(1f)
                )

                IconButton(onClick = onCopy) {
                    Icon(
                        imageVector = Icons.Filled.ContentCopy,
                        contentDescription = "Copy",
                        tint = Color.White.copy(alpha = 0.75f),
                        modifier = Modifier.size(20.dp)
                    )
                }
            }

            Spacer(Modifier.height(6.dp))

            Text(
                text = value,
                color = Color.White,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Medium
            )
        }
    }
}

@Composable
private fun PasswordCard(
    password: String,
    revealed: Boolean,
    onToggleReveal: () -> Unit,
    onCopy: () -> Unit
) {
    val shape = RoundedCornerShape(22.dp)
    val surface = Color.White.copy(alpha = 0.05f)
    val border = Color.White.copy(alpha = 0.08f)

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clip(shape)
            .border(1.dp, border, shape),
        shape = shape,
        colors = CardDefaults.cardColors(containerColor = surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Password",
                    color = Color.White.copy(alpha = 0.65f),
                    style = MaterialTheme.typography.labelLarge,
                    modifier = Modifier.weight(1f)
                )

                IconButton(onClick = onToggleReveal) {
                    Icon(
                        imageVector = if (revealed) Icons.Outlined.VisibilityOff else Icons.Outlined.Visibility,
                        contentDescription = if (revealed) "Hide password" else "Show password",
                        tint = Color.White.copy(alpha = 0.75f),
                        modifier = Modifier.size(22.dp)
                    )
                }

                IconButton(onClick = onCopy) {
                    Icon(
                        imageVector = Icons.Filled.ContentCopy,
                        contentDescription = "Copy password",
                        tint = Color.White.copy(alpha = 0.75f),
                        modifier = Modifier.size(20.dp)
                    )
                }
            }

            Spacer(Modifier.height(6.dp))

            Text(
                text = if (revealed) password else "•".repeat(maxOf(8, minOf(password.length, 14))),
                color = Color.White,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Medium,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
private fun NoteCard(note: String) {
    val shape = RoundedCornerShape(22.dp)
    val surface = Color.White.copy(alpha = 0.05f)
    val border = Color.White.copy(alpha = 0.08f)

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clip(shape)
            .border(1.dp, border, shape),
        shape = shape,
        colors = CardDefaults.cardColors(containerColor = surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "Note",
                color = Color.White.copy(alpha = 0.65f),
                style = MaterialTheme.typography.labelLarge
            )
            Spacer(Modifier.height(8.dp))
            Text(
                text = note,
                color = Color.White.copy(alpha = 0.9f),
                style = MaterialTheme.typography.bodyMedium
            )
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
        SecretType.ELSE -> Triple(Color(0xFF2A2F3A), Color(0xFFB9C2D3), "Else")
    }

    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(999.dp))
            .background(bg.copy(alpha = 0.95f))
            .border(1.dp, fg.copy(alpha = 0.35f), RoundedCornerShape(999.dp))
            .padding(horizontal = 12.dp, vertical = 6.dp),
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

private fun copyToClipboard(context: Context, label: String, value: String) {
    val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
    clipboard.setPrimaryClip(ClipData.newPlainText(label, value))
}

private fun formatDate(timestamp: Long): String {
    val sdf = SimpleDateFormat("dd MMM yyyy • HH:mm", Locale.getDefault())
    return sdf.format(Date(timestamp))
}

private fun showSnack(scope: CoroutineScope, host: SnackbarHostState, message: String) {
    scope.launch { host.showSnackbar(message) }
}
