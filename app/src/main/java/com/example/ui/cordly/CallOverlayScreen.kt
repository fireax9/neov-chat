package com.example.ui.cordly

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.AutoAwesome
import androidx.compose.material.icons.rounded.Call
import androidx.compose.material.icons.rounded.CallEnd
import androidx.compose.material.icons.rounded.Cameraswitch
import androidx.compose.material.icons.rounded.FiberManualRecord
import androidx.compose.material.icons.rounded.GraphicEq
import androidx.compose.material.icons.rounded.HighQuality
import androidx.compose.material.icons.rounded.Mic
import androidx.compose.material.icons.rounded.MicOff
import androidx.compose.material.icons.rounded.PersonAdd
import androidx.compose.material.icons.rounded.RecordVoiceOver
import androidx.compose.material.icons.rounded.Stop
import androidx.compose.material.icons.rounded.Videocam
import androidx.compose.material.icons.rounded.VideocamOff
import androidx.compose.material.icons.rounded.VolumeOff
import androidx.compose.material.icons.rounded.VolumeUp
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableDoubleStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.R
import com.example.data.model.CallSession
import com.example.data.model.CallSignal
import com.example.data.model.CallStreamFrame
import com.example.data.repository.CordlyRepository
import com.example.ui.theme.CordlyAmber
import com.example.ui.theme.CordlyBlurple
import com.example.ui.theme.CordlyCardSurface
import com.example.ui.theme.CordlyCrimson
import com.example.ui.theme.CordlyCyan
import com.example.ui.theme.CordlyDivider
import com.example.ui.theme.CordlyElevatedSurface
import com.example.ui.theme.CordlyEmerald
import com.example.ui.theme.CordlySurfaceDark
import com.example.ui.theme.CordlyTextPrimary
import com.example.ui.theme.CordlyTextSecondary
import android.graphics.Bitmap
import android.os.Handler
import android.os.Looper
import androidx.camera.view.PreviewView
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.lifecycle.Observer
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import org.webrtc.VideoFrame
import org.webrtc.VideoSink
import org.webrtc.VideoTrack
import java.util.concurrent.Executors
import java.util.concurrent.atomic.AtomicBoolean

data class InviteableFriend(
    val uid: String,
    val name: String,
    val photoUrl: String?
)

@Composable
fun IncomingCallBanner(
    call: CallSession,
    onAnswerVoice: () -> Unit,
    onAnswerVideo: () -> Unit,
    onDecline: () -> Unit,
    modifier: Modifier = Modifier
) {
    val strings = LocalAppStrings.current
    val pulse by rememberInfiniteTransition(label = "ringPulse").animateFloat(
        initialValue = 0.45f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(650), RepeatMode.Reverse),
        label = "ringAlpha"
    )

    DisposableEffect(call.id) {
        SoundEffectsManager.startRingingLoop(incoming = true)
        onDispose { SoundEffectsManager.stopRingingLoop() }
    }

    Surface(
        color = CordlySurfaceDark,
        shape = RoundedCornerShape(22.dp),
        border = BorderStroke(1.5.dp, CordlyEmerald.copy(alpha = pulse)),
        tonalElevation = 8.dp,
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 14.dp, vertical = 10.dp)
            .testTag("incoming_call_banner")
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    Brush.horizontalGradient(
                        listOf(CordlyEmerald.copy(alpha = 0.16f), CordlyBlurple.copy(alpha = 0.14f))
                    )
                )
                .padding(14.dp)
        ) {
            UserAvatar(
                displayName = call.callerName,
                photoUrl = call.callerPhoto,
                status = "online",
                size = 48.dp
            )
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = call.callerName,
                    style = MaterialTheme.typography.titleMedium,
                    color = CordlyTextPrimary,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = if (call.callType == "video") strings.incomingVideoCall else strings.incomingVoiceCall,
                    style = MaterialTheme.typography.labelMedium,
                    color = CordlyCyan
                )
            }
            IconButton(
                onClick = {
                    SoundEffectsManager.stopRingingLoop()
                    SoundEffectsManager.play(AppSoundEffect.CALL_END)
                    onDecline()
                },
                modifier = Modifier
                    .size(44.dp)
                    .clip(CircleShape)
                    .background(CordlyCrimson)
                    .testTag("decline_call_button")
            ) {
                Icon(Icons.Rounded.CallEnd, contentDescription = strings.declineCall, tint = Color.White)
            }
            Spacer(modifier = Modifier.width(8.dp))
            IconButton(
                onClick = {
                    SoundEffectsManager.stopRingingLoop()
                    SoundEffectsManager.play(AppSoundEffect.CALL_CONNECT)
                    if (call.callType == "video") onAnswerVideo() else onAnswerVoice()
                },
                modifier = Modifier
                    .size(44.dp)
                    .clip(CircleShape)
                    .background(CordlyEmerald)
                    .testTag("answer_call_button")
            ) {
                Icon(
                    imageVector = if (call.callType == "video") Icons.Rounded.Videocam else Icons.Rounded.Call,
                    contentDescription = strings.acceptCall,
                    tint = Color.White
                )
            }
        }
    }
}

