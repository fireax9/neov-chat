package com.example.ui.cordly

import android.content.Context
import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioTrack
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlin.math.exp
import kotlin.math.sin

enum class AppSoundEffect {
    ENTRANCE_CHIME,
    MESSAGE_IN,
    MESSAGE_OUT,
    CALL_CONNECT,
    CALL_END,
    OUTGOING_RINGBACK,
    INCOMING_RINGTONE,
    NEW_ACCOUNT_CELEBRATION,
    BONUS_SPARKLE
}

enum class SoundCategory {
    STARTUP,
    RINGTONE,
    MESSAGE,
    CALL_EVENT
}

enum class SoundWaveform {
    PURE,
    WARM_CHIME,
    BRIGHT_CRYSTAL,
    RETRO_SYNTH,
    DEEP_BELL
}

data class SoundPreset(
    val id: String,
    val category: SoundCategory,
    val titleEn: String,
    val titleAr: String,
    val notesHz: List<Double>,
    val noteMs: Int,
    val volume: Double = 0.24,
    val waveform: SoundWaveform = SoundWaveform.WARM_CHIME
)

object SoundPresetCatalog {
    val startupPresets: List<SoundPreset> = listOf(
        SoundPreset("startup_classic", SoundCategory.STARTUP, "Classic Studio", "استوديو كلاسيك", listOf(440.0, 554.37, 659.25, 880.0), 110, 0.23, SoundWaveform.WARM_CHIME),
        SoundPreset("startup_crystal", SoundCategory.STARTUP, "Crystal Dawn", "فجر الكريستال", listOf(523.25, 659.25, 783.99, 1046.50, 1318.51), 95, 0.24, SoundWaveform.BRIGHT_CRYSTAL),
        SoundPreset("startup_cyber", SoundCategory.STARTUP, "Cyber Boot", "إقلاع سيبراني", listOf(329.63, 493.88, 659.25, 987.77, 1318.51), 85, 0.23, SoundWaveform.RETRO_SYNTH),
        SoundPreset("startup_royal", SoundCategory.STARTUP, "Royal Fanfare", "استقبال ملكي", listOf(392.00, 523.25, 659.25, 783.99, 1046.50), 105, 0.25, SoundWaveform.DEEP_BELL),
        SoundPreset("startup_velvet", SoundCategory.STARTUP, "Velvet Lo-Fi", "نغمة مخملية", listOf(349.23, 440.00, 523.25, 659.25), 125, 0.22, SoundWaveform.PURE),
        SoundPreset("startup_arcade", SoundCategory.STARTUP, "Arcade 8-Bit", "أركيد ريترو", listOf(523.25, 659.25, 783.99, 1046.50, 783.99, 1046.50), 75, 0.22, SoundWaveform.RETRO_SYNTH),
        SoundPreset("startup_cosmos", SoundCategory.STARTUP, "Space Drift", "نبض الفضاء", listOf(261.63, 392.00, 587.33, 880.00, 1174.66), 110, 0.24, SoundWaveform.DEEP_BELL),
        SoundPreset("startup_emerald", SoundCategory.STARTUP, "Emerald Dew", "ندى زمردي", listOf(587.33, 739.99, 880.00, 1174.66), 95, 0.24, SoundWaveform.BRIGHT_CRYSTAL),
        SoundPreset("startup_sunset", SoundCategory.STARTUP, "Golden Horizon", "أفق ذهبي", listOf(440.00, 523.25, 659.25, 783.99, 880.00), 95, 0.23, SoundWaveform.WARM_CHIME),
        SoundPreset("startup_neon", SoundCategory.STARTUP, "Neon Surge", "وميض النيون", listOf(659.25, 830.61, 987.77, 1318.51, 1661.22), 80, 0.23, SoundWaveform.BRIGHT_CRYSTAL)
    )

