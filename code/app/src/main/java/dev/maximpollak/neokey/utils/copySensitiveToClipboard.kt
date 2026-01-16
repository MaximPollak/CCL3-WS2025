package dev.maximpollak.neokey.utils

import android.content.ClipData
import android.content.ClipDescription
import android.content.ClipboardManager
import android.content.Context
import android.os.Build
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.workDataOf
import java.util.concurrent.TimeUnit

private const val CLEAR_CLIPBOARD_WORK = "clear_sensitive_clipboard"

/**
 * Copies a sensitive value to the clipboard, marks it as sensitive (Android 13+),
 * and clears it automatically after [clearAfterMs] if the clipboard still contains the same value.
 */
fun copySensitiveToClipboard(
    context: Context,
    label: String,
    value: String,
    clearAfterMs: Long = 30_000L
) {
    val appContext = context.applicationContext

    val clipboard = appContext.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
    val clip = ClipData.newPlainText(label, value)

    // Android 13+ → mark as sensitive so the system hides previews
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        val extras = android.os.PersistableBundle().apply {
            putBoolean(ClipDescription.EXTRA_IS_SENSITIVE, true)
        }
        clip.description.extras = extras
    }

    clipboard.setPrimaryClip(clip)

    val clearWork = OneTimeWorkRequestBuilder<ClearClipboardWorker>()
        .setInitialDelay(clearAfterMs, TimeUnit.MILLISECONDS)
        .setInputData(workDataOf(ClearClipboardWorker.KEY_VALUE to value))
        .build()

    // Replace any previous scheduled clear
    WorkManager.getInstance(appContext).enqueueUniqueWork(
        CLEAR_CLIPBOARD_WORK,
        ExistingWorkPolicy.REPLACE,
        clearWork
    )
}