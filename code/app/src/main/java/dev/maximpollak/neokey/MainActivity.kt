// File: MainActivity.kt
package dev.maximpollak.neokey

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.ProcessLifecycleOwner
import dev.maximpollak.neokey.security.AppLockObserver
import dev.maximpollak.neokey.security.SessionManager
import dev.maximpollak.neokey.ui.main.MainScreen
import dev.maximpollak.neokey.ui.navigation.NavGraph
import dev.maximpollak.neokey.ui.theme.NEOKeyTheme

class MainActivity : FragmentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // ✅ lock whenever the app goes to background
        ProcessLifecycleOwner.get().lifecycle.addObserver(AppLockObserver())

        setContent {
            NEOKeyTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    val unlocked by SessionManager.unlocked.collectAsState()

                    if (unlocked) {
                        // ✅ unlocked area (categories/secrets/detail/add/edit)
                        NavGraph()
                    } else {
                        // ✅ gate screen
                        MainScreen(
                            onUnlocked = { SessionManager.unlock() }
                        )
                    }
                }
            }
        }
    }
}