    val ringtonePresets: List<SoundPreset> = listOf(
        SoundPreset("ring_classic", SoundCategory.RINGTONE, "Neov Ring", "رنين نيو الكلاسيكي", listOf(659.25, 880.0, 659.25, 987.77), 140, 0.26, SoundWaveform.WARM_CHIME),
        SoundPreset("ring_horizon", SoundCategory.RINGTONE, "Horizon Bells", "أجراس الأفق", listOf(783.99, 987.77, 1174.66, 987.77, 1318.51), 120, 0.26, SoundWaveform.DEEP_BELL),
        SoundPreset("ring_pulse", SoundCategory.RINGTONE, "Cyber Pulse", "نبض سيبراني", listOf(587.33, 880.00, 587.33, 1046.50, 880.00), 115, 0.25, SoundWaveform.RETRO_SYNTH),
        SoundPreset("ring_marimba", SoundCategory.RINGTONE, "Tropical Marimba", "ماريمبا استوائية", listOf(523.25, 659.25, 783.99, 1046.50, 783.99), 110, 0.26, SoundWaveform.WARM_CHIME),
        SoundPreset("ring_galaxy", SoundCategory.RINGTONE, "Galaxy Echo", "صدى المجرة", listOf(440.00, 659.25, 880.00, 1318.51, 880.00), 130, 0.25, SoundWaveform.BRIGHT_CRYSTAL),
        SoundPreset("ring_retro", SoundCategory.RINGTONE, "Retro Phone", "هاتف ريترو", listOf(880.0, 1108.73, 880.0, 1108.73, 880.0), 95, 0.24, SoundWaveform.RETRO_SYNTH),
        SoundPreset("ring_aurora", SoundCategory.RINGTONE, "Aurora Harp", "قيثارة الشفق", listOf(523.25, 587.33, 783.99, 1046.50, 1174.66), 115, 0.25, SoundWaveform.BRIGHT_CRYSTAL),
        SoundPreset("ring_turbo", SoundCategory.RINGTONE, "Turbo Alert", "تنبيه سريع", listOf(987.77, 1318.51, 987.77, 1318.51, 1567.98), 95, 0.25, SoundWaveform.PURE),
        SoundPreset("ring_jazz", SoundCategory.RINGTONE, "Midnight Jazz", "جاز ليلي", listOf(392.00, 493.88, 587.33, 739.99, 587.33), 140, 0.24, SoundWaveform.DEEP_BELL),
        SoundPreset("ring_diamond", SoundCategory.RINGTONE, "Diamond Chime", "رنين الألماس", listOf(1046.50, 1318.51, 1567.98, 1318.51, 1567.98), 110, 0.25, SoundWaveform.BRIGHT_CRYSTAL)
    )

    val messagePresets: List<SoundPreset> = listOf(
        SoundPreset("msg_classic", SoundCategory.MESSAGE, "Crisp Pop", "فقاعة كلاسيكية", listOf(783.99, 1046.50), 75, 0.24, SoundWaveform.WARM_CHIME),
        SoundPreset("msg_water", SoundCategory.MESSAGE, "Water Drop", "قطرة ماء", listOf(659.25, 1174.66), 70, 0.24, SoundWaveform.PURE),
        SoundPreset("msg_crystal", SoundCategory.MESSAGE, "Crystal Ping", "رنة كريستال", listOf(1046.50, 1567.98), 70, 0.23, SoundWaveform.BRIGHT_CRYSTAL),
        SoundPreset("msg_bubble", SoundCategory.MESSAGE, "Soft Bubble", "فقاعة ناعمة", listOf(523.25, 783.99, 987.77), 60, 0.23, SoundWaveform.WARM_CHIME),
        SoundPreset("msg_coin", SoundCategory.MESSAGE, "Pixel Coin", "عملة بكسل", listOf(987.77, 1318.51), 65, 0.22, SoundWaveform.RETRO_SYNTH),
        SoundPreset("msg_feather", SoundCategory.MESSAGE, "Feather Tap", "نقرة خفيفة", listOf(587.33, 880.00), 65, 0.22, SoundWaveform.PURE),
        SoundPreset("msg_laser", SoundCategory.MESSAGE, "Neon Zap", "ومضة نيون", listOf(1174.66, 880.00, 1318.51), 55, 0.22, SoundWaveform.RETRO_SYNTH),
        SoundPreset("msg_wood", SoundCategory.MESSAGE, "Zen Wood", "نقرة خشبية", listOf(440.00, 659.25), 75, 0.24, SoundWaveform.DEEP_BELL),
        SoundPreset("msg_spark", SoundCategory.MESSAGE, "Star Spark", "شرارة نجمية", listOf(880.00, 1174.66, 1567.98), 60, 0.23, SoundWaveform.BRIGHT_CRYSTAL),
        SoundPreset("msg_whisper", SoundCategory.MESSAGE, "Velvet Note", "همسة ناعمة", listOf(493.88, 739.99), 80, 0.22, SoundWaveform.WARM_CHIME)
    )

