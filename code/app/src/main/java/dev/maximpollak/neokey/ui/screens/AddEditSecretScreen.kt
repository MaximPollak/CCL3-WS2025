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
import androidx.compose.foundation.layout.imePadding
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
import androidx.compose.foundation.BorderStroke
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.draw.clip

enum class SecretWizardStep { Service, Password, Category, NotesReview }

@Composable
fun AddEditSecretScreen(
    secretId: Int? = null,
    onNavigateBack: () -> Unit
) {
    val context = LocalContext.current
    val viewModel: SecretsViewModel = viewModel(factory = SecretsViewModelFactory(context))

    var saving by remember { mutableStateOf(false) }

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

    val stepLabel = when (step) {
        SecretWizardStep.Service -> "Service"
        SecretWizardStep.Password -> "Password"
        SecretWizardStep.Category -> "Category"
        SecretWizardStep.NotesReview -> "Notes"
    }

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
            account = account.trim(),
            password = password,
            category = category,
            note = note.trim().takeIf { it.isNotBlank() },
            createdAt = editingSecret?.createdAt ?: System.currentTimeMillis()
        )
        if (isEdit) viewModel.updateSecret(secret) else viewModel.addSecret(secret)
        onNavigateBack()
    }

    Scaffold(
        containerColor = Color(0xFF0E0E12),
        topBar = {
            // Match mock: back arrow + centered title + step text + progress bar
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .statusBarsPadding()
                    .padding(horizontal = 16.dp, vertical = 12.dp)
            ) {
                Box(modifier = Modifier.fillMaxWidth()) {
                    IconButton(
                        onClick = {
                            if (step == SecretWizardStep.Service) onNavigateBack() else prevStep()
                        },
                        modifier = Modifier.align(Alignment.CenterStart)
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = Color(0xFF25E6C8)
                        )
                    }

                    Text(
                        text = if (isEdit) "Edit Entry" else "Add Entry",
                        style = MaterialTheme.typography.headlineSmall,
                        color = Color.White,
                        modifier = Modifier.align(Alignment.Center)
                    )
                }

                Spacer(Modifier.height(6.dp))

                Text(
                    text = "Step $stepIndex of $totalSteps • $stepLabel",
                    color = Color.White.copy(alpha = 0.60f),
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.align(Alignment.CenterHorizontally)
                )

                Spacer(Modifier.height(10.dp))

                LinearProgressIndicator(
                    progress = stepIndex / totalSteps.toFloat(),
                    modifier = Modifier.fillMaxWidth(),
                    color = Color(0xFF25E6C8),
                    trackColor = Color.White.copy(alpha = 0.10f)
                )
            }
        },
        bottomBar = {
            // Match mock: single big button fixed at bottom
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .imePadding()              // <-- moves it up when keyboard shows
                    .navigationBarsPadding()
                    .padding(16.dp)
            ) {
                Button(
                    onClick = {
                        if (step == SecretWizardStep.NotesReview) {
                            if (saving) return@Button
                            saving = true
                            save()
                        } else {
                            nextStep()
                        }
                    },
                    enabled = canGoNext() && !saving,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF25E6C8),
                        contentColor = Color(0xFF0E0E12),
                        disabledContainerColor = Color(0xFF25E6C8).copy(alpha = 0.35f),
                        disabledContentColor = Color(0xFF0E0E12).copy(alpha = 0.6f)
                    ),
                    shape = MaterialTheme.shapes.extraLarge
                ) {
                    Text(
                        text = if (saving) "Saving..."
                        else if (step == SecretWizardStep.NotesReview)
                            (if (isEdit) "Update Entry" else "Save Entry")
                        else "Continue"
                    )
                }
            }
        }
    ) { padding ->

        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .padding(horizontal = 16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Spacer(Modifier.height(14.dp))

            when (step) {
                SecretWizardStep.Service -> {
                    Text("Service / Label *", color = Color.White.copy(alpha = 0.75f))

                    OutlinedTextField(
                        value = title,
                        onValueChange = { title = it },
                        placeholder = { Text("Netflix") },
                        modifier = Modifier.fillMaxWidth(),
                        colors = neoFieldColors()
                    )

                    Text("Username (optional)", color = Color.White.copy(alpha = 0.75f))

                    OutlinedTextField(
                        value = account,
                        onValueChange = { account = it },
                        placeholder = { Text("user@email.com") },
                        modifier = Modifier.fillMaxWidth(),
                        colors = neoFieldColors()
                    )
                }

                SecretWizardStep.Password -> {
                    Text("Password / Secret *", color = Color.White.copy(alpha = 0.75f))

                    OutlinedTextField(
                        value = password,
                        onValueChange = { password = it },
                        placeholder = { Text("Enter password") },
                        modifier = Modifier.fillMaxWidth(),
                        colors = neoFieldColors()
                    )

                    Spacer(Modifier.height(6.dp))

// Make sure "very strong" can reach a full bar + remove the tiny dot cap
                    Spacer(Modifier.height(6.dp))

// Keep your strength *label* logic untouched.
// Only map score -> bar progress, clamped to [0..1].
                    val maxScore = 6f
                    val strengthProgress = (passwordStrength.score.toFloat() / maxScore).coerceIn(0f, 1f)

                    val barShape = RoundedCornerShape(999.dp)
                    val trackColor = Color.White.copy(alpha = 0.10f)

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(8.dp)
                            .clip(barShape)
                            .background(trackColor)
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxHeight()
                                .fillMaxWidth(strengthProgress)
                                .clip(barShape)
                                .background(passwordStrength.color)
                        )
                    }

                    Text(
                        text = passwordStrength.label,
                        color = Color.White.copy(alpha = 0.60f),
                        style = MaterialTheme.typography.bodySmall
                    )


                    OutlinedButton(
                        onClick = {
                            password = generatePassword(
                                length = 12,
                                upper = true,
                                lower = true,
                                digits = true,
                                symbols = true
                            )
                        },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFF8E52F5)),
                        border = BorderStroke(1.dp, Color(0xFF8E52F5))
                    ) {
                        Icon(Icons.Filled.Refresh, contentDescription = null)
                        Spacer(Modifier.width(8.dp))
                        Text("Generate Strong Password")
                    }
                }

                SecretWizardStep.Category -> {
                    Text("Select Category", color = Color.White.copy(alpha = 0.75f))

                    // Your enum has only these 5 categories (no Finance/Social/Personal in code)
                    SecretType.values().forEach { c ->
                        val selected = c == category
                        OutlinedButton(
                            onClick = { category = c },
                            modifier = Modifier.fillMaxWidth(),
                            colors = ButtonDefaults.outlinedButtonColors(
                                containerColor = if (selected) Color(0xFF0E3D3A) else Color.White.copy(alpha = 0.05f),
                                contentColor = if (selected) Color(0xFF25E6C8) else Color.White.copy(alpha = 0.70f)
                            ),
                            border = ButtonDefaults.outlinedButtonBorder.copy(
                                width = 1.dp
                            )
                        ) {
                            Text(c.prettyLabel())
                        }
                    }
                }

                SecretWizardStep.NotesReview -> {
                    Text("Notes (optional)", color = Color.White.copy(alpha = 0.75f))

                    OutlinedTextField(
                        value = note,
                        onValueChange = { note = it },
                        placeholder = { Text("Add any additional information...") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(min = 140.dp),
                        colors = neoFieldColors()
                    )
                }
            }

            Spacer(Modifier.height(90.dp)) // keep content above bottom button
        }
    }
}

private fun SecretType.prettyLabel(): String = when (this) {
    SecretType.WORK -> "Work"
    SecretType.WIFI -> "WiFi"
    SecretType.EDUCATION -> "Education"
    SecretType.PRIVATE -> "Private"
    SecretType.ELSE -> "Other"
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