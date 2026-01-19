// File: EditSecretScreen.kt
package dev.maximpollak.neokey.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ArrowBackIosNew
import androidx.compose.material.icons.outlined.Check
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
import dev.maximpollak.neokey.domain.model.Secret
import dev.maximpollak.neokey.domain.model.SecretType
import dev.maximpollak.neokey.security.CryptoManager
import dev.maximpollak.neokey.utils.calculatePasswordStrength
import dev.maximpollak.neokey.viewmodel.SecretsViewModel
import dev.maximpollak.neokey.viewmodel.SecretsViewModelFactory
import kotlin.math.max
import kotlin.math.min
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditSecretScreen(
    secretId: Int,
    onSaved: () -> Unit,
    onNavigateBack: () -> Unit
) {
    val context = LocalContext.current
    val viewModel: SecretsViewModel = viewModel(factory = SecretsViewModelFactory(context))

    val secret by viewModel.selectedSecret.collectAsState()

    LaunchedEffect(secretId) {
        viewModel.loadSecretById(secretId)
    }

    // --- Colors (same as Detail) ---
    val neoMint = Color(0xFF38FBDB)
    val bgTop = Color(0xFF070A12)
    val bgBottom = Color(0xFF0B1020)
    val cardFill = Color.White.copy(alpha = 0.05f)
    val cardBorder = Color.White.copy(alpha = 0.08f)

    var revealPassword by remember { mutableStateOf(false) }
    var saving by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    val secretValue = secret
    if (secretValue == null) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Brush.verticalGradient(listOf(bgTop, bgBottom)))
                .statusBarsPadding(),
            contentAlignment = Alignment.Center
        ) {
            Text("Loading…", color = Color.White.copy(alpha = 0.70f))
        }
        return
    }

    // ---- Decrypt initial values ----
    val initialTitle = remember(secretValue.id, secretValue.title) { secretValue.title }
    val initialUsername = remember(secretValue.id, secretValue.account) {
        CryptoManager.decrypt(secretValue.account).trim()
    }
    val initialPassword = remember(secretValue.id, secretValue.password) {
        CryptoManager.decrypt(secretValue.password)
    }
    val initialNote = remember(secretValue.id, secretValue.note) {
        secretValue.note?.let { CryptoManager.decrypt(it) } ?: ""
    }
    val initialCategory = remember(secretValue.id, secretValue.category) { secretValue.category }

    // ---- Editable state (reset when secret changes) ----
    var title by remember { mutableStateOf(initialTitle) }
    var username by remember { mutableStateOf(initialUsername) }
    var password by remember { mutableStateOf(initialPassword) }
    var note by remember { mutableStateOf(initialNote) }
    var category by remember { mutableStateOf(initialCategory) }

    LaunchedEffect(secretValue.id) {
        title = initialTitle
        username = initialUsername
        password = initialPassword
        note = initialNote
        category = initialCategory
        errorMessage = null
        saving = false
        revealPassword = false
    }

    val strength = calculatePasswordStrength(password)
    val progress = (strength.score.toFloat() / 6f).coerceIn(0f, 1f)

    val canSave = title.trim().isNotEmpty() && password.isNotEmpty() && !saving

    fun saveNow() {
        errorMessage = null

        val t = title.trim()
        val u = username.trim()
        val p = password

        if (t.isEmpty()) {
            errorMessage = "Service name cannot be empty."
            return
        }
        if (p.isEmpty()) {
            errorMessage = "Password cannot be empty."
            return
        }

        saving = true

        val updated: Secret = secretValue.copy(
            title = t,
            account = CryptoManager.encrypt(u),
            password = CryptoManager.encrypt(p),
            note = note.takeIf { it.isNotBlank() }?.let { CryptoManager.encrypt(it) },
            category = category
        )

        // ✅ Assumes you already have this (or similar) in your ViewModel/Repository.
        // If your function name differs, rename this call.
        viewModel.updateSecret(updated)

        saving = false
        viewModel.clearSelectedSecret()
        onSaved()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Brush.verticalGradient(listOf(bgTop, bgBottom)))
            .statusBarsPadding()
    ) {
        // Fixed header
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 18.dp)
        ) {
            Spacer(Modifier.height(10.dp))

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
                    text = "Edit Entry",
                    color = Color.White,
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.align(Alignment.Center)
                )
            }

            Spacer(Modifier.height(16.dp))

            // Scrollable content
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(bottom = 140.dp)
            ) {
                // SERVICE (editable)
                FigmaCard(title = "SERVICE", cardFill = cardFill, cardBorder = cardBorder) {
                    NeoTextField(
                        value = title,
                        onValueChange = { title = it },
                        placeholder = "e.g., Steam, Google, Uni Portal…"
                    )
                }

                Spacer(Modifier.height(14.dp))

                // USERNAME (editable)
                FigmaCard(title = "USERNAME", cardFill = cardFill, cardBorder = cardBorder) {
                    NeoTextField(
                        value = username,
                        onValueChange = { username = it },
                        placeholder = "Optional"
                    )
                }

                Spacer(Modifier.height(14.dp))

                // PASSWORD (editable + strength)
                FigmaCard(
                    title = "PASSWORD",
                    trailing = {
                        IconButton(onClick = { revealPassword = !revealPassword }) {
                            Icon(
                                imageVector = if (revealPassword) Icons.Outlined.VisibilityOff else Icons.Outlined.Visibility,
                                contentDescription = "Toggle password",
                                tint = Color.White.copy(alpha = 0.55f)
                            )
                        }
                    },
                    cardFill = cardFill,
                    cardBorder = cardBorder
                ) {
                    NeoTextField(
                        value = password,
                        onValueChange = { password = it },
                        placeholder = "Enter password…",
                        isPassword = !revealPassword
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

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(10.dp)
                            .clip(RoundedCornerShape(999.dp))
                            .background(Color.White.copy(alpha = 0.08f))
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth(progress)
                                .fillMaxHeight()
                                .clip(RoundedCornerShape(999.dp))
                                .background(strength.color)
                        )
                    }
                }

                Spacer(Modifier.height(14.dp))

                // CATEGORY (editable)
                FigmaCard(title = "CATEGORY", cardFill = cardFill, cardBorder = cardBorder) {
                    CategoryPickerFigma(
                        selected = category,
                        neoMint = neoMint,
                        onSelect = { category = it }
                    )
                }

                Spacer(Modifier.height(14.dp))

                // NOTES (editable)
                FigmaCard(title = "NOTES", cardFill = cardFill, cardBorder = cardBorder) {
                    NeoTextField(
                        value = note,
                        onValueChange = { note = it },
                        placeholder = "Optional notes…",
                        minLines = 4
                    )
                }

                if (errorMessage != null) {
                    Spacer(Modifier.height(12.dp))
                    Text(
                        text = errorMessage!!,
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodyMedium
                    )
                }

                Spacer(Modifier.height(10.dp))
            }
        }

        // Bottom bar
        EditBottomActionBar(
            neoMint = neoMint,
            cardFill = cardFill,
            cardBorder = cardBorder,
            enabled = canSave,
            saving = saving,
            onSave = { saveNow() },
            onCancel = {
                viewModel.clearSelectedSecret()
                onNavigateBack()
            },
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .navigationBarsPadding()
                .padding(horizontal = 18.dp, vertical = 14.dp)
        )
    }
}