    val callEventPresets: List<SoundPreset> = listOf(
        SoundPreset("event_classic", SoundCategory.CALL_EVENT, "Studio Sync", "مزامنة استوديو", listOf(523.25, 659.25, 783.99), 95, 0.25, SoundWaveform.WARM_CHIME),
        SoundPreset("event_arcade", SoundCategory.CALL_EVENT, "Power Up", "طاقة الألعاب", listOf(440.00, 659.25, 880.00, 1174.66), 75, 0.24, SoundWaveform.RETRO_SYNTH),
        SoundPreset("event_aurora", SoundCategory.CALL_EVENT, "Glass Flow", "انسياب زجاجي", listOf(587.33, 739.99, 987.77), 90, 0.24, SoundWaveform.BRIGHT_CRYSTAL),
        SoundPreset("event_cyber", SoundCategory.CALL_EVENT, "Tech Lock", "قفل تقني", listOf(659.25, 987.77, 1318.51), 85, 0.24, SoundWaveform.RETRO_SYNTH),
        SoundPreset("event_warm", SoundCategory.CALL_EVENT, "Acoustic Chord", "وتر دافئ", listOf(392.00, 493.88, 659.25), 100, 0.24, SoundWaveform.DEEP_BELL),
        SoundPreset("event_space", SoundCategory.CALL_EVENT, "Warp Gate", "بوابة الفضاء", listOf(329.63, 493.88, 783.99, 1046.50), 85, 0.24, SoundWaveform.DEEP_BELL),
        SoundPreset("event_royal", SoundCategory.CALL_EVENT, "Crown Seal", "ختم ملكي", listOf(523.25, 783.99, 1046.50), 95, 0.25, SoundWaveform.WARM_CHIME),
        SoundPreset("event_minimal", SoundCategory.CALL_EVENT, "Clean Click", "نغمة بسيطة", listOf(783.99, 987.77), 85, 0.23, SoundWaveform.PURE),
        SoundPreset("event_emerald", SoundCategory.CALL_EVENT, "Emerald Chime", "نغمة زمردية", listOf(493.88, 659.25, 987.77), 90, 0.24, SoundWaveform.BRIGHT_CRYSTAL),
        SoundPreset("event_flash", SoundCategory.CALL_EVENT, "Flash Link", "اتصال خاطف", listOf(880.00, 1174.66, 1567.98), 75, 0.23, SoundWaveform.BRIGHT_CRYSTAL)
    )

    fun presetsFor(category: SoundCategory): List<SoundPreset> = when (category) {
        SoundCategory.STARTUP -> startupPresets
        SoundCategory.RINGTONE -> ringtonePresets
        SoundCategory.MESSAGE -> messagePresets
        SoundCategory.CALL_EVENT -> callEventPresets
    }

    fun findStartup(id: String): SoundPreset =
        startupPresets.firstOrNull { it.id == id } ?: startupPresets.first()

    fun findRingtone(id: String): SoundPreset =
        ringtonePresets.firstOrNull { it.id == id } ?: ringtonePresets.first()

    fun findMessage(id: String): SoundPreset =
        messagePresets.firstOrNull { it.id == id } ?: messagePresets.first()

    fun findCallEvent(id: String): SoundPreset =
        callEventPresets.firstOrNull { it.id == id } ?: callEventPresets.first()
}

/**
 * Lightweight synthesized PCM sound effects manager — requires no external audio files,
 * supports 40 Neo+ customizable sound presets across Startup, Ringtones, Messages, and Call Events,
 * and safely handles playback off the UI thread.
 */
object SoundEffectsManager {
    private const val SAMPLE_RATE = 22050
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private val trackLock = Any()
    private var ringtoneJob: Job? = null
    private var activeEffectJob: Job? = null
    private var activeTrack: AudioTrack? = null

    @Volatile
    private var currentPrefs: UserSoundPrefs = UserSoundPrefs()

    @Volatile
    private var customSoundsAllowed: Boolean = false

    fun init(context: Context) {
        currentPrefs = AppAppearanceStore.loadSoundPrefs(context)
        customSoundsAllowed = AppAppearanceStore.loadNeoPlusSoundsEnabled(context)
    }

    fun updatePreferences(prefs: UserSoundPrefs, allowedForNeoPlus: Boolean = customSoundsAllowed) {
        currentPrefs = prefs
        customSoundsAllowed = allowedForNeoPlus
    }

    fun setCustomSoundsAllowed(allowed: Boolean) {
        customSoundsAllowed = allowed
    }

