package com.example.ui.cordly

import android.content.Context
import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioTrack
import android.media.MediaPlayer
import com.example.R
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.exp
import kotlin.math.sin
import kotlin.random.Random

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
    private val cardTrackLock = Any()
    private val cardMediaLock = Any()
    private var cardSoundJob: Job? = null
    private var activeCardTrack: AudioTrack? = null
    private var activeCardMediaPlayer: MediaPlayer? = null

    @Volatile
    private var appContext: Context? = null

    @Volatile
    private var cardSoundGeneration: Long = 0L

    @Volatile
    private var currentPrefs: UserSoundPrefs = UserSoundPrefs()

    @Volatile
    private var customSoundsAllowed: Boolean = true

    fun init(context: Context) {
        appContext = context.applicationContext
        currentPrefs = AppAppearanceStore.loadSoundPrefs(context)
        customSoundsAllowed = true
    }

    fun updatePreferences(prefs: UserSoundPrefs, allowedForNeoPlus: Boolean = true) {
        currentPrefs = prefs
        customSoundsAllowed = allowedForNeoPlus || true
    }

    fun setCustomSoundsAllowed(allowed: Boolean) {
        customSoundsAllowed = allowed || true
    }

    fun playEntranceOnAppOpen(context: Context): UserSoundPrefs {
        var prefs = AppAppearanceStore.loadSoundPrefs(context)
        if (AppAppearanceStore.loadAutoRotateStartupSound(context)) {
            val catalog = SoundPresetCatalog.startupPresets
            val idx = catalog.indexOfFirst { it.id == prefs.startupId }
            val nextPreset = catalog[(idx + 1).coerceAtLeast(0) % catalog.size]
            prefs = AppAppearanceStore.saveSoundPrefs(context, prefs.copy(startupId = nextPreset.id))
        }
        currentPrefs = prefs
        customSoundsAllowed = true
        play(AppSoundEffect.ENTRANCE_CHIME)
        return prefs
    }

    fun previewPreset(preset: SoundPreset) {
        activeEffectJob?.cancel()
        activeEffectJob = scope.launch {
            playToneSequence(
                notesHz = preset.notesHz,
                noteMs = preset.noteMs,
                volume = (preset.volume * 1.45).coerceAtMost(0.45),
                waveform = preset.waveform
            )
        }
    }

    fun play(effect: AppSoundEffect) {
        activeEffectJob?.cancel()
        activeEffectJob = scope.launch {
            val prefs = currentPrefs
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
                val prefs = currentPrefs
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
        stopCardEntranceSound()
    }

    private const val CARD_SAMPLE_RATE = 44100

    private val ANIME_CARD_SOUND_IDS = setOf(
        "card_jjk_gojo_void",
        "card_jjk_gojo_domain",
        "card_dbz_kamehameha",
        "card_bluelock_striker",
        "card_jjk_sukuna_shrine"
    )

    fun isAnimeSoundAvatar(frameId: String): Boolean {
        return frameId in setOf(
            "jjk_gojo_hollow",
            "jjk_sukuna_hands",
            "dbz_kamehameha_sphere",
            "dbz_super_saiyan",
            "bluelock_egoist",
            "bluelock_isagi_aura"
        )
    }

    fun playAvatarFrameSound(frameId: String, context: Context? = null) {
        if (context != null) appContext = context.applicationContext
        val mappedCardId = when (frameId) {
            "jjk_gojo_hollow" -> "card_jjk_gojo_void"
            "jjk_sukuna_hands" -> "card_jjk_sukuna_shrine"
            "dbz_kamehameha_sphere", "dbz_super_saiyan" -> "card_dbz_kamehameha"
            "bluelock_egoist", "bluelock_isagi_aura" -> "card_bluelock_striker"
            else -> return
        }
        playCardEntranceSound(mappedCardId, context)
    }

    private fun rawResIdForCardFrame(cardFrameId: String): Int? = when (cardFrameId) {
        "card_jjk_gojo_domain" -> R.raw.sfx_gojo_domain
        "card_jjk_gojo_void" -> R.raw.sfx_gojo_purple
        "card_jjk_sukuna_shrine" -> R.raw.sfx_sukuna_domain
        else -> null
    }

    fun playCardEntranceSound(cardFrameId: String, context: Context? = null) {
        if (cardFrameId !in ANIME_CARD_SOUND_IDS) return
        if (context != null) appContext = context.applicationContext
        val generation = ++cardSoundGeneration
        cardSoundJob?.cancel()
        releaseCardTrack()
        releaseCardMediaPlayer()

        val rawResId = rawResIdForCardFrame(cardFrameId)
        val ctx = context?.applicationContext ?: appContext
        if (rawResId != null && ctx != null) {
            cardSoundJob = scope.launch {
                val player = try {
                    val afd = ctx.resources.openRawResourceFd(rawResId)
                    MediaPlayer().apply {
                        setAudioAttributes(
                            AudioAttributes.Builder()
                                .setUsage(AudioAttributes.USAGE_MEDIA)
                                .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                                .build()
                        )
                        afd.use {
                            setDataSource(it.fileDescriptor, it.startOffset, it.length)
                        }
                        setVolume(1.0f, 1.0f)
                        prepare()
                    }
                } catch (_: Exception) {
                    null
                } ?: return@launch

                if (cardSoundGeneration != generation || !isActive) {
                    try {
                        player.release()
                    } catch (_: Exception) {
                    }
                    return@launch
                }

                synchronized(cardMediaLock) {
                    if (cardSoundGeneration != generation) {
                        try {
                            player.release()
                        } catch (_: Exception) {
                        }
                        return@launch
                    }
                    activeCardMediaPlayer = player
                }

                player.setOnCompletionListener { mp ->
                    releaseSpecificCardMediaPlayer(mp)
                }
                player.setOnErrorListener { mp, _, _ ->
                    releaseSpecificCardMediaPlayer(mp)
                    true
                }
                try {
                    player.start()
                } catch (_: Exception) {
                    releaseSpecificCardMediaPlayer(player)
                }
            }
            return
        }

        cardSoundJob = scope.launch {
            val sr = CARD_SAMPLE_RATE
            val durationSec = 4.8
            val totalSamples = (sr * durationSec).toInt()
            val pcm = ShortArray(totalSamples)
            val rng = Random(cardFrameId.hashCode())
            var lpNoise = 0.0
            var lpNoiseSlow = 0.0
            var phase1 = 0.0
            var phase2 = 0.0
            var phase3 = 0.0
            var phase4 = 0.0

            // Per-card output boost (Dragon Ball Kamehameha extra loud & impactful!)
            val masterGain = when (cardFrameId) {
                "card_dbz_kamehameha" -> 0.94
                "card_jjk_gojo_domain" -> 0.88
                "card_jjk_sukuna_shrine" -> 0.88
                "card_jjk_gojo_void" -> 0.86
                else -> 0.82
            }

            for (i in 0 until totalSamples) {
                val t = i.toDouble() / sr
                val masterFadeIn = (t / 0.06).coerceAtMost(1.0)
                val masterFadeOut = ((durationSec - t) / 0.42).coerceIn(0.0, 1.0)
                val envMaster = masterFadeIn * masterFadeOut
                val rawNoise = rng.nextDouble() * 2.0 - 1.0
                lpNoise = lpNoise * 0.82 + rawNoise * 0.18
                lpNoiseSlow = lpNoiseSlow * 0.94 + rawNoise * 0.06

                val sample = when (cardFrameId) {
                    "card_jjk_gojo_domain" -> {
                        // Gojo Unlimited Void ("Ryoiki Tenkai: Muryokusho"):
                        // 1) Crisp crossed-finger snap + icy Six Eyes glassy chime (0.0s..0.95s)
                        // 2) Reality vacuum tear & brief silence drop (0.95s..1.25s)
                        // 3) Massive Infinite Void bloom: deep cosmic sub-drone + celestial bell harmonics + space shimmer (1.25s..4.8s)
                        val fingerSnap = if (t < 0.22) {
                            (sin(2.0 * PI * 1950.0 * t) * 0.45 + rawNoise * 0.55) * exp(-28.0 * t)
                        } else 0.0
                        val sixEyesBell = if (t in 0.18..1.10) {
                            val bt = t - 0.18
                            (0.45 * sin(2.0 * PI * 1318.51 * bt) + 0.35 * sin(2.0 * PI * 1975.53 * bt) + 0.25 * sin(2.0 * PI * 2637.02 * bt)) *
                                exp(-3.2 * bt)
                        } else 0.0
                        val vacuumSuck = if (t in 0.72..1.20) {
                            val vt = (t - 0.72) / 0.48
                            val sweep = 180.0 + 1400.0 * vt * vt
                            phase1 += 2.0 * PI * sweep / sr
                            sin(phase1) * vt * 0.52 + lpNoise * vt * 0.35
                        } else 0.0
                        val domainBloom = if (t >= 1.24) {
                            val dt = t - 1.24
                            val rise = (dt / 0.14).coerceAtMost(1.0)
                            val slowDecay = exp(-0.22 * dt)
                            // Deep reality warp boom + warm infinite void choir pad (F#m/C# cosmic chord)
                            val impactBoom = sin(2.0 * PI * (48.0 + 52.0 * exp(-5.0 * dt)) * dt) * exp(-2.4 * dt) * 0.85
                            phase2 += 2.0 * PI * 65.41 / sr // C2 deep void bass
                            phase3 += 2.0 * PI * 261.63 / sr
                            phase4 += 2.0 * PI * 392.00 / sr
                            val cosmicPad = (
                                0.36 * sin(phase2) +
                                    0.22 * sin(phase2 * 2.0) +
                                    0.18 * sin(phase3) +
                                    0.16 * sin(phase4) +
                                    0.14 * sin(2.0 * PI * 493.88 * dt) +
                                    0.12 * sin(2.0 * PI * 739.99 * dt)
                                ) * (0.85 + 0.15 * sin(2.0 * PI * 3.2 * dt))
                            val crystalStars = if (rng.nextDouble() > 0.996) rawNoise * 0.32 else 0.0
                            val infiniteHiss = (rawNoise - lpNoise) * 0.18
                            (impactBoom + (cosmicPad + infiniteHiss + crystalStars) * rise * slowDecay)
                        } else 0.0
                        fingerSnap + sixEyesBell + vacuumSuck + domainBloom
                    }

                    "card_jjk_gojo_void" -> {
                        // Gojo Hollow Purple (Purple Avatar & Void Card):
                        // Lapse Blue whistle + Reversal Red thrum converging -> sharp Hollow snap -> Void erasure beam!
                        val convergeP = (t / 2.85).coerceIn(0.0, 1.0)
                        val blueHz = 330.0 + 520.0 * convergeP * convergeP
                        val redHz = 220.0 + 440.0 * convergeP
                        phase1 += 2.0 * PI * blueHz / sr
                        phase2 += 2.0 * PI * redHz / sr
                        phase3 += 2.0 * PI * 76.0 / sr
                        val eyePing = if (t in 0.25..1.35) {
                            val pt = t - 0.25
                            (0.42 * sin(2.0 * PI * 1567.98 * pt) + 0.30 * sin(2.0 * PI * 2093.00 * pt)) * exp(-3.4 * pt)
                        } else 0.0
                        val chargePhase = if (t < 2.85) {
                            val amp = 0.25 + 0.55 * convergeP
                            val beat = 0.72 + 0.28 * sin(2.0 * PI * (6.0 + 18.0 * convergeP) * t)
                            (0.45 * sin(phase1) + 0.45 * sin(phase2) + 0.25 * sin(phase3)) * amp * beat + lpNoise * 0.22 * convergeP
                        } else 0.0
                        val hollowBlast = if (t >= 2.85) {
                            val bt = t - 2.85
                            val snap = if (bt < 0.12) sin(2.0 * PI * 2400.0 * bt) * exp(-25.0 * bt) * 0.55 else 0.0
                            val subBlast = sin(2.0 * PI * (46.0 + 68.0 * exp(-3.8 * bt)) * bt) * exp(-1.1 * bt) * 0.92
                            val voidRoar = (lpNoise * 0.65 + (rawNoise - lpNoise) * 0.28 + 0.28 * sin(phase1 * 0.5)) * exp(-0.75 * bt)
                            val chimeTrail = (sin(2.0 * PI * 1046.50 * bt) + sin(2.0 * PI * 1567.98 * bt)) * 0.22 * exp(-1.8 * bt)
                            snap + subBlast + voidRoar + chimeTrail
                        } else 0.0
                        eyePing + chargePhase + hollowBlast
                    }

                    "card_dbz_kamehameha" -> {
                        // LOUD & BOOMING Dragon Ball Kamehameha!
                        // Stage surges ("KA-ME-HA-ME" 0.0..2.1s) + Super Saiyan lightning -> GIANT BEAM ROAR ("HAAA!" 2.1..4.8s)
                        val stage = (t / 0.52).toInt().coerceAtMost(3)
                        val stagePhase = (t % 0.52) / 0.52
                        val stagePitch = when (stage) {
                            0 -> 190.0
                            1 -> 260.0
                            2 -> 350.0
                            else -> 480.0
                        }
                        val kiChargeHz = if (t < 2.10) {
                            stagePitch + 190.0 * stagePhase
                        } else {
                            680.0 - 280.0 * ((t - 2.10) / 2.70).coerceIn(0.0, 1.0)
                        }
                        phase1 += 2.0 * PI * kiChargeHz / sr
                        phase2 += 2.0 * PI * (kiChargeHz * 1.5) / sr
                        phase3 += 2.0 * PI * (kiChargeHz * 0.5) / sr
                        val arcPulse = 0.78 + 0.22 * sin(2.0 * PI * 18.0 * t)
                        val lightningCrack = if (rng.nextDouble() > 0.978) rawNoise * 0.72 else 0.0
                        if (t < 2.10) {
                            val envelope = 0.48 + 0.52 * (t / 2.10)
                            val surgeKick = if (stagePhase < 0.18) {
                                sin(2.0 * PI * 95.0 * stagePhase) * exp(-12.0 * stagePhase) * 0.60
                            } else 0.0
                            val kiWall = (0.52 * sin(phase1) + 0.34 * sin(phase2) + 0.30 * sin(phase3)) * arcPulse * envelope
                            kiWall + lpNoise * 0.38 * envelope + surgeKick + lightningCrack
                        } else {
                            val bt = t - 2.10
                            val blastSub = sin(2.0 * PI * (58.0 + 95.0 * exp(-5.0 * bt)) * bt) * exp(-0.9 * bt) * 0.96
                            val plasmaJet = (
                                lpNoise * 0.72 +
                                    (rawNoise - lpNoise) * 0.30 +
                                    0.46 * sin(phase1) +
                                    0.32 * sin(phase2) +
                                    0.28 * sin(phase3)
                                ) * exp(-0.24 * bt)
                            blastSub + plasmaJet + lightningCrack * 0.9
                        }
                    }

                    "card_bluelock_striker" -> {
                        // Stadium striker kick impact -> Metavision sonar lock -> puzzle snap clicks -> victory chord
                        val ballKick = if (t < 0.35) {
                            sin(2.0 * PI * (62.0 + 110.0 * exp(-18.0 * t)) * t) * exp(-7.5 * t) * 0.92
                        } else 0.0
                        val pulsePeriod = 0.44
                        val pulseLocal = t % pulsePeriod
                        val heartbeat = if (pulseLocal < 0.18) {
                            sin(2.0 * PI * 74.0 * pulseLocal) * exp(-13.0 * pulseLocal) * 0.62
                        } else 0.0
                        val sonar = if (t in 0.25..1.35) {
                            val st = t - 0.25
                            sin(2.0 * PI * 1174.66 * st) * exp(-3.0 * st) * 0.44
                        } else 0.0
                        var puzzleClicks = 0.0
                        for (ctMark in listOf(1.40, 2.00, 2.60)) {
                            if (t in ctMark..(ctMark + 0.28)) {
                                val dt = t - ctMark
                                puzzleClicks += (sin(2.0 * PI * 1567.98 * dt) + 0.55 * sin(2.0 * PI * 2349.32 * dt)) * exp(-15.0 * dt) * 0.58
                            }
                        }
                        val climaxChord = if (t >= 3.10) {
                            val ct = t - 3.10
                            val env = (ct / 0.10).coerceAtMost(1.0) * exp(-0.85 * ct)
                            (0.42 * sin(2.0 * PI * 523.25 * ct) +
                                0.36 * sin(2.0 * PI * 659.25 * ct) +
                                0.36 * sin(2.0 * PI * 783.99 * ct) +
                                0.32 * sin(2.0 * PI * 1046.50 * ct)) * env
                        } else 0.0
                        val synthBass = sin(2.0 * PI * (130.81 + 65.4 * (t / durationSec)) * t) * 0.28
                        ballKick + heartbeat + sonar + puzzleClicks + climaxChord + synthBass
                    }

                    else -> {
                        // card_jjk_sukuna_shrine: Ominous Malevolent Shrine ("Fukuma Mizushi"):
                        // Deep cursed bass rumble + war taiko + ancient shrine bell + roaring hellfire + razor Cleave/Dismantle slashes
                        val fireCrack = if (rng.nextDouble() > 0.978) rawNoise * 0.58 else 0.0
                        val fireRoar = lpNoise * (0.42 + 0.28 * sin(2.0 * PI * 1.6 * t).coerceAtLeast(0.0)) + fireCrack
                        phase1 += 2.0 * PI * 55.0 / sr
                        phase2 += 2.0 * PI * 58.2 / sr // ominous beating interval
                        val cursedDrone = (0.32 * sin(phase1) + 0.28 * sin(phase2)) * (t / 1.2).coerceIn(0.2, 1.0)

                        var taiko = 0.0
                        for (beat in listOf(0.06, 0.68, 1.32)) {
                            if (t >= beat && t < beat + 0.65) {
                                val dt = t - beat
                                taiko += sin(2.0 * PI * (52.0 + 58.0 * exp(-9.0 * dt)) * dt) * exp(-4.0 * dt) * 0.88
                            }
                        }
                        val shrineBell = if (t >= 1.48) {
                            val dt = t - 1.48
                            (0.44 * sin(2.0 * PI * 220.0 * dt) +
                                0.32 * sin(2.0 * PI * 330.0 * dt) +
                                0.22 * sin(2.0 * PI * 554.37 * dt)) * exp(-0.85 * dt)
                        } else 0.0
                        var slashFx = 0.0
                        for (stMark in listOf(2.05, 2.55, 3.05, 3.55)) {
                            if (t in stMark..(stMark + 0.30)) {
                                val dt = t - stMark
                                val sweepHz = 3200.0 * exp(-9.0 * dt) + 280.0
                                slashFx += (sin(2.0 * PI * sweepHz * dt) * 0.42 + (rawNoise - lpNoise) * 0.58) * exp(-8.5 * dt)
                            }
                        }
                        fireRoar + cursedDrone + taiko + shrineBell + slashFx
                    }
                }

                // Soft-clipping limiter (tanh) prevents harsh digital clipping while delivering punchy loudness
                val driven = sample * envMaster * masterGain * 1.45
                val limited = kotlin.math.tanh(driven)
                val scaled = (limited * 0.96 * Short.MAX_VALUE).toInt()
                    .coerceIn(Short.MIN_VALUE.toInt(), Short.MAX_VALUE.toInt()).toShort()
                pcm[i] = scaled
            }

            val byteCount = pcm.size * 2
            val track = try {
                AudioTrack.Builder()
                    .setAudioAttributes(
                        AudioAttributes.Builder()
                            .setUsage(AudioAttributes.USAGE_MEDIA)
                            .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                            .build()
                    )
                    .setAudioFormat(
                        AudioFormat.Builder()
                            .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
                            .setSampleRate(sr)
                            .setChannelMask(AudioFormat.CHANNEL_OUT_MONO)
                            .build()
                    )
                    .setBufferSizeInBytes(byteCount)
                    .setTransferMode(AudioTrack.MODE_STATIC)
                    .build()
            } catch (_: Exception) {
                null
            } ?: return@launch

            synchronized(cardTrackLock) {
                activeCardTrack = track
            }
            try {
                track.write(pcm, 0, pcm.size)
                track.play()
                delay((durationSec * 1000).toLong() + 120L)
            } catch (_: Exception) {
            } finally {
                releaseSpecificCardTrack(track)
            }
        }
    }

    fun stopCardEntranceSound() {
        cardSoundGeneration++
        cardSoundJob?.cancel()
        cardSoundJob = null
        releaseCardTrack()
        releaseCardMediaPlayer()
    }

    private fun releaseCardMediaPlayer() {
        val toStop = synchronized(cardMediaLock) {
            val prev = activeCardMediaPlayer
            activeCardMediaPlayer = null
            prev
        }
        toStop?.let { stopAndReleaseMediaPlayer(it) }
    }

    private fun releaseSpecificCardMediaPlayer(mp: MediaPlayer) {
        val shouldRelease = synchronized(cardMediaLock) {
            if (activeCardMediaPlayer === mp) {
                activeCardMediaPlayer = null
                true
            } else {
                false
            }
        }
        if (shouldRelease) {
            stopAndReleaseMediaPlayer(mp)
        }
    }

    private fun stopAndReleaseMediaPlayer(mp: MediaPlayer) {
        try {
            if (mp.isPlaying) {
                mp.stop()
            }
        } catch (_: Exception) {
        }
        try {
            mp.release()
        } catch (_: Exception) {
        }
    }

    private fun releaseCardTrack() {
        val toStop = synchronized(cardTrackLock) {
            val prev = activeCardTrack
            activeCardTrack = null
            prev
        }
        toStop?.let { stopAndReleaseTrack(it) }
    }

    private fun releaseSpecificCardTrack(track: AudioTrack) {
        val shouldRelease = synchronized(cardTrackLock) {
            if (activeCardTrack === track) {
                activeCardTrack = null
                true
            } else {
                false
            }
        }
        if (shouldRelease) {
            stopAndReleaseTrack(track)
        }
    }

    private fun stopAndReleaseTrack(track: AudioTrack) {
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
        val boostedVolume = (volume * 1.45).coerceAtMost(0.55)
        if (boostedVolume > volume) {
            for (k in pcm.indices) {
                val amplified = (pcm[k] * (boostedVolume / volume.coerceAtLeast(0.01))).toInt()
                    .coerceIn(Short.MIN_VALUE.toInt(), Short.MAX_VALUE.toInt()).toShort()
                pcm[k] = amplified
            }
        }
        val track = try {
            AudioTrack.Builder()
                .setAudioAttributes(
                    AudioAttributes.Builder()
                        .setUsage(AudioAttributes.USAGE_MEDIA)
                        .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
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
