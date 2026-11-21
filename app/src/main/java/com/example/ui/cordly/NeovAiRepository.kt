package com.example.ui.cordly

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.media.ExifInterface
import android.net.Uri
import android.util.Log
import com.google.firebase.Firebase
import com.google.firebase.ai.GenerativeModel
import com.google.firebase.ai.ai
import com.google.firebase.ai.type.Content
import com.google.firebase.ai.type.content
import com.google.firebase.ai.type.generationConfig
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeout
import java.io.ByteArrayOutputStream
import java.util.UUID

private const val TAG = "NeovAiRepository"
private const val MAX_INLINE_MEDIA_BYTES = 18 * 1024 * 1024
private const val FAST_GEMINI_MODEL = "gemini-3.5-flash-lite"
private const val STANDARD_GEMINI_MODEL = "gemini-3.8-flash"

sealed interface NeovAiUiState {
    data object Idle : NeovAiUiState
    data object Loading : NeovAiUiState
    data class Streaming(val partialText: String) : NeovAiUiState
    data class Success(val lastResponse: String) : NeovAiUiState
    data class Error(val message: String) : NeovAiUiState
}

enum class NeovAttachmentKind {
    PHOTO,
    VIDEO,
    AUDIO
}

data class NeovAiAttachment(
    val id: String = UUID.randomUUID().toString(),
    val kind: NeovAttachmentKind,
    val label: String,
    val mimeType: String,
    val bitmap: Bitmap? = null,
    val bytes: ByteArray? = null,
    val durationMs: Long? = null
)

data class NeovAiChatMessage(
    val id: String = UUID.randomUUID().toString(),
    val isFromUser: Boolean,
    val text: String,
    val attachmentKind: NeovAttachmentKind? = null,
    val previewBitmap: Bitmap? = null,
    val audioWavBytes: ByteArray? = null,
    val mediaDurationMs: Long? = null,
    val attachmentLabel: String? = null,
    val isStreaming: Boolean = false
)

class NeovAiRepository {

    private fun buildChatModel(modelName: String): GenerativeModel =
        Firebase.ai.generativeModel(
            modelName = modelName,
            generationConfig = generationConfig {
                temperature = 0.7f
            },
            systemInstruction = content {
                text(
                    "You are Neov AI, a fast, helpful, intelligent, friendly multimodal AI assistant powered by Gemini inside neov chat. " +
                        "Respond naturally in the same language the user uses (Arabic, English, or any other language). " +
                        "Keep greetings and simple replies brief and direct. " +
                        "You can see photos, analyze recorded videos, listen to voice audio, and answer questions clearly."
                )
            }
        )

    private fun buildLiveModel(modelName: String): GenerativeModel =
        Firebase.ai.generativeModel(
            modelName = modelName,
            generationConfig = generationConfig {
                temperature = 0.7f
                maxOutputTokens = 260
            },
            systemInstruction = content {
                text(
                    "You are Neov AI powered by Gemini in a live real-time voice and camera video call. " +
                        "Reply concisely and naturally in 1 to 3 spoken sentences matching the user's language (Arabic or English). " +
                        "Do NOT use markdown formatting, bullet points, hashtags, or asterisks because your reply is read aloud immediately."
                )
            }
        )

    private val chatFastModel by lazy { buildChatModel(FAST_GEMINI_MODEL) }
    private val chatStandardModel by lazy { buildChatModel(STANDARD_GEMINI_MODEL) }

    private val liveFastModel by lazy { buildLiveModel(FAST_GEMINI_MODEL) }
    private val liveStandardModel by lazy { buildLiveModel(STANDARD_GEMINI_MODEL) }

    fun warmUp() {
        // Materialize lazy Gemini model handles off the critical send path.
        chatFastModel
        liveFastModel
    }