@Composable
fun ActiveCallOverlayDialog(
    call: CallSession,
    currentUserId: String,
    currentUserRank: String,
    streams: List<CallStreamFrame>,
    incomingSignals: List<CallSignal> = emptyList(),
    inviteableFriends: List<InviteableFriend> = emptyList(),
    minimized: Boolean = false,
    onSendSignal: (
        recipientId: String,
        type: String,
        sdp: String?,
        candidate: String?,
        sdpMid: String?,
        sdpMLineIndex: Int?
    ) -> Unit = { _, _, _, _, _, _ -> },
    onConsumeSignal: (signalId: String) -> Unit = {},
    onToggleMute: (Boolean) -> Unit,
    onToggleVideo: (Boolean) -> Unit,
    onUpdateVideoQuality: (String) -> Unit,
    onAddParticipant: (friendUid: String, friendName: String) -> Unit,
    onPublishStreamSample: (
        frameData: String?,
        audioData: String?,
        audioSeq: Long,
        voiceEffect: String,
        faceFilter: String,
        videoQuality: String,
        audioLevel: Double,
        videoEnabled: Boolean,
        muted: Boolean
    ) -> Unit,
    onStatusToast: (String) -> Unit = {},
    onEndCall: () -> Unit,
    onMinimize: () -> Unit
) {
    val strings = LocalAppStrings.current
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

    val isUltraOrPlus = currentUserRank == CordlyRepository.RANK_ULTRA || currentUserRank == CordlyRepository.RANK_NEO_PLUS
    val isNeoPlus = currentUserRank == CordlyRepository.RANK_NEO_PLUS

    val isCaller = call.callerId == currentUserId
    var isMuted by remember(call.id) { mutableStateOf(false) }
    var isSpeakerOn by remember(call.id) { mutableStateOf(true) }
    var isVideoEnabled by remember(call.id) {
        mutableStateOf(
            call.callType == "video" ||
                (if (isCaller) call.callerVideoOn else call.calleeVideoOn)
        )
    }
    var elapsedSeconds by remember(call.id) { mutableLongStateOf(0L) }

    var selectedQuality by remember(call.id) { mutableStateOf(VideoQualityPreset.fromId(call.videoQuality)) }
    val latestOnPublishSample by rememberUpdatedState(onPublishStreamSample)
    val latestOnStatusToast by rememberUpdatedState(onStatusToast)
    val latestOnSendSignal by rememberUpdatedState(onSendSignal)
    val latestOnConsumeSignal by rememberUpdatedState(onConsumeSignal)

    // Shared global Video Quality sync: whenever ANY peer changes quality, update for everyone
    LaunchedEffect(call.videoQuality) {
        val synced = VideoQualityPreset.fromId(call.videoQuality)
        if (synced != selectedQuality) {
            selectedQuality = synced
            latestOnStatusToast("${strings.videoQualityLabel}: ${synced.label}")
        }
    }
    var selectedFaceFilter by remember(call.id) { mutableStateOf(CallFaceFilter.NONE) }
    var selectedVoiceEffect by remember(call.id) { mutableStateOf(VoiceEffectType.NORMAL) }
    var isRecordingCall by remember(call.id) { mutableStateOf(false) }
    var showQualityBar by remember(call.id) { mutableStateOf(false) }
    var showFilterBar by remember(call.id) { mutableStateOf(false) }
    var showVoiceFxBar by remember(call.id) { mutableStateOf(false) }
    var showAddFriendDialog by remember(call.id) { mutableStateOf(false) }

    var hasAudioPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(context, Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED
        )
    }
    var hasCameraPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED
        )
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { grants ->
        hasAudioPermission = grants[Manifest.permission.RECORD_AUDIO] == true ||
            ContextCompat.checkSelfPermission(context, Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED
        hasCameraPermission = grants[Manifest.permission.CAMERA] == true ||
            ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED
    }

    LaunchedEffect(Unit) {
        if (!hasAudioPermission || (isVideoEnabled && !hasCameraPermission)) {
            permissionLauncher.launch(
                arrayOf(Manifest.permission.RECORD_AUDIO, Manifest.permission.CAMERA)
            )
        }
    }

    val audioEngine = remember(call.id) { CallAudioEngine(context.applicationContext) }
    val webRtc = remember(call.id) {
        WebRtcCallManager(
            context = context.applicationContext,
            currentUserId = currentUserId,
            onSendSignal = { recipientId, type, sdp, candidate, sdpMid, sdpMLineIndex ->
                latestOnSendSignal(recipientId, type, sdp, candidate, sdpMid, sdpMLineIndex)
            },
            onConsumeSignal = { sigId ->
                latestOnConsumeSignal(sigId)
            }
        )
    }

    val localTrack by webRtc.localVideoTrack.collectAsStateWithLifecycle()
    val remoteTracks by webRtc.remoteVideoTracks.collectAsStateWithLifecycle()
    val isFrontCam by webRtc.isUsingFrontCamera.collectAsStateWithLifecycle()
    val connectedPeers by webRtc.connectedPeerCount.collectAsStateWithLifecycle()
    val cameraError by webRtc.cameraError.collectAsStateWithLifecycle()
    val usingCameraXPreview by webRtc.usingCameraXPreview.collectAsStateWithLifecycle()
    val localFrameProduced by webRtc.localFrameProduced.collectAsStateWithLifecycle()

    // Configure speakerphone routing without opening dual AudioRecord/AudioTrack (WebRTC owns hardware audio).
    DisposableEffect(call.id, isSpeakerOn) {
        val prev = CallAudioEngine.configureAudioRouting(context, isSpeakerOn)
        onDispose {
            CallAudioEngine.restoreAudioRouting(context, prev)
        }
    }
    DisposableEffect(call.id) {
        VoiceNoteHelper.stopPlayback()
        onDispose {
            SoundEffectsManager.stopRingingLoop()
            webRtc.release()
            audioEngine.release()
        }
    }

    // Start local mic & camera capture first so initial SDP offers/answers already carry active senders.
    LaunchedEffect(hasAudioPermission, hasCameraPermission, isVideoEnabled, isMuted, selectedQuality, lifecycleOwner) {
        webRtc.startOrUpdateLocalMedia(
            hasAudioPermission = hasAudioPermission,
            hasCameraPermission = hasCameraPermission,
            videoEnabled = isVideoEnabled,
            muted = isMuted,
            preset = selectedQuality,
            lifecycleOwner = lifecycleOwner
        )
    }

    LaunchedEffect(call.id, call.status, call.participantIds) {
        webRtc.ensurePeersAndConnect(call.status, call.participantIds)
    }

    LaunchedEffect(incomingSignals) {
        webRtc.handleIncomingSignals(incomingSignals)
    }

    // Ringback sound while ringing; connect chime once active
    LaunchedEffect(call.status) {
        if (call.status == "ringing" && call.callerId == currentUserId) {
            SoundEffectsManager.startRingingLoop(incoming = false)
        } else if (call.status == "active") {
            SoundEffectsManager.stopRingingLoop()
            SoundEffectsManager.play(AppSoundEffect.CALL_CONNECT)
        }
    }

    // Live call duration timer
    LaunchedEffect(call.id, call.status) {
        while (isActive) {
            if (call.status == "active") {
                elapsedSeconds += 1L
            }
            delay(1000L)
        }
    }

    val localMicAmplitude = if (!isMuted && call.status == "active" && connectedPeers > 0) 0.36 else 0.0

    // Lightweight call metadata sync (no heavy audio/video Base64 in Firestore)
    LaunchedEffect(
        call.id,
        call.status,
        selectedVoiceEffect,
        selectedFaceFilter,
        selectedQuality,
        isVideoEnabled,
        hasCameraPermission,
        isMuted,
        connectedPeers
    ) {
        latestOnPublishSample(
            null,
            null,
            0L,
            selectedVoiceEffect.id,
            selectedFaceFilter.id,
            selectedQuality.id,
            localMicAmplitude,
            isVideoEnabled && hasCameraPermission,
            isMuted
        )
    }

    val remoteStreams = streams.filter { it.senderId != currentUserId }

    val peerName = when {
        call.participantIds.size > 2 -> "${call.calleeName} (${call.participantIds.size})"
        call.targetType == "server" -> call.calleeName
        isCaller -> call.calleeName
        else -> call.callerName
    }
    val peerPhoto = if (isCaller) call.calleePhoto else call.callerPhoto

    if (!minimized) {
        Dialog(
            onDismissRequest = onMinimize,
            properties = DialogProperties(
                usePlatformDefaultWidth = false,
                decorFitsSystemWindows = false
            )
        ) {
        SanitizeDialogWindowIds()
        Surface(
            color = Color.Black,
            modifier = Modifier
                .fillMaxSize()
                .systemBarsPadding()
                .testTag("active_call_overlay")
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.verticalGradient(
                            listOf(Color(0xFF000000), Color(0xFF07070B), Color(0xFF000000))
                        )
                    )
                    .padding(14.dp),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                // Top Call Header + Quality / Multi-call / Close actions
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = peerName,
                            style = MaterialTheme.typography.titleLarge,
                            color = CordlyTextPrimary,
                            fontWeight = FontWeight.Bold,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        val durationText = String.format(
                            java.util.Locale.US,
                            "%02d:%02d",
                            elapsedSeconds / 60L,
                            elapsedSeconds % 60L
                        )
                        Text(
                            text = when {
                                call.status == "ringing" -> strings.callingStatus
                                call.targetType == "server" -> "${strings.groupVoiceCall} • $durationText • ${selectedQuality.label}"
                                else -> "${strings.connectedCallStatus} • $durationText • ${selectedQuality.label}"
                            },
                            style = MaterialTheme.typography.labelMedium,
                            color = if (call.status == "active") CordlyEmerald else CordlyAmber
                        )
                    }

                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalAlignment = Alignment.CenterVertically) {
                        if (isNeoPlus && inviteableFriends.isNotEmpty()) {
                            Surface(
                                color = Color(0xFFFFD166).copy(alpha = 0.16f),
                                shape = RoundedCornerShape(12.dp),
                                border = BorderStroke(1.dp, Color(0xFFFFD166)),
                                modifier = Modifier
                                    .clip(RoundedCornerShape(12.dp))
                                    .clickable { showAddFriendDialog = true }
                                    .testTag("call_add_participant_button")
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp)
                                ) {
                                    Icon(Icons.Rounded.PersonAdd, contentDescription = strings.addToCall, tint = Color(0xFFFFD166), modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("+Call", style = MaterialTheme.typography.labelSmall, color = Color(0xFFFFD166), fontWeight = FontWeight.Bold)
                                }
                            }
                        }

                        Surface(
                            color = CordlyCardSurface,
                            shape = RoundedCornerShape(12.dp),
                            border = BorderStroke(1.dp, CordlyDivider),
                            modifier = Modifier
                                .clip(RoundedCornerShape(12.dp))
                                .clickable(onClick = onMinimize)
                                .minimumInteractiveComponentSize()
                        ) {
                            Text(
                                text = strings.closeButton,
                                style = MaterialTheme.typography.labelMedium,
                                color = CordlyTextSecondary,
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)
                            )
                        }
                    }
                }

                // Missing runtime permission banner
                AnimatedVisibility(visible = !hasAudioPermission || (isVideoEnabled && !hasCameraPermission)) {
                    Surface(
                        color = CordlyAmber.copy(alpha = 0.14f),
                        border = BorderStroke(1.dp, CordlyAmber.copy(alpha = 0.6f)),
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 6.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(12.dp)
                        ) {
                            Text(
                                text = strings.permissionCallRequired,
                                style = MaterialTheme.typography.bodySmall,
                                color = CordlyTextPrimary,
                                modifier = Modifier.weight(1f)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Button(
                                onClick = {
                                    permissionLauncher.launch(
                                        arrayOf(Manifest.permission.RECORD_AUDIO, Manifest.permission.CAMERA)
                                    )
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = CordlyAmber, contentColor = Color.Black),
                                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp)
                            ) {
                                Text(strings.grantPermissions, style = MaterialTheme.typography.labelSmall)
                            }
                        }
                    }
                }

                // Video / Voice Stage
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
                    contentAlignment = Alignment.Center
                ) {
                    val activeVideoRemote = remoteStreams.firstOrNull { it.videoEnabled && remoteTracks.containsKey(it.senderId) }
                    val firstRemote = activeVideoRemote ?: remoteStreams.firstOrNull()
                    val remoteTrack = firstRemote?.senderId?.let { remoteTracks[it] }
                        ?: remoteTracks.values.firstOrNull()
                    val remoteCallDocVideoOn = if (call.participantIds.size <= 2) {
                        if (isCaller) call.calleeVideoOn else call.callerVideoOn
                    } else {
                        false
                    }
                    var remoteFrameRendered by remember(call.id, remoteTrack) { mutableStateOf(false) }
                    var localPreviewStreaming by remember(call.id) { mutableStateOf(false) }
                    val localFrameRendered = localPreviewStreaming || localFrameProduced
                    val activeRemoteEntries = remoteTracks.entries.filter { (peerId, _) ->
                        val stream = remoteStreams.firstOrNull { it.senderId == peerId }
                        stream == null || stream.videoEnabled || call.callType == "video"
                    }
                    val peerVideoExplicitlyOff = remoteStreams.isNotEmpty() &&
                        remoteStreams.none { it.videoEnabled } &&
                        !remoteCallDocVideoOn &&
                        call.callType != "video"
                    val showRemoteVideo = activeRemoteEntries.isNotEmpty() && (
                        remoteFrameRendered ||
                            !peerVideoExplicitlyOff
                        )
                    val showVideoLayout = (isVideoEnabled && hasCameraPermission) || showRemoteVideo
                    val isArabic = AppLanguageManager.resolveEffectiveCode(LocalSelectedLanguageCode.current).startsWith("ar")

                    if (showVideoLayout) {
                        Column(
                            verticalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.fillMaxSize()
                        ) {
                            // Remote WebRTC P2P video stage (supports 1-on-1 & multi-participant group video calls)
                            Surface(
                                color = Color.Black,
                                shape = RoundedCornerShape(22.dp),
                                border = BorderStroke(
                                    width = 1.5.dp,
                                    color = if (connectedPeers > 0) CordlyEmerald else CordlyDivider
                                ),
                                modifier = Modifier
                                    .weight(1f)
                                    .fillMaxWidth()
                            ) {
                                Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
                                    if (showRemoteVideo && activeRemoteEntries.size > 1) {
                                        LazyVerticalGrid(
                                            columns = GridCells.Fixed(2),
                                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                                            verticalArrangement = Arrangement.spacedBy(6.dp),
                                            modifier = Modifier
                                                .fillMaxSize()
                                                .padding(6.dp)
                                        ) {
                                            items(activeRemoteEntries, key = { it.key }) { (peerId, track) ->
                                                val streamMeta = remoteStreams.firstOrNull { it.senderId == peerId }
                                                Box(
                                                    modifier = Modifier
                                                        .fillMaxWidth()
                                                        .height(175.dp)
                                                        .clip(RoundedCornerShape(14.dp))
                                                        .background(Color(0xFF0A0A0F))
                                                ) {
                                                    BitmapWebRtcVideoSurface(
                                                        track = track,
                                                        mirror = false,
                                                        faceFilter = CallFaceFilter.fromId(streamMeta?.faceFilter),
                                                        onFirstFrameRendered = { remoteFrameRendered = true },
                                                        modifier = Modifier.fillMaxSize()
                                                    )
                                                    if (streamMeta != null) {
                                                        Surface(
                                                            color = Color.Black.copy(alpha = 0.65f),
                                                            shape = RoundedCornerShape(8.dp),
                                                            modifier = Modifier
                                                                .align(Alignment.BottomStart)
                                                                .padding(6.dp)
                                                        ) {
                                                            Text(
                                                                text = streamMeta.senderName,
                                                                style = MaterialTheme.typography.labelSmall,
                                                                color = Color.White,
                                                                maxLines = 1,
                                                                overflow = TextOverflow.Ellipsis,
                                                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                                            )
                                                        }
                                                    }
                                                }
                                            }
                                        }
                                    } else if (showRemoteVideo) {
                                        BitmapWebRtcVideoSurface(
                                            track = remoteTrack,
                                            mirror = false,
                                            faceFilter = CallFaceFilter.fromId(firstRemote?.faceFilter),
                                            onFirstFrameRendered = { remoteFrameRendered = true },
                                            modifier = Modifier.fillMaxSize()
                                        )
                                    }
                                    if (!showRemoteVideo || (!remoteFrameRendered && activeRemoteEntries.size <= 1)) {
                                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                            UserAvatar(
                                                displayName = firstRemote?.senderName ?: peerName,
                                                photoUrl = peerPhoto,
                                                status = "online",
                                                size = 92.dp
                                            )
                                            Spacer(modifier = Modifier.height(10.dp))
                                            Text(
                                                text = firstRemote?.senderName ?: peerName,
                                                style = MaterialTheme.typography.titleMedium,
                                                color = CordlyTextPrimary
                                            )
                                            if (call.status == "ringing" || showRemoteVideo) {
                                                Text(
                                                    text = if (showRemoteVideo) {
                                                        if (isArabic) "جاري استقبال الكاميرا..." else "Connecting video..."
                                                    } else {
                                                        strings.callingStatus
                                                    },
                                                    style = MaterialTheme.typography.labelMedium,
                                                    color = CordlyCyan
                                                )
                                            }
                                        }
                                    }

                                    // Peer quality + P2P Live indicator pills
                                    Row(
                                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                                        modifier = Modifier
                                            .align(Alignment.TopStart)
                                            .padding(10.dp)
                                    ) {
                                        if (connectedPeers > 0) {
                                            Surface(
                                                color = CordlyEmerald.copy(alpha = 0.25f),
                                                shape = RoundedCornerShape(10.dp),
                                                border = BorderStroke(1.dp, CordlyEmerald)
                                            ) {
                                                Text(
                                                    text = strings.webrtcConnectedBadge,
                                                    style = MaterialTheme.typography.labelSmall,
                                                    color = CordlyEmerald,
                                                    fontWeight = FontWeight.Bold,
                                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                                )
                                            }
                                        }
                                        if (firstRemote != null) {
                                            Surface(
                                                color = Color.Black.copy(alpha = 0.65f),
                                                shape = RoundedCornerShape(10.dp)
                                            ) {
                                                Text(
                                                    text = "${firstRemote.videoQuality} • ${CallFaceFilter.fromId(firstRemote.faceFilter).emoji}",
                                                    style = MaterialTheme.typography.labelSmall,
                                                    color = CordlyCyan,
                                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                                )
                                            }
                                        }
                                    }

                                    if (isVideoEnabled && hasCameraPermission) {
                                        Surface(
                                            color = Color.Black.copy(alpha = 0.68f),
                                            shape = RoundedCornerShape(14.dp),
                                            modifier = Modifier
                                                .align(Alignment.TopEnd)
                                                .padding(10.dp)
                                                .clip(RoundedCornerShape(14.dp))
                                                .clickable { webRtc.switchCamera() }
                                        ) {
                                            Row(
                                                verticalAlignment = Alignment.CenterVertically,
                                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                                            ) {
                                                Icon(Icons.Rounded.Cameraswitch, contentDescription = strings.flipCamera, tint = CordlyCyan, modifier = Modifier.size(16.dp))
                                                Spacer(modifier = Modifier.width(4.dp))
                                                Text(strings.flipCamera, style = MaterialTheme.typography.labelSmall, color = Color.White)
                                            }
                                        }
                                    }
                                }
                            }

                            // Self live camera preview (CameraX PreviewView COMPATIBLE primary path + Bitmap fallback)
                            if (isVideoEnabled && hasCameraPermission) {
                                Surface(
                                    color = Color.Black,
                                    shape = RoundedCornerShape(20.dp),
                                    border = BorderStroke(
                                        width = 1.5.dp,
                                        color = when {
                                            cameraError != null -> CordlyCrimson
                                            !isMuted && connectedPeers > 0 -> CordlyEmerald
                                            else -> CordlyBlurple
                                        }
                                    ),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(185.dp)
                                ) {
                                    Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
                                        if (usingCameraXPreview || cameraError == null) {
                                            LocalCameraPreviewBox(
                                                webRtc = webRtc,
                                                lifecycleOwner = lifecycleOwner,
                                                faceFilter = selectedFaceFilter,
                                                onStreamingChanged = { streaming ->
                                                    localPreviewStreaming = streaming
                                                },
                                                modifier = Modifier.fillMaxSize()
                                            )
                                        } else {
                                            BitmapWebRtcVideoSurface(
                                                track = localTrack,
                                                mirror = isFrontCam,
                                                faceFilter = selectedFaceFilter,
                                                onFirstFrameRendered = { localPreviewStreaming = true },
                                                modifier = Modifier.fillMaxSize()
                                            )
                                        }
                                        if (!localFrameRendered) {
                                            Column(
                                                horizontalAlignment = Alignment.CenterHorizontally,
                                                verticalArrangement = Arrangement.spacedBy(6.dp),
                                                modifier = Modifier.padding(12.dp)
                                            ) {
                                                if (cameraError != null) {
                                                    Text(
                                                        text = if (isArabic) "تعذر تشغيل الكاميرا" else "Camera failed to start",
                                                        style = MaterialTheme.typography.labelMedium,
                                                        color = CordlyCrimson,
                                                        fontWeight = FontWeight.Bold
                                                    )
                                                    Button(
                                                        onClick = { webRtc.retryLocalCamera(hasCameraPermission, lifecycleOwner) },
                                                        colors = ButtonDefaults.buttonColors(containerColor = CordlyBlurple),
                                                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                                                        modifier = Modifier.testTag("retry_call_camera_button")
                                                    ) {
                                                        Text(
                                                            text = if (isArabic) "إعادة تشغيل الكاميرا" else "Retry Camera",
                                                            style = MaterialTheme.typography.labelSmall,
                                                            color = Color.White
                                                        )
                                                    }
                                                } else {
                                                    CircularProgressIndicator(
                                                        color = CordlyCyan,
                                                        strokeWidth = 2.dp,
                                                        modifier = Modifier.size(22.dp)
                                                    )
                                                    Text(
                                                        text = if (isArabic) "جاري تشغيل الكاميرا..." else "Starting camera...",
                                                        style = MaterialTheme.typography.labelSmall,
                                                        color = CordlyTextSecondary
                                                    )
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    } else {
                        val displayParticipants = if (remoteStreams.isNotEmpty()) {
                            remoteStreams
                        } else {
                            listOf(
                                CallStreamFrame(
                                    callId = call.id,
                                    senderId = call.calleeId ?: call.targetId,
                                    senderName = peerName,
                                    audioLevel = 0.0,
                                    videoEnabled = false,
                                    muted = false
                                )
                            )
                        }
                        LazyVerticalGrid(
                            columns = GridCells.Adaptive(minSize = 148.dp),
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            item {
                                VoiceParticipantTile(
                                    name = "You",
                                    photoUrl = if (isCaller) call.callerPhoto else call.calleePhoto,
                                    audioLevel = if (isMuted) 0.0 else localMicAmplitude,
                                    muted = isMuted,
                                    badge = if (selectedVoiceEffect != VoiceEffectType.NORMAL) selectedVoiceEffect.titleEn else null
                                )
                            }
                            items(displayParticipants, key = { it.senderId }) { participant ->
                                VoiceParticipantTile(
                                    name = participant.senderName,
                                    photoUrl = if (participant.senderId == call.callerId) call.callerPhoto else peerPhoto,
                                    audioLevel = if (participant.muted) 0.0 else participant.audioLevel,
                                    muted = participant.muted,
                                    badge = participant.voiceEffect.takeIf { it != "normal" }?.let { VoiceEffectType.fromId(it).titleEn }
                                )
                            }
                        }
                    }
                }

                // Expandable tool trays: Video Quality (144p..4K), Snapchat Filters, Voice Changer
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    AnimatedVisibility(visible = showQualityBar) {
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .horizontalScroll(rememberScrollState())
                                .padding(bottom = 4.dp)
                        ) {
                            VideoQualityPreset.entries.forEach { preset ->
                                val selected = preset == selectedQuality
                                val rankLocked = preset.requiredRank == CordlyRepository.RANK_NEO_PLUS && !isNeoPlus
                                Surface(
                                    color = if (selected) CordlyBlurple else CordlyCardSurface,
                                    shape = RoundedCornerShape(12.dp),
                                    border = BorderStroke(
                                        1.dp,
                                        when {
                                            rankLocked -> CordlyAmber
                                            preset.isHdTier -> CordlyCyan.copy(alpha = 0.7f)
                                            else -> CordlyDivider
                                        }
                                    ),
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(12.dp))
                                        .clickable {
                                            if (rankLocked) {
                                                onStatusToast(strings.neoPlusRequiredNotice)
                                            } else {
                                                selectedQuality = preset
                                                webRtc.applyVideoQuality(preset)
                                                onUpdateVideoQuality(preset.id)
                                                onStatusToast("${strings.videoQualityLabel}: ${preset.label}")
                                            }
                                        }
                                        .testTag("quality_chip_${preset.id}")
                                ) {
                                    Text(
                                        text = if (rankLocked) "🔒 ${preset.label}" else preset.label,
                                        style = MaterialTheme.typography.labelSmall,
                                        color = when {
                                            rankLocked -> CordlyAmber
                                            selected -> Color.White
                                            else -> CordlyTextPrimary
                                        },
                                        fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
                                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 7.dp)
                                    )
                                }
                            }
                        }
                    }

                    AnimatedVisibility(visible = showFilterBar) {
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .horizontalScroll(rememberScrollState())
                                .padding(bottom = 4.dp)
                        ) {
                            CallFaceFilter.entries.forEach { filter ->
                                val selected = filter == selectedFaceFilter
                                Surface(
                                    color = if (selected) Color(0xFFEC4899) else CordlyCardSurface,
                                    shape = RoundedCornerShape(12.dp),
                                    border = BorderStroke(1.dp, if (selected) Color(0xFFF9A8D4) else CordlyDivider),
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(12.dp))
                                        .clickable { selectedFaceFilter = filter }
                                        .testTag("filter_chip_${filter.id}")
                                ) {
                                    Text(
                                        text = "${filter.emoji} ${filter.titleEn}",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = Color.White,
                                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 7.dp)
                                    )
                                }
                            }
                        }
                    }

                    AnimatedVisibility(visible = showVoiceFxBar) {
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .horizontalScroll(rememberScrollState())
                                .padding(bottom = 4.dp)
                        ) {
                            VoiceEffectType.entries.forEach { fx ->
                                val locked = fx != VoiceEffectType.NORMAL && !isUltraOrPlus
                                val selected = fx == selectedVoiceEffect
                                Surface(
                                    color = if (selected) CordlyEmerald else CordlyCardSurface,
                                    shape = RoundedCornerShape(12.dp),
                                    border = BorderStroke(1.dp, if (locked) CordlyAmber else CordlyDivider),
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(12.dp))
                                        .clickable {
                                            if (locked) {
                                                onStatusToast(strings.ultraFeatureLockedNotice)
                                            } else {
                                                selectedVoiceEffect = fx
                                            }
                                        }
                                        .testTag("voice_fx_${fx.id}")
                                ) {
                                    Text(
                                        text = if (locked) "🔒 ${fx.titleEn}" else fx.titleEn,
                                        style = MaterialTheme.typography.labelSmall,
                                        color = if (locked) CordlyAmber else Color.White,
                                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 7.dp)
                                    )
                                }
                            }
                        }
                    }

                    // Feature toggle strip (Quality / Snapchat Filters / Voice Changer / Record)
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState())
                    ) {
                        MiniCallFeatureChip(
                            icon = Icons.Rounded.HighQuality,
                            label = selectedQuality.id,
                            active = showQualityBar,
                            accent = CordlyCyan,
                            testTag = "call_quality_toggle",
                            onClick = {
                                showQualityBar = !showQualityBar
                                showFilterBar = false
                                showVoiceFxBar = false
                            }
                        )
                        MiniCallFeatureChip(
                            icon = Icons.Rounded.AutoAwesome,
                            label = strings.faceFiltersLabel,
                            active = showFilterBar || selectedFaceFilter != CallFaceFilter.NONE,
                            accent = Color(0xFFEC4899),
                            testTag = "call_filters_toggle",
                            onClick = {
                                showFilterBar = !showFilterBar
                                showQualityBar = false
                                showVoiceFxBar = false
                            }
                        )
                        MiniCallFeatureChip(
                            icon = Icons.Rounded.RecordVoiceOver,
                            label = strings.voiceChangerLabel,
                            active = showVoiceFxBar || selectedVoiceEffect != VoiceEffectType.NORMAL,
                            accent = CordlyEmerald,
                            testTag = "call_voice_fx_toggle",
                            onClick = {
                                showVoiceFxBar = !showVoiceFxBar
                                showQualityBar = false
                                showFilterBar = false
                            }
                        )
                        MiniCallFeatureChip(
                            icon = if (isRecordingCall) Icons.Rounded.Stop else Icons.Rounded.FiberManualRecord,
                            label = if (isRecordingCall) strings.stopRecordingLabel else strings.recordCallLabel,
                            active = isRecordingCall,
                            accent = CordlyCrimson,
                            testTag = "call_record_toggle",
                            onClick = {
                                if (!isUltraOrPlus) {
                                    onStatusToast(strings.ultraFeatureLockedNotice)
                                } else if (!isRecordingCall) {
                                    audioEngine.startCallRecording(call.id)
                                    isRecordingCall = true
                                    onStatusToast(strings.recordingStartedNotice)
                                } else {
                                    val saved = audioEngine.stopCallRecording()
                                    isRecordingCall = false
                                    onStatusToast(
                                        if (saved != null) strings.recordingSavedNotice else "Recording stopped"
                                    )
                                }
                            }
                        )
                    }

                    // Bottom Call Controls Bar
                    Surface(
                        color = CordlySurfaceDark,
                        shape = RoundedCornerShape(24.dp),
                        border = BorderStroke(1.dp, CordlyDivider),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            horizontalArrangement = Arrangement.SpaceEvenly,
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 10.dp, horizontal = 8.dp)
                        ) {
                            CallControlPill(
                                icon = if (isMuted) Icons.Rounded.MicOff else Icons.Rounded.Mic,
                                label = if (isMuted) strings.unmuteMic else strings.muteMic,
                                active = !isMuted,
                                activeColor = CordlyElevatedSurface,
                                inactiveColor = CordlyCrimson,
                                testTag = "call_mute_button",
                                onClick = {
                                    val next = !isMuted
                                    isMuted = next
                                    webRtc.setMuted(next)
                                    onToggleMute(next)
                                }
                            )

                            CallControlPill(
                                icon = if (isVideoEnabled) Icons.Rounded.Videocam else Icons.Rounded.VideocamOff,
                                label = if (isVideoEnabled) strings.cameraOff else strings.cameraOn,
                                active = isVideoEnabled,
                                activeColor = CordlyBlurple,
                                inactiveColor = CordlyCardSurface,
                                testTag = "call_video_button",
                                onClick = {
                                    val next = !isVideoEnabled
                                    isVideoEnabled = next
                                    if (next && !hasCameraPermission) {
                                        permissionLauncher.launch(arrayOf(Manifest.permission.CAMERA))
                                    }
                                    webRtc.setVideoEnabled(next, hasCameraPermission, lifecycleOwner)
                                    onToggleVideo(next)
                                }
                            )

                            CallControlPill(
                                icon = if (isSpeakerOn) Icons.Rounded.VolumeUp else Icons.Rounded.VolumeOff,
                                label = strings.speakerOn,
                                active = isSpeakerOn,
                                activeColor = CordlyCyan.copy(alpha = 0.25f),
                                inactiveColor = CordlyCardSurface,
                                testTag = "call_speaker_button",
                                onClick = { isSpeakerOn = !isSpeakerOn }
                            )

                            CallControlPill(
                                icon = Icons.Rounded.CallEnd,
                                label = strings.endCall,
                                active = false,
                                activeColor = CordlyCrimson,
                                inactiveColor = CordlyCrimson,
                                testTag = "call_end_button",
                                onClick = {
                                    if (isRecordingCall) {
                                        audioEngine.stopCallRecording()
                                        isRecordingCall = false
                                    }
                                    SoundEffectsManager.play(AppSoundEffect.CALL_END)
                                    onEndCall()
                                }
                            )
                        }
                    }
                }
            }
        }
    }

    // Neo Plus Multi-Person Add Friend Dialog
    if (showAddFriendDialog) {
        val available = inviteableFriends.filter { !call.participantIds.contains(it.uid) }
        AlertDialog(
            onDismissRequest = { showAddFriendDialog = false },
            containerColor = CordlySurfaceDark,
            title = { Text(strings.addToCall, color = CordlyTextPrimary) },
            text = {
                if (available.isEmpty()) {
                    Text(strings.noFriendsToAddCall, color = CordlyTextSecondary)
                } else {
                    LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        items(available, key = { it.uid }) { friend ->
                            Surface(
                                color = CordlyCardSurface,
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(12.dp))
                                    .clickable {
                                        onAddParticipant(friend.uid, friend.name)
                                        showAddFriendDialog = false
                                    }
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.padding(10.dp)
                                ) {
                                    UserAvatar(displayName = friend.name, photoUrl = friend.photoUrl, size = 36.dp)
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Text(friend.name, color = CordlyTextPrimary, modifier = Modifier.weight(1f))
                                    Icon(Icons.Rounded.PersonAdd, contentDescription = null, tint = Color(0xFFFFD166))
                                }
                            }
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showAddFriendDialog = false }) {
                    Text(strings.closeButton, color = CordlyTextSecondary)
                }
            }
        )
        }
    }
}

