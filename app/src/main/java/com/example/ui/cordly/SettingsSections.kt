package com.example.ui.cordly

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.Logout
import androidx.compose.material.icons.rounded.AccountCircle
import androidx.compose.material.icons.rounded.AddPhotoAlternate
import androidx.compose.material.icons.rounded.AppShortcut
import androidx.compose.material.icons.rounded.Badge
import androidx.compose.material.icons.rounded.Brush
import androidx.compose.material.icons.rounded.CameraAlt
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.ColorLens
import androidx.compose.material.icons.rounded.DarkMode
import androidx.compose.material.icons.rounded.DeleteOutline
import androidx.compose.material.icons.rounded.Image
import androidx.compose.material.icons.rounded.LightMode
import androidx.compose.material.icons.rounded.Lock
import androidx.compose.material.icons.rounded.NotificationsActive
import androidx.compose.material.icons.rounded.Palette
import androidx.compose.material.icons.rounded.PlayCircleFilled
import androidx.compose.material.icons.rounded.Refresh
import androidx.compose.material.icons.rounded.VideoLibrary
import androidx.compose.material.icons.rounded.ViewQuilt
import androidx.compose.material.icons.rounded.Wallpaper
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.example.data.model.UserPrivate
import com.example.service.NeovBackgroundMessagingService
import com.example.data.repository.CordlyRepository
import com.example.ui.theme.CordlyAmber
import com.example.ui.theme.CordlyBlurple
import com.example.ui.theme.CordlyCanvasBg
import com.example.ui.theme.CordlyCardSurface
import com.example.ui.theme.CordlyCrimson
import com.example.ui.theme.CordlyCyan
import com.example.ui.theme.CordlyDivider
import com.example.ui.theme.CordlyElevatedSurface
import com.example.ui.theme.CordlyEmerald
import com.example.ui.theme.CordlyRailBg
import com.example.ui.theme.CordlyTextPrimary
import com.example.ui.theme.CordlyTextSecondary
import com.example.ui.theme.CordlyThemeState
import com.example.ui.theme.LocalAppAccentColor
import com.example.ui.theme.LocalAppThemeMode
import com.example.ui.theme.LocalOnAppAccentColorSelected
import com.example.ui.theme.LocalOnAppThemeModeSelected

