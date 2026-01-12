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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import dev.maximpollak.neokey.domain.model.Secret
import dev.maximpollak.neokey.security.CryptoManager
import dev.maximpollak.neokey.utils.color
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

    val secret = viewModel.secrets.collectAsState(initial = emptyList()).value
        .firstOrNull { it.id == secretId }

    var revealed by remember { mutableStateOf(false) }
    val decryptedContent = remember(secret?.content) {
        secret?.content?.let {
            try { CryptoManager.decrypt(it) } catch (e: Exception) { "Decryption failed" }
        } ?: ""
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF121212))
            .padding(16.dp)
    ) {
        secret?.let { currentSecret ->

            // ---- TYPE ----
            Text(text = "Type", color = Color.Gray, fontSize = 12.sp)
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = currentSecret.type.name,
                color = Color.White,
                modifier = Modifier
                    .background(currentSecret.type.color())
                    .padding(horizontal = 6.dp, vertical = 2.dp),
                fontSize = 12.sp
            )

            Spacer(modifier = Modifier.height(12.dp))

            // ---- TITLE ----
            Text(text = "Title", color = Color.Gray, fontSize = 12.sp)
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = currentSecret.title,
                style = MaterialTheme.typography.headlineSmall,
                color = Color.White
            )

            Spacer(modifier = Modifier.height(12.dp))

            // ---- CONTENT ----
            Text(text = "Content", color = Color.Gray, fontSize = 12.sp)
            Spacer(modifier = Modifier.height(4.dp))
            Surface(
                tonalElevation = 2.dp,
                shape = MaterialTheme.shapes.medium,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = if (revealed) decryptedContent else "••••••••••••",
                        style = MaterialTheme.typography.bodyLarge,
                        color = Color.White
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        // Reveal / Hide toggle
                        IconButton(onClick = { revealed = !revealed }) {
                            Icon(
                                imageVector = if (revealed) Icons.Filled.VisibilityOff else Icons.Filled.Visibility,
                                contentDescription = if (revealed) "Hide" else "Reveal",
                                tint = Color.White
                            )
                        }

                        // Copy decrypted content
                        IconButton(
                            onClick = {
                                val clipboard =
                                    context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                clipboard.setPrimaryClip(ClipData.newPlainText("secret", decryptedContent))
                            }
                        ) {
                            Icon(Icons.Filled.ContentCopy, contentDescription = "Copy", tint = Color.White)
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // ---- EDIT / DELETE ----
            Button(
                modifier = Modifier.fillMaxWidth(),
                onClick = { onEdit(currentSecret.id) },
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1F8EF1))
            ) {
                Text("Edit", color = Color.White)
            }

            Spacer(modifier = Modifier.height(8.dp))

            Button(
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFB00020)),
                onClick = {
                    viewModel.deleteSecret(currentSecret)
                    onNavigateBack()
                }
            ) {
                Text("Delete", color = Color.White)
            }

            Spacer(modifier = Modifier.height(8.dp))

            // ---- BACK BUTTON ----
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
}