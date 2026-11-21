package com.example.ui.cordly

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.VolumeUp
import androidx.compose.material.icons.rounded.Call
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.GraphicEq
import androidx.compose.material.icons.rounded.Lock
import androidx.compose.material.icons.rounded.MarkChatUnread
import androidx.compose.material.icons.rounded.NotificationsActive
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material.icons.rounded.PowerSettingsNew
import androidx.compose.material.icons.rounded.Refresh
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.data.repository.CordlyRepository
import com.example.ui.theme.CordlyAmber
import com.example.ui.theme.CordlyBlurple
import com.example.ui.theme.CordlyCyan
import com.example.ui.theme.CordlyDivider
import com.example.ui.theme.CordlyElevatedSurface
import com.example.ui.theme.CordlyEmerald
import com.example.ui.theme.CordlyTextPrimary
import com.example.ui.theme.CordlyTextSecondary

@Composable
internal fun SettingsSoundsSection(
    currentRank: String,
    isArabicLocale: Boolean,
    soundPrefs: UserSoundPrefs,
    onUpdateSoundPrefs: (UserSoundPrefs) -> Unit,
    onOpenShopForRanks: () -> Unit
) {
    val hasNeoPlus = hasRequiredRank(currentRank, CordlyRepository.RANK_NEO_PLUS)
    val badge = if (hasNeoPlus) null else "NEO+"

    Column(
        verticalArrangement = Arrangement.spacedBy(14.dp),
        modifier = Modifier
            .fillMaxWidth()
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
            .testTag("settings_sounds_section")
    ) {
        // Top Neo+ Studio Status Banner
        Surface(
            color = if (hasNeoPlus) CordlyEmerald.copy(alpha = 0.14f) else CordlyAmber.copy(alpha = 0.14f),
            shape = RoundedCornerShape(18.dp),
            border = BorderStroke(1.2.dp, if (hasNeoPlus) CordlyEmerald else CordlyAmber),
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .background(
                        Brush.horizontalGradient(
                            if (hasNeoPlus) {
                                listOf(CordlyEmerald.copy(alpha = 0.12f), CordlyCyan.copy(alpha = 0.10f))
                            } else {
                                listOf(CordlyAmber.copy(alpha = 0.16f), CordlyBlurple.copy(alpha = 0.12f))
                            }
                        )
                    )
                    .padding(14.dp)
            ) {
                Icon(
                    imageVector = if (hasNeoPlus) Icons.Rounded.GraphicEq else Icons.Rounded.Lock,
                    contentDescription = null,
                    tint = if (hasNeoPlus) CordlyEmerald else CordlyAmber,
                    modifier = Modifier.size(26.dp)
                )
                Spacer(modifier = Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = if (isArabicLocale) {
                            if (hasNeoPlus) "استوديو أصوات نيو بلس مُفعّل" else "ميزة حصرية لرتبة نيو بلس (Neo+)"
                        } else {
                            if (hasNeoPlus) "Neo+ Sound Studio Active" else "Exclusive to Neo Plus (Neo+)"
                        },
                        style = MaterialTheme.typography.titleSmall,
                        color = CordlyTextPrimary,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = if (isArabicLocale) {
                            "40 مؤثراً صوتياً احترافياً (10 لكل فئة) لفتح التطبيق والرنين والرسائل والمكالمات."
                        } else {
                            "40 curated sound effects (10 per category) for launch, ringtones, messages & calls."
                        },
                        style = MaterialTheme.typography.labelSmall,
                        color = CordlyTextSecondary
                    )
                }
                if (!hasNeoPlus) {
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = onOpenShopForRanks,
                        colors = ButtonDefaults.buttonColors(containerColor = CordlyAmber, contentColor = Color.Black),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.height(40.dp)
                    ) {
                        Text(
                            text = if (isArabicLocale) "ترقية" else "Unlock",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }

        // 1. App Startup / Launch Sound Box (10 Presets)
        SoundCategoryBox(
            title = if (isArabicLocale) "أصوات التسجيل وفتح التطبيق" else "App Launch & Entrance Sounds",
            subtitle = if (isArabicLocale) "10 مؤثرات عند فتح التطبيق والدخول" else "10 studio chimes played when opening neov chat",
            icon = Icons.Rounded.PowerSettingsNew,
            iconTint = CordlyCyan,
            badgeText = badge,
            presets = SoundPresetCatalog.startupPresets,
            selectedId = soundPrefs.startupId,
            hasNeoPlus = hasNeoPlus,
            isArabicLocale = isArabicLocale,
            onSelectPreset = { preset ->
                SoundEffectsManager.previewPreset(preset)
                if (hasNeoPlus) {
                    onUpdateSoundPrefs(soundPrefs.copy(startupId = preset.id))
                } else {
                    onOpenShopForRanks()
                }
            },
            onPreviewPreset = { preset -> SoundEffectsManager.previewPreset(preset) }
        )

        // 2. Incoming Ringtone Box (10 Presets)
        SoundCategoryBox(
            title = if (isArabicLocale) "أصوات الرنين عند الاتصال" else "Incoming Call Ringtones",
            subtitle = if (isArabicLocale) "10 نغمات رنين عندما يتصل بك شخص" else "10 ringtones played when friends call you",
            icon = Icons.Rounded.NotificationsActive,
            iconTint = CordlyAmber,
            badgeText = badge,
            presets = SoundPresetCatalog.ringtonePresets,
            selectedId = soundPrefs.ringtoneId,
            hasNeoPlus = hasNeoPlus,
            isArabicLocale = isArabicLocale,
            onSelectPreset = { preset ->
                SoundEffectsManager.previewPreset(preset)
                if (hasNeoPlus) {
                    onUpdateSoundPrefs(soundPrefs.copy(ringtoneId = preset.id))
                } else {
                    onOpenShopForRanks()
                }
            },
            onPreviewPreset = { preset -> SoundEffectsManager.previewPreset(preset) }
        )

        // 3. Messages Sound Box (10 Presets)
        SoundCategoryBox(
            title = if (isArabicLocale) "أصوات الرسائل" else "Message Sounds",
            subtitle = if (isArabicLocale) "10 مؤثرات عند إرسال واستلام الرسائل" else "10 effects for incoming & sent chat messages",
            icon = Icons.Rounded.MarkChatUnread,
            iconTint = CordlyBlurple,
            badgeText = badge,
            presets = SoundPresetCatalog.messagePresets,
            selectedId = soundPrefs.messageId,
            hasNeoPlus = hasNeoPlus,
            isArabicLocale = isArabicLocale,
            onSelectPreset = { preset ->
                SoundEffectsManager.previewPreset(preset)
                if (hasNeoPlus) {
                    onUpdateSoundPrefs(soundPrefs.copy(messageId = preset.id))
                } else {
                    onOpenShopForRanks()
                }
            },
            onPreviewPreset = { preset -> SoundEffectsManager.previewPreset(preset) }
        )

        // 4. Call Connect & Events Box (10 Presets)
        SoundCategoryBox(
            title = if (isArabicLocale) "أصوات المكالمات والأحداث" else "Call Connect & Alert Sounds",
            subtitle = if (isArabicLocale) "10 مؤثرات لبدء وإنهاء المكالمة والتنبيهات" else "10 effects for call join, hangup & alerts",
            icon = Icons.Rounded.Call,
            iconTint = CordlyEmerald,
            badgeText = badge,
            presets = SoundPresetCatalog.callEventPresets,
            selectedId = soundPrefs.callEventId,
            hasNeoPlus = hasNeoPlus,
            isArabicLocale = isArabicLocale,
            onSelectPreset = { preset ->
                SoundEffectsManager.previewPreset(preset)
                if (hasNeoPlus) {
                    onUpdateSoundPrefs(soundPrefs.copy(callEventId = preset.id))
                } else {
                    onOpenShopForRanks()
                }
            },
            onPreviewPreset = { preset -> SoundEffectsManager.previewPreset(preset) }
        )

        if (hasNeoPlus) {
            OutlinedButton(
                onClick = {
                    val defaults = UserSoundPrefs()
                    onUpdateSoundPrefs(defaults)
                    SoundEffectsManager.previewPreset(SoundPresetCatalog.findStartup(defaults.startupId))
                },
                border = BorderStroke(1.dp, CordlyDivider),
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
                    .testTag("reset_sounds_button")
            ) {
                Icon(Icons.Rounded.Refresh, contentDescription = null, tint = CordlyTextSecondary, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = if (isArabicLocale) "استعادة الأصوات الافتراضية" else "Reset All Sounds to Default",
                    color = CordlyTextPrimary
                )
            }
        }
    }
}

@Composable
private fun SoundCategoryBox(
    title: String,
    subtitle: String,
    icon: ImageVector,
    iconTint: Color,
    badgeText: String?,
    presets: List<SoundPreset>,
    selectedId: String,
    hasNeoPlus: Boolean,
    isArabicLocale: Boolean,
    onSelectPreset: (SoundPreset) -> Unit,
    onPreviewPreset: (SoundPreset) -> Unit
) {
    SettingsSectionCard(
        title = title,
        subtitle = subtitle,
        icon = icon,
        iconTint = iconTint,
        badgeText = badgeText,
        badgeColor = CordlyAmber
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
            presets.chunked(2).forEachIndexed { rowIdx, pair ->
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    pair.forEachIndexed { colIdx, preset ->
                        val trackNumber = rowIdx * 2 + colIdx + 1
                        val isSelected = preset.id == selectedId
                        SoundPresetTile(
                            trackNumber = trackNumber,
                            preset = preset,
                            accentColor = iconTint,
                            selected = isSelected,
                            locked = !hasNeoPlus && trackNumber > 1,
                            isArabicLocale = isArabicLocale,
                            onSelect = { onSelectPreset(preset) },
                            onPreview = { onPreviewPreset(preset) },
                            modifier = Modifier.weight(1f)
                        )
                    }
                    if (pair.size == 1) {
                        Spacer(modifier = Modifier.weight(1f))
                    }
                }
            }
        }
    }
}

