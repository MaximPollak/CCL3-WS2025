package dev.maximpollak.neokey.ui.main

import androidx.activity.ComponentActivity
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import dev.maximpollak.neokey.security.BiometricAuth

@Composable
fun MainScreen(
    onUnlocked: () -> Unit,
    onUsePin: () -> Unit // you can route to a PIN screen later
) {
    val context = LocalContext.current
    val activity = context as? ComponentActivity

    var errorMessage by remember { mutableStateOf<String?>(null) }

    Surface(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(Modifier.height(80.dp))

            // 🔒 Icon placeholder (replace with your own vector/image)
            Text("🔒", style = MaterialTheme.typography.displaySmall)

            Spacer(Modifier.height(24.dp))

            Text(
                text = "NEOKey",
                style = MaterialTheme.typography.displayLarge
            )

            Spacer(Modifier.height(12.dp))

            Text(
                text = "Secure • Private • Offline",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(Modifier.height(56.dp))

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(24.dp),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.35f)),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.25f))
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // fingerprint icon placeholder
                    Text("🫆", style = MaterialTheme.typography.displaySmall)

                    Spacer(Modifier.height(12.dp))

                    Button(
                        onClick = {
                            errorMessage = null

                            if (activity == null) {
                                errorMessage = "Biometric not available (no Activity)."
                                return@Button
                            }

                            if (!BiometricAuth.canAuthenticate(activity)) {
                                errorMessage = "Biometric authentication is not set up on this device."
                                return@Button
                            }

                            BiometricAuth.authenticate(
                                activity = activity,
                                onSuccess = onUnlocked,
                                onError = { msg -> errorMessage = msg },
                                onFailure = { /* optional: show a tiny hint */ }
                            )
                        },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Text("Unlock with Biometric")
                    }

                    Spacer(Modifier.height(10.dp))

                    Text(
                        text = "Your data never leaves this device",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center
                    )

                    TextButton(onClick = onUsePin) {
                        Text("Use PIN / Password")
                    }

                    if (errorMessage != null) {
                        Spacer(Modifier.height(8.dp))
                        Text(
                            text = errorMessage!!,
                            color = MaterialTheme.colorScheme.error,
                            style = MaterialTheme.typography.bodySmall,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }

            Spacer(Modifier.weight(1f))
            Spacer(Modifier.height(24.dp))
        }
    }
}