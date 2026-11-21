package com.example.service

import java.util.concurrent.atomic.AtomicBoolean

/**
 * Shared process-wide foreground/active-chat tracker used by both MainActivity/CordlyHomeScreen
 * and NeovBackgroundMessagingService to avoid duplicate alerts and deliver notifications
 * reliably whenever the app is in background or closed.
 */
object AppForegroundState {
    private val foregroundFlag = AtomicBoolean(false)

    @Volatile
    var activeConversationId: String? = null

    @Volatile
    var activeServerId: String? = null

    @Volatile
    var activeChannelName: String? = null

    val isForeground: Boolean
        get() = foregroundFlag.get()

    fun setForeground(foreground: Boolean) {
        foregroundFlag.set(foreground)
        if (!foreground) {
            // Clear active chat focus when UI leaves foreground so incoming messages still notify.
            activeConversationId = null
            activeServerId = null
            activeChannelName = null
        }
    }

    fun updateActiveChat(conversationId: String?, serverId: String?, channelName: String?) {
        activeConversationId = conversationId
        activeServerId = serverId
        activeChannelName = channelName
    }
}
