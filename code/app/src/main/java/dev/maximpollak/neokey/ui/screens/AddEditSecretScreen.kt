@file:OptIn(ExperimentalMaterial3Api::class)

package dev.maximpollak.neokey.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import dev.maximpollak.neokey.domain.model.Secret
import dev.maximpollak.neokey.domain.model.SecretType
import dev.maximpollak.neokey.utils.calculatePasswordStrength
import dev.maximpollak.neokey.utils.generatePassword
import dev.maximpollak.neokey.viewmodel.SecretsViewModel
import dev.maximpollak.neokey.viewmodel.SecretsViewModelFactory

enum class SecretWizardStep { Service, Password, Category, NotesReview }

@Composable
fun AddEditSecretScreen(
    secretId: Int? = null,
    onNavigateBack: () -> Unit
) {
    val context = LocalContext.current
    val viewModel: SecretsViewModel = viewModel(factory = SecretsViewModelFactory(context))

    val secrets by viewModel.secrets.collectAsState(initial = emptyList())
    val editingSecret = secrets.firstOrNull { it.id == secretId }

    var step by remember { mutableStateOf(SecretWizardStep.Service) }

    var category by remember(editingSecret) { mutableStateOf(editingSecret?.category ?: SecretType.ELSE) }
    var title by remember(editingSecret) { mutableStateOf(editingSecret?.title ?: "") }
    var account by remember(editingSecret) { mutableStateOf(editingSecret?.account ?: "") }
    var password by remember(editingSecret) { mutableStateOf(editingSecret?.password ?: "") }
    var note by remember(editingSecret) { mutableStateOf(editingSecret?.note ?: "") }

    val passwordStrength = calculatePasswordStrength(password)

    val isEdit = editingSecret != null
    val stepIndex = when (step) {
        SecretWizardStep.Service -> 1
        SecretWizardStep.Password -> 2
        SecretWizardStep.Category -> 3
        SecretWizardStep.NotesReview -> 4
    }
    val totalSteps = 4

    fun canGoNext(): Boolean = when (step) {
        SecretWizardStep.Service -> title.isNotBlank()
        SecretWizardStep.Password -> password.isNotBlank()
        SecretWizardStep.Category -> true
        SecretWizardStep.NotesReview -> true
    }

    fun nextStep() {
        step = when (step) {
            SecretWizardStep.Service -> SecretWizardStep.Password
            SecretWizardStep.Password -> SecretWizardStep.Category
            SecretWizardStep.Category -> SecretWizardStep.NotesReview
            SecretWizardStep.NotesReview -> SecretWizardStep.NotesReview
        }
    }

    fun prevStep() {
        step = when (step) {
            SecretWizardStep.Service -> SecretWizardStep.Service
            SecretWizardStep.Password -> SecretWizardStep.Service
            SecretWizardStep.Category -> SecretWizardStep.Password
            SecretWizardStep.NotesReview -> SecretWizardStep.Category
        }
    }

    fun save() {
        val secret = Secret(
            id = editingSecret?.id ?: 0,
            title = title.trim(),
            account = account.trim(), // must always be a String (can be empty)
            password = password,
            category = category,
            note = note.trim().takeIf { it.isNotBlank() }, // String? (null if empty)
            createdAt = editingSecret?.createdAt ?: System.currentTimeMillis()
        )
        if (isEdit) viewModel.updateSecret(secret) else viewModel.addSecret(secret)
        onNavigateBack()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (isEdit) "Edit Entry" else "Add Entry") },
                navigationIcon = {
                    IconButton(
                        onClick = {
                            // Back behavior like your wizard: if first step -> leave; else -> previous step
                            if (step == SecretWizardStep.Service) onNavigateBack() else prevStep()
                        }
                    ) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        },
        containerColor = Color(0xFF0E0E12)
    ) { padding ->

        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {

            // Step indicator
            Text(
                text = "Step $stepIndex of $totalSteps",
                color = Color.White.copy(alpha = 0.65f),
                style = MaterialTheme.typography.bodyMedium
            )
            LinearProgressIndicator(
                progress = stepIndex / totalSteps.toFloat(),
                modifier = Modifier.fillMaxWidth(),
                color = Color(0xFF25E6C8),
                trackColor = Color.White.copy(alpha = 0.08f)
            )

            Spacer(Modifier.height(8.dp))

            when (step) {
                SecretWizardStep.Service -> {
                    Text("Service Info", style = MaterialTheme.typography.titleMedium, color = Color.White)

                    OutlinedTextField(
                        value = title,
                        onValueChange = { title = it },
                        label = { Text("Service / Title") },
                        modifier = Modifier.fillMaxWidth(),
                        colors = neoFieldColors()
                    )

                    OutlinedTextField(
                        value = account,
                        onValueChange = { account = it },
                        label = { Text("Account / Username (optional)") },
                        modifier = Modifier.fillMaxWidth(),
                        colors = neoFieldColors()
                    )
                }

                SecretWizardStep.Password -> {
                    Text("Password / Secret", style = MaterialTheme.typography.titleMedium, color = Color.White)

                    OutlinedTextField(
                        value = password,
                        onValueChange = { password = it },
                        label = { Text("Password / Secret") },
                        modifier = Modifier.fillMaxWidth(),
                        colors = neoFieldColors()
                    )

                    LinearProgressIndicator(
                        progress = passwordStrength.score.toFloat().div(6f),
                        color = passwordStrength.color,
                        trackColor = Color.White.copy(alpha = 0.08f),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(8.dp)
                    )

                    Text(
                        text = passwordStrength.label,
                        color = passwordStrength.color,
                        style = MaterialTheme.typography.bodyMedium
                    )

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
                            containerColor = Color(0xFF25E6C8),
                            contentColor = Color(0xFF0E0E12)
                        ),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(Icons.Filled.Refresh, contentDescription = "Generate")
                        Spacer(Modifier.width(8.dp))
                        Text("Generate Strong Password")
                    }
                }

                SecretWizardStep.Category -> {
                    Text("Category", style = MaterialTheme.typography.titleMedium, color = Color.White)

                    // simple choice list (like your prototype step)
                    SecretType.entries.forEach { c ->
                        val selected = c == category
                        OutlinedButton(
                            onClick = { category = c },
                            modifier = Modifier.fillMaxWidth(),
                            colors = ButtonDefaults.outlinedButtonColors(
                                containerColor = if (selected) Color(0xFF0E3D3A) else Color.Transparent,
                                contentColor = if (selected) Color(0xFF25E6C8) else Color.White
                            ),
                            border = ButtonDefaults.outlinedButtonBorder.copy(
                                width = 1.dp
                            )
                        ) {
                            Text(c.name)
                        }
                    }
                }

                SecretWizardStep.NotesReview -> {
                    Text("Notes & Review", style = MaterialTheme.typography.titleMedium, color = Color.White)

                    OutlinedTextField(
                        value = note,
                        onValueChange = { note = it },
                        label = { Text("Note (optional)") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(min = 96.dp),
                        colors = neoFieldColors()
                    )

                    // small review block
                    Card(
                        colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.05f))
                    ) {
                        Column(Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            Text("Service: ${title.trim()}", color = Color.White)
                            if (account.isNotBlank()) Text("Account: ${account.trim()}", color = Color.White.copy(alpha = 0.75f))
                            Text("Category: ${category.name}", color = Color.White.copy(alpha = 0.75f))
                            Text("Password strength: ${passwordStrength.label}", color = passwordStrength.color)
                        }
                    }
                }
            }

            Spacer(Modifier.height(8.dp))

            // Bottom nav buttons (Back / Next / Save)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                OutlinedButton(
                    onClick = { if (step == SecretWizardStep.Service) onNavigateBack() else prevStep() },
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.White)
                ) {
                    Text("Back")
                }

                Button(
                    onClick = {
                        if (step == SecretWizardStep.NotesReview) save() else nextStep()
                    },
                    enabled = canGoNext(),
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF25E6C8),
                        contentColor = Color(0xFF0E0E12)
                    )
                ) {
                    Text(if (step == SecretWizardStep.NotesReview) (if (isEdit) "Update Entry" else "Save Entry") else "Continue")
                }
            }

            Spacer(Modifier.height(18.dp))
        }
    }
}

@Composable
private fun neoFieldColors() = OutlinedTextFieldDefaults.colors(
    focusedTextColor = Color.White,
    unfocusedTextColor = Color.White,
    focusedContainerColor = Color.White.copy(alpha = 0.05f),
    unfocusedContainerColor = Color.White.copy(alpha = 0.05f),
    cursorColor = Color(0xFF25E6C8),
    focusedBorderColor = Color.White.copy(alpha = 0.15f),
    unfocusedBorderColor = Color.White.copy(alpha = 0.10f),
    focusedLabelColor = Color.White.copy(alpha = 0.70f),
    unfocusedLabelColor = Color.White.copy(alpha = 0.55f)
)