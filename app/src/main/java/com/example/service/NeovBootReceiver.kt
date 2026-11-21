package com.example.service

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

/**
 * Restarts background message delivery after device reboot or app update if the user is signed in.
 */
class NeovBootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent?) {
        val action = intent?.action ?: return
        if (action == Intent.ACTION_BOOT_COMPLETED || action == Intent.ACTION_MY_PACKAGE_REPLACED) {
            NeovBackgroundMessagingService.syncServiceState(context.applicationContext)
        }
    }
}