    private fun isTransientDemandOrNetworkError(error: Throwable): Boolean {
        if (error is TimeoutCancellationException) return true
        val name = error::class.simpleName.orEmpty().lowercase()
        val msg = (error.message.orEmpty() + " " + error.cause?.message.orEmpty()).lowercase()
        return name.contains("server") ||
            name.contains("unavailable") ||
            name.contains("quota") ||
            msg.contains("high demand") ||
            msg.contains("spikes in demand") ||
            msg.contains("overloaded") ||
            msg.contains("unavailable") ||
            msg.contains("try again later") ||
            msg.contains("resource exhausted") ||
            msg.contains("resource_exhausted") ||
            msg.contains("quota") ||
            msg.contains("rate limit") ||
            msg.contains("503") ||
            msg.contains("502") ||
            msg.contains("504") ||
            msg.contains("500") ||
            msg.contains("429") ||
            msg.contains("deadline") ||
            msg.contains("timed out")
    }

    private fun streamWithFirstTokenTimeout(
        model: GenerativeModel,
        contents: List<Content>,
        firstTokenTimeoutMs: Long,
        totalTimeoutMs: Long = 45_000L
    ): Flow<String> = flow {
        coroutineScope {
            val chunkChannel = Channel<Result<String>>(Channel.UNLIMITED)
            val producerJob = launch {
                try {
                    withTimeout(totalTimeoutMs) {
                        model.generateContentStream(contents).collect { chunk ->
                            val part = chunk.text.orEmpty()
                            if (part.isNotEmpty()) {
                                chunkChannel.trySend(Result.success(part))
                            }
                        }
                    }
                    chunkChannel.close()
                } catch (e: CancellationException) {
                    if (e is TimeoutCancellationException) {
                        chunkChannel.trySend(Result.failure(e))
                        chunkChannel.close()
                    } else {
                        chunkChannel.close(e)
                        throw e
                    }
                } catch (e: Exception) {
                    chunkChannel.trySend(Result.failure(e))
                    chunkChannel.close()
                }
            }

            try {
                val firstResult = withTimeout(firstTokenTimeoutMs) {
                    chunkChannel.receiveCatching().getOrNull()
                } ?: throw IllegalStateException("Gemini returned an empty response.")

                emit(firstResult.getOrThrow())
                for (nextResult in chunkChannel) {
                    emit(nextResult.getOrThrow())
                }
            } finally {
                producerJob.cancel()
                chunkChannel.close()
            }
        }
    }

    fun streamMultimodalTurn(
        history: List<NeovAiChatMessage>,
        promptText: String,
        attachment: NeovAiAttachment?
    ): Flow<String> = flow {
        val contents = mutableListOf<Content>()
        // Include recent conversation turns as plain text context
        history.takeLast(8).forEach { msg ->
            if (!msg.isStreaming && msg.text.isNotBlank()) {
                contents.add(
                    content(if (msg.isFromUser) "user" else "model") {
                        text(msg.text)
                    }
                )
            }
        }

        val currentContent = content("user") {
            when (attachment?.kind) {
                NeovAttachmentKind.PHOTO -> {
                    attachment.bitmap?.let { bmp ->
                        image(bmp)
                    }
                }
                NeovAttachmentKind.VIDEO -> {
                    attachment.bytes?.let { raw ->
                        if (raw.size > MAX_INLINE_MEDIA_BYTES) {
                            throw IllegalArgumentException("Video exceeds 18 MB limit for AI analysis. Record a shorter video.")
                        }
                        inlineData(raw, attachment.mimeType.ifBlank { "video/mp4" })
                    }
                }
                NeovAttachmentKind.AUDIO -> {
                    attachment.bytes?.let { raw ->
                        if (raw.size > MAX_INLINE_MEDIA_BYTES) {
                            throw IllegalArgumentException("Audio exceeds 18 MB limit.")
                        }
                        inlineData(raw, attachment.mimeType.ifBlank { "audio/wav" })
                    }
                }
                null -> Unit
            }

            val effectivePrompt = promptText.trim().ifBlank {
                when (attachment?.kind) {
                    NeovAttachmentKind.PHOTO -> "What is in this photo? Describe and help me with it."
                    NeovAttachmentKind.VIDEO -> "Watch this video carefully and describe or answer what happens in it."
                    NeovAttachmentKind.AUDIO -> "Listen to this audio carefully and respond directly in the same language."
                    null -> "Hello!"
                }
            }
            text(effectivePrompt)
        }
        contents.add(currentContent)

        val hasHeavyMedia = attachment?.kind == NeovAttachmentKind.VIDEO || attachment?.kind == NeovAttachmentKind.AUDIO
        val firstTokenMs = if (hasHeavyMedia) 18_000L else 9_000L

        val candidates = listOf(
            Triple(FAST_GEMINI_MODEL, chatFastModel, true),
            Triple(STANDARD_GEMINI_MODEL, chatStandardModel, true),
            Triple("$FAST_GEMINI_MODEL (unary)", chatFastModel, false),
            Triple("$STANDARD_GEMINI_MODEL (unary)", chatStandardModel, false)
        )

        var lastError: Exception? = null
        var delivered = false

        for ((index, candidate) in candidates.withIndex()) {
            val (label, model, useStream) = candidate
            var emittedAny = false
            try {
                if (index > 0) {
                    delay(if (index == 1) 120L else 400L)
                }
                if (useStream) {
                    streamWithFirstTokenTimeout(
                        model = model,
                        contents = contents,
                        firstTokenTimeoutMs = firstTokenMs,
                        totalTimeoutMs = 45_000L
                    ).collect { part ->
                        emittedAny = true
                        emit(part)
                    }
                    if (emittedAny) {
                        delivered = true
                        break
                    }
                } else {
                    val response = withTimeout(30_000L) {
                        model.generateContent(contents)
                    }
                    val text = response.text.orEmpty()
                    if (text.isNotBlank()) {
                        emittedAny = true
                        emit(text)
                        delivered = true
                        break
                    }
                }
            } catch (e: CancellationException) {
                if (e is TimeoutCancellationException && !emittedAny && index < candidates.lastIndex) {
                    Log.w(TAG, "Gemini model $label timed out waiting for first token; switching to fallback model.", e)
                    lastError = e
                    continue
                }
                throw e
            } catch (e: Exception) {
                lastError = e
                if (!emittedAny && index < candidates.lastIndex && isTransientDemandOrNetworkError(e)) {
                    Log.w(TAG, "Gemini model $label hit demand/transient error (${e.message}); trying fallback.", e)
                    continue
                }
                throw e
            }
        }

        if (!delivered) {
            throw (lastError ?: IllegalStateException("Gemini returned an empty response."))
        }
    }

