package com.example.service

import android.app.AlarmManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.pm.ServiceInfo
import android.os.Build
import android.os.IBinder
import android.os.SystemClock
import android.util.Log
import androidx.core.app.ServiceCompat
import androidx.core.content.ContextCompat
import com.example.R
import com.example.data.model.CallSession
import com.example.data.model.Conversation
import com.example.data.model.Server
import com.example.data.model.ServerMessage
import com.example.ui.cordly.AppAppearanceStore
import com.example.ui.cordly.CordlyNotificationHelper
import com.example.ui.cordly.SoundEffectsManager
import com.google.firebase.Firebase
import com.google.firebase.FirebaseApp
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.auth
import com.google.firebase.firestore.DocumentChange
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration

/**
 * Foreground dataSync service that maintains live Firestore listeners for Direct Messages,
 * Server channels, and Incoming Calls even when the app is backgrounded or swiped closed.
 */
class NeovBackgroundMessagingService : Service() {

    private var authListener: FirebaseAuth.AuthStateListener? = null
    private var activeUid: String? = null
    private var conversationsReg: ListenerRegistration? = null
    private var serversReg: ListenerRegistration? = null
    private var callsReg: ListenerRegistration? = null
    private val serverMessageRegs = mutableMapOf<String, ListenerRegistration>()
    private val serverNames = mutableMapOf<String, String>()
    private val notifiedCallIds = HashSet<String>()
    private val dmSeededInMemory = HashSet<String>()
    private val serverSeededInMemory = HashSet<String>()

    private val syncPrefs by lazy {
        getSharedPreferences(SYNC_PREFS_NAME, Context.MODE_PRIVATE)
    }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onCreate() {
        super.onCreate()
        SoundEffectsManager.init(applicationContext)
        startForegroundCompat()
        ensureFirebaseAndAttach()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        startForegroundCompat()
        ensureFirebaseAndAttach()
        return START_STICKY
    }