@Composable
internal fun SettingsSectionCard(
    title: String,
    subtitle: String? = null,
    icon: ImageVector,
    iconTint: Color = CordlyCyan,
    badgeText: String? = null,
    badgeColor: Color = CordlyAmber,
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit
) {
    Surface(
        color = CordlyCardSurface,
        shape = RoundedCornerShape(18.dp),
        border = BorderStroke(1.dp, CordlyDivider),
        modifier = modifier.fillMaxWidth()
    ) {
        Column(
            verticalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier.padding(14.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Surface(
                    color = iconTint.copy(alpha = 0.14f),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.size(34.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = icon,
                            contentDescription = null,
                            tint = iconTint,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
                Spacer(modifier = Modifier.width(10.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.titleSmall,
                        color = CordlyTextPrimary,
                        fontWeight = FontWeight.Bold
                    )
                    if (!subtitle.isNullOrBlank()) {
                        Text(
                            text = subtitle,
                            style = MaterialTheme.typography.labelSmall,
                            color = CordlyTextSecondary
                        )
                    }
                }
                if (!badgeText.isNullOrBlank()) {
                    Surface(
                        color = badgeColor.copy(alpha = 0.16f),
                        shape = RoundedCornerShape(8.dp),
                        border = BorderStroke(1.dp, badgeColor.copy(alpha = 0.45f))
                    ) {
                        Text(
                            text = badgeText,
                            style = MaterialTheme.typography.labelSmall,
                            color = badgeColor,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                        )
                    }
                }
            }
            HorizontalDivider(color = CordlyDivider.copy(alpha = 0.7f))
            content()
        }
    }
}

@Composable
internal fun SettingsProfileSection(
    profile: UserPrivate?,
    currentRank: String,
    isArabicLocale: Boolean,
    displayName: String,
    onDisplayNameChange: (String) -> Unit,
    customStatus: String,
    onCustomStatusChange: (String) -> Unit,
    selectedStatus: String,
    onStatusSelect: (String) -> Unit,
    pendingPhotoUrl: String?,
    pendingBannerUrl: String?,
    isBusy: Boolean,
    onSave: () -> Unit,
    onPreviewOwnProfile: () -> Unit,
    onSignOut: () -> Unit
) {
    val strings = LocalAppStrings.current
    val statuses = listOf("online" to CordlyEmerald, "idle" to CordlyAmber, "dnd" to CordlyCrimson)

    Column(
        verticalArrangement = Arrangement.spacedBy(14.dp),
        modifier = Modifier
            .fillMaxWidth()
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        // 1. Live Profile Preview Card
        Surface(
            color = CordlyCardSurface,
            shape = RoundedCornerShape(20.dp),
            border = BorderStroke(1.dp, CordlyDivider),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(92.dp)
                        .background(Brush.linearGradient(listOf(CordlyBlurple, Color(0xFF9333EA), CordlyCyan)))
                ) {
                    if (!pendingBannerUrl.isNullOrBlank()) {
                        CordlyRemoteOrDataImage(pendingBannerUrl, "Profile banner", ContentScale.Crop, Modifier.fillMaxSize())
                    }
                }
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth().padding(14.dp)
                ) {
                    UserAvatar(
                        displayName = displayName.ifBlank { profile?.displayName ?: "User" },
                        photoUrl = pendingPhotoUrl,
                        status = selectedStatus,
                        frameId = profile?.equippedFrame ?: "none",
                        size = 58.dp
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = displayName.ifBlank { profile?.displayName ?: "neov user" },
                            style = MaterialTheme.typography.titleMedium,
                            color = CordlyTextPrimary,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "${rankDisplayTitle(currentRank, isArabicLocale)} • ${profile?.friendCode ?: "..."}",
                            style = MaterialTheme.typography.labelSmall,
                            color = CordlyCyan
                        )
                        if (customStatus.isNotBlank()) {
                            Text(
                                text = customStatus,
                                style = MaterialTheme.typography.labelSmall,
                                color = CordlyTextSecondary,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                    OutlinedButton(
                        onClick = onPreviewOwnProfile,
                        border = BorderStroke(1.dp, CordlyCyan),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.minimumInteractiveComponentSize()
                    ) {
                        Icon(Icons.Rounded.PlayCircleFilled, contentDescription = null, tint = CordlyCyan, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = if (isArabicLocale) "معاينة" else "Preview",
                            style = MaterialTheme.typography.labelSmall,
                            color = CordlyCyan
                        )
                    }
                }
            }
        }

        // 2. Identity & Status Box
        SettingsSectionCard(
            title = if (isArabicLocale) "الاسم والحالة" else "Identity & Status",
            subtitle = if (isArabicLocale) "تعديل اسم العرض والحالة النشطة" else "Customize your display name, bio status, and availability",
            icon = Icons.Rounded.Badge,
            iconTint = CordlyBlurple
        ) {
            OutlinedTextField(
                value = displayName,
                onValueChange = onDisplayNameChange,
                label = { Text(strings.displayNameLabel) },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )
            OutlinedTextField(
                value = customStatus,
                onValueChange = onCustomStatusChange,
                label = { Text(strings.customStatusLabel) },
                placeholder = { Text(strings.customStatusPlaceholder) },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                statuses.forEach { (key, color) ->
                    val active = key == selectedStatus
                    Surface(
                        color = if (active) color.copy(alpha = 0.22f) else CordlyElevatedSurface,
                        shape = RoundedCornerShape(12.dp),
                        border = BorderStroke(1.dp, if (active) color else CordlyDivider),
                        modifier = Modifier
                            .weight(1f)
                            .minimumInteractiveComponentSize()
                            .clip(RoundedCornerShape(12.dp))
                            .clickable { onStatusSelect(key) }
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center,
                            modifier = Modifier.padding(vertical = 12.dp, horizontal = 6.dp)
                        ) {
                            Box(modifier = Modifier.size(8.dp).clip(CircleShape).background(color))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(statusLabel(key, strings), style = MaterialTheme.typography.labelSmall, color = CordlyTextPrimary, maxLines = 1)
                        }
                    }
                }
            }
            Button(
                onClick = onSave,
                enabled = !isBusy,
                colors = ButtonDefaults.buttonColors(containerColor = CordlyBlurple),
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
                    .testTag("save_profile_settings_button")
            ) {
                Text(strings.saveProfileButton)
            }
        }

        // 3. Account Box
        SettingsSectionCard(
            title = if (isArabicLocale) "الحساب" else "Account",
            subtitle = profile?.email ?: if (isArabicLocale) "إدارة الجلسة وتسجيل الخروج" else "Manage your session and sign out",
            icon = Icons.Rounded.AccountCircle,
            iconTint = CordlyCrimson
        ) {
            OutlinedButton(
                onClick = onSignOut,
                border = BorderStroke(1.dp, CordlyCrimson.copy(alpha = 0.65f)),
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier.fillMaxWidth().height(48.dp)
            ) {
                Icon(Icons.AutoMirrored.Rounded.Logout, contentDescription = null, tint = CordlyCrimson, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text(strings.signOutButton, color = CordlyCrimson)
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
internal fun SettingsColorsSection(
    profile: UserPrivate?,
    currentRank: String,
    isArabicLocale: Boolean,
    onUpdateProfileThemeColor: (String?) -> Unit,
    onOpenShopForRanks: () -> Unit
) {
    val strings = LocalAppStrings.current
    val currentThemeMode = LocalAppThemeMode.current
    val onThemeModeSelected = LocalOnAppThemeModeSelected.current
    val currentAccentColor = LocalAppAccentColor.current
    val onAccentColorSelected = LocalOnAppAccentColorSelected.current
    val hasUltra = hasRequiredRank(currentRank, CordlyRepository.RANK_ULTRA)
    val profileColors = listOf("#5865F2", "#22D3EE", "#10B981", "#F59E0B", "#EC4899", "#9333EA", "#EF4444")

    Column(
        verticalArrangement = Arrangement.spacedBy(14.dp),
        modifier = Modifier
            .fillMaxWidth()
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        // 1. Night / Day Mode Box
        SettingsSectionCard(
            title = strings.appearanceModeTitle,
            subtitle = strings.appearanceModeSubtitle,
            icon = Icons.Rounded.DarkMode,
            iconTint = CordlyBlurple
        ) {
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
                ThemeModeOptionCard(
                    label = strings.nightModeLabel,
                    icon = Icons.Rounded.DarkMode,
                    previewBg = Color.Black,
                    previewBorder = Color(0xFF232330),
                    previewContentTint = Color.White,
                    selected = currentThemeMode == AppThemeMode.NIGHT,
                    testTag = "theme_mode_night",
                    onClick = { onThemeModeSelected(AppThemeMode.NIGHT) },
                    modifier = Modifier.weight(1f)
                )
                ThemeModeOptionCard(
                    label = strings.dayModeLabel,
                    icon = Icons.Rounded.LightMode,
                    previewBg = Color.White,
                    previewBorder = Color(0xFFCBD5E1),
                    previewContentTint = CordlyAmber,
                    selected = currentThemeMode == AppThemeMode.DAY,
                    testTag = "theme_mode_day",
                    onClick = { onThemeModeSelected(AppThemeMode.DAY) },
                    modifier = Modifier.weight(1f)
                )
            }
        }

        // 2. App Accent Color Box
        SettingsSectionCard(
            title = strings.appColorTitle,
            subtitle = strings.appColorSubtitle,
            icon = Icons.Rounded.ColorLens,
            iconTint = currentAccentColor.primary
        ) {
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                AppAccentColor.entries.forEach { accent ->
                    val isSelected = accent == currentAccentColor
                    val label = if (isArabicLocale) accent.labelAr else accent.labelEn
                    Surface(
                        color = if (isSelected) accent.primary.copy(alpha = 0.18f) else CordlyElevatedSurface,
                        shape = RoundedCornerShape(14.dp),
                        border = BorderStroke(if (isSelected) 2.dp else 1.dp, if (isSelected) accent.primary else CordlyDivider),
                        modifier = Modifier
                            .minimumInteractiveComponentSize()
                            .clip(RoundedCornerShape(14.dp))
                            .clickable { onAccentColorSelected(accent) }
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp)
                        ) {
                            Box(
                                contentAlignment = Alignment.Center,
                                modifier = Modifier
                                    .size(26.dp)
                                    .clip(CircleShape)
                                    .background(Brush.linearGradient(listOf(accent.primary, accent.darkSecondary)))
                                    .border(1.dp, Color.White.copy(alpha = 0.6f), CircleShape)
                            ) {
                                if (isSelected) {
                                    Icon(Icons.Rounded.Check, contentDescription = label, tint = Color.White, modifier = Modifier.size(14.dp))
                                }
                            }
                            Text(
                                text = label,
                                style = MaterialTheme.typography.labelMedium,
                                color = CordlyTextPrimary,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                            )
                        }
                    }
                }
            }
        }

        // 3. Full Profile Card Theme Color Box (Ultra)
        SettingsSectionCard(
            title = strings.fullProfileColorTitle,
            subtitle = if (isArabicLocale) "لون خاص لبطاقة ملفك الشخصي عند فتحها" else "Tint your entire profile card when friends view your profile",
            icon = Icons.Rounded.Brush,
            iconTint = CordlyAmber,
            badgeText = if (hasUltra) null else "ULTRA",
            badgeColor = CordlyAmber
        ) {
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Surface(
                    color = if (profile?.profileThemeColorHex == null) CordlyBlurple.copy(alpha = 0.16f) else CordlyElevatedSurface,
                    shape = RoundedCornerShape(12.dp),
                    border = BorderStroke(1.dp, if (profile?.profileThemeColorHex == null) CordlyBlurple else CordlyDivider),
                    modifier = Modifier
                        .height(44.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .clickable { if (hasUltra) onUpdateProfileThemeColor(null) else onOpenShopForRanks() }
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(horizontal = 12.dp)
                    ) {
                        Icon(Icons.Rounded.Refresh, contentDescription = null, tint = CordlyTextSecondary, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(if (isArabicLocale) "افتراضي" else "Default", style = MaterialTheme.typography.labelMedium, color = CordlyTextPrimary)
                    }
                }
                profileColors.forEach { hex ->
                    val color = parseHexColor(hex)
                    val isSelected = profile?.profileThemeColorHex == hex
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .size(44.dp)
                            .clip(CircleShape)
                            .background(color)
                            .border(if (isSelected) 3.dp else 1.dp, if (isSelected) Color.White else CordlyDivider, CircleShape)
                            .clickable { if (hasUltra) onUpdateProfileThemeColor(hex) else onOpenShopForRanks() }
                    ) {
                        if (isSelected) {
                            Icon(Icons.Rounded.Check, contentDescription = hex, tint = Color.White, modifier = Modifier.size(18.dp))
                        } else if (!hasUltra) {
                            Icon(Icons.Rounded.Lock, contentDescription = null, tint = Color.White.copy(alpha = 0.85f), modifier = Modifier.size(14.dp))
                        }
                    }
                }
            }
        }
    }
}

