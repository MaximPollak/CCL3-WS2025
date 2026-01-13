package dev.maximpollak.neokey.security

import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner

class AppLockObserver : DefaultLifecycleObserver {

    override fun onStop(owner: LifecycleOwner) {
        // app goes to background → lock
        SessionManager.lock()
    }
}