    fun previewPreset(preset: SoundPreset) {
        activeEffectJob?.cancel()
        activeEffectJob = scope.launch {
            playToneSequence(
                notesHz = preset.notesHz,
                noteMs = preset.noteMs,
                volume = preset.volume,
                waveform = preset.waveform
            )
        }
    }

    fun play(effect: AppSoundEffect) {
        activeEffectJob?.cancel()
        activeEffectJob = scope.launch {
            val prefs = if (customSoundsAllowed) currentPrefs else UserSoundPrefs()
            when (effect) {
                AppSoundEffect.ENTRANCE_CHIME -> {
                    val preset = SoundPresetCatalog.findStartup(prefs.startupId)
                    playToneSequence(
                        notesHz = preset.notesHz,
                        noteMs = preset.noteMs,
                        volume = preset.volume,
                        waveform = preset.waveform
                    )
                }
                AppSoundEffect.MESSAGE_IN -> {
                    val preset = SoundPresetCatalog.findMessage(prefs.messageId)
                    playToneSequence(
                        notesHz = preset.notesHz,
                        noteMs = preset.noteMs,
                        volume = preset.volume,
                        waveform = preset.waveform
                    )
                }
                AppSoundEffect.MESSAGE_OUT -> {
                    val preset = SoundPresetCatalog.findMessage(prefs.messageId)
                    val outNotes = preset.notesHz.takeLast(2).map { (it * 0.84).coerceAtLeast(220.0) }
                    playToneSequence(
                        notesHz = outNotes,
                        noteMs = (preset.noteMs * 0.8).toInt().coerceAtLeast(48),
                        volume = (preset.volume * 0.78).coerceAtLeast(0.15),
                        waveform = preset.waveform
                    )
                }
                AppSoundEffect.CALL_CONNECT -> {
                    val preset = SoundPresetCatalog.findCallEvent(prefs.callEventId)
                    playToneSequence(
                        notesHz = preset.notesHz,
                        noteMs = preset.noteMs,
                        volume = preset.volume,
                        waveform = preset.waveform
                    )
                }
                AppSoundEffect.CALL_END -> {
                    val preset = SoundPresetCatalog.findCallEvent(prefs.callEventId)
                    val descending = preset.notesHz.reversed().map { (it * 0.78).coerceAtLeast(180.0) }
                    playToneSequence(
                        notesHz = descending,
                        noteMs = preset.noteMs,
                        volume = preset.volume,
                        waveform = preset.waveform
                    )
                }
                AppSoundEffect.OUTGOING_RINGBACK -> {
                    val preset = SoundPresetCatalog.findCallEvent(prefs.callEventId)
                    playToneSequence(
                        notesHz = listOf(preset.notesHz.first()),
                        noteMs = 360,
                        volume = 0.14,
                        waveform = SoundWaveform.PURE
                    )
                }
                AppSoundEffect.INCOMING_RINGTONE -> {
                    val preset = SoundPresetCatalog.findRingtone(prefs.ringtoneId)
                    playToneSequence(
                        notesHz = preset.notesHz,
                        noteMs = preset.noteMs,
                        volume = preset.volume,
                        waveform = preset.waveform
                    )
                }
                AppSoundEffect.NEW_ACCOUNT_CELEBRATION -> {
                    val preset = SoundPresetCatalog.findStartup(prefs.startupId)
                    val celebrationNotes = if (prefs.startupId == "startup_classic") {
                        listOf(523.25, 659.25, 783.99, 1046.50, 1318.51, 1567.98)
                    } else {
                        preset.notesHz + listOf(preset.notesHz.last() * 1.25)
                    }
                    playToneSequence(
                        notesHz = celebrationNotes,
                        noteMs = 95,
                        volume = 0.28,
                        waveform = preset.waveform
                    )
                }
                AppSoundEffect.BONUS_SPARKLE -> {
                    playToneSequence(
                        notesHz = listOf(1046.50, 1318.51, 1567.98, 2093.00),
                        noteMs = 80,
                        volume = 0.24,
                        waveform = SoundWaveform.BRIGHT_CRYSTAL
                    )
                }
            }
        }
    }

    fun startRingingLoop(incoming: Boolean) {
        if (ringtoneJob?.isActive == true) return
        ringtoneJob = scope.launch {
            while (isActive) {
                val prefs = if (customSoundsAllowed) currentPrefs else UserSoundPrefs()
                if (incoming) {
                    val preset = SoundPresetCatalog.findRingtone(prefs.ringtoneId)
                    playToneSequence(
                        notesHz = preset.notesHz,
                        noteMs = preset.noteMs,
                        volume = preset.volume,
                        waveform = preset.waveform
                    )
                    delay(1350L)
                } else {
                    val preset = SoundPresetCatalog.findCallEvent(prefs.callEventId)
                    playToneSequence(
                        notesHz = listOf(preset.notesHz.first()),
                        noteMs = 420,
                        volume = 0.14,
                        waveform = SoundWaveform.PURE
                    )
                    delay(1800L)
                }
            }
        }
    }

