package com.example.ui.cordly

import android.Manifest
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import com.example.MainActivity
import com.example.R

object CordlyNotificationHelper {
    private const val CHANNEL_MESSAGES = "neov_messages_channel"
    private const val CHANNEL_CALLS = "neov_calls_channel"
    private const val CHANNEL_BG_SYNC = "neov_bg_sync_channel"

    private val dedupeLock = Any()
    private val recentNotifications = HashMap<String, Long>()

    private fun shouldEmitNotification(key: String, windowMs: Long = 4_000L): Boolean {
        val now = System.currentTimeMillis()
        synchronized(dedupeLock) {
            val prev = recentNotifications[key]
            if (prev != null && now - prev < windowMs) {
                return false
            }
            recentNotifications[key] = now
            if (recentNotifications.size > 40) {
                recentNotifications.entries.removeAll { now - it.value > 15_000L }
            }
            return true
        }
    }

    fun ensureChannels(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as? NotificationManager ?: return
            val msgChannel = NotificationChannel(
                CHANNEL_MESSAGES,
                "neov chat Messages",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Alerts when friends send messages"
            }
            val callChannel = NotificationChannel(
                CHANNEL_CALLS,
                "neov chat Calls",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Incoming voice and video calls"
            }
            val bgChannel = NotificationChannel(
                CHANNEL_BG_SYNC,
                "neov chat Background Delivery",
                NotificationManager.IMPORTANCE_MIN
            ).apply {
                description = "Keeps message and call delivery active when app is closed"
                setShowBadge(false)
                enableLights(false)
                enableVibration(false)
                setSound(null, null)
            }
            manager.createNotificationChannel(msgChannel)
            manager.createNotificationChannel(callChannel)
            manager.createNotificationChannel(bgChannel)
        }
    }

    fun hasNotificationPermission(context: Context): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED
        } else {
            true
        }
    }

    fun buildBackgroundSyncNotification(context: Context): Notification {
        ensureChannels(context)
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
        }
        val pendingIntent = PendingIntent.getActivity(
            context,
            4041,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        val isAr = AppLanguageManager.resolveEffectiveCode(
            AppAppearanceStore.loadLanguageCode(context)
        ).startsWith("ar")
        val text = if (isAr) {
            "متصل لاستلام الرسائل والمكالمات في الخلفية"
        } else {
            "Connected for background messages & calls"
        }

        return NotificationCompat.Builder(context, CHANNEL_BG_SYNC)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentTitle("neov chat")
            .setContentText(text)
            .setPriority(NotificationCompat.PRIORITY_MIN)
            .setCategory(NotificationCompat.CATEGORY_SERVICE)
            .setSilent(true)
            .setOngoing(true)
            .setShowWhen(false)
            .setContentIntent(pendingIntent)
            .build()
    }

    fun notifyIncomingMessage(
        context: Context,
        senderName: String,
        messagePreview: String
    ) {
        if (!hasNotificationPermission(context)) return
        if (!shouldEmitNotification("dm|$senderName|$messagePreview")) return
        ensureChannels(context)

        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
        }
        val pendingIntent = PendingIntent.getActivity(
            context,
            senderName.hashCode(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val bodyText = "لقد أرسل لك $senderName: $messagePreview"
        val notification = NotificationCompat.Builder(context, CHANNEL_MESSAGES)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentTitle(senderName)
            .setContentText(bodyText)
            .setStyle(NotificationCompat.BigTextStyle().bigText(bodyText))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .build()

        try {
            NotificationManagerCompat.from(context).notify(
                (System.currentTimeMillis() and 0xFFFFFF).toInt(),
                notification
            )
        } catch (_: SecurityException) {
        }
    }

    fun notifyIncomingServerMessage(
        context: Context,
        serverName: String,
        channelName: String,
        senderName: String,
        messagePreview: String
    ) {
        if (!hasNotificationPermission(context)) return
        if (!shouldEmitNotification("srv|$serverName|$channelName|$senderName|$messagePreview")) return
        ensureChannels(context)

        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
        }
        val pendingIntent = PendingIntent.getActivity(
            context,
            (serverName + channelName).hashCode(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val title = "$serverName • #$channelName"
        val bodyText = "$senderName: $messagePreview"
        val notification = NotificationCompat.Builder(context, CHANNEL_MESSAGES)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentTitle(title)
            .setContentText(bodyText)
            .setStyle(NotificationCompat.BigTextStyle().bigText(bodyText))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .build()

        try {
            NotificationManagerCompat.from(context).notify(
                ((System.currentTimeMillis() + 77) and 0xFFFFFF).toInt(),
                notification
            )
        } catch (_: SecurityException) {
        }
    }

    fun notifyIncomingCall(
        context: Context,
        callerName: String,
        isVideo: Boolean
    ) {
        if (!hasNotificationPermission(context)) return
        if (!shouldEmitNotification("call|$callerName|$isVideo", windowMs = 6_000L)) return
        ensureChannels(context)

        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
        }
        val pendingIntent = PendingIntent.getActivity(
            context,
            callerName.hashCode() + 1000,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val callLabel = if (isVideo) "مكالمة فيديو واردة من $callerName" else "مكالمة صوتية واردة من $callerName"
        val notification = NotificationCompat.Builder(context, CHANNEL_CALLS)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentTitle("neov chat")
            .setContentText(callLabel)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .build()

        try {
            NotificationManagerCompat.from(context).notify(
                99881,
                notification
            )
        } catch (_: SecurityException) {
        }
    }
}