@Composable
private fun MiniCallFeatureChip(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    active: Boolean,
    accent: Color,
    testTag: String,
    onClick: () -> Unit
) {
    Surface(
        color = if (active) accent.copy(alpha = 0.22f) else CordlyCardSurface,
        shape = RoundedCornerShape(14.dp),
        border = BorderStroke(1.dp, if (active) accent else CordlyDivider),
        modifier = Modifier
            .clip(RoundedCornerShape(14.dp))
            .clickable(onClick = onClick)
            .testTag(testTag)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 7.dp)
        ) {
            Icon(icon, contentDescription = label, tint = if (active) accent else CordlyTextSecondary, modifier = Modifier.size(15.dp))
            Spacer(modifier = Modifier.width(5.dp))
            Text(label, style = MaterialTheme.typography.labelSmall, color = if (active) Color.White else CordlyTextSecondary, maxLines = 1)
        }
    }
}

@Composable
private fun VoiceParticipantTile(
    name: String,
    photoUrl: String?,
    audioLevel: Double,
    muted: Boolean,
    badge: String? = null
) {
    val speaking = audioLevel > 0.12 && !muted
    Surface(
        color = CordlyCardSurface,
        shape = RoundedCornerShape(20.dp),
        border = BorderStroke(
            width = if (speaking) 2.dp else 1.dp,
            color = if (speaking) CordlyEmerald else CordlyDivider
        ),
        modifier = Modifier
            .fillMaxWidth()
            .height(172.dp)
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier.padding(14.dp)
        ) {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .clip(CircleShape)
                    .border(
                        width = if (speaking) 3.dp else 0.dp,
                        color = if (speaking) CordlyEmerald else Color.Transparent,
                        shape = CircleShape
                    )
                    .padding(4.dp)
            ) {
                UserAvatar(
                    displayName = name,
                    photoUrl = photoUrl,
                    status = if (muted) "dnd" else "online",
                    size = 76.dp
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = if (muted) Icons.Rounded.MicOff else Icons.Rounded.GraphicEq,
                    contentDescription = null,
                    tint = if (muted) CordlyCrimson else if (speaking) CordlyEmerald else CordlyTextSecondary,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = name,
                    style = MaterialTheme.typography.titleMedium,
                    color = CordlyTextPrimary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
            if (!badge.isNullOrBlank()) {
                Text(
                    text = badge,
                    style = MaterialTheme.typography.labelSmall,
                    color = CordlyCyan
                )
            }
        }
    }
}