    suspend fun askLiveCallTurn(
        liveHistory: List<NeovAiChatMessage>,
        spokenText: String?,
        spokenWavBytes: ByteArray?,
        cameraFrame: Bitmap?,
        preferArabic: Boolean
    ): Result<String> {
        val contents = mutableListOf<Content>()
        liveHistory.takeLast(6).forEach { msg ->
            if (msg.text.isNotBlank()) {
                contents.add(
                    content(if (msg.isFromUser) "user" else "model") {
                        text(msg.text)
                    }
                )
            }
        }

        val userTurn = content("user") {
            if (cameraFrame != null) {
                image(cameraFrame)
            }
            if (spokenWavBytes != null && spokenWavBytes.isNotEmpty()) {
                if (spokenWavBytes.size <= MAX_INLINE_MEDIA_BYTES) {
                    inlineData(spokenWavBytes, "audio/wav")
                }
            }
            val cue = when {
                !spokenText.isNullOrBlank() -> spokenText.trim()
                spokenWavBytes != null -> {
                    if (cameraFrame != null) {
                        if (preferArabic) {
                            "استمع إلى صوتي وانظر إلى بث الكاميرا المباشر الآن، ثم أجبني مباشرة باختصار."
                        } else {
                            "Listen to what I just said and look at my live camera frame, then reply concisely."
                        }
                    } else {
                        if (preferArabic) {
                            "استمع إلى صوتي وأجبني مباشرة باختصار."
                        } else {
                            "Listen to my voice question and answer concisely."
                        }
                    }
                }
                cameraFrame != null -> {
                    if (preferArabic) {
                        "انظر إلى ما أريك إياه عبر الكاميرا الآن وأخبرني باختصار ماذا ترى."
                    } else {
                        "Look at what I am showing you on my live camera right now and briefly tell me what you see."
                    }
                }
                else -> "Hello"
            }
            text(cue)
        }
        contents.add(userTurn)

        val liveAttempts = listOf(
            FAST_GEMINI_MODEL to liveFastModel,
            STANDARD_GEMINI_MODEL to liveStandardModel,
            "$FAST_GEMINI_MODEL-retry" to liveFastModel
        )

        var lastError: Throwable? = null
        for ((index, attempt) in liveAttempts.withIndex()) {
            val (label, model) = attempt
            try {
                if (index > 0) {
                    delay(if (index == 1) 120L else 400L)
                }
                val response = withTimeout(30_000L) {
                    model.generateContent(contents)
                }
                val text = response.text?.trim()
                if (!text.isNullOrBlank()) {
                    return Result.success(text)
                }
                lastError = IllegalStateException("Gemini returned an empty response.")
            } catch (e: TimeoutCancellationException) {
                Log.w(TAG, "Live Gemini turn ($label) timed out", e)
                lastError = IllegalStateException("Neov AI timed out. Please try again.")
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                Log.w(TAG, "Live Gemini turn ($label) failed: ${e.message}", e)
                lastError = e
                if (!isTransientDemandOrNetworkError(e)) {
                    return Result.failure(e)
                }
            }
        }
        return Result.failure(lastError ?: IllegalStateException("Neov AI request failed."))
    }

