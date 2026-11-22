package com.example.ui.cordly

import android.content.Context
import android.graphics.Bitmap
import android.net.Uri
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

private const val TAG = "NeovAiViewModel"

class NeovAiViewModel(
    private val repository: NeovAiRepository = NeovAiRepository()
) : ViewModel() {

    private val _uiState = MutableStateFlow<NeovAiUiState>(NeovAiUiState.Idle)
    val uiState: StateFlow<NeovAiUiState> = _uiState.asStateFlow()

    private val _messages = MutableStateFlow<List<NeovAiChatMessage>>(emptyList())
    val messages: StateFlow<List<NeovAiChatMessage>> = _messages.asStateFlow()

    private val _pendingAttachment = MutableStateFlow<NeovAiAttachment?>(null)
    val pendingAttachment: StateFlow<NeovAiAttachment?> = _pendingAttachment.asStateFlow()

    private val _isPreparingMedia = MutableStateFlow(false)
    val isPreparingMedia: StateFlow<Boolean> = _isPreparingMedia.asStateFlow()

    private val _liveTranscript = MutableStateFlow<List<NeovAiChatMessage>>(emptyList())
    val liveTranscript: StateFlow<List<NeovAiChatMessage>> = _liveTranscript.asStateFlow()

    private val _liveCallError = MutableStateFlow<String?>(null)
    val liveCallError: StateFlow<String?> = _liveCallError.asStateFlow()

    private val _isLiveThinking = MutableStateFlow(false)
    val isLiveThinking: StateFlow<Boolean> = _isLiveThinking.asStateFlow()

    private var activeChatJob: Job? = null
    private var activeLiveJob: Job? = null
    private var lastFailedPrompt: String? = null
    private var lastFailedAttachment: NeovAiAttachment? = null
    private var lastFailedPriorHistory: List<NeovAiChatMessage> = emptyList()

    fun warmUp() {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                com.google.firebase.appcheck.FirebaseAppCheck.getInstance().getAppCheckToken(false)
            } catch (_: Exception) {
            }
            try {
                repository.warmUp()
            } catch (_: Exception) {
            }
        }
    }

    fun attachBitmap(bitmap: Bitmap, label: String = "Photo") {
        _pendingAttachment.value = NeovAiAttachment(
            kind = NeovAttachmentKind.PHOTO,
            label = label,
            mimeType = "image/jpeg",
            bitmap = bitmap
        )
        clearErrorIfNeeded()
    }

    fun attachPhotoUri(context: Context, uri: Uri, label: String = "Photo") {
        viewModelScope.launch {
            _isPreparingMedia.value = true
            val bmp = NeovAiRepository.loadScaledBitmapFromUri(context, uri)
            _isPreparingMedia.value = false
            if (bmp != null) {
                attachBitmap(bmp, label)
            } else {
                _uiState.value = NeovAiUiState.Error("Could not read selected photo.")
            }
        }
    }

    fun attachVideoUri(context: Context, uri: Uri, label: String = "Video") {
        viewModelScope.launch {
            _isPreparingMedia.value = true
            val bytes = NeovAiRepository.readUriBytes(context, uri)
            val mime = context.contentResolver.getType(uri)?.takeIf { it.startsWith("video/") } ?: "video/mp4"
            _isPreparingMedia.value = false
            if (bytes != null && bytes.isNotEmpty()) {
                _pendingAttachment.value = NeovAiAttachment(
                    kind = NeovAttachmentKind.VIDEO,
                    label = "$label (${bytes.size / 1024} KB)",
                    mimeType = mime,
                    bytes = bytes
                )
                clearErrorIfNeeded()
            } else {
                _uiState.value = NeovAiUiState.Error("Video could not be attached or exceeds 18 MB limit.")
            }
        }
    }

    fun attachRecordedAudio(wavBytes: ByteArray, durationMs: Long, label: String) {
        _pendingAttachment.value = NeovAiAttachment(
            kind = NeovAttachmentKind.AUDIO,
            label = label,
            mimeType = "audio/wav",
            bytes = wavBytes,
            durationMs = durationMs
        )
        clearErrorIfNeeded()
    }

    fun clearAttachment() {
        _pendingAttachment.value = null
    }

    fun clearChat() {
        activeChatJob?.cancel()
        _messages.value = emptyList()
        _pendingAttachment.value = null
        lastFailedPrompt = null
        lastFailedAttachment = null
        lastFailedPriorHistory = emptyList()
        _uiState.value = NeovAiUiState.Idle
    }

    fun dismissError() {
        if (_uiState.value is NeovAiUiState.Error) {
            _uiState.value = NeovAiUiState.Idle
        }
        _liveCallError.value = null
    }

    fun retryLastMessage() {
        val retryPrompt = lastFailedPrompt ?: return
        val retryAttachment = lastFailedAttachment
        val priorHistory = lastFailedPriorHistory
        if (_uiState.value is NeovAiUiState.Loading || _uiState.value is NeovAiUiState.Streaming) return
        val retryUserMsg = _messages.value.lastOrNull { it.isFromUser }
        if (retryUserMsg == null) {
            _pendingAttachment.value = retryAttachment
            sendChatMessage(retryPrompt)
            return
        }
        val assistantMsg = NeovAiChatMessage(
            isFromUser = false,
            text = "",
            isStreaming = true
        )
        _messages.value = priorHistory + retryUserMsg + assistantMsg
        launchChatGeneration(
            priorHistory = priorHistory,
            prompt = retryPrompt,
            attachment = retryAttachment,
            assistantMsgId = assistantMsg.id
        )
    }

    fun sendChatMessage(rawPrompt: String) {
        val attachment = _pendingAttachment.value
        val prompt = rawPrompt.trim()
        if (prompt.isEmpty() && attachment == null) return
        if (_uiState.value is NeovAiUiState.Loading || _uiState.value is NeovAiUiState.Streaming) return

        val displayUserText = prompt.ifBlank {
            when (attachment?.kind) {
                NeovAttachmentKind.PHOTO -> "📷 ${attachment.label}"
                NeovAttachmentKind.VIDEO -> "🎥 ${attachment.label}"
                NeovAttachmentKind.AUDIO -> "🎤 ${attachment.label}"
                null -> ""
            }
        }

        val userMsg = NeovAiChatMessage(
            isFromUser = true,
            text = displayUserText,
            attachmentKind = attachment?.kind,
            previewBitmap = attachment?.bitmap,
            audioWavBytes = if (attachment?.kind == NeovAttachmentKind.AUDIO) attachment.bytes else null,
            mediaDurationMs = attachment?.durationMs,
            attachmentLabel = attachment?.label
        )
        val assistantMsg = NeovAiChatMessage(
            isFromUser = false,
            text = "",
            isStreaming = true
        )

        val priorHistory = _messages.value
        _messages.value = priorHistory + userMsg + assistantMsg
        _pendingAttachment.value = null
        lastFailedPrompt = prompt
        lastFailedAttachment = attachment
        lastFailedPriorHistory = priorHistory

        launchChatGeneration(
            priorHistory = priorHistory,
            prompt = prompt,
            attachment = attachment,
            assistantMsgId = assistantMsg.id
        )
    }

    private fun launchChatGeneration(
        priorHistory: List<NeovAiChatMessage>,
        prompt: String,
        attachment: NeovAiAttachment?,
        assistantMsgId: String
    ) {
        activeChatJob?.cancel()
        activeChatJob = viewModelScope.launch {
            _uiState.value = NeovAiUiState.Loading
            val accumulated = StringBuilder()
            try {
                repository.streamMultimodalTurn(
                    history = priorHistory,
                    promptText = prompt,
                    attachment = attachment
                ).collect { chunk ->
                    accumulated.append(chunk)
                    val currentText = accumulated.toString()
                    _uiState.value = NeovAiUiState.Streaming(currentText)
                    updateStreamingMessage(assistantMsgId, currentText, stillStreaming = true)
                }

                val finalText = accumulated.toString().trim()
                if (finalText.isNotBlank()) {
                    lastFailedPrompt = null
                    lastFailedAttachment = null
                    lastFailedPriorHistory = emptyList()
                    updateStreamingMessage(assistantMsgId, finalText, stillStreaming = false)
                    _uiState.value = NeovAiUiState.Success(finalText)
                } else {
                    removeMessageById(assistantMsgId)
                    _uiState.value = NeovAiUiState.Error("Gemini returned an empty response. Tap Retry.")
                }
            } catch (e: TimeoutCancellationException) {
                Log.e(TAG, "Neov AI streaming timed out", e)
                removeEmptyStreamingOrFinalize(assistantMsgId, accumulated.toString())
                _uiState.value = NeovAiUiState.Error("Gemini timed out. Please tap Retry.")
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                Log.e(TAG, "Neov AI inference failed", e)
                removeEmptyStreamingOrFinalize(assistantMsgId, accumulated.toString())
                _uiState.value = NeovAiUiState.Error(e.localizedMessage ?: "Neov AI request failed.")
            }
        }
    }

    fun startNewLiveCallSession() {
        activeLiveJob?.cancel()
        _liveTranscript.value = emptyList()
        _liveCallError.value = null
        _isLiveThinking.value = false
    }

    fun submitLiveCallTurn(
        spokenText: String?,
        spokenWavBytes: ByteArray?,
        cameraFrame: Bitmap?,
        preferArabic: Boolean,
        onReplyReady: (String) -> Unit
    ) {
        if (spokenText.isNullOrBlank() && spokenWavBytes == null && cameraFrame == null) return
        activeLiveJob?.cancel()
        activeLiveJob = viewModelScope.launch {
            _liveCallError.value = null
            _isLiveThinking.value = true
            val userLabel = spokenText?.trim()?.takeIf { it.isNotBlank() }
                ?: if (spokenWavBytes != null) {
                    if (preferArabic) "🎤 سؤال صوتي مباشر" else "🎤 Live voice question"
                } else {
                    if (preferArabic) "📷 فحص المشهد المباشر" else "📷 Live camera check"
                }

            val historySnapshot = _liveTranscript.value
            val userEntry = NeovAiChatMessage(
                isFromUser = true,
                text = userLabel,
                attachmentKind = if (cameraFrame != null) NeovAttachmentKind.PHOTO else null
            )
            _liveTranscript.value = historySnapshot + userEntry

            val result = repository.askLiveCallTurn(
                liveHistory = historySnapshot,
                spokenText = spokenText,
                spokenWavBytes = spokenWavBytes,
                cameraFrame = cameraFrame,
                preferArabic = preferArabic
            )
            _isLiveThinking.value = false
            result
                .onSuccess { reply ->
                    _liveTranscript.value = _liveTranscript.value + NeovAiChatMessage(
                        isFromUser = false,
                        text = reply
                    )
                    onReplyReady(reply)
                }
                .onFailure { err ->
                    _liveCallError.value = err.localizedMessage ?: "Neov AI Live turn failed."
                }
        }
    }

    fun endLiveCallSession() {
        activeLiveJob?.cancel()
        _isLiveThinking.value = false
    }

    private fun updateStreamingMessage(id: String, text: String, stillStreaming: Boolean) {
        _messages.value = _messages.value.map { msg ->
            if (msg.id == id) msg.copy(text = text, isStreaming = stillStreaming) else msg
        }
    }

    private fun removeMessageById(id: String) {
        _messages.value = _messages.value.filterNot { it.id == id }
    }

    private fun removeEmptyStreamingOrFinalize(id: String, text: String) {
        if (text.isBlank()) {
            removeMessageById(id)
        } else {
            updateStreamingMessage(id, text.trim(), stillStreaming = false)
        }
    }

    private fun clearErrorIfNeeded() {
        if (_uiState.value is NeovAiUiState.Error) {
            _uiState.value = NeovAiUiState.Idle
        }
    }
}
