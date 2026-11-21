package com.example.ui.cordly

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Matrix
import android.os.Bundle
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.AutoAwesome
import androidx.compose.material.icons.rounded.CallEnd
import androidx.compose.material.icons.rounded.Cameraswitch
import androidx.compose.material.icons.rounded.Mic
import androidx.compose.material.icons.rounded.MicOff
import androidx.compose.material.icons.rounded.Stop
import androidx.compose.material.icons.rounded.Videocam
import androidx.compose.material.icons.rounded.VideocamOff
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.core.content.ContextCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.ui.theme.CordlyCrimson
import com.example.ui.theme.CordlyEmerald
import com.example.ui.theme.CordlyTextPrimary
import com.example.ui.theme.CordlyTextSecondary
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.util.Locale
import java.util.concurrent.Executors
import java.util.concurrent.atomic.AtomicReference

/**
 * Gemini-Live-style voice + live CameraX video call experience for Neov AI.
 */
@Composable
fun NeovAiLiveCallOverlay(
    aiViewModel: NeovAiViewModel,
    onClose: () -> Unit
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val scope = rememberCoroutineScope()
    val isArabic = AppLanguageManager.resolveEffectiveCode(LocalSelectedLanguageCode.current).startsWith("ar")

    val liveTranscript by aiViewModel.liveTranscript.collectAsStateWithLifecycle()
    val liveError by aiViewModel.liveCallError.collectAsStateWithLifecycle()
    val isAiThinking by aiViewModel.isLiveThinking.collectAsStateWithLifecycle()

    var hasPermissions by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED &&
                ContextCompat.checkSelfPermission(context, Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED
        )
    }

    val permsLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { grants ->
        hasPermissions = (grants[Manifest.permission.CAMERA] == true ||
            ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) &&
            (grants[Manifest.permission.RECORD_AUDIO] == true ||
                ContextCompat.checkSelfPermission(context, Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED)
    }

    var isCameraEnabled by remember { mutableStateOf(true) }
    var useFrontCamera by remember { mutableStateOf(false) }
    var isMuted by remember { mutableStateOf(false) }
    var isSpeaking by remember { mutableStateOf(false) }
    var isSpeechListening by remember { mutableStateOf(false) }
    var isDirectRecording by remember { mutableStateOf(false) }
    var directRecordMs by remember { mutableLongStateOf(0L) }
    var speechAvailable by remember {
        mutableStateOf(SpeechRecognizer.isRecognitionAvailable(context))
    }
    var speechResetTick by remember { mutableIntStateOf(0) }

    val latestFrameRef = remember { AtomicReference<Bitmap?>(null) }
    val analysisExecutor = remember { Executors.newSingleThreadExecutor() }

    // Text-to-Speech engine for speaking Neov AI replies aloud
    var ttsReady by remember { mutableStateOf(false) }
    val tts = remember {
        var engine: TextToSpeech? = null
        engine = TextToSpeech(context) { status ->
            ttsReady = (status == TextToSpeech.SUCCESS)
        }
        engine
    }

    LaunchedEffect(ttsReady) {
        if (ttsReady) {
            tts.setOnUtteranceProgressListener(object : UtteranceProgressListener() {
                override fun onStart(utteranceId: String?) {
                    isSpeaking = true
                }

                override fun onDone(utteranceId: String?) {
                    isSpeaking = false
                    speechResetTick++
                }

                @Deprecated("Deprecated in Java")
                override fun onError(utteranceId: String?) {
                    isSpeaking = false
                    speechResetTick++
                }
            })
        }
    }

    fun speakOut(replyText: String) {
        if (!ttsReady) return
        val hasArabicChars = replyText.any { it in '\u0600'..'\u06FF' }
        val targetLocale = if (hasArabicChars) Locale("ar") else Locale.US
        val langSupport = tts.isLanguageAvailable(targetLocale)
        if (langSupport >= TextToSpeech.LANG_AVAILABLE) {
            tts.language = targetLocale
        }
        isSpeaking = true
        tts.speak(replyText, TextToSpeech.QUEUE_FLUSH, null, "neov_live_${System.currentTimeMillis()}")
    }

    fun grabLatestFrameSnapshot(): Bitmap? {
        if (!isCameraEnabled) return null
        val src = latestFrameRef.get() ?: return null
        return try {
            src.copy(Bitmap.Config.ARGB_8888, false)
        } catch (_: Exception) {
            null
        }
    }

    LaunchedEffect(Unit) {
        aiViewModel.startNewLiveCallSession()
        if (!hasPermissions) {
            permsLauncher.launch(arrayOf(Manifest.permission.CAMERA, Manifest.permission.RECORD_AUDIO))
        }
    }

    // SpeechRecognizer loop when available, unmuted, not direct-recording, and AI is not currently speaking/thinking
    val speechRecognizer = remember {
        if (SpeechRecognizer.isRecognitionAvailable(context)) {
            SpeechRecognizer.createSpeechRecognizer(context)
        } else {
            null
        }
    }

    DisposableEffect(speechRecognizer) {
        val listener = object : RecognitionListener {
            override fun onReadyForSpeech(params: Bundle?) {
                isSpeechListening = true
            }

            override fun onBeginningOfSpeech() {}
            override fun onRmsChanged(rmsdB: Float) {}
            override fun onBufferReceived(buffer: ByteArray?) {}
            override fun onEndOfSpeech() {
                isSpeechListening = false
            }

            override fun onError(error: Int) {
                isSpeechListening = false
                if (error == SpeechRecognizer.ERROR_INSUFFICIENT_PERMISSIONS ||
                    error == SpeechRecognizer.ERROR_RECOGNIZER_BUSY
                ) {
                    return
                }
                if (error == SpeechRecognizer.ERROR_SERVER_DISCONNECTED || error == 11) {
                    speechAvailable = false
                    return
                }
                if (!isMuted && !isSpeaking && !isAiThinking && !isDirectRecording) {
                    scope.launch {
                        delay(600L)
                        speechResetTick++
                    }
                }
            }

            override fun onResults(results: Bundle?) {
                isSpeechListening = false
                val spoken = results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)?.firstOrNull()?.trim()
                if (!spoken.isNullOrBlank()) {
                    tts.stop()
                    isSpeaking = false
                    aiViewModel.submitLiveCallTurn(
                        spokenText = spoken,
                        spokenWavBytes = null,
                        cameraFrame = grabLatestFrameSnapshot(),
                        preferArabic = isArabic,
                        onReplyReady = { reply -> speakOut(reply) }
                    )
                } else {
                    speechResetTick++
                }
            }

            override fun onPartialResults(partialResults: Bundle?) {}
            override fun onEvent(eventType: Int, params: Bundle?) {}
        }
        val lifecycleObserver = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_STOP) {
                isDirectRecording = false
                isSpeaking = false
                isSpeechListening = false
                try {
                    tts.stop()
                } catch (_: Exception) {
                }
                try {
                    speechRecognizer?.stopListening()
                } catch (_: Exception) {
                }
            }
        }
        lifecycleOwner.lifecycle.addObserver(lifecycleObserver)
        speechRecognizer?.setRecognitionListener(listener)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(lifecycleObserver)
            try {
                speechRecognizer?.destroy()
            } catch (_: Exception) {
            }
            try {
                tts.stop()
                tts.shutdown()
            } catch (_: Exception) {
            }
            analysisExecutor.shutdownNow()
            aiViewModel.endLiveCallSession()
        }
    }

    LaunchedEffect(hasPermissions, speechAvailable, isMuted, isSpeaking, isAiThinking, isDirectRecording, speechResetTick) {
        val rec = speechRecognizer ?: return@LaunchedEffect
        if (hasPermissions && speechAvailable && !isMuted && !isSpeaking && !isAiThinking && !isDirectRecording) {
            delay(350L)
            try {
                val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
                    putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
                    putExtra(RecognizerIntent.EXTRA_LANGUAGE, if (isArabic) "ar-SA" else Locale.getDefault().toLanguageTag())
                    putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 1)
                }
                rec.startListening(intent)
            } catch (_: Exception) {
                speechAvailable = false
            }
        } else {
            try {
                rec.stopListening()
            } catch (_: Exception) {
            }
            isSpeechListening = false
        }
    }

    Dialog(
        onDismissRequest = onClose,
        properties = DialogProperties(usePlatformDefaultWidth = false, decorFitsSystemWindows = false)
    ) {
        SanitizeDialogWindowIds()
        Surface(
            color = Color.Black,
            modifier = Modifier.fillMaxSize()
        ) {
            Box(modifier = Modifier.fillMaxSize()) {
                // Live CameraX Preview + Frame Grabber
                if (hasPermissions && isCameraEnabled) {
                    val previewView = remember(context) {
                        PreviewView(context).apply {
                            implementationMode = PreviewView.ImplementationMode.COMPATIBLE
                            scaleType = PreviewView.ScaleType.FILL_CENTER
                        }
                    }

                    DisposableEffect(lifecycleOwner, useFrontCamera, isCameraEnabled) {
                        val cameraProviderFuture = ProcessCameraProvider.getInstance(context)
                        val mainExecutor = ContextCompat.getMainExecutor(context)
                        var boundProvider: ProcessCameraProvider? = null
                        cameraProviderFuture.addListener({
                            try {
                                val provider = cameraProviderFuture.get()
                                boundProvider = provider
                                val preview = Preview.Builder().build().also {
                                    it.surfaceProvider = previewView.surfaceProvider
                                }
                                val analysis = ImageAnalysis.Builder()
                                    .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                                    .build()
                                    .also { ia ->
                                        ia.setAnalyzer(analysisExecutor) { proxy ->
                                            try {
                                                val rawBmp = proxy.toBitmap()
                                                val rot = proxy.imageInfo.rotationDegrees.toFloat()
                                                val maxEdge = 640f
                                                val scale = minOf(maxEdge / rawBmp.width, maxEdge / rawBmp.height, 1f)
                                                val matrix = Matrix().apply {
                                                    if (scale < 1f) postScale(scale, scale)
                                                    if (rot != 0f) postRotate(rot)
                                                }
                                                val finalBmp = Bitmap.createBitmap(
                                                    rawBmp, 0, 0, rawBmp.width, rawBmp.height, matrix, true
                                                )
                                                if (finalBmp != rawBmp) rawBmp.recycle()
                                                latestFrameRef.getAndSet(finalBmp)?.recycle()
                                            } catch (_: Exception) {
                                            } finally {
                                                proxy.close()
                                            }
                                        }
                                    }
                                val selector = if (useFrontCamera) {
                                    CameraSelector.DEFAULT_FRONT_CAMERA
                                } else {
                                    CameraSelector.DEFAULT_BACK_CAMERA
                                }
                                provider.unbindAll()
                                provider.bindToLifecycle(lifecycleOwner, selector, preview, analysis)
                            } catch (_: Exception) {
                            }
                        }, mainExecutor)

                        onDispose {
                            try {
                                boundProvider?.unbindAll()
                            } catch (_: Exception) {
                            }
                        }
                    }

                    AndroidView(
                        factory = { previewView },
                        modifier = Modifier.fillMaxSize()
                    )
                } else {
                    // Ambient Red & Yellow backdrop when camera is off or awaiting permissions
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(
                                Brush.radialGradient(
                                    colors = listOf(Color(0xFF3A0A12), Color(0xFF13080B), Color.Black)
                                )
                            )
                    )
                }

                // Subtle Dark Top/Bottom Scrims for Legibility
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            Brush.verticalGradient(
                                colors = listOf(
                                    Color.Black.copy(alpha = 0.72f),
                                    Color.Transparent,
                                    Color.Transparent,
                                    Color.Black.copy(alpha = 0.88f)
                                )
                            )
                        )
                )

                // Top Live Status Bar
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .align(Alignment.TopCenter)
                        .fillMaxWidth()
                        .statusBarsPadding()
                        .padding(horizontal = 16.dp, vertical = 12.dp)
                ) {
                    NeovAiOrbGlyph(
                        size = 44.dp,
                        pulseIntensity = if (isSpeaking || isAiThinking || isDirectRecording || isSpeechListening) 1.35f else 0.9f
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Text(
                                text = "Neov AI Live",
                                style = MaterialTheme.typography.titleMedium,
                                color = Color.White,
                                fontWeight = FontWeight.ExtraBold
                            )
                            Surface(
                                color = NeovAiRed,
                                shape = RoundedCornerShape(999.dp)
                            ) {
                                Text(
                                    text = if (isArabic) "مباشر" else "LIVE",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = NeovAiYellow,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                                )
                            }
                        }
                        val statusStr = when {
                            !hasPermissions -> if (isArabic) "يرجى السماح بالكاميرا والمايك" else "Allow camera & mic permissions"
                            isAiThinking -> if (isArabic) "Neov AI يرى ويفكر..." else "Neov AI is watching & thinking..."
                            isSpeaking -> if (isArabic) "Neov AI يتحدث الآن..." else "Neov AI is speaking..."
                            isDirectRecording -> {
                                val sec = directRecordMs / 1000L
                                if (isArabic) "تحدث الآن (${sec}ث) — اضغط إرسال" else "Speaking (${sec}s) — tap to send"
                            }
                            isSpeechListening -> if (isArabic) "يستمع إليك مباشرة..." else "Listening hands-free..."
                            isMuted -> if (isArabic) "الميكروفون مكتوم" else "Mic muted"
                            else -> if (isArabic) "جاهز • أره الفيديو أو تحدث معه" else "Ready • Show camera or talk"
                        }
                        Text(
                            text = statusStr,
                            style = MaterialTheme.typography.labelMedium,
                            color = NeovAiYellow,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }

                    if (!hasPermissions) {
                        Button(
                            onClick = {
                                permsLauncher.launch(arrayOf(Manifest.permission.CAMERA, Manifest.permission.RECORD_AUDIO))
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = NeovAiRed),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text(if (isArabic) "السماح" else "Grant", color = Color.White)
                        }
                    }
                }

                // Live Error Card
                AnimatedVisibility(
                    visible = liveError != null,
                    modifier = Modifier
                        .align(Alignment.Center)
                        .padding(horizontal = 20.dp)
                ) {
                    Surface(
                        color = CordlyCrimson.copy(alpha = 0.9f),
                        shape = RoundedCornerShape(16.dp),
                        border = BorderStroke(1.dp, NeovAiYellow)
                    ) {
                        Text(
                            text = liveError.orEmpty(),
                            color = Color.White,
                            style = MaterialTheme.typography.bodyMedium,
                            modifier = Modifier.padding(14.dp)
                        )
                    }
                }

                // Bottom Transcript + Gemini-Live-Style Action Controls
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .fillMaxWidth()
                        .navigationBarsPadding()
                        .padding(horizontal = 16.dp, vertical = 14.dp)
                ) {
                    // Live Caption Transcript Card
                    if (liveTranscript.isNotEmpty() || isAiThinking) {
                        Surface(
                            color = Color.Black.copy(alpha = 0.68f),
                            shape = RoundedCornerShape(18.dp),
                            border = BorderStroke(1.dp, NeovAiAmber.copy(alpha = 0.45f)),
                            modifier = Modifier
                                .fillMaxWidth()
                                .heightIn(max = 175.dp)
                        ) {
                            Column(
                                verticalArrangement = Arrangement.spacedBy(6.dp),
                                modifier = Modifier
                                    .verticalScroll(rememberScrollState())
                                    .padding(12.dp)
                            ) {
                                liveTranscript.takeLast(4).forEach { turn ->
                                    Text(
                                        text = (if (turn.isFromUser) "👤 " else "🔴🟡 Neov AI: ") + turn.text,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = if (turn.isFromUser) CordlyTextSecondary else CordlyTextPrimary,
                                        fontWeight = if (turn.isFromUser) FontWeight.Normal else FontWeight.SemiBold
                                    )
                                }
                                if (isAiThinking) {
                                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                        CircularProgressIndicator(color = NeovAiYellow, strokeWidth = 2.dp, modifier = Modifier.size(14.dp))
                                        Text(
                                            text = if (isArabic) "جاري تحليل الصوت والفيديو..." else "Analyzing audio & camera...",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = NeovAiYellow
                                        )
                                    }
                                }
                            }
                        }
                    }

                    // Primary Interactive Voice & Camera Prompt Pill
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Button(
                            onClick = {
                                tts.stop()
                                isSpeaking = false
                                if (isDirectRecording) {
                                    isDirectRecording = false
                                } else {
                                    if (!hasPermissions) {
                                        permsLauncher.launch(arrayOf(Manifest.permission.CAMERA, Manifest.permission.RECORD_AUDIO))
                                    } else {
                                        isDirectRecording = true
                                        directRecordMs = 0L
                                        scope.launch {
                                            val captured = VoiceNoteHelper.recordWavForAi(
                                                shouldContinue = { isDirectRecording },
                                                onDurationTick = { directRecordMs = it }
                                            )
                                            isDirectRecording = false
                                            if (captured != null) {
                                                aiViewModel.submitLiveCallTurn(
                                                    spokenText = null,
                                                    spokenWavBytes = captured.first,
                                                    cameraFrame = grabLatestFrameSnapshot(),
                                                    preferArabic = isArabic,
                                                    onReplyReady = { reply -> speakOut(reply) }
                                                )
                                            }
                                        }
                                    }
                                }
                            },
                            enabled = !isAiThinking,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (isDirectRecording) CordlyEmerald else NeovAiRed
                            ),
                            shape = RoundedCornerShape(999.dp),
                            modifier = Modifier
                                .height(50.dp)
                                .testTag("neov_live_talk_button")
                        ) {
                            Icon(
                                imageVector = if (isDirectRecording) Icons.Rounded.Stop else Icons.Rounded.Mic,
                                contentDescription = null,
                                tint = if (isDirectRecording) Color.Black else NeovAiYellow
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = if (isDirectRecording) {
                                    if (isArabic) "أرسل الصوت الآن" else "Send Voice"
                                } else {
                                    if (isArabic) "اسأل بصوتك" else "Speak Question"
                                },
                                color = if (isDirectRecording) Color.Black else Color.White,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        if (isCameraEnabled && hasPermissions) {
                            Button(
                                onClick = {
                                    tts.stop()
                                    isSpeaking = false
                                    aiViewModel.submitLiveCallTurn(
                                        spokenText = null,
                                        spokenWavBytes = null,
                                        cameraFrame = grabLatestFrameSnapshot(),
                                        preferArabic = isArabic,
                                        onReplyReady = { reply -> speakOut(reply) }
                                    )
                                },
                                enabled = !isAiThinking && !isDirectRecording,
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF281904)),
                                border = BorderStroke(1.dp, NeovAiYellow),
                                shape = RoundedCornerShape(999.dp),
                                modifier = Modifier
                                    .height(50.dp)
                                    .testTag("neov_live_show_camera_button")
                            ) {
                                Icon(Icons.Rounded.AutoAwesome, contentDescription = null, tint = NeovAiYellow, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(5.dp))
                                Text(
                                    text = if (isArabic) "انظر الآن" else "See This",
                                    color = NeovAiYellow,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }

                    // Bottom Control Dock: Mute / Camera / Flip / End Live Call
                    Surface(
                        color = Color(0xFF160E10).copy(alpha = 0.92f),
                        shape = RoundedCornerShape(999.dp),
                        border = BorderStroke(1.dp, NeovAiRed.copy(alpha = 0.45f))
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                        ) {
                            // Mute hands-free / stop TTS
                            IconButton(
                                onClick = {
                                    if (isSpeaking) {
                                        tts.stop()
                                        isSpeaking = false
                                    } else {
                                        isMuted = !isMuted
                                    }
                                },
                                modifier = Modifier
                                    .size(48.dp)
                                    .clip(CircleShape)
                                    .background(if (isMuted) NeovAiRed else Color(0xFF2A1C20))
                                    .minimumInteractiveComponentSize()
                            ) {
                                Icon(
                                    imageVector = if (isMuted) Icons.Rounded.MicOff else Icons.Rounded.Mic,
                                    contentDescription = if (isMuted) "Unmute" else "Mute",
                                    tint = Color.White
                                )
                            }

                            // Camera On / Off
                            IconButton(
                                onClick = { isCameraEnabled = !isCameraEnabled },
                                modifier = Modifier
                                    .size(48.dp)
                                    .clip(CircleShape)
                                    .background(if (isCameraEnabled) Color(0xFF2A1C20) else NeovAiRed)
                                    .minimumInteractiveComponentSize()
                            ) {
                                Icon(
                                    imageVector = if (isCameraEnabled) Icons.Rounded.Videocam else Icons.Rounded.VideocamOff,
                                    contentDescription = "Toggle camera",
                                    tint = if (isCameraEnabled) NeovAiYellow else Color.White
                                )
                            }

                            // Flip Camera
                            IconButton(
                                onClick = { useFrontCamera = !useFrontCamera },
                                enabled = isCameraEnabled,
                                modifier = Modifier
                                    .size(48.dp)
                                    .clip(CircleShape)
                                    .background(Color(0xFF2A1C20))
                                    .minimumInteractiveComponentSize()
                            ) {
                                Icon(
                                    imageVector = Icons.Rounded.Cameraswitch,
                                    contentDescription = "Flip camera",
                                    tint = Color.White
                                )
                            }

                            // End Live AI Call
                            IconButton(
                                onClick = {
                                    isDirectRecording = false
                                    tts.stop()
                                    onClose()
                                },
                                modifier = Modifier
                                    .size(52.dp)
                                    .clip(CircleShape)
                                    .background(CordlyCrimson)
                                    .minimumInteractiveComponentSize()
                                    .testTag("neov_live_end_call_button")
                            ) {
                                Icon(
                                    imageVector = Icons.Rounded.CallEnd,
                                    contentDescription = "End Live Call",
                                    tint = Color.White
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