@Composable
private fun SoundPresetTile(
    trackNumber: Int,
    preset: SoundPreset,
    accentColor: Color,
    selected: Boolean,
    locked: Boolean,
    isArabicLocale: Boolean,
    onSelect: () -> Unit,
    onPreview: () -> Unit,
    modifier: Modifier = Modifier
) {
    val label = if (isArabicLocale) preset.titleAr else preset.titleEn
    Surface(
        color = if (selected) accentColor.copy(alpha = 0.20f) else CordlyElevatedSurface,
        shape = RoundedCornerShape(14.dp),
        border = BorderStroke(
            width = if (selected) 1.8.dp else 1.dp,
            color = if (selected) accentColor else CordlyDivider
        ),
        modifier = modifier
            .heightIn(min = 54.dp)
            .clip(RoundedCornerShape(14.dp))
            .clickable(onClick = onSelect)
            .testTag("sound_preset_${preset.id}")
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 8.dp, end = 4.dp, top = 6.dp, bottom = 6.dp)
        ) {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .size(26.dp)
                    .clip(CircleShape)
                    .background(if (selected) accentColor else accentColor.copy(alpha = 0.14f))
            ) {
                Text(
                    text = trackNumber.toString().padStart(2, '0'),
                    style = MaterialTheme.typography.labelSmall,
                    color = if (selected) Color.Black else accentColor,
                    fontWeight = FontWeight.ExtraBold
                )
            }
            Spacer(modifier = Modifier.width(8.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = label,
                    style = MaterialTheme.typography.labelMedium,
                    color = CordlyTextPrimary,
                    fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                if (selected) {
                    Text(
                        text = if (isArabicLocale) "مُفعّل" else "Active",
                        style = MaterialTheme.typography.labelSmall,
                        color = accentColor,
                        maxLines = 1
                    )
                }
            }
            if (selected) {
                Icon(
                    imageVector = Icons.Rounded.CheckCircle,
                    contentDescription = label,
                    tint = accentColor,
                    modifier = Modifier.padding(end = 6.dp).size(18.dp)
                )
            } else {
                IconButton(
                    onClick = onPreview,
                    modifier = Modifier.minimumInteractiveComponentSize()
                ) {
                    Icon(
                        imageVector = if (locked) Icons.AutoMirrored.Rounded.VolumeUp else Icons.Rounded.PlayArrow,
                        contentDescription = if (isArabicLocale) "معاينة الصوت" else "Preview sound",
                        tint = if (locked) CordlyAmber else accentColor,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }
    }
}
