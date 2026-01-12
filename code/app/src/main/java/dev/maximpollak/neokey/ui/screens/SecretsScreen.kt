package dev.maximpollak.neokey.ui.screens

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ContentCopy
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
import dev.maximpollak.neokey.domain.model.SecretType
import dev.maximpollak.neokey.security.CryptoManager
import dev.maximpollak.neokey.utils.color
import dev.maximpollak.neokey.viewmodel.SecretsViewModel
import dev.maximpollak.neokey.viewmodel.SecretsViewModelFactory

@Composable
fun SecretsScreen(
    onAddClick: () -> Unit,
    onBackClick: () -> Unit,
    onSecretClick: (Int) -> Unit
) {
    val context = LocalContext.current
    val viewModel: SecretsViewModel = viewModel(factory = SecretsViewModelFactory(context))
    val secrets by viewModel.secrets.collectAsState(initial = emptyList())
    var filter by remember { mutableStateOf<SecretType?>(null) }
    val filteredSecrets = secrets.filter { filter == null || it.type == filter }

    Scaffold(containerColor = Color(0xFF121212)) { paddingValues ->
        Box(modifier = Modifier.fillMaxSize().padding(paddingValues)) {

            Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
                // Filter Buttons
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    FilterButton("All", selected = filter == null) { filter = null }
                    FilterButton("Password", selected = filter == SecretType.PASSWORD) { filter = SecretType.PASSWORD }
                    FilterButton("WiFi", selected = filter == SecretType.WIFI) { filter = SecretType.WIFI }
                    FilterButton("Note", selected = filter == SecretType.NOTE) { filter = SecretType.NOTE }
                }

                Spacer(modifier = Modifier.height(16.dp))

                if (filteredSecrets.isEmpty()) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text("No secrets found", color = Color.Gray)
                    }
                } else {
                    LazyColumn(
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxSize()
                    ) {
                        items(filteredSecrets) { secret ->
                            SecretCard(secret = secret, onClick = { onSecretClick(secret.id) }, context = context)
                        }
                    }
                }
            }

            // Bottom FAB buttons
            Box(modifier = Modifier.fillMaxSize()) {
                FloatingActionButton(
                    onClick = onBackClick,
                    containerColor = Color(0xFF1F1F1F),
                    modifier = Modifier.align(Alignment.BottomStart).padding(16.dp).size(56.dp)
                ) {
                    Icon(Icons.Filled.ArrowBack, contentDescription = "Back", tint = Color.White)
                }

                FloatingActionButton(
                    onClick = onAddClick,
                    containerColor = Color(0xFF1F8EF1),
                    modifier = Modifier.align(Alignment.BottomEnd).padding(16.dp).size(56.dp)
                ) {
                    Text("+", color = Color.White, fontSize = 24.sp)
                }
            }
        }
    }
}

@Composable
fun FilterButton(label: String, selected: Boolean, onClick: () -> Unit) {
    Button(
        onClick = onClick,
        colors = ButtonDefaults.buttonColors(
            containerColor = if (selected) Color(0xFF1F8EF1) else Color(0xFF1F1F1F),
            contentColor = if (selected) Color.White else Color.White
        )
    ) {
        Text(label)
    }
}

@Composable
fun SecretCard(secret: Secret, onClick: () -> Unit, context: Context) {
    val decryptedContent = remember(secret.content) {
        try { CryptoManager.decrypt(secret.content) } catch (e: Exception) { "" }
    }

    Card(
        modifier = Modifier.fillMaxWidth().clickable { onClick() },
        colors = CardDefaults.cardColors(containerColor = Color(0xFF1E1E1E)),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = secret.type.name,
                color = Color.White,
                modifier = Modifier.background(secret.type.color()).padding(horizontal = 6.dp, vertical = 2.dp),
                fontSize = 12.sp
            )

            Spacer(modifier = Modifier.height(6.dp))

            Text(secret.title, style = MaterialTheme.typography.titleMedium, color = Color.White)

            Spacer(modifier = Modifier.height(4.dp))

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = when (secret.type) {
                        SecretType.PASSWORD -> "••••••••"
                        SecretType.WIFI -> "Hidden WiFi password"
                        SecretType.NOTE -> "Encrypted note"
                    },
                    color = Color.Gray
                )

                IconButton(
                    onClick = {
                        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                        clipboard.setPrimaryClip(ClipData.newPlainText("secret", decryptedContent))
                    }
                ) {
                    Icon(Icons.Filled.ContentCopy, contentDescription = "Copy", tint = Color.White)
                }
            }
        }
    }
}