@Composable
internal fun SettingsMediaSection(
    profile: UserPrivate?,
    currentRank: String,
    isArabicLocale: Boolean,
    displayName: String,
    selectedStatus: String,
    pendingPhotoUrl: String?,
    pendingBannerUrl: String?,
    wallpaperState: WallpaperState,
    onPickAvatar: () -> Unit,
    onPickAvatarVideo: () -> Unit,
    onPickBanner: () -> Unit,
    onClearBanner: () -> Unit,
    onSelectPresetBanner: (ProfileBannerPreset) -> Unit,
    onSelectWallpaperMode: (AppWallpaperMode) -> Unit,
    onPickCustomWallpaper: () -> Unit,
    onOpenShopForRanks: () -> Unit
) {
    val strings = LocalAppStrings.current
    val hasNeoPlus = hasRequiredRank(currentRank, CordlyRepository.RANK_NEO_PLUS)

    Column(
        verticalArrangement = Arrangement.spacedBy(14.dp),
        modifier = Modifier
            .fillMaxWidth()
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        // 1. Avatar & Video Avatar Box
        SettingsSectionCard(
            title = if (isArabicLocale) "صورة الملف الشخصي والأفاتار" else "Profile Photo & Video Avatar",
            subtitle = if (isArabicLocale) "تغيير صورتك أو إضافة أفاتار فيديو متحرك" else "Upload a profile picture or 5-second animated video avatar",
            icon = Icons.Rounded.CameraAlt,
            iconTint = CordlyCyan
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                UserAvatar(
                    displayName = displayName.ifBlank { profile?.displayName ?: "User" },
                    photoUrl = pendingPhotoUrl,
                    status = selectedStatus,
                    frameId = profile?.equippedFrame ?: "none",
                    size = 64.dp
                )
                Column(verticalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.weight(1f)) {
                    Button(
                        onClick = onPickAvatar,
                        colors = ButtonDefaults.buttonColors(containerColor = CordlyBlurple),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth().height(48.dp)
                    ) {
                        Icon(Icons.Rounded.CameraAlt, contentDescription = null, modifier = Modifier.size(17.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(if (isArabicLocale) "تغيير الصورة" else "Change Photo")
                    }
                    OutlinedButton(
                        onClick = { if (hasNeoPlus) onPickAvatarVideo() else onOpenShopForRanks() },
                        border = BorderStroke(1.dp, if (hasNeoPlus) CordlyAmber else CordlyDivider),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth().height(48.dp)
                    ) {
                        Icon(
                            if (hasNeoPlus) Icons.Rounded.VideoLibrary else Icons.Rounded.Lock,
                            contentDescription = null,
                            tint = CordlyAmber,
                            modifier = Modifier.size(17.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(strings.videoAvatarButton, color = if (hasNeoPlus) CordlyAmber else CordlyTextSecondary)
                    }
                }
            }
        }

        // 2. Profile Banner Box
        SettingsSectionCard(
            title = strings.bannerStylesTitle,
            subtitle = strings.bannerStylesSubtitle,
            icon = Icons.Rounded.Image,
            iconTint = CordlyBlurple
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(92.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(Brush.linearGradient(listOf(CordlyBlurple, Color(0xFF9333EA), CordlyCyan)))
            ) {
                if (!pendingBannerUrl.isNullOrBlank()) {
                    CordlyRemoteOrDataImage(pendingBannerUrl, "Profile banner", ContentScale.Crop, Modifier.fillMaxSize())
                }
                Row(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    modifier = Modifier.align(Alignment.TopEnd).padding(8.dp)
                ) {
                    Surface(
                        color = Color.Black.copy(alpha = 0.65f),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.minimumInteractiveComponentSize().clip(RoundedCornerShape(10.dp)).clickable(onClick = onPickBanner)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp)) {
                            Icon(Icons.Rounded.Wallpaper, contentDescription = null, tint = Color.White, modifier = Modifier.size(15.dp))
                            Spacer(modifier = Modifier.width(5.dp))
                            Text(strings.bannerLabel, style = MaterialTheme.typography.labelSmall, color = Color.White)
                        }
                    }
                    if (pendingBannerUrl != null) {
                        Surface(
                            color = Color.Black.copy(alpha = 0.65f),
                            shape = CircleShape,
                            modifier = Modifier.size(34.dp).clip(CircleShape).clickable(onClick = onClearBanner)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(Icons.Rounded.DeleteOutline, contentDescription = "Remove banner", tint = CordlyCrimson, modifier = Modifier.size(18.dp))
                            }
                        }
                    }
                }
            }
            Row(
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState())
            ) {
                ProfileBannerCatalog.presets.forEach { preset ->
                    val isSelected = pendingBannerUrl == preset.bannerSource
                    val unlocked = hasRequiredRank(currentRank, preset.requiredRank)
                    Surface(
                        color = CordlyElevatedSurface,
                        shape = RoundedCornerShape(14.dp),
                        border = BorderStroke(if (isSelected) 2.5.dp else 1.dp, if (isSelected) CordlyCyan else CordlyDivider),
                        modifier = Modifier
                            .width(118.dp)
                            .height(74.dp)
                            .clip(RoundedCornerShape(14.dp))
                            .clickable { onSelectPresetBanner(preset) }
                    ) {
                        Box(modifier = Modifier.fillMaxSize()) {
                            PresetBannerCanvas(preset = preset, modifier = Modifier.fillMaxSize())
                            if (!unlocked) {
                                Icon(Icons.Rounded.Lock, contentDescription = null, tint = CordlyAmber, modifier = Modifier.align(Alignment.TopStart).padding(6.dp).size(16.dp))
                            }
                            if (isSelected) {
                                Icon(Icons.Rounded.CheckCircle, contentDescription = null, tint = CordlyCyan, modifier = Modifier.align(Alignment.TopEnd).padding(5.dp).size(18.dp))
                            }
                            Box(
                                modifier = Modifier
                                    .align(Alignment.BottomCenter)
                                    .fillMaxWidth()
                                    .background(Color.Black.copy(alpha = 0.65f))
                                    .padding(horizontal = 6.dp, vertical = 4.dp)
                            ) {
                                Text(
                                    text = if (isArabicLocale) preset.titleAr else preset.titleEn,
                                    style = MaterialTheme.typography.labelSmall,
                                    color = Color.White,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        }
                    }
                }
            }
        }

        // 3. App Wallpaper / Background Box
        SettingsSectionCard(
            title = strings.backgroundTitle,
            subtitle = strings.backgroundSubtitle,
            icon = Icons.Rounded.Wallpaper,
            iconTint = CordlyEmerald
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState())
            ) {
                listOf(
                    AppWallpaperMode.DEFAULT, AppWallpaperMode.PRESET_1, AppWallpaperMode.PRESET_2, AppWallpaperMode.PRESET_3,
                    AppWallpaperMode.SHAPE_HEX, AppWallpaperMode.SHAPE_AURORA, AppWallpaperMode.SHAPE_WAVES, AppWallpaperMode.SHAPE_GALAXY
                ).forEach { mode ->
                    WallpaperSwatchCard(mode, wallpaperState.mode == mode) { onSelectWallpaperMode(mode) }
                }
                Surface(
                    color = CordlyElevatedSurface,
                    shape = RoundedCornerShape(14.dp),
                    border = BorderStroke(
                        if (wallpaperState.mode == AppWallpaperMode.CUSTOM) 2.dp else 1.dp,
                        if (wallpaperState.mode == AppWallpaperMode.CUSTOM) CordlyCyan else CordlyDivider
                    ),
                    modifier = Modifier
                        .width(96.dp)
                        .height(110.dp)
                        .clip(RoundedCornerShape(14.dp))
                        .clickable(onClick = onPickCustomWallpaper)
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center,
                        modifier = Modifier.padding(8.dp)
                    ) {
                        Icon(Icons.Rounded.AddPhotoAlternate, contentDescription = strings.yourPhoto, tint = CordlyCyan, modifier = Modifier.size(26.dp))
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(strings.yourPhoto, style = MaterialTheme.typography.labelSmall, color = CordlyTextPrimary)
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
internal fun SettingsCustomizeSection(
    currentRank: String,
    isArabicLocale: Boolean,
    currentLogoVariant: AppLogoVariant,
    layoutPrefs: AppUiLayoutPrefs,
    onUpdateAppLogo: (AppLogoVariant) -> Unit,
    onUpdateLayoutPrefs: (AppUiLayoutPrefs) -> Unit,
    onOpenShopForRanks: () -> Unit
) {
    val strings = LocalAppStrings.current
    val hasNeoPlus = hasRequiredRank(currentRank, CordlyRepository.RANK_NEO_PLUS)

    Column(
        verticalArrangement = Arrangement.spacedBy(14.dp),
        modifier = Modifier
            .fillMaxWidth()
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        // 1. App Launcher Icon Box
        SettingsSectionCard(
            title = strings.appLogoSectionTitle,
            subtitle = if (isArabicLocale) "اختر أيقونة التطبيق على الشاشة الرئيسية" else "Pick your custom home-screen launcher icon style",
            icon = Icons.Rounded.AppShortcut,
            iconTint = CordlyAmber,
            badgeText = if (hasNeoPlus) null else "NEO+",
            badgeColor = CordlyAmber
        ) {
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                AppLogoVariant.entries.forEach { variant ->
                    val selected = variant == currentLogoVariant
                    Surface(
                        color = if (selected) variant.badgeColor.copy(alpha = 0.22f) else CordlyElevatedSurface,
                        shape = RoundedCornerShape(16.dp),
                        border = BorderStroke(if (selected) 2.dp else 1.dp, if (selected) variant.badgeColor else CordlyDivider),
                        modifier = Modifier
                            .width(96.dp)
                            .clip(RoundedCornerShape(16.dp))
                            .clickable { if (hasNeoPlus) onUpdateAppLogo(variant) else onOpenShopForRanks() }
                            .testTag("app_logo_variant_${variant.storageKey}")
                    ) {
                        Box(modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp, vertical = 10.dp)) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.spacedBy(8.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                NeovBrandEmblem(variant = variant, size = 54.dp, cornerRadius = 14.dp)
                                Text(
                                    text = if (isArabicLocale) variant.titleAr else variant.titleEn,
                                    style = MaterialTheme.typography.labelSmall,
                                    color = CordlyTextPrimary,
                                    fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                            if (selected) {
                                Icon(
                                    imageVector = Icons.Rounded.CheckCircle,
                                    contentDescription = null,
                                    tint = variant.badgeColor,
                                    modifier = Modifier.align(Alignment.TopEnd).size(16.dp)
                                )
                            }
                        }
                    }
                }
            }
        }

        // 2. App Layout Box
        SettingsSectionCard(
            title = strings.layoutSectionTitle,
            subtitle = if (isArabicLocale) "تخصيص مكان شريط السيرفرات وتباعد الرسائل" else "Arrange the server rail position and message card density",
            icon = Icons.Rounded.ViewQuilt,
            iconTint = CordlyCyan,
            badgeText = if (hasNeoPlus) null else "NEO+",
            badgeColor = CordlyAmber
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = if (isArabicLocale) "شريط السيرفرات على اليمين" else "Server Rail on Right",
                    style = MaterialTheme.typography.bodyMedium,
                    color = CordlyTextPrimary
                )
                Switch(
                    checked = layoutPrefs.railOnRight,
                    onCheckedChange = { checked ->
                        if (hasNeoPlus) onUpdateLayoutPrefs(layoutPrefs.copy(railOnRight = checked)) else onOpenShopForRanks()
                    }
                )
            }
            HorizontalDivider(color = CordlyDivider.copy(alpha = 0.5f))
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = if (isArabicLocale) "تباعد رسائل مضغوط" else "Compact Chat Spacing",
                    style = MaterialTheme.typography.bodyMedium,
                    color = CordlyTextPrimary
                )
                Switch(
                    checked = layoutPrefs.compactMessageCards,
                    onCheckedChange = { checked ->
                        if (hasNeoPlus) onUpdateLayoutPrefs(layoutPrefs.copy(compactMessageCards = checked)) else onOpenShopForRanks()
                    }
                )
            }
            HorizontalDivider(color = CordlyDivider.copy(alpha = 0.5f))
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.weight(1f).padding(end = 12.dp)) {
                    Text(
                        text = if (isArabicLocale) "إخفاء قائمة السيرفرات" else "Hide Servers Rail",
                        style = MaterialTheme.typography.bodyMedium,
                        color = CordlyTextPrimary
                    )
                    Text(
                        text = if (isArabicLocale) "اسحب من حافة الشاشة لإظهارها مجدداً" else "Swipe inward from the screen edge to reveal it",
                        style = MaterialTheme.typography.labelSmall,
                        color = CordlyTextSecondary
                    )
                }
                Switch(
                    checked = layoutPrefs.railHidden,
                    onCheckedChange = { checked ->
                        onUpdateLayoutPrefs(layoutPrefs.copy(railHidden = checked))
                    }
                )
            }
        }

        // 3. Closed-App & Background Notifications Box (Free for All Users)
        val context = LocalContext.current
        var bgSyncEnabled by remember {
            mutableStateOf(AppAppearanceStore.loadBackgroundSyncEnabled(context))
        }
        SettingsSectionCard(
            title = strings.bgSyncTitle,
            subtitle = strings.bgSyncSubtitle,
            icon = Icons.Rounded.NotificationsActive,
            iconTint = CordlyEmerald
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = if (isArabicLocale) "تشغيل استلام الرسائل بالخلفية" else "Active Background Delivery",
                    style = MaterialTheme.typography.bodyMedium,
                    color = CordlyTextPrimary,
                    modifier = Modifier.weight(1f).padding(end = 12.dp)
                )
                Switch(
                    checked = bgSyncEnabled,
                    onCheckedChange = { checked ->
                        bgSyncEnabled = AppAppearanceStore.saveBackgroundSyncEnabled(context, checked)
                        NeovBackgroundMessagingService.syncServiceState(context)
                    },
                    modifier = Modifier.testTag("background_sync_toggle")
                )
            }
        }
    }
}

