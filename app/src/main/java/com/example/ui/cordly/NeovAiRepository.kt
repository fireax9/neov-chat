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
import com.google.firebase.ai.type.GenerativeBackend
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

    private val chatSystemInstruction by lazy {
        content {
            text(
                "You are Neov AI, a fast, helpful, intelligent, friendly multimodal AI assistant powered by Gemini inside neov chat. " +
                    "Respond naturally in the same language the user uses (Arabic, English, or any other language). " +
                    "Keep greetings and simple replies brief and direct. " +
                    "You can see photos, analyze recorded videos, listen to voice audio, and answer questions clearly."
            )
        }
    }

    private val liveSystemInstruction by lazy {
        content {
            text(
                "You are Neov AI powered by Gemini in a live real-time voice and camera video call. " +
                    "Reply concisely and naturally in 1 to 3 spoken sentences matching the user's language (Arabic or English). " +
                    "Do NOT use markdown formatting, bullet points, hashtags, or asterisks because your reply is read aloud immediately."
            )
        }
    }

    private fun buildChatModel(
        modelName: String,
        backend: GenerativeBackend = GenerativeBackend.googleAI()
    ): GenerativeModel =
        Firebase.ai(backend = backend).generativeModel(
            modelName = modelName,
            generationConfig = generationConfig {
                temperature = 0.7f
            },
            systemInstruction = chatSystemInstruction
        )

    private fun buildLiveModel(
        modelName: String,
        backend: GenerativeBackend = GenerativeBackend.googleAI()
    ): GenerativeModel =
        Firebase.ai(backend = backend).generativeModel(
            modelName = modelName,
            generationConfig = generationConfig {
                temperature = 0.7f
                maxOutputTokens = 260
            },
            systemInstruction = liveSystemInstruction
        )

    private val chatFastModel by lazy { buildChatModel(FAST_GEMINI_MODEL) }
    private val chatStandardModel by lazy { buildChatModel(STANDARD_GEMINI_MODEL) }
    private val chatVertexFallbackModel by lazy { buildChatModel(STANDARD_GEMINI_MODEL, GenerativeBackend.vertexAI()) }

    private val liveFastModel by lazy { buildLiveModel(FAST_GEMINI_MODEL) }
    private val liveStandardModel by lazy { buildLiveModel(STANDARD_GEMINI_MODEL) }
    private val liveVertexFallbackModel by lazy { buildLiveModel(STANDARD_GEMINI_MODEL, GenerativeBackend.vertexAI()) }

    fun warmUp() {
        // Materialize lazy Gemini model handles off the critical send path.
        chatFastModel
        liveFastModel
    }

    private fun isProjectBillingOrUnavailableError(error: Throwable): Boolean {
        val chain = generateSequence(error) { it.cause }
        return chain.any { t ->
            val msg = (t.message.orEmpty() + " " + t.toString()).lowercase()
            msg.contains("free tier is not available") ||
                msg.contains("set up billing") ||
                msg.contains("project-unavailable") ||
                msg.contains("billing")
        }
    }

    private fun isNonRetryableClientError(error: Throwable): Boolean {
        if (error is IllegalArgumentException) return true
        if (isProjectBillingOrUnavailableError(error)) return true
        val chain = generateSequence(error) { it.cause }
        return chain.any { t ->
            val msg = (t.message.orEmpty() + " " + t.toString()).lowercase()
            msg.contains("18 mb") ||
                msg.contains("invalid argument") ||
                msg.contains("api key not valid") ||
                msg.contains("permission denied") ||
                msg.contains("forbidden")
        }
    }

    private fun isTransientDemandOrNetworkError(error: Throwable): Boolean {
        if (isNonRetryableClientError(error)) return false
        if (error is TimeoutCancellationException) return true
        val chain = generateSequence(error) { it.cause }
        return chain.any { t ->
            val name = t::class.simpleName.orEmpty().lowercase()
            val msg = (t.message.orEmpty() + " " + t.toString()).lowercase()
            name.contains("server") ||
                name.contains("unavailable") ||
                name.contains("quota") ||
                name.contains("firebaseai") ||
                name.contains("unknownhost") ||
                name.contains("sockettimeout") ||
                name.contains("connect") ||
                name.contains("ioexception") ||
                msg.contains("high demand") ||
                msg.contains("spikes in demand") ||
                msg.contains("overloaded") ||
                msg.contains("unavailable") ||
                msg.contains("try again") ||
                msg.contains("temporarily") ||
                msg.contains("busy") ||
                msg.contains("resource") ||
                msg.contains("quota") ||
                msg.contains("rate") ||
                msg.contains("503") ||
                msg.contains("502") ||
                msg.contains("504") ||
                msg.contains("500") ||
                msg.contains("429") ||
                msg.contains("deadline") ||
                msg.contains("timed out") ||
                msg.contains("timeout") ||
                msg.contains("empty response") ||
                msg.contains("network") ||
                msg.contains("socket") ||
                msg.contains("connection") ||
                msg.contains("internal") ||
                msg.contains("app check") ||
                msg.contains("appcheck")
        }
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

    private fun buildNormalizedHistory(
        history: List<NeovAiChatMessage>,
        maxTurns: Int
    ): List<Content> {
        val cleaned = mutableListOf<Pair<String, String>>()
        history.takeLast(maxTurns).forEach { msg ->
            val trimmed = msg.text.trim()
            if (!msg.isStreaming && trimmed.isNotEmpty()) {
                val role = if (msg.isFromUser) "user" else "model"
                if (cleaned.lastOrNull()?.first == role) {
                    val merged = cleaned.last().second + "\n" + trimmed
                    cleaned[cleaned.lastIndex] = role to merged
                } else {
                    cleaned.add(role to trimmed)
                }
            }
        }
        // Current turn is always "user", so ensure history does not end on an unanswered "user" turn
        if (cleaned.lastOrNull()?.first == "user") {
            cleaned.removeAt(cleaned.lastIndex)
        }
        if (cleaned.firstOrNull()?.first == "model") {
            cleaned.removeAt(0)
        }
        return cleaned.map { (role, text) ->
            content(role) { text(text) }
        }
    }

    fun streamMultimodalTurn(
        history: List<NeovAiChatMessage>,
        promptText: String,
        attachment: NeovAiAttachment?
    ): Flow<String> = flow {
        val contents = buildNormalizedHistory(history, maxTurns = 8).toMutableList()

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
        val firstTokenMs = if (hasHeavyMedia) 24_000L else 12_000L

        val candidates = listOf(
            Triple(FAST_GEMINI_MODEL, chatFastModel, true),
            Triple(STANDARD_GEMINI_MODEL, chatStandardModel, true),
            Triple("$FAST_GEMINI_MODEL (unary)", chatFastModel, false),
            Triple("$STANDARD_GEMINI_MODEL (unary)", chatStandardModel, false),
            Triple("$STANDARD_GEMINI_MODEL (unary-retry)", chatStandardModel, false)
        )

        var lastError: Exception? = null
        var delivered = false

        for ((index, candidate) in candidates.withIndex()) {
            val (label, model, useStream) = candidate
            var emittedAny = false
            try {
                if (index > 0) {
                    val backoffMs = when (index) {
                        1 -> 250L
                        2 -> 750L
                        3 -> 1250L
                        else -> 1800L
                    }
                    delay(backoffMs)
                }
                if (useStream) {
                    streamWithFirstTokenTimeout(
                        model = model,
                        contents = contents,
                        firstTokenTimeoutMs = firstTokenMs,
                        totalTimeoutMs = 55_000L
                    ).collect { part ->
                        emittedAny = true
                        emit(part)
                    }
                    if (emittedAny) {
                        delivered = true
                        break
                    }
                } else {
                    val response = withTimeout(35_000L) {
                        model.generateContent(contents)
                    }
                    val text = response.text.orEmpty()
                    if (text.isNotBlank()) {
                        emittedAny = true
                        emit(text)
                        delivered = true
                        break
                    }
                    throw IllegalStateException("Gemini returned an empty response.")
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
                if (!emittedAny && isProjectBillingOrUnavailableError(e)) {
                    try {
                        var vertexEmitted = false
                        streamWithFirstTokenTimeout(
                            model = chatVertexFallbackModel,
                            contents = contents,
                            firstTokenTimeoutMs = firstTokenMs,
                            totalTimeoutMs = 45_000L
                        ).collect { part ->
                            vertexEmitted = true
                            emit(part)
                        }
                        if (vertexEmitted) {
                            delivered = true
                            break
                        }
                    } catch (_: CancellationException) {
                    } catch (vertexErr: Exception) {
                        Log.w(TAG, "Vertex AI fallback also unavailable (${vertexErr.message})")
                    }
                    throw e
                }
                if (!emittedAny && index < candidates.lastIndex && !isNonRetryableClientError(e)) {
                    Log.w(TAG, "Gemini model $label failed (${e.message}); trying next fallback.", e)
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
        val contents = buildNormalizedHistory(liveHistory, maxTurns = 6).toMutableList()

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
            "$FAST_GEMINI_MODEL-retry" to liveFastModel,
            "$STANDARD_GEMINI_MODEL-retry" to liveStandardModel
        )

        var lastError: Throwable? = null
        for ((index, attempt) in liveAttempts.withIndex()) {
            val (label, model) = attempt
            try {
                if (index > 0) {
                    delay(when (index) { 1 -> 250L; 2 -> 750L; else -> 1250L })
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
                if (isProjectBillingOrUnavailableError(e)) {
                    try {
                        val vertexResp = withTimeout(25_000L) {
                            liveVertexFallbackModel.generateContent(contents)
                        }
                        val vertexText = vertexResp.text?.trim()
                        if (!vertexText.isNullOrBlank()) {
                            return Result.success(vertexText)
                        }
                    } catch (vertexErr: Exception) {
                        Log.w(TAG, "Live Vertex AI fallback also unavailable (${vertexErr.message})")
                    }
                    return Result.failure(e)
                }
                if (isNonRetryableClientError(e)) {
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