@Composable
private fun CallControlPill(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    active: Boolean,
    activeColor: Color,
    inactiveColor: Color,
    testTag: String,
    onClick: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .clip(RoundedCornerShape(14.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 6.dp, vertical = 4.dp)
            .testTag(testTag)
    ) {
        Surface(
            color = if (active) activeColor else inactiveColor,
            shape = CircleShape,
            modifier = Modifier.size(48.dp)
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(
                    imageVector = icon,
                    contentDescription = label,
                    tint = Color.White,
                    modifier = Modifier.size(22.dp)
                )
            }
        }
        Spacer(modifier = Modifier.height(3.dp))
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = CordlyTextSecondary,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@Composable
private fun LocalCameraPreviewBox(
    webRtc: WebRtcCallManager,
    lifecycleOwner: androidx.lifecycle.LifecycleOwner,
    faceFilter: CallFaceFilter,
    onStreamingChanged: (Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val latestOnStreaming by rememberUpdatedState(onStreamingChanged)
    val previewView = remember(context) {
        PreviewView(context).apply {
            id = R.id.webrtc_local_renderer
            implementationMode = PreviewView.ImplementationMode.COMPATIBLE
            scaleType = PreviewView.ScaleType.FILL_CENTER
        }
    }

    DisposableEffect(previewView, lifecycleOwner) {
        val observer = Observer<PreviewView.StreamState> { state ->
            latestOnStreaming(state == PreviewView.StreamState.STREAMING)
        }
        previewView.previewStreamState.observe(lifecycleOwner, observer)
        webRtc.setLocalPreviewSurfaceProvider(previewView.surfaceProvider, lifecycleOwner)
        onDispose {
            previewView.previewStreamState.removeObserver(observer)
            webRtc.setLocalPreviewSurfaceProvider(null, null)
            latestOnStreaming(false)
        }
    }

    Box(modifier = modifier.background(Color.Black)) {
        AndroidView(
            factory = { previewView },
            modifier = Modifier.fillMaxSize()
        )
        if (faceFilter != CallFaceFilter.NONE) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                drawIntoCanvas { canvas ->
                    CallVideoFrameProcessor.drawFaceFilterOnCanvas(
                        canvas.nativeCanvas,
                        size.width,
                        size.height,
                        faceFilter
                    )
                }
            }
        }
    }
}

