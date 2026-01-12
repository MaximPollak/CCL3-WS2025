package dev.maximpollak.neokey.ui.screens

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import dev.maximpollak.neokey.domain.model.Secret
import dev.maximpollak.neokey.domain.model.SecretType
import dev.maximpollak.neokey.security.CryptoManager
import dev.maximpollak.neokey.utils.calculatePasswordStrength
import dev.maximpollak.neokey.utils.generatePassword
import dev.maximpollak.neokey.viewmodel.SecretsViewModel
import dev.maximpollak.neokey.viewmodel.SecretsViewModelFactory

@Composable
fun AddEditSecretScreen(
    secretId: Int? = null,
    onNavigateBack: () -> Unit
) {
    val context = LocalContext.current
    val viewModel: SecretsViewModel = viewModel(factory = SecretsViewModelFactory(context))

    val secrets by viewModel.secrets.collectAsState(initial = emptyList())
    val editingSecret = secrets.firstOrNull { it.id == secretId }

    var type by remember(editingSecret) { mutableStateOf(editingSecret?.type ?: SecretType.NOTE) }
    var title by remember(editingSecret) { mutableStateOf(editingSecret?.title ?: "") }
    var content by remember(editingSecret) {
        mutableStateOf(
            editingSecret?.content?.let {
                try { CryptoManager.decrypt(it) } catch (e: Exception) { "" }
            } ?: ""
        )
    }

    // Password strength state
    val passwordStrength = if (type == SecretType.PASSWORD) calculatePasswordStrength(content) else null

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF121212))
            .padding(16.dp),
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        Column {
            Text(
                text = if (editingSecret == null) "Add Secret" else "Edit Secret",
                style = MaterialTheme.typography.headlineSmall,
                color = Color.White
            )

            Spacer(modifier = Modifier.height(16.dp))

            // ---- TYPE DROPDOWN FIRST ----
            var expanded by remember { mutableStateOf(false) }
            Box {
                OutlinedTextField(
                    value = type.name,
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Type") },
                    trailingIcon = {
                        IconButton(onClick = { expanded = true }) {
                            Icon(Icons.Filled.ArrowDropDown, contentDescription = null)
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        focusedContainerColor = Color(0xFF1E1E1E),
                        unfocusedContainerColor = Color(0xFF1E1E1E),
                        cursorColor = Color(0xFF1F8EF1),
                        focusedBorderColor = Color(0xFF1F8EF1),
                        unfocusedBorderColor = Color.Gray,
                        focusedLabelColor = Color(0xFF1F8EF1),
                        unfocusedLabelColor = Color.Gray
                    )
                )

                DropdownMenu(
                    expanded = expanded,
                    onDismissRequest = { expanded = false },
                    modifier = Modifier.background(Color(0xFF1E1E1E))
                ) {
                    SecretType.entries.forEach { t ->
                        DropdownMenuItem(
                            text = { Text(t.name, color = Color.White) },
                            onClick = {
                                type = t
                                expanded = false
                            }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // ---- TITLE ----
            OutlinedTextField(
                value = title,
                onValueChange = { title = it },
                label = { Text("Title") },
                modifier = Modifier.fillMaxWidth(),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White,
                    focusedContainerColor = Color(0xFF1E1E1E),
                    unfocusedContainerColor = Color(0xFF1E1E1E),
                    cursorColor = Color(0xFF1F8EF1),
                    focusedBorderColor = Color(0xFF1F8EF1),
                    unfocusedBorderColor = Color.Gray,
                    focusedLabelColor = Color(0xFF1F8EF1),
                    unfocusedLabelColor = Color.Gray
                )
            )

            Spacer(modifier = Modifier.height(16.dp))

            // ---- CONTENT ----
            OutlinedTextField(
                value = content,
                onValueChange = { content = it },
                label = { Text(if (type == SecretType.PASSWORD) "Password" else "Content") },
                modifier = Modifier.fillMaxWidth(),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White,
                    focusedContainerColor = Color(0xFF1E1E1E),
                    unfocusedContainerColor = Color(0xFF1E1E1E),
                    cursorColor = Color(0xFF1F8EF1),
                    focusedBorderColor = Color(0xFF1F8EF1),
                    unfocusedBorderColor = Color.Gray,
                    focusedLabelColor = Color(0xFF1F8EF1),
                    unfocusedLabelColor = Color.Gray
                )
            )

            // ---- PASSWORD STRENGTH & GENERATOR ----
            if (type == SecretType.PASSWORD) {
                Spacer(modifier = Modifier.height(12.dp))

                // Strength bar + label
                LinearProgressIndicator(
                    progress = passwordStrength?.score?.toFloat()?.div(6f) ?: 0f,
                    color = passwordStrength?.color ?: Color.Gray,
                    trackColor = Color(0xFF2E2E2E),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(8.dp)
                )

                passwordStrength?.label?.let { label ->
                    Text(
                        text = label,
                        color = passwordStrength.color,
                        fontSize = 14.sp,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Generate password button
                Button(
                    onClick = {
                        content = generatePassword(
                            length = 12,
                            upper = true,
                            lower = true,
                            digits = true,
                            symbols = true
                        )
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF03DAC5),
                        contentColor = Color.Black
                    ),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(Icons.Filled.Refresh, contentDescription = "Generate Password")
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Generate Password")
                }
            }
        }

        // ---- SAVE / BACK BUTTONS ----
        Column {
            Button(
                modifier = Modifier.fillMaxWidth(),
                onClick = {
                    if (title.isBlank() || content.isBlank()) return@Button
                    val secret = Secret(
                        id = editingSecret?.id ?: 0,
                        title = title,
                        content = CryptoManager.encrypt(content),
                        type = type,
                        createdAt = editingSecret?.createdAt ?: System.currentTimeMillis()
                    )
                    if (editingSecret == null) viewModel.addSecret(secret)
                    else viewModel.updateSecret(secret)
                    onNavigateBack()
                },
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF1F8EF1),
                    contentColor = Color.White
                )
            ) {
                Text(if (editingSecret == null) "Save Secret" else "Update Secret")
            }

            Spacer(modifier = Modifier.height(12.dp))

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