// ---------------- helpers (same style as your Detail screen) ----------------

@Composable
private fun EditBottomActionBar(
    neoMint: Color,
    cardFill: Color,
    cardBorder: Color,
    enabled: Boolean,
    saving: Boolean,
    onSave: () -> Unit,
    onCancel: () -> Unit,
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
            OutlinedButton(
                onClick = onCancel,
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(16.dp),
                border = BorderStroke(1.dp, Color.White.copy(alpha = 0.18f)),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.White.copy(alpha = 0.85f))
            ) {
                Text("Cancel", fontWeight = FontWeight.SemiBold)
            }

            Button(
                onClick = onSave,
                enabled = enabled,
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = neoMint,
                    contentColor = Color.Black,
                    disabledContainerColor = neoMint.copy(alpha = 0.35f),
                    disabledContentColor = Color.Black.copy(alpha = 0.55f)
                )
            ) {
                Icon(
                    imageVector = Icons.Outlined.Check,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(Modifier.width(8.dp))
                Text(if (saving) "Saving…" else "Save", fontWeight = FontWeight.SemiBold)
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
private fun NeoTextField(
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    isPassword: Boolean = false,
    minLines: Int = 1
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        modifier = Modifier.fillMaxWidth(),
        singleLine = minLines == 1,
        minLines = minLines,
        placeholder = { Text(placeholder, color = Color.White.copy(alpha = 0.35f)) },
        colors = OutlinedTextFieldDefaults.colors(
            focusedTextColor = Color.White,
            unfocusedTextColor = Color.White.copy(alpha = 0.90f),
            cursorColor = Color(0xFF25E6C8),
            focusedContainerColor = Color.White.copy(alpha = 0.06f),
            unfocusedContainerColor = Color.White.copy(alpha = 0.06f),
            focusedBorderColor = Color.White.copy(alpha = 0.16f),
            unfocusedBorderColor = Color.White.copy(alpha = 0.10f)
        ),
        visualTransformation = if (isPassword) PasswordVisualTransformation() else VisualTransformation.None,
        shape = RoundedCornerShape(18.dp)
    )
}

@Composable
private fun CategoryPickerFigma(
    selected: SecretType,
    neoMint: Color,
    onSelect: (SecretType) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        CategoryChoiceRow("Work", selected == SecretType.WORK, neoMint) { onSelect(SecretType.WORK) }
        CategoryChoiceRow("Education", selected == SecretType.EDUCATION, neoMint) { onSelect(SecretType.EDUCATION) }
        CategoryChoiceRow("WiFi", selected == SecretType.WIFI, neoMint) { onSelect(SecretType.WIFI) }
        CategoryChoiceRow("Private", selected == SecretType.PRIVATE, neoMint) { onSelect(SecretType.PRIVATE) }
        CategoryChoiceRow("Other", selected == SecretType.ELSE, neoMint) { onSelect(SecretType.ELSE) }
    }
}

@Composable
private fun CategoryChoiceRow(
    label: String,
    selected: Boolean,
    neoMint: Color,
    onClick: () -> Unit
) {
    val shape = RoundedCornerShape(16.dp)
    val bg = if (selected) neoMint.copy(alpha = 0.12f) else Color.White.copy(alpha = 0.04f)
    val border = if (selected) neoMint.copy(alpha = 0.45f) else Color.White.copy(alpha = 0.10f)
    val fg = if (selected) neoMint else Color.White.copy(alpha = 0.85f)

    Surface(
        onClick = onClick,
        shape = shape,
        color = bg,
        border = BorderStroke(1.dp, border),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(label, color = fg, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Medium)
        }
    }
}