    fun stopRingingLoop() {
        ringtoneJob?.cancel()
        ringtoneJob = null
        releaseActiveTrack()
    }

    fun stopAll() {
        ringtoneJob?.cancel()
        ringtoneJob = null
        activeEffectJob?.cancel()
        activeEffectJob = null
        releaseActiveTrack()
    }

    private fun releaseActiveTrack() {
        val toStop = synchronized(trackLock) {
            val prev = activeTrack
            activeTrack = null
            prev
        }
        if (toStop != null) {
            try {
                toStop.pause()
                toStop.flush()
                toStop.stop()
            } catch (_: Exception) {
            }
            try {
                toStop.release()
            } catch (_: Exception) {
            }
        }
    }

    private suspend fun playToneSequence(
        notesHz: List<Double>,
        noteMs: Int,
        volume: Double,
        waveform: SoundWaveform = SoundWaveform.WARM_CHIME
    ) {
        val samplesPerNote = (SAMPLE_RATE * noteMs) / 1000
        val totalSamples = samplesPerNote * notesHz.size
        if (totalSamples <= 0) return

        val pcm = ShortArray(totalSamples)
        var cursor = 0
        for (freq in notesHz) {
            for (i in 0 until samplesPerNote) {
                val progress = i.toDouble() / samplesPerNote.toDouble()
                val attack = (progress * 14.0).coerceAtMost(1.0)
                val decay = exp(-3.4 * progress)
                val env = attack * decay
                val phase = 2.0 * Math.PI * freq * i / SAMPLE_RATE
                val wave = when (waveform) {
                    SoundWaveform.PURE -> sin(phase)
                    SoundWaveform.WARM_CHIME -> {
                        0.78 * sin(phase) + 0.18 * sin(phase * 2.0) + 0.04 * sin(phase * 3.0)
                    }
                    SoundWaveform.BRIGHT_CRYSTAL -> {
                        0.68 * sin(phase) + 0.24 * sin(phase * 2.0) + 0.12 * sin(phase * 4.0)
                    }
                    SoundWaveform.RETRO_SYNTH -> {
                        0.66 * sin(phase) + 0.22 * sin(phase * 3.0) + 0.12 * sin(phase * 5.0)
                    }
                    SoundWaveform.DEEP_BELL -> {
                        0.72 * sin(phase) + 0.20 * sin(phase * 0.5) + 0.10 * sin(phase * 2.0)
                    }
                }
                pcm[cursor++] = (wave * env * volume * Short.MAX_VALUE).toInt()
                    .coerceIn(Short.MIN_VALUE.toInt(), Short.MAX_VALUE.toInt()).toShort()
            }
        }

        val byteCount = pcm.size * 2
        val track = try {
            AudioTrack.Builder()
                .setAudioAttributes(
                    AudioAttributes.Builder()
                        .setUsage(AudioAttributes.USAGE_NOTIFICATION_COMMUNICATION_INSTANT)
                        .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                        .build()
                )
                .setAudioFormat(
                    AudioFormat.Builder()
                        .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
                        .setSampleRate(SAMPLE_RATE)
                        .setChannelMask(AudioFormat.CHANNEL_OUT_MONO)
                        .build()
                )
                .setBufferSizeInBytes(byteCount)
                .setTransferMode(AudioTrack.MODE_STATIC)
                .build()
        } catch (_: Exception) {
            null
        } ?: return

        releaseActiveTrack()
        synchronized(trackLock) {
            activeTrack = track
        }

        try {
            track.write(pcm, 0, pcm.size)
            track.play()
            delay((notesHz.size * noteMs + 120).toLong())
        } catch (_: Exception) {
        } finally {
            val shouldRelease = synchronized(trackLock) {
                if (activeTrack === track) {
                    activeTrack = null
                    true
                } else {
                    false
                }
            }
            if (shouldRelease) {
                try {
                    track.pause()
                    track.flush()
                    track.stop()
                } catch (_: Exception) {
                }
                try {
                    track.release()
                } catch (_: Exception) {
                }
            }
        }
    }
}