    companion object {
        suspend fun loadScaledBitmapFromUri(
            context: Context,
            uri: Uri,
            maxEdge: Int = 1280
        ): Bitmap? = withContext(Dispatchers.IO) {
            try {
                val rawBytes = context.contentResolver.openInputStream(uri)?.use { it.readBytes() }
                    ?: return@withContext null
                decodeScaledBitmapFromBytes(rawBytes, maxEdge)
            } catch (e: Exception) {
                Log.e(TAG, "Failed to decode bitmap from uri", e)
                null
            }
        }

        fun decodeScaledBitmapFromBytes(
            bytes: ByteArray,
            maxEdge: Int = 1280
        ): Bitmap? {
            return try {
                val opts = BitmapFactory.Options().apply { inJustDecodeBounds = true }
                BitmapFactory.decodeByteArray(bytes, 0, bytes.size, opts)
                var sample = 1
                val largest = maxOf(opts.outWidth, opts.outHeight)
                while (largest / sample > maxEdge * 2) {
                    sample *= 2
                }
                val decodeOpts = BitmapFactory.Options().apply { inSampleSize = sample }
                val bmp = BitmapFactory.decodeByteArray(bytes, 0, bytes.size, decodeOpts) ?: return null

                val rotation = try {
                    val exif = ExifInterface(bytes.inputStream())
                    when (exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL)) {
                        ExifInterface.ORIENTATION_ROTATE_90 -> 90f
                        ExifInterface.ORIENTATION_ROTATE_180 -> 180f
                        ExifInterface.ORIENTATION_ROTATE_270 -> 270f
                        else -> 0f
                    }
                } catch (_: Exception) {
                    0f
                }

                val ratio = minOf(maxEdge.toFloat() / bmp.width, maxEdge.toFloat() / bmp.height, 1f)
                if (ratio < 1f || rotation != 0f) {
                    val m = Matrix().apply {
                        if (ratio < 1f) postScale(ratio, ratio)
                        if (rotation != 0f) postRotate(rotation)
                    }
                    Bitmap.createBitmap(bmp, 0, 0, bmp.width, bmp.height, m, true).also {
                        if (it != bmp) bmp.recycle()
                    }
                } else {
                    bmp
                }
            } catch (e: Exception) {
                Log.e(TAG, "Failed scaling bitmap", e)
                null
            }
        }

        suspend fun readUriBytes(
            context: Context,
            uri: Uri,
            maxBytes: Int = MAX_INLINE_MEDIA_BYTES
        ): ByteArray? = withContext(Dispatchers.IO) {
            try {
                context.contentResolver.openInputStream(uri)?.use { input ->
                    val out = ByteArrayOutputStream()
                    val buf = ByteArray(16_384)
                    var total = 0
                    while (true) {
                        val read = input.read(buf)
                        if (read <= 0) break
                        total += read
                        if (total > maxBytes) {
                            return@withContext null
                        }
                        out.write(buf, 0, read)
                    }
                    out.toByteArray()
                }
            } catch (e: Exception) {
                Log.e(TAG, "Failed reading media bytes", e)
                null
            }
        }
    }
}
