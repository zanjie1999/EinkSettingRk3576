package com.zyyme.einkrk

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

class BootCompletedReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent?) {
        if (intent?.action == Intent.ACTION_BOOT_COMPLETED) {
            // Apply silently in the background; no activity or UI is launched.
            runCatching { SettingsRepository.apply(context) }
        }
    }
}
