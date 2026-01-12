package dev.maximpollak.neokey.ui.screens

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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import dev.maximpollak.neokey.domain.model.Secret
import dev.maximpollak.neokey.domain.model.SecretType
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

    // IMPORTANT: your repository currently decrypts in getAllSecrets(),
    // so secrets here are already plaintext.
    val secrets by viewModel.secrets.collectAsState(initial = emptyList())
    val editingSecret = secrets.firstOrNull { it.id == secretId }

    var category by remember(editingSecret) { mutableStateOf(editingSecret?.category ?: SecretType.ELSE) }
    var title by remember(editingSecret) { mutableStateOf(editingSecret?.title ?: "") }
    var account by remember(editingSecret) { mutableStateOf(editingSecret?.account ?: "") }
    var password by remember(editingSecret) { mutableStateOf(editingSecret?.password ?: "") }
    var note by remember(editingSecret) { mutableStateOf(editingSecret?.note ?: "") }

    // Strength state (optional, but you already have it)
    val passwordStrength = calculatePasswordStrength(password)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF121212))
            .padding(16.dp),
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        Column {
            Text(
                text = if (editingSecret == null) "Add Entry" else "Edit Entry",
                style = MaterialTheme.typography.headlineSmall,
                color = Color.White
            )

            Spacer(modifier = Modifier.height(16.dp))

            // ---- CATEGORY DROPDOWN ----
            var expanded by remember { mutableStateOf(false) }
            Box {
                OutlinedTextField(
                    value = category.name,
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Category") },
                    trailingIcon = {
                        IconButton(onClick = { expanded = true }) {
                            Icon(Icons.Filled.ArrowDropDown, contentDescription = null)
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    colors = neoFieldColors()
                )

                DropdownMenu(
                    expanded = expanded,
                    onDismissRequest = { expanded = false },
                    modifier = Modifier.background(Color(0xFF1E1E1E))
                ) {
                    SecretType.entries.forEach { c ->
                        DropdownMenuItem(
                            text = { Text(c.name, color = Color.White) },
                            onClick = {
                                category = c
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
                label = { Text("Service / Title") },
                modifier = Modifier.fillMaxWidth(),
                colors = neoFieldColors()
            )

            Spacer(modifier = Modifier.height(16.dp))

            // ---- ACCOUNT ----
            OutlinedTextField(
                value = account,
                onValueChange = { account = it },
                label = { Text("Account / Username") },
                modifier = Modifier.fillMaxWidth(),
                colors = neoFieldColors()
            )

            Spacer(modifier = Modifier.height(16.dp))

            // ---- PASSWORD ----
            OutlinedTextField(
                value = password,
                onValueChange = { password = it },
                label = { Text("Password") },
                modifier = Modifier.fillMaxWidth(),
                colors = neoFieldColors()
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Strength bar + label (uses your existing helper)
            LinearProgressIndicator(
                progress = passwordStrength.score.toFloat().div(6f),
                color = passwordStrength.color,
                trackColor = Color(0xFF2E2E2E),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp)
            )

            Text(
                text = passwordStrength.label,
                color = passwordStrength.color,
                fontSize = 14.sp,
                modifier = Modifier.padding(top = 4.dp)
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Generate password button
            Button(
                onClick = {
                    password = generatePassword(
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

            Spacer(modifier = Modifier.height(16.dp))

            // ---- NOTE ----
            OutlinedTextField(
                value = note,
                onValueChange = { note = it },
                label = { Text("Note (optional)") },
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 96.dp),
                colors = neoFieldColors()
            )
        }

        // ---- SAVE / BACK BUTTONS ----
        Column {
            Button(
                modifier = Modifier.fillMaxWidth(),
                onClick = {
                    // Basic validation (tweak as you like)
                    if (title.isBlank()) return@Button

                    val secret = Secret(
                        id = editingSecret?.id ?: 0,
                        title = title.trim(),
                        account = account.trim(),
                        password = password,
                        category = category,
                        note = note.takeIf { it.isNotBlank() },
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
                Text(if (editingSecret == null) "Save Entry" else "Update Entry")
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

@Composable
private fun neoFieldColors() = OutlinedTextFieldDefaults.colors(
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