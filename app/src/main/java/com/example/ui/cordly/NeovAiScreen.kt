package com.example.ui.cordly

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
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
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.Send
import androidx.compose.material.icons.rounded.AutoAwesome
import androidx.compose.material.icons.rounded.CameraAlt
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.DeleteSweep
import androidx.compose.material.icons.rounded.GraphicEq
import androidx.compose.material.icons.rounded.Image
import androidx.compose.material.icons.rounded.Mic
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material.icons.rounded.RadioButtonChecked
import androidx.compose.material.icons.rounded.Stop
import androidx.compose.material.icons.rounded.VideoLibrary
import androidx.compose.material.icons.rounded.Videocam
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.ui.theme.CordlyCardSurface
import com.example.ui.theme.CordlyCrimson
import com.example.ui.theme.CordlyDivider
import com.example.ui.theme.CordlyElevatedSurface
import com.example.ui.theme.CordlyEmerald
import com.example.ui.theme.CordlySurfaceDark
import com.example.ui.theme.CordlyTextMuted
import com.example.ui.theme.CordlyTextPrimary
import com.example.ui.theme.CordlyTextSecondary
import kotlinx.coroutines.launch
import java.io.File
import java.util.Locale

@Composable
fun NeovAiDialog(
    aiViewModel: NeovAiViewModel,
    onOpenLiveCall: () -> Unit,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val isArabic = AppLanguageManager.resolveEffectiveCode(LocalSelectedLanguageCode.current).startsWith("ar")

    val uiState by aiViewModel.uiState.collectAsStateWithLifecycle()
    val messages by aiViewModel.messages.collectAsStateWithLifecycle()
    val pendingAttachment by aiViewModel.pendingAttachment.collectAsStateWithLifecycle()
    val isPreparingMedia by aiViewModel.isPreparingMedia.collectAsStateWithLifecycle()
    val playingAudioId by VoiceNoteHelper.playingClipId.collectAsStateWithLifecycle()

    var inputText by remember { mutableStateOf("") }
    var isRecordingAudio by remember { mutableStateOf(false) }
    var recordingDurationMs by remember { mutableLongStateOf(0L) }
    var pendingCameraPhotoUri by remember { mutableStateOf<Uri?>(null) }
    var pendingCameraVideoUri by remember { mutableStateOf<Uri?>(null) }
    var pendingCameraAction by remember { mutableStateOf<String?>(null) }

    val listState = rememberLazyListState()
    LaunchedEffect(Unit) {
        aiViewModel.warmUp()
    }
    LaunchedEffect(messages.size) {
        if (messages.isNotEmpty()) {
            listState.animateScrollToItem(messages.lastIndex)
        }
    }
    LaunchedEffect(messages.lastOrNull()?.text?.length) {
        if (messages.isNotEmpty()) {
            listState.scrollToItem(messages.lastIndex)
        }
    }

    val lifecycleOwner = LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_STOP || event == Lifecycle.Event.ON_PAUSE) {
                isRecordingAudio = false
                VoiceNoteHelper.stopPlayback()
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
            isRecordingAudio = false
            VoiceNoteHelper.stopPlayback()
        }
    }

    val takePhotoLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicture()
    ) { success ->
        val uri = pendingCameraPhotoUri
        if (success && uri != null) {
            aiViewModel.attachPhotoUri(
                context = context,
                uri = uri,
                label = if (isArabic) "صورة بالكاميرا" else "Camera photo"
            )
        }
    }

    val captureVideoLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CaptureVideo()
    ) { success ->
        val uri = pendingCameraVideoUri
        if (success && uri != null) {
            aiViewModel.attachVideoUri(
                context = context,
                uri = uri,
                label = if (isArabic) "فيديو مسجّل" else "Recorded video"
            )
        }
    }

    val pickPhotoLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        if (uri != null) {
            aiViewModel.attachPhotoUri(
                context = context,
                uri = uri,
                label = if (isArabic) "صورة" else "Photo"
            )
        }
    }

    val pickVideoLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        if (uri != null) {
            aiViewModel.attachVideoUri(
                context = context,
                uri = uri,
                label = if (isArabic) "فيديو" else "Video"
            )
        }
    }

    fun createTempUri(prefix: String, suffix: String): Uri {
        val file = File(context.cacheDir, "${prefix}_${System.currentTimeMillis()}$suffix")
        return FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
    }

    fun launchTakePhoto() {
        try {
            val uri = createTempUri("neov_ai_photo", ".jpg")
            pendingCameraPhotoUri = uri
            takePhotoLauncher.launch(uri)
        } catch (_: Exception) {
            android.widget.Toast.makeText(
                context,
                if (isArabic) "تعذر فتح الكاميرا على هذا الجهاز" else "Unable to open camera on this device",
                android.widget.Toast.LENGTH_SHORT
            ).show()
        }
    }

    fun launchCaptureVideo() {
        try {
            val uri = createTempUri("neov_ai_video", ".mp4")
            pendingCameraVideoUri = uri
            captureVideoLauncher.launch(uri)
        } catch (_: Exception) {
            android.widget.Toast.makeText(
                context,
                if (isArabic) "تعذر فتح كاميرا الفيديو على هذا الجهاز" else "Unable to open video camera on this device",
                android.widget.Toast.LENGTH_SHORT
            ).show()
        }
    }

    fun startUnlimitedAudioCapture() {
        VoiceNoteHelper.stopPlayback()
        isRecordingAudio = true
        recordingDurationMs = 0L
        scope.launch {
            val recorded = VoiceNoteHelper.recordWavForAi(
                shouldContinue = { isRecordingAudio },
                onDurationTick = { recordingDurationMs = it }
            )
            isRecordingAudio = false
            if (recorded != null) {
                val (wavBytes, durMs) = recorded
                val secs = (durMs / 1000L).coerceAtLeast(1L)
                val label = if (isArabic) "تسجيل صوتي (${secs}ث)" else "Voice audio (${secs}s)"
                aiViewModel.attachRecordedAudio(wavBytes, durMs, label)
            }
        }
    }

    val cameraPermsLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { grants ->
        val camOk = grants[Manifest.permission.CAMERA] == true ||
            ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED
        val action = pendingCameraAction
        pendingCameraAction = null
        if (camOk) {
            when (action) {
                "photo" -> launchTakePhoto()
                "video" -> launchCaptureVideo()
            }
        }
    }

    val micPermLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) {
            startUnlimitedAudioCapture()
        }
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false, decorFitsSystemWindows = false)
    ) {
        SanitizeDialogWindowIds()
        Surface(
            color = Color(0xFF0A0607),
            modifier = Modifier.fillMaxSize()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .statusBarsPadding()
                    .navigationBarsPadding()
                    .imePadding()
            ) {
                // Header with Red & Yellow Neov AI Branding + Live Call Button
                Surface(
                    color = Color(0xFF140B0D),
                    tonalElevation = 4.dp,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 12.dp, vertical = 10.dp)
                        ) {
                            NeovAiOrbGlyph(size = 44.dp)
                            Spacer(modifier = Modifier.width(10.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = "Neov AI",
                                        style = MaterialTheme.typography.titleLarge,
                                        color = CordlyTextPrimary,
                                        fontWeight = FontWeight.ExtraBold
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Surface(
                                        shape = RoundedCornerShape(8.dp),
                                        color = Color.Transparent
                                    ) {
                                        Text(
                                            text = if (isArabic) "جديد" else "NEW",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = Color.White,
                                            fontWeight = FontWeight.Bold,
                                            modifier = Modifier
                                                .background(
                                                    Brush.horizontalGradient(listOf(NeovAiRed, NeovAiAmber))
                                                )
                                                .padding(horizontal = 6.dp, vertical = 2.dp)
                                        )
                                    }
                                }
                                Text(
                                    text = if (isArabic) "صور • فيديو • صوت لا نهائي • مكالمة لايف"
                                    else "Photo • Video • Unlimited Audio • Live Call",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = NeovAiYellow,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }

                            // Launch Live Call Button (Voice + Live Camera like Gemini Live)
                            Button(
                                onClick = onOpenLiveCall,
                                colors = ButtonDefaults.buttonColors(containerColor = NeovAiRed),
                                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp),
                                shape = RoundedCornerShape(14.dp),
                                modifier = Modifier
                                    .minimumInteractiveComponentSize()
                                    .testTag("neov_ai_live_call_button")
                            ) {
                                Icon(
                                    imageVector = Icons.Rounded.Videocam,
                                    contentDescription = if (isArabic) "مكالمة لايف" else "Live Call",
                                    tint = NeovAiYellow,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(5.dp))
                                Text(
                                    text = if (isArabic) "لايف" else "Live",
                                    color = Color.White,
                                    style = MaterialTheme.typography.labelLarge,
                                    fontWeight = FontWeight.Bold
                                )
                            }

                            if (messages.isNotEmpty()) {
                                IconButton(
                                    onClick = { aiViewModel.clearChat() },
                                    modifier = Modifier.minimumInteractiveComponentSize()
                                ) {
                                    Icon(
                                        imageVector = Icons.Rounded.DeleteSweep,
                                        contentDescription = "Clear AI chat",
                                        tint = CordlyTextSecondary
                                    )
                                }
                            }

                            IconButton(
                                onClick = onDismiss,
                                modifier = Modifier
                                    .minimumInteractiveComponentSize()
                                    .testTag("neov_ai_close_button")
                            ) {
                                Icon(
                                    imageVector = Icons.Rounded.Close,
                                    contentDescription = "Close Neov AI",
                                    tint = CordlyTextPrimary
                                )
                            }
                        }
                        HorizontalDivider(color = NeovAiRed.copy(alpha = 0.28f), thickness = 1.dp)
                    }
                }

                // Error Banner (transparently surfaced from Firebase AI Logic)
                val errState = uiState as? NeovAiUiState.Error
                AnimatedVisibility(visible = errState != null) {
                    if (errState != null) {
                        val isBillingError = errState.message.contains("free tier", ignoreCase = true) ||
                            errState.message.contains("billing", ignoreCase = true) ||
                            errState.message.contains("project-unavailable", ignoreCase = true)
                        Surface(
                            color = CordlyCrimson.copy(alpha = 0.2f),
                            border = BorderStroke(1.dp, CordlyCrimson),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 12.dp, vertical = 6.dp)
                        ) {
                            Column(modifier = Modifier.padding(10.dp)) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        if (isBillingError && isArabic) {
                                            Text(
                                                text = "الباقة المجانية لـ Gemini API غير متاحة لهذا المشروع حالياً. فعّل الفوترة (Billing) في Google AI Studio ليعمل الذكاء الاصطناعي:",
                                                style = MaterialTheme.typography.labelMedium,
                                                color = Color.White,
                                                fontWeight = FontWeight.Bold
                                            )
                                            Spacer(modifier = Modifier.height(4.dp))
                                        }
                                        Text(
                                            text = errState.message,
                                            style = MaterialTheme.typography.bodySmall,
                                            color = Color(0xFFFCA5A5)
                                        )
                                    }
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Surface(
                                        color = NeovAiRed,
                                        shape = RoundedCornerShape(8.dp),
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(8.dp))
                                            .clickable { aiViewModel.retryLastMessage() }
                                            .testTag("neov_ai_retry_button")
                                    ) {
                                        Text(
                                            text = if (isArabic) "إعادة" else "Retry",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = Color.White,
                                            fontWeight = FontWeight.Bold,
                                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                                        )
                                    }
                                    Spacer(modifier = Modifier.width(4.dp))
                                    IconButton(
                                        onClick = { aiViewModel.dismissError() },
                                        modifier = Modifier.size(32.dp)
                                    ) {
                                        Icon(Icons.Rounded.Close, contentDescription = "Dismiss error", tint = Color.White, modifier = Modifier.size(16.dp))
                                    }
                                }
                                if (isBillingError) {
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Surface(
                                        color = NeovAiYellow.copy(alpha = 0.18f),
                                        border = BorderStroke(1.dp, NeovAiYellow),
                                        shape = RoundedCornerShape(8.dp),
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(8.dp))
                                            .clickable {
                                                try {
                                                    context.startActivity(
                                                        Intent(Intent.ACTION_VIEW, Uri.parse("https://ai.studio.google.com/billing"))
                                                    )
                                                } catch (_: Exception) {
                                                }
                                            }
                                            .testTag("neov_ai_open_billing_button")
                                    ) {
                                        Text(
                                            text = if (isArabic) "فتح صفحة الفوترة في Google AI Studio" else "Open Google AI Studio Billing",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = NeovAiYellow,
                                            fontWeight = FontWeight.Bold,
                                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                // Chat Messages / Welcome Empty State
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                ) {
                    if (messages.isEmpty()) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center,
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(20.dp)
                        ) {
                            NeovAiOrbGlyph(size = 86.dp)
                            Spacer(modifier = Modifier.height(14.dp))
                            Text(
                                text = if (isArabic) "أهلاً بك في Neov AI" else "Welcome to Neov AI",
                                style = MaterialTheme.typography.headlineSmall,
                                color = CordlyTextPrimary,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = if (isArabic) {
                                    "صوّر له بكاميرتك، سجّل له فيديو أو أوديو بلا حد زمني، أو افتح مكالمة فيديو صوتية مباشرة معه!"
                                } else {
                                    "Snap a photo, record video or unlimited voice audio, or start a live voice & camera call!"
                                },
                                style = MaterialTheme.typography.bodyMedium,
                                color = CordlyTextSecondary
                            )
                            Spacer(modifier = Modifier.height(18.dp))

                            Row(
                                horizontalArrangement = Arrangement.spacedBy(10.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                QuickAiStarterCard(
                                    icon = Icons.Rounded.Videocam,
                                    title = if (isArabic) "مكالمة لايف" else "Live Call",
                                    subtitle = if (isArabic) "تحدث وأره الفيديو" else "Talk + live video",
                                    accent = NeovAiRed,
                                    modifier = Modifier.weight(1f),
                                    onClick = onOpenLiveCall
                                )
                                QuickAiStarterCard(
                                    icon = Icons.Rounded.CameraAlt,
                                    title = if (isArabic) "تصوير لـ AI" else "Snap Photo",
                                    subtitle = if (isArabic) "اسأله عن أي شيء" else "Ask about camera",
                                    accent = NeovAiYellow,
                                    modifier = Modifier.weight(1f),
                                    onClick = {
                                        val hasCam = ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED
                                        if (hasCam) launchTakePhoto() else {
                                            pendingCameraAction = "photo"
                                            cameraPermsLauncher.launch(arrayOf(Manifest.permission.CAMERA))
                                        }
                                    }
                                )
                            }
                        }
                    } else {
                        LazyColumn(
                            state = listState,
                            contentPadding = PaddingValues(14.dp),
                            verticalArrangement = Arrangement.spacedBy(10.dp),
                            modifier = Modifier.fillMaxSize()
                        ) {
                            items(messages, key = { it.id }) { msg ->
                                NeovAiMessageBubble(
                                    message = msg,
                                    isPlayingAudio = playingAudioId == msg.id,
                                    onPlayAudioToggle = {
                                        msg.audioWavBytes?.let { wav ->
                                            VoiceNoteHelper.toggleOrPlayWavBytes(msg.id, wav)
                                        }
                                    }
                                )
                            }
                        }
                    }
                }

                // Pending Multimodal Attachment Preview Bar
                val attached = pendingAttachment
                AnimatedVisibility(visible = attached != null || isPreparingMedia) {
                    Surface(
                        color = Color(0xFF1A1013),
                        border = BorderStroke(1.dp, NeovAiAmber.copy(alpha = 0.55f)),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 12.dp, vertical = 4.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(10.dp)
                        ) {
                            if (isPreparingMedia) {
                                CircularProgressIndicator(color = NeovAiYellow, strokeWidth = 2.dp, modifier = Modifier.size(20.dp))
                                Spacer(modifier = Modifier.width(10.dp))
                                Text(
                                    text = if (isArabic) "جاري تجهيز المرفق..." else "Preparing attachment...",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = CordlyTextPrimary
                                )
                            } else if (attached != null) {
                                val bmp = attached.previewBitmapOrNull()
                                if (bmp != null) {
                                    Image(
                                        bitmap = bmp.asImageBitmap(),
                                        contentDescription = attached.label,
                                        contentScale = ContentScale.Crop,
                                        modifier = Modifier
                                            .size(44.dp)
                                            .clip(RoundedCornerShape(8.dp))
                                    )
                                } else {
                                    Icon(
                                        imageVector = if (attached.kind == NeovAttachmentKind.VIDEO) Icons.Rounded.Videocam else Icons.Rounded.GraphicEq,
                                        contentDescription = null,
                                        tint = NeovAiYellow,
                                        modifier = Modifier.size(24.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.width(10.dp))
                                Text(
                                    text = attached.label,
                                    style = MaterialTheme.typography.labelLarge,
                                    color = CordlyTextPrimary,
                                    modifier = Modifier.weight(1f),
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                IconButton(
                                    onClick = { aiViewModel.clearAttachment() },
                                    modifier = Modifier.size(32.dp)
                                ) {
                                    Icon(Icons.Rounded.Close, contentDescription = "Remove attachment", tint = CordlyTextMuted)
                                }
                            }
                        }
                    }
                }

                // Active Unlimited Audio Recording Banner
                AnimatedVisibility(visible = isRecordingAudio) {
                    Surface(
                        color = NeovAiRed.copy(alpha = 0.22f),
                        border = BorderStroke(1.dp, NeovAiRed),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp)
                        ) {
                            Icon(Icons.Rounded.GraphicEq, contentDescription = null, tint = NeovAiYellow)
                            Spacer(modifier = Modifier.width(8.dp))
                            val sec = recordingDurationMs / 1000L
                            val durText = String.format(Locale.US, "%02d:%02d", sec / 60L, sec % 60L)
                            Text(
                                text = if (isArabic) "جاري تسجيل الصوت (بلا حد) $durText" else "Recording audio (unlimited) $durText",
                                style = MaterialTheme.typography.labelLarge,
                                color = CordlyTextPrimary,
                                modifier = Modifier.weight(1f)
                            )
                            Surface(
                                color = NeovAiRed,
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier
                                    .clip(RoundedCornerShape(10.dp))
                                    .clickable { isRecordingAudio = false }
                            ) {
                                Text(
                                    text = if (isArabic) "إيقاف وإرفاق" else "Stop",
                                    color = Color.White,
                                    style = MaterialTheme.typography.labelMedium,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                                )
                            }
                        }
                    }
                }

                // Multimodal Quick-Action Strip: Take Photo, Record Video, Gallery Photo, Gallery Video, Unlimited Audio
                Surface(
                    color = Color(0xFF140B0D),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp)) {
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .horizontalScroll(rememberScrollState())
                                .padding(bottom = 8.dp)
                        ) {
                            AiActionChip(
                                icon = Icons.Rounded.CameraAlt,
                                label = if (isArabic) "تصوير" else "Camera",
                                tint = NeovAiYellow,
                                testTag = "neov_ai_camera_photo_button",
                                onClick = {
                                    val hasCam = ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED
                                    if (hasCam) launchTakePhoto() else {
                                        pendingCameraAction = "photo"
                                        cameraPermsLauncher.launch(arrayOf(Manifest.permission.CAMERA))
                                    }
                                }
                            )
                            AiActionChip(
                                icon = Icons.Rounded.RadioButtonChecked,
                                label = if (isArabic) "تسجيل فيديو" else "Record Video",
                                tint = NeovAiRed,
                                testTag = "neov_ai_record_video_button",
                                onClick = {
                                    val hasCam = ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED
                                    val hasMic = ContextCompat.checkSelfPermission(context, Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED
                                    if (hasCam && hasMic) launchCaptureVideo() else {
                                        pendingCameraAction = "video"
                                        cameraPermsLauncher.launch(arrayOf(Manifest.permission.CAMERA, Manifest.permission.RECORD_AUDIO))
                                    }
                                }
                            )
                            AiActionChip(
                                icon = Icons.Rounded.Image,
                                label = if (isArabic) "صورة" else "Photo",
                                tint = CordlyEmerald,
                                testTag = "neov_ai_pick_photo_button",
                                onClick = {
                                    pickPhotoLauncher.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
                                }
                            )
                            AiActionChip(
                                icon = Icons.Rounded.VideoLibrary,
                                label = if (isArabic) "فيديو" else "Video",
                                tint = NeovAiAmber,
                                testTag = "neov_ai_pick_video_button",
                                onClick = {
                                    pickVideoLauncher.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.VideoOnly))
                                }
                            )
                            AiActionChip(
                                icon = Icons.Rounded.Videocam,
                                label = if (isArabic) "مكالمة لايف" else "Live Call",
                                tint = NeovAiCrimson,
                                testTag = "neov_ai_live_chip_button",
                                onClick = onOpenLiveCall
                            )
                        }

                        // Text Composer Row
                        val isBusy = uiState is NeovAiUiState.Loading || uiState is NeovAiUiState.Streaming
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            OutlinedTextField(
                                value = inputText,
                                onValueChange = { inputText = it },
                                placeholder = {
                                    Text(
                                        text = if (isArabic) "اسأل Neov AI أو أرفق صورة / فيديو / صوت..." else "Ask Neov AI anything...",
                                        color = CordlyTextMuted,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                },
                                maxLines = 4,
                                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Send),
                                keyboardActions = KeyboardActions(
                                    onSend = {
                                        if (!isBusy && (inputText.isNotBlank() || pendingAttachment != null)) {
                                            aiViewModel.sendChatMessage(inputText)
                                            inputText = ""
                                        }
                                    }
                                ),
                                shape = RoundedCornerShape(22.dp),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedContainerColor = CordlyCardSurface,
                                    unfocusedContainerColor = CordlyCardSurface,
                                    focusedBorderColor = NeovAiYellow,
                                    unfocusedBorderColor = CordlyDivider,
                                    focusedTextColor = CordlyTextPrimary,
                                    unfocusedTextColor = CordlyTextPrimary
                                ),
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("neov_ai_input")
                            )

                            Spacer(modifier = Modifier.width(6.dp))

                            // Unlimited Audio Record Button
                            IconButton(
                                onClick = {
                                    if (isRecordingAudio) {
                                        isRecordingAudio = false
                                    } else {
                                        val granted = ContextCompat.checkSelfPermission(
                                            context,
                                            Manifest.permission.RECORD_AUDIO
                                        ) == PackageManager.PERMISSION_GRANTED
                                        if (granted) startUnlimitedAudioCapture() else micPermLauncher.launch(Manifest.permission.RECORD_AUDIO)
                                    }
                                },
                                modifier = Modifier
                                    .size(48.dp)
                                    .clip(CircleShape)
                                    .background(if (isRecordingAudio) NeovAiRed else CordlyCardSurface)
                                    .testTag("neov_ai_audio_record_button")
                            ) {
                                Icon(
                                    imageVector = if (isRecordingAudio) Icons.Rounded.Stop else Icons.Rounded.Mic,
                                    contentDescription = if (isArabic) "تسجيل صوت" else "Record audio",
                                    tint = if (isRecordingAudio) Color.White else NeovAiYellow
                                )
                            }

                            Spacer(modifier = Modifier.width(6.dp))

                            val canSend = !isBusy && (inputText.isNotBlank() || pendingAttachment != null)
                            Surface(
                                color = if (canSend) NeovAiRed else CordlyCardSurface,
                                shape = CircleShape,
                                modifier = Modifier
                                    .size(48.dp)
                                    .clip(CircleShape)
                            ) {
                                IconButton(
                                    onClick = {
                                        if (canSend) {
                                            aiViewModel.sendChatMessage(inputText)
                                            inputText = ""
                                        }
                                    },
                                    enabled = canSend,
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .testTag("neov_ai_send_button")
                                ) {
                                    if (isBusy) {
                                        CircularProgressIndicator(
                                            color = NeovAiYellow,
                                            strokeWidth = 2.dp,
                                            modifier = Modifier.size(18.dp)
                                        )
                                    } else {
                                        Icon(
                                            imageVector = Icons.AutoMirrored.Rounded.Send,
                                            contentDescription = "Send to Neov AI",
                                            tint = if (canSend) NeovAiYellow else CordlyTextMuted
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

private fun NeovAiAttachment.previewBitmapOrNull() = bitmap

@Composable
private fun AiActionChip(
    icon: ImageVector,
    label: String,
    tint: Color,
    testTag: String,
    onClick: () -> Unit
) {
    Surface(
        color = tint.copy(alpha = 0.14f),
        shape = RoundedCornerShape(999.dp),
        border = BorderStroke(1.dp, tint.copy(alpha = 0.45f)),
        modifier = Modifier
            .clip(RoundedCornerShape(999.dp))
            .clickable(onClick = onClick)
            .minimumInteractiveComponentSize()
            .testTag(testTag)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 7.dp)
        ) {
            Icon(imageVector = icon, contentDescription = null, tint = tint, modifier = Modifier.size(16.dp))
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = label,
                style = MaterialTheme.typography.labelMedium,
                color = CordlyTextPrimary,
                fontWeight = FontWeight.SemiBold,
                maxLines = 1
            )
        }
    }
}

@Composable
private fun QuickAiStarterCard(
    icon: ImageVector,
    title: String,
    subtitle: String,
    accent: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        color = Color(0xFF191012),
        shape = RoundedCornerShape(18.dp),
        border = BorderStroke(1.dp, accent.copy(alpha = 0.5f)),
        modifier = modifier
            .clip(RoundedCornerShape(18.dp))
            .clickable(onClick = onClick)
            .minimumInteractiveComponentSize()
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Icon(imageVector = icon, contentDescription = null, tint = accent, modifier = Modifier.size(24.dp))
            Spacer(modifier = Modifier.height(8.dp))
            Text(text = title, style = MaterialTheme.typography.titleSmall, color = CordlyTextPrimary, fontWeight = FontWeight.Bold)
            Text(text = subtitle, style = MaterialTheme.typography.labelSmall, color = CordlyTextSecondary)
        }
    }
}