@Composable
private fun BitmapWebRtcVideoSurface(
    track: VideoTrack?,
    mirror: Boolean,
    faceFilter: CallFaceFilter,
    onFirstFrameRendered: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val latestOnFirstFrame by rememberUpdatedState(onFirstFrameRendered)
    val mainHandler = remember { Handler(Looper.getMainLooper()) }
    val convertExecutor = remember { Executors.newSingleThreadExecutor() }
    var renderedBitmap by remember(track) { mutableStateOf<Bitmap?>(null) }
    var frameTick by remember(track) { mutableIntStateOf(0) }

    DisposableEffect(convertExecutor) {
        onDispose {
            convertExecutor.shutdownNow()
        }
    }

    DisposableEffect(track) {
        val disposed = AtomicBoolean(false)
        val converting = AtomicBoolean(false)
        var reportedFirst = false
        var bmpA: Bitmap? = null
        var bmpB: Bitmap? = null
        var useSlotA = true
        var pixelBuf = IntArray(0)

        val sink = VideoSink { frame: VideoFrame ->
            if (disposed.get() || !converting.compareAndSet(false, true)) {
                return@VideoSink
            }
            frame.retain()
            val rot = frame.rotation
            try {
                convertExecutor.execute {
                    try {
                        if (disposed.get()) return@execute
                        val i420 = frame.buffer.toI420() ?: return@execute
                        try {
                            val srcW = i420.width
                            val srcH = i420.height
                            if (srcW <= 0 || srcH <= 0) return@execute

                            val step = if (maxOf(srcW, srcH) > 960) 2 else 1
                            val sampledW = (srcW + step - 1) / step
                            val sampledH = (srcH + step - 1) / step
                            val normRot = ((rot % 360) + 360) % 360
                            val outW = if (normRot == 90 || normRot == 270) sampledH else sampledW
                            val outH = if (normRot == 90 || normRot == 270) sampledW else sampledH
                            val totalPx = outW * outH
                            if (pixelBuf.size < totalPx) {
                                pixelBuf = IntArray(totalPx)
                            }

                            val dataY = i420.dataY
                            val dataU = i420.dataU
                            val dataV = i420.dataV
                            val strideY = i420.strideY
                            val strideU = i420.strideU
                            val strideV = i420.strideV

                            for (dy in 0 until sampledH) {
                                val sy = dy * step
                                val yRow = sy * strideY
                                val uvRowU = (sy shr 1) * strideU
                                val uvRowV = (sy shr 1) * strideV
                                for (dx in 0 until sampledW) {
                                    val sx = dx * step
                                    val yVal = dataY.get(yRow + sx).toInt() and 0xFF
                                    val uvCol = sx shr 1
                                    val uVal = (dataU.get(uvRowU + uvCol).toInt() and 0xFF) - 128
                                    val vVal = (dataV.get(uvRowV + uvCol).toInt() and 0xFF) - 128

                                    val c = (yVal - 16).coerceAtLeast(0)
                                    val r = ((298 * c + 409 * vVal + 128) shr 8).coerceIn(0, 255)
                                    val g = ((298 * c - 100 * uVal - 208 * vVal + 128) shr 8).coerceIn(0, 255)
                                    val b = ((298 * c + 516 * uVal + 128) shr 8).coerceIn(0, 255)
                                    val argb = (0xFF shl 24) or (r shl 16) or (g shl 8) or b

                                    val ox: Int
                                    val oy: Int
                                    when (normRot) {
                                        90 -> {
                                            ox = sampledH - 1 - dy
                                            oy = dx
                                        }
                                        180 -> {
                                            ox = sampledW - 1 - dx
                                            oy = sampledH - 1 - dy
                                        }
                                        270 -> {
                                            ox = dy
                                            oy = sampledW - 1 - dx
                                        }
                                        else -> {
                                            ox = dx
                                            oy = dy
                                        }
                                    }
                                    pixelBuf[oy * outW + ox] = argb
                                }
                            }

                            var target = if (useSlotA) bmpA else bmpB
                            if (target == null || target.isRecycled || target.width != outW || target.height != outH) {
                                target = Bitmap.createBitmap(outW, outH, Bitmap.Config.ARGB_8888)
                                if (useSlotA) bmpA = target else bmpB = target
                            }
                            target.setPixels(pixelBuf, 0, outW, 0, 0, outW, outH)
                            useSlotA = !useSlotA

                            if (!disposed.get()) {
                                val firstNow = !reportedFirst
                                reportedFirst = true
                                val nextBmp = target
                                mainHandler.post {
                                    if (!disposed.get()) {
                                        renderedBitmap = nextBmp
                                        frameTick++
                                        if (firstNow) latestOnFirstFrame()
                                    }
                                }
                            }
                        } finally {
                            i420.release()
                        }
                    } catch (_: Throwable) {
                    } finally {
                        frame.release()
                        converting.set(false)
                    }
                }
            } catch (_: Throwable) {
                frame.release()
                converting.set(false)
            }
        }

        if (track != null) {
            try {
                track.addSink(sink)
            } catch (_: Exception) {
            }
        }
        onDispose {
            disposed.set(true)
            if (track != null) {
                try {
                    track.removeSink(sink)
                } catch (_: Exception) {
                }
            }
            renderedBitmap = null
        }
    }

    BoxWithConstraints(
        contentAlignment = Alignment.Center,
        modifier = modifier.background(Color.Black)
    ) {
        val bmp = renderedBitmap
        val tickRead = frameTick
        if (bmp != null && !bmp.isRecycled && tickRead >= 0) {
            val boxAspect = if (maxHeight > 0.dp) maxWidth / maxHeight else 1f
            val bmpAspect = bmp.width.toFloat() / bmp.height.coerceAtLeast(1).toFloat()
            val fittedModifier = if (bmpAspect > 0f && maxWidth > 0.dp && maxHeight > 0.dp) {
                if (bmpAspect > boxAspect) {
                    val w = maxWidth
                    val h = (w / bmpAspect).coerceAtMost(maxHeight)
                    Modifier
                        .width(w)
                        .height(h)
                } else {
                    val h = maxHeight
                    val w = (h * bmpAspect).coerceAtMost(maxWidth)
                    Modifier
                        .width(w)
                        .height(h)
                }
            } else {
                Modifier.fillMaxSize()
            }

            Image(
                bitmap = bmp.asImageBitmap(),
                contentDescription = "Call video feed",
                contentScale = ContentScale.Fit,
                modifier = fittedModifier.graphicsLayer {
                    scaleX = if (mirror) -1f else 1f
                }
            )
            if (faceFilter != CallFaceFilter.NONE) {
                Canvas(modifier = fittedModifier) {
                    drawIntoCanvas { canvas ->
                        CallVideoFrameProcessor.drawFaceFilterOnCanvas(
                            canvas.nativeCanvas,
                            size.width,
                            size.height,
                            faceFilter
                        )
                    }
                }
            }
        }
    }
}