@Composable
internal fun ThemeModeOptionCard(
    label: String,
    icon: ImageVector,
    previewBg: Color,
    previewBorder: Color,
    previewContentTint: Color,
    selected: Boolean,
    testTag: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        color = if (selected) CordlyBlurple.copy(alpha = 0.16f) else CordlyElevatedSurface,
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(if (selected) 2.dp else 1.dp, if (selected) CordlyBlurple else CordlyDivider),
        modifier = modifier.height(60.dp).clip(RoundedCornerShape(16.dp)).clickable(onClick = onClick).testTag(testTag)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            modifier = Modifier.fillMaxSize().padding(horizontal = 12.dp)
        ) {
            Surface(color = previewBg, shape = CircleShape, border = BorderStroke(1.5.dp, previewBorder), modifier = Modifier.size(32.dp)) {
                Box(contentAlignment = Alignment.Center) { Icon(icon, contentDescription = null, tint = previewContentTint, modifier = Modifier.size(17.dp)) }
            }
            Text(label, style = MaterialTheme.typography.titleSmall, color = CordlyTextPrimary, modifier = Modifier.weight(1f), maxLines = 1)
            if (selected) Icon(Icons.Rounded.CheckCircle, contentDescription = null, tint = CordlyBlurple, modifier = Modifier.size(18.dp))
        }
    }
}