    private fun startForegroundCompat() {
        val notification = CordlyNotificationHelper.buildBackgroundSyncNotification(this)
        val type = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            ServiceInfo.FOREGROUND_SERVICE_TYPE_DATA_SYNC
        } else {
            0
        }
        try {
            ServiceCompat.startForeground(this, BG_SYNC_NOTIF_ID, notification, type)
        } catch (t: Throwable) {
            Log.w(TAG, "Unable to enter foreground sync state cleanly", t)
            stopSelf()
        }
    }

    private fun ensureFirebaseAndAttach() {
        try {
            if (FirebaseApp.getApps(this).isEmpty()) {
                FirebaseApp.initializeApp(this)
            }
        } catch (t: Throwable) {
            Log.w(TAG, "Firebase init skipped in service", t)
        }

        val auth = try {
            Firebase.auth
        } catch (_: Throwable) {
            return
        }

        if (authListener == null) {
            val listener = FirebaseAuth.AuthStateListener { firebaseAuth ->
                val user = firebaseAuth.currentUser
                if (user == null || !AppAppearanceStore.loadBackgroundSyncEnabled(this)) {
                    clearFirestoreListeners()
                    activeUid = null
                    stopSelf()
                } else if (user.uid != activeUid) {
                    attachListenersForUser(user.uid)
                }
            }
            authListener = listener
            auth.addAuthStateListener(listener)
        }

        val user = auth.currentUser
        if (user == null || !AppAppearanceStore.loadBackgroundSyncEnabled(this)) {
            clearFirestoreListeners()
            stopSelf()
            return
        }
        if (user.uid != activeUid) {
            attachListenersForUser(user.uid)
        }
    }

    private fun attachListenersForUser(uid: String) {
        clearFirestoreListeners()
        activeUid = uid
        dmSeededInMemory.clear()
        serverSeededInMemory.clear()
        notifiedCallIds.clear()

        val dbId = getString(R.string.firestore_database_id)
        val db = FirebaseFirestore.getInstance(dbId)

        // 1. Direct Message Conversations Listener (works while app is backgrounded or closed)
        conversationsReg = db.collection("conversations")
            .whereArrayContains("participantIds", uid)
            .addSnapshotListener { snap, error ->
                if (error != null || snap == null) return@addSnapshotListener
                val firstRun = !dmSeededInMemory.contains(uid)
                if (firstRun) {
                    dmSeededInMemory.add(uid)
                }
                for (change in snap.documentChanges) {
                    val conv = change.document.toObject(
                        Conversation::class.java,
                        DocumentSnapshot.ServerTimestampBehavior.ESTIMATE
                    ) ?: continue
                    val convId = change.document.id
                    val lastText = conv.lastMessageText.orEmpty()
                    val senderId = conv.lastMessageSenderId.orEmpty()
                    val updatedMs = timestampToMs(conv.updatedAt ?: conv.createdAt)
                    val sigKey = "dm_sig_$convId"
                    val timeKey = "dm_ms_$convId"
                    val signature = "$updatedMs|$senderId|$lastText"
                    val prevSig = syncPrefs.getString(sigKey, null)
                    val prevMs = syncPrefs.getLong(timeKey, 0L)

                    val isNewerTimestamp = updatedMs > prevMs
                    val isChanged = signature != prevSig
                    if (prevSig == null && firstRun) {
                        // First time seeing this conversation on device: baseline without notification flood
                        syncPrefs.edit()
                            .putString(sigKey, signature)
                            .putLong(timeKey, updatedMs)
                            .apply()
                        continue
                    }

                    if ((isChanged || isNewerTimestamp) && lastText.isNotBlank()) {
                        syncPrefs.edit()
                            .putString(sigKey, signature)
                            .putLong(timeKey, maxOf(prevMs, updatedMs))
                            .apply()

                        val shouldNotify = senderId.isNotBlank() &&
                            senderId != uid &&
                            (!AppForegroundState.isForeground || AppForegroundState.activeConversationId != convId)

                        if (shouldNotify) {
                            val senderName = if (conv.participantAId == uid) {
                                conv.participantBName
                            } else {
                                conv.participantAName
                            }.ifBlank { "Friend" }
                            CordlyNotificationHelper.notifyIncomingMessage(
                                context = applicationContext,
                                senderName = senderName,
                                messagePreview = lastText
                            )
                        }
                    }
                }
            }

        // 2. Joined Servers & Per-Server Message Listeners
        serversReg = db.collection("servers")
            .whereArrayContains("memberUids", uid)
            .addSnapshotListener { snap, error ->
                if (error != null || snap == null) return@addSnapshotListener
                val activeServerIds = HashSet<String>()
                for (doc in snap.documents) {
                    val server = doc.toObject(
                        Server::class.java,
                        DocumentSnapshot.ServerTimestampBehavior.ESTIMATE
                    ) ?: continue
                    val serverId = doc.id
                    activeServerIds.add(serverId)
                    serverNames[serverId] = server.name.ifBlank { "Server" }
                    if (!serverMessageRegs.containsKey(serverId)) {
                        attachServerMessagesListener(db, uid, serverId)
                    }
                }
                val staleIds = serverMessageRegs.keys.filter { !activeServerIds.contains(it) }
                staleIds.forEach { staleId ->
                    serverMessageRegs.remove(staleId)?.remove()
                    serverNames.remove(staleId)
                    serverSeededInMemory.remove(staleId)
                }
            }

        // 3. Incoming Ringing Calls Listener
        callsReg = db.collection("calls")
            .whereArrayContains("participantIds", uid)
            .addSnapshotListener { snap, error ->
                if (error != null || snap == null) return@addSnapshotListener
                for (doc in snap.documents) {
                    val call = doc.toObject(
                        CallSession::class.java,
                        DocumentSnapshot.ServerTimestampBehavior.ESTIMATE
                    ) ?: continue
                    val callId = doc.id
                    if (call.status == "ringing" && call.callerId != uid) {
                        if (notifiedCallIds.add(callId) && !AppForegroundState.isForeground) {
                            CordlyNotificationHelper.notifyIncomingCall(
                                context = applicationContext,
                                callerName = call.callerName,
                                isVideo = call.callType == "video"
                            )
                        }
                    } else if (call.status != "ringing") {
                        notifiedCallIds.remove(callId)
                    }
                }
            }
    }

    private fun attachServerMessagesListener(
        db: FirebaseFirestore,
        uid: String,
        serverId: String
    ) {
        val reg = db.collection("servers").document(serverId).collection("messages")
            .whereArrayContains("memberUids", uid)
            .addSnapshotListener { snap, error ->
                if (error != null || snap == null) return@addSnapshotListener
                val firstSnapshot = !serverSeededInMemory.contains(serverId)
                val lastSeenKey = "srv_last_ms_$serverId"
                var storedLastMs = syncPrefs.getLong(lastSeenKey, 0L)

                if (firstSnapshot) {
                    serverSeededInMemory.add(serverId)
                    if (storedLastMs == 0L) {
                        var maxExistingMs = System.currentTimeMillis()
                        for (doc in snap.documents) {
                            val ts = doc.getTimestamp("createdAt")
                            val ms = timestampToMs(ts)
                            if (ms > maxExistingMs) maxExistingMs = ms
                        }
                        syncPrefs.edit().putLong(lastSeenKey, maxExistingMs).apply()
                        return@addSnapshotListener
                    }
                }

                var newestSeenMs = storedLastMs
                for (change in snap.documentChanges) {
                    if (change.type != DocumentChange.Type.ADDED) continue
                    val msg = change.document.toObject(
                        ServerMessage::class.java,
                        DocumentSnapshot.ServerTimestampBehavior.ESTIMATE
                    ) ?: continue
                    val createdMs = timestampToMs(msg.createdAt)
                    if (createdMs <= storedLastMs) continue
                    if (createdMs > newestSeenMs) newestSeenMs = createdMs

                    if (msg.senderId == uid || msg.deletedForEveryone == true || msg.messageType == "deleted") {
                        continue
                    }

                    val viewingSameChannel = AppForegroundState.isForeground &&
                        AppForegroundState.activeServerId == serverId &&
                        AppForegroundState.activeChannelName == msg.channelName

                    if (!viewingSameChannel) {
                        val preview = when (msg.messageType) {
                            "image" -> if (msg.text.isNotBlank()) "📷 ${msg.text}" else "📷 Photo"
                            "video" -> if (msg.text.isNotBlank()) "🎬 ${msg.text}" else "🎬 Video"
                            "audio" -> "🎤 Voice message"
                            "sticker" -> "✨ Sticker"
                            "gif" -> "🎞️ GIF"
                            else -> msg.text.ifBlank { "New message" }
                        }
                        CordlyNotificationHelper.notifyIncomingServerMessage(
                            context = applicationContext,
                            serverName = serverNames[serverId] ?: "Server",
                            channelName = msg.channelName,
                            senderName = msg.senderName,
                            messagePreview = preview
                        )
                    }
                }
                if (newestSeenMs > storedLastMs) {
                    syncPrefs.edit().putLong(lastSeenKey, newestSeenMs).apply()
                }
            }
        serverMessageRegs[serverId] = reg
    }

    private fun timestampToMs(ts: Timestamp?): Long {
        if (ts == null) return 0L
        return ts.seconds * 1000L + (ts.nanoseconds / 1_000_000L)
    }

    private fun clearFirestoreListeners() {
        conversationsReg?.remove()
        conversationsReg = null
        serversReg?.remove()
        serversReg = null
        callsReg?.remove()
        callsReg = null
        serverMessageRegs.values.forEach { it.remove() }
        serverMessageRegs.clear()
        serverNames.clear()
    }

    override fun onTaskRemoved(rootIntent: Intent?) {
        super.onTaskRemoved(rootIntent)
        val hasUser = try {
            Firebase.auth.currentUser != null
        } catch (_: Throwable) {
            false
        }
        if (hasUser && AppAppearanceStore.loadBackgroundSyncEnabled(applicationContext)) {
            scheduleServiceRestart(applicationContext)
        }
    }

    override fun onDestroy() {
        clearFirestoreListeners()
        authListener?.let { listener ->
            try {
                Firebase.auth.removeAuthStateListener(listener)
            } catch (_: Throwable) {
            }
        }
        authListener = null
        super.onDestroy()
    }

    companion object {
        private const val TAG = "NeovBgSyncService"
        private const val BG_SYNC_NOTIF_ID = 77012
        private const val SYNC_PREFS_NAME = "neov_bg_sync_prefs"

        fun syncServiceState(context: Context) {
            val appCtx = context.applicationContext
            val enabled = AppAppearanceStore.loadBackgroundSyncEnabled(appCtx)
            val signedIn = try {
                Firebase.auth.currentUser != null
            } catch (_: Throwable) {
                false
            }
            val serviceIntent = Intent(appCtx, NeovBackgroundMessagingService::class.java)
            if (signedIn && enabled) {
                try {
                    ContextCompat.startForegroundService(appCtx, serviceIntent)
                } catch (t: Throwable) {
                    Log.w(TAG, "Failed starting background messaging service", t)
                }
            } else {
                try {
                    appCtx.stopService(serviceIntent)
                } catch (_: Throwable) {
                }
            }
        }

        private fun scheduleServiceRestart(context: Context) {
            val restartIntent = Intent(context, NeovBackgroundMessagingService::class.java)
            val flags = PendingIntent.FLAG_ONE_SHOT or PendingIntent.FLAG_IMMUTABLE
            val pendingIntent = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                PendingIntent.getForegroundService(context, 77013, restartIntent, flags)
            } else {
                PendingIntent.getService(context, 77013, restartIntent, flags)
            }
            val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as? AlarmManager ?: return
            alarmManager.set(
                AlarmManager.ELAPSED_REALTIME_WAKEUP,
                SystemClock.elapsedRealtime() + 1500L,
                pendingIntent
            )
        }
    }
}
