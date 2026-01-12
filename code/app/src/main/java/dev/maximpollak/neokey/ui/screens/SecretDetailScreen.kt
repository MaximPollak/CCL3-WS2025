package dev.maximpollak.neokey.ui.screens

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import dev.maximpollak.neokey.viewmodel.SecretsViewModel
import dev.maximpollak.neokey.viewmodel.SecretsViewModelFactory

@Composable
fun SecretDetailScreen(
    secretId: Int,
    onEdit: (Int) -> Unit,
    onNavigateBack: () -> Unit
) {
    val context = LocalContext.current
    val viewModel: SecretsViewModel = viewModel(factory = SecretsViewModelFactory(context))

    // Repository already decrypts -> secret fields are plaintext here
    val secret = viewModel.secrets.collectAsState(initial = emptyList()).value
        .firstOrNull { it.id == secretId }

    var showAccount by remember(secretId) { mutableStateOf(false) }
    var showPassword by remember(secretId) { mutableStateOf(false) }
    var showNote by remember(secretId) { mutableStateOf(false) }

    fun copy(label: String, value: String) {
        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        clipboard.setPrimaryClip(ClipData.newPlainText(label, value))
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF121212))
            .padding(16.dp)
    ) {
        if (secret == null) {
            Text("Entry not found", color = Color.White)
            Spacer(Modifier.height(12.dp))
            Button(
                modifier = Modifier.fillMaxWidth(),
                onClick = onNavigateBack
            ) {
                Text("Back")
            }
            return@Column
        }

        // ---- CATEGORY ----
        Text(text = "Category", color = Color.Gray, fontSize = 12.sp)
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = secret.category.name,
            color = Color.White,
            modifier = Modifier
                .background(Color(0xFF2A2A2A))
                .padding(horizontal = 8.dp, vertical = 4.dp),
            fontSize = 12.sp
        )

        Spacer(modifier = Modifier.height(12.dp))

        // ---- TITLE ----
        Text(text = "Title", color = Color.Gray, fontSize = 12.sp)
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = secret.title,
            style = MaterialTheme.typography.headlineSmall,
            color = Color.White
        )

        Spacer(modifier = Modifier.height(16.dp))

        // ---- ACCOUNT ----
        FieldCard(
            label = "Account / Username",
            value = secret.account,
            revealed = showAccount,
            onToggleReveal = { showAccount = !showAccount },
            onCopy = { copy("account", secret.account) }
        )

        Spacer(modifier = Modifier.height(12.dp))

        // ---- PASSWORD ----
        FieldCard(
            label = "Password",
            value = secret.password,
            revealed = showPassword,
            onToggleReveal = { showPassword = !showPassword },
            onCopy = { copy("password", secret.password) }
        )

        // ---- NOTE (optional) ----
        if (!secret.note.isNullOrBlank()) {
            Spacer(modifier = Modifier.height(12.dp))
            FieldCard(
                label = "Note",
                value = secret.note ?: "",
                revealed = showNote,
                onToggleReveal = { showNote = !showNote },
                onCopy = { copy("note", secret.note ?: "") }
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        // ---- EDIT / DELETE / BACK ----
        Button(
            modifier = Modifier.fillMaxWidth(),
            onClick = { onEdit(secret.id) },
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1F8EF1))
        ) {
            Text("Edit", color = Color.White)
        }

        Spacer(modifier = Modifier.height(8.dp))

        Button(
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFB00020)),
            onClick = {
                viewModel.deleteSecret(secret)
                onNavigateBack()
            }
        ) {
            Text("Delete", color = Color.White)
        }

        Spacer(modifier = Modifier.height(8.dp))

        Button(
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(
                containerColor = Color(0xFF1F1F1F),
                contentColor = Color.White
            ),
            onClick = onNavigateBack
        ) {
            Icon(Icons.Filled.ArrowBack, contentDescription = "Back")
            Spacer(modifier = Modifier.width(8.dp))
            Text("Back")
        }
    }
}

@Composable
private fun FieldCard(
    label: String,
    value: String,
    revealed: Boolean,
    onToggleReveal: () -> Unit,
    onCopy: () -> Unit
) {
    Text(text = label, color = Color.Gray, fontSize = 12.sp)
    Spacer(modifier = Modifier.height(4.dp))

    Surface(
        tonalElevation = 2.dp,
        shape = MaterialTheme.shapes.medium,
        modifier = Modifier.fillMaxWidth(),
        color = Color(0xFF1E1E1E)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = if (revealed) value else "••••••••••••",
                style = MaterialTheme.typography.bodyLarge,
                color = Color.White
            )

            Spacer(modifier = Modifier.height(12.dp))

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                IconButton(onClick = onToggleReveal) {
                    Icon(
                        imageVector = if (revealed) Icons.Filled.VisibilityOff else Icons.Filled.Visibility,
                        contentDescription = if (revealed) "Hide" else "Reveal",
                        tint = Color.White
                    )
                }

                IconButton(onClick = onCopy) {
                    Icon(Icons.Filled.ContentCopy, contentDescription = "Copy", tint = Color.White)
                }
            }
        }
    }
}