@Composable
private fun NeovAiMessageBubble(
    message: NeovAiChatMessage,
    isPlayingAudio: Boolean,
    onPlayAudioToggle: () -> Unit
) {
    Row(
        horizontalArrangement = if (message.isFromUser) Arrangement.End else Arrangement.Start,
        modifier = Modifier.fillMaxWidth()
    ) {
        if (!message.isFromUser) {
            NeovAiOrbGlyph(size = 30.dp, modifier = Modifier.padding(top = 2.dp))
            Spacer(modifier = Modifier.width(8.dp))
        }

        Surface(
            color = if (message.isFromUser) Color(0xFF281216) else CordlySurfaceDark,
            shape = RoundedCornerShape(18.dp),
            border = BorderStroke(
                1.dp,
                if (message.isFromUser) NeovAiRed.copy(alpha = 0.55f) else NeovAiYellow.copy(alpha = 0.35f)
            ),
            modifier = Modifier.widthIn(max = 310.dp)
        ) {
            Column(modifier = Modifier.padding(12.dp)) {
                message.previewBitmap?.let { bmp ->
                    Image(
                        bitmap = bmp.asImageBitmap(),
                        contentDescription = message.attachmentLabel ?: "Photo attachment",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(170.dp)
                            .clip(RoundedCornerShape(12.dp))
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                }
                if (message.audioWavBytes != null) {
                    val secs = ((message.mediaDurationMs ?: 1000L) / 1000L).coerceAtLeast(1L)
                    val durText = if (secs >= 60L) {
                        String.format(Locale.US, "%02d:%02d", secs / 60L, secs % 60L)
                    } else {
                        "${secs}s"
                    }
                    Surface(
                        color = if (isPlayingAudio) NeovAiRed.copy(alpha = 0.25f) else CordlyElevatedSurface,
                        shape = RoundedCornerShape(12.dp),
                        border = BorderStroke(1.dp, if (isPlayingAudio) NeovAiYellow else CordlyDivider),
                        modifier = Modifier
                            .clip(RoundedCornerShape(12.dp))
                            .clickable(onClick = onPlayAudioToggle)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 7.dp)
                        ) {
                            Icon(
                                imageVector = if (isPlayingAudio) Icons.Rounded.Stop else Icons.Rounded.PlayArrow,
                                contentDescription = "Play voice note",
                                tint = NeovAiYellow,
                                modifier = Modifier.size(20.dp)
                            )
                            Text(
                                text = "🎤 $durText",
                                style = MaterialTheme.typography.labelMedium,
                                color = CordlyTextPrimary,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                    if (message.text.isNotBlank() && !message.text.startsWith("🎤")) {
                        Spacer(modifier = Modifier.height(6.dp))
                    }
                }
                if (message.text.isNotBlank()) {
                    Text(
                        text = message.text,
                        style = MaterialTheme.typography.bodyMedium,
                        color = CordlyTextPrimary
                    )
                } else if (message.isStreaming) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        CircularProgressIndicator(color = NeovAiYellow, strokeWidth = 2.dp, modifier = Modifier.size(14.dp))
                        Text(text = "...", style = MaterialTheme.typography.bodyMedium, color = NeovAiYellow)
                    }
                }
            }
        }
    }
}