@Composable
internal fun WallpaperSwatchCard(mode: AppWallpaperMode, selected: Boolean, onClick: () -> Unit) {
    val presetRes = mode.drawableRes()
    Surface(
        color = CordlyCanvasBg,
        shape = RoundedCornerShape(14.dp),
        border = BorderStroke(if (selected) 2.5.dp else 1.dp, if (selected) CordlyCyan else CordlyDivider),
        modifier = Modifier.width(96.dp).height(110.dp).clip(RoundedCornerShape(14.dp)).clickable(onClick = onClick)
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            when {
                mode.isShapeWallpaper() -> WallpaperShapeCanvas(mode = mode, isDark = CordlyThemeState.isDark, modifier = Modifier.fillMaxSize())
                presetRes != null -> AsyncImage(model = presetRes, contentDescription = mode.label, contentScale = ContentScale.Crop, modifier = Modifier.fillMaxSize())
                else -> Box(modifier = Modifier.fillMaxSize().background(Brush.verticalGradient(listOf(CordlyRailBg, CordlyCanvasBg))))
            }
            if (selected) Icon(Icons.Rounded.CheckCircle, contentDescription = null, tint = CordlyCyan, modifier = Modifier.align(Alignment.TopEnd).padding(6.dp).size(18.dp))
            Box(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth()
                    .background(Color.Black.copy(alpha = 0.65f))
                    .padding(horizontal = 6.dp, vertical = 5.dp)
            ) {
                Text(mode.label, style = MaterialTheme.typography.labelSmall, color = Color.White, maxLines = 1, overflow = TextOverflow.Ellipsis)
            }
        }
    }
}
