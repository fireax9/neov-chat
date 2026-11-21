package com.example.ui.cordly

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.ColorLens
import androidx.compose.material.icons.rounded.Language
import androidx.compose.material.icons.rounded.MusicNote
import androidx.compose.material.icons.rounded.Person
import androidx.compose.material.icons.rounded.PhotoLibrary
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material.icons.rounded.Tune
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.data.model.UserPrivate
import com.example.data.repository.CordlyRepository
import com.example.ui.theme.CordlyAmber
import com.example.ui.theme.CordlyBlurple
import com.example.ui.theme.CordlyCardSurface
import com.example.ui.theme.CordlyCrimson
import com.example.ui.theme.CordlyCyan
import com.example.ui.theme.CordlyDivider
import com.example.ui.theme.CordlyRailBg
import com.example.ui.theme.CordlySurfaceDark
import com.example.ui.theme.CordlyTextPrimary
import com.example.ui.theme.CordlyTextSecondary
import kotlinx.coroutines.launch

/**
 * Organized Settings & World Languages Dialog — each category has its own dedicated UI section.
 */
@Composable
fun SettingsAndShopDialog(
    initialTab: Int,
    profile: UserPrivate?,
    wallpaperState: WallpaperState,
    layoutPrefs: AppUiLayoutPrefs,
    soundPrefs: UserSoundPrefs,
    currentLogoVariant: AppLogoVariant,
    isBusy: Boolean,
    onSelectWallpaperMode: (AppWallpaperMode) -> Unit,
    onCustomWallpaperSaved: () -> Unit,
    onSaveProfile: (
        status: String,
        customStatus: String?,
        displayName: String?,
        photoUrl: String?,
        bannerUrl: String?,
        clearBanner: Boolean
    ) -> Unit,
    onQuickUpdateMedia: (
        photoUrl: String?,
        bannerUrl: String?,
        clearBanner: Boolean,
        label: String
    ) -> Unit,
    onUpdateProfileThemeColor: (String?) -> Unit,
    onUpdateAvatarVideo: (String?) -> Unit,
    onUpdateAppLogo: (AppLogoVariant) -> Unit,
    onUpdateLayoutPrefs: (AppUiLayoutPrefs) -> Unit,
    onUpdateSoundPrefs: (UserSoundPrefs) -> Unit,
    onPreviewOwnProfile: () -> Unit,
    onOpenShopForRanks: () -> Unit,
    onSignOut: () -> Unit,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val strings = LocalAppStrings.current
    val langCode = LocalSelectedLanguageCode.current
    val isArabicLocale = remember(langCode) {
        AppLanguageManager.resolveEffectiveCode(langCode).startsWith("ar")
    }

    var selectedTab by remember(initialTab) {
        mutableIntStateOf(
            when (initialTab) {
                in 0..5 -> initialTab
                else -> 0
            }
        )
    }
    var displayName by remember(profile?.userId) { mutableStateOf(profile?.displayName.orEmpty()) }
    var customStatus by remember(profile?.userId) { mutableStateOf(profile?.customStatus.orEmpty()) }
    var selectedStatus by remember(profile?.userId) { mutableStateOf(profile?.status ?: "online") }
    var pendingPhotoUrl by remember(profile?.userId) { mutableStateOf(profile?.photoUrl) }
    var pendingBannerUrl by remember(profile?.userId) { mutableStateOf(profile?.bannerUrl) }
    var clearBannerRequested by remember { mutableStateOf(false) }
    var isProcessingImage by remember { mutableStateOf(false) }
    var imageError by remember { mutableStateOf<String?>(null) }

    val uid = profile?.userId.orEmpty()
    val currentRank = remember(profile?.rank, profile?.rankExpiresAtMs) {
        CordlyRepository.effectiveRank(profile)
    }

    val avatarPicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        if (uri != null) {
            imageError = null
            isProcessingImage = true
            scope.launch {
                val encoded = ImageEncodingHelper.encodePickedImageToDataUri(context, uri, 144, 144, 40_000)
                isProcessingImage = false
                if (encoded != null) {
                    pendingPhotoUrl = encoded
                    onQuickUpdateMedia(encoded, null, false, "Profile photo")
                } else {
                    imageError = "Could not load selected profile photo."
                }
            }
        }
    }

    val avatarVideoPicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        if (uri != null && uid.isNotBlank()) {
            imageError = null
            isProcessingImage = true
            scope.launch {
                VideoEffectHelper.processPickedVideoTo5Seconds(context, uri, uid)
                    .onSuccess { processed ->
                        isProcessingImage = false
                        onUpdateAvatarVideo(processed.dataUri)
                    }
                    .onFailure {
                        isProcessingImage = false
                        imageError = "Could not process avatar video."
                    }
            }
        }
    }

    val bannerPicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        if (uri != null) {
            imageError = null
            isProcessingImage = true
            scope.launch {
                val encoded = ImageEncodingHelper.encodePickedImageToDataUri(context, uri, 400, 160, 55_000)
                isProcessingImage = false
                if (encoded != null) {
                    pendingBannerUrl = encoded
                    clearBannerRequested = false
                    onQuickUpdateMedia(null, encoded, false, "Profile banner")
                } else {
                    imageError = "Could not load selected banner image."
                }
            }
        }
    }

    val wallpaperPicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        if (uri != null) {
            imageError = null
            isProcessingImage = true
            scope.launch {
                val dest = AppAppearanceStore.customWallpaperFile(context)
                val saved = ImageEncodingHelper.savePickedWallpaperToInternalStorage(context, uri, dest)
                isProcessingImage = false
                if (saved) onCustomWallpaperSaved() else imageError = "Could not load selected wallpaper photo."
            }
        }
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false, decorFitsSystemWindows = false)
    ) {
        SanitizeDialogWindowIds()
        Surface(
            color = CordlySurfaceDark,
            shape = RoundedCornerShape(24.dp),
            border = BorderStroke(1.dp, CordlyDivider),
            modifier = Modifier
                .systemBarsPadding()
                .fillMaxWidth(0.94f)
                .widthIn(max = 560.dp)
                .padding(vertical = 16.dp)
        ) {
            Column(modifier = Modifier.fillMaxWidth()) {
                // Header
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(CordlyRailBg)
                        .padding(horizontal = 18.dp, vertical = 14.dp)
                ) {
                    Column {
                        Text(strings.settingsTitle, style = MaterialTheme.typography.titleLarge, color = CordlyTextPrimary)
                        Text(
                            text = "${profile?.peyPoints ?: 200} ${strings.peyAvailableSuffix}",
                            style = MaterialTheme.typography.labelSmall,
                            color = CordlyAmber,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    IconButton(onClick = onDismiss, modifier = Modifier.minimumInteractiveComponentSize()) {
                        Icon(Icons.Rounded.Close, contentDescription = strings.closeButton, tint = CordlyTextSecondary)
                    }
                }

                // Organized Category Tabs: Profile | Colors | Images | Layout | Sounds | Language
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(CordlyRailBg)
                        .horizontalScroll(rememberScrollState())
                        .padding(start = 14.dp, end = 14.dp, bottom = 12.dp)
                ) {
                    ModalTabButton(
                        icon = Icons.Rounded.Person,
                        label = strings.tabProfileTheme,
                        selected = selectedTab == 0,
                        testTag = "settings_profile_tab",
                        onClick = { selectedTab = 0 }
                    )
                    ModalTabButton(
                        icon = Icons.Rounded.ColorLens,
                        label = if (isArabicLocale) "الألوان" else "Colors",
                        selected = selectedTab == 1,
                        testTag = "settings_colors_tab",
                        onClick = { selectedTab = 1 }
                    )
                    ModalTabButton(
                        icon = Icons.Rounded.PhotoLibrary,
                        label = if (isArabicLocale) "الصور" else "Images",
                        selected = selectedTab == 2,
                        testTag = "settings_media_tab",
                        onClick = { selectedTab = 2 }
                    )
                    ModalTabButton(
                        icon = Icons.Rounded.Tune,
                        label = if (isArabicLocale) "التخطيط" else "Layout",
                        selected = selectedTab == 3,
                        testTag = "settings_customize_tab",
                        onClick = { selectedTab = 3 }
                    )
                    ModalTabButton(
                        icon = Icons.Rounded.MusicNote,
                        label = if (isArabicLocale) "الأصوات" else "Sounds",
                        selected = selectedTab == 4,
                        testTag = "settings_sounds_tab",
                        onClick = { selectedTab = 4 }
                    )
                    ModalTabButton(
                        icon = Icons.Rounded.Language,
                        label = strings.tabLanguages,
                        selected = selectedTab == 5,
                        testTag = "settings_language_tab",
                        onClick = { selectedTab = 5 }
                    )
                }

                HorizontalDivider(color = CordlyDivider)

                if (isProcessingImage) {
                    LinearProgressIndicator(color = CordlyCyan, trackColor = CordlyDivider, modifier = Modifier.fillMaxWidth())
                }
                if (imageError != null) {
                    Surface(
                        color = CordlyCrimson.copy(alpha = 0.16f),
                        border = BorderStroke(1.dp, CordlyCrimson),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp)
                    ) {
                        Text(imageError!!, style = MaterialTheme.typography.labelMedium, color = CordlyCrimson, modifier = Modifier.padding(10.dp))
                    }
                }

                Crossfade(
                    targetState = selectedTab,
                    animationSpec = tween(durationMillis = 180),
                    label = "settings_tab_crossfade"
                ) { tab ->
                    when (tab) {
                        0 -> SettingsProfileSection(
                            profile = profile,
                            currentRank = currentRank,
                            isArabicLocale = isArabicLocale,
                            displayName = displayName,
                            onDisplayNameChange = { displayName = it },
                            customStatus = customStatus,
                            onCustomStatusChange = { customStatus = it },
                            selectedStatus = selectedStatus,
                            onStatusSelect = { selectedStatus = it },
                            pendingPhotoUrl = pendingPhotoUrl,
                            pendingBannerUrl = pendingBannerUrl,
                            isBusy = isBusy,
                            onSave = {
                                onSaveProfile(selectedStatus, customStatus, displayName, pendingPhotoUrl, pendingBannerUrl, clearBannerRequested)
                            },
                            onPreviewOwnProfile = onPreviewOwnProfile,
                            onSignOut = {
                                onDismiss()
                                onSignOut()
                            }
                        )
                        1 -> SettingsColorsSection(
                            profile = profile,
                            currentRank = currentRank,
                            isArabicLocale = isArabicLocale,
                            onUpdateProfileThemeColor = onUpdateProfileThemeColor,
                            onOpenShopForRanks = onOpenShopForRanks
                        )
                        2 -> SettingsMediaSection(
                            profile = profile,
                            currentRank = currentRank,
                            isArabicLocale = isArabicLocale,
                            displayName = displayName,
                            selectedStatus = selectedStatus,
                            pendingPhotoUrl = pendingPhotoUrl,
                            pendingBannerUrl = pendingBannerUrl,
                            wallpaperState = wallpaperState,
                            onPickAvatar = {
                                avatarPicker.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
                            },
                            onPickAvatarVideo = {
                                avatarVideoPicker.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.VideoOnly))
                            },
                            onPickBanner = {
                                bannerPicker.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
                            },
                            onClearBanner = {
                                pendingBannerUrl = null
                                clearBannerRequested = true
                                onQuickUpdateMedia(null, null, true, "Profile banner")
                            },
                            onSelectPresetBanner = { preset ->
                                if (hasRequiredRank(currentRank, preset.requiredRank)) {
                                    pendingBannerUrl = preset.bannerSource
                                    clearBannerRequested = false
                                    onQuickUpdateMedia(null, preset.bannerSource, false, "Profile banner")
                                } else {
                                    onOpenShopForRanks()
                                }
                            },
                            onSelectWallpaperMode = onSelectWallpaperMode,
                            onPickCustomWallpaper = {
                                wallpaperPicker.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
                            },
                            onOpenShopForRanks = onOpenShopForRanks
                        )
                        3 -> SettingsCustomizeSection(
                            currentRank = currentRank,
                            isArabicLocale = isArabicLocale,
                            currentLogoVariant = currentLogoVariant,
                            layoutPrefs = layoutPrefs,
                            onUpdateAppLogo = onUpdateAppLogo,
                            onUpdateLayoutPrefs = onUpdateLayoutPrefs,
                            onOpenShopForRanks = onOpenShopForRanks
                        )
                        4 -> SettingsSoundsSection(
                            currentRank = currentRank,
                            isArabicLocale = isArabicLocale,
                            soundPrefs = soundPrefs,
                            onUpdateSoundPrefs = onUpdateSoundPrefs,
                            onOpenShopForRanks = onOpenShopForRanks
                        )
                        else -> WorldLanguagesTab()
                    }
                }
            }
        }
    }
}

@Composable
private fun ModalTabButton(
    icon: ImageVector,
    label: String,
    selected: Boolean,
    testTag: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        color = if (selected) CordlyBlurple else CordlyCardSurface,
        shape = RoundedCornerShape(12.dp),
        border = if (selected) null else BorderStroke(1.dp, CordlyDivider),
        modifier = modifier
            .height(44.dp)
            .clip(RoundedCornerShape(12.dp))
            .clickable(onClick = onClick)
            .testTag(testTag)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center,
            modifier = Modifier.padding(horizontal = 14.dp)
        ) {
            Icon(icon, contentDescription = null, tint = if (selected) Color.White else CordlyTextSecondary, modifier = Modifier.size(18.dp))
            Spacer(modifier = Modifier.width(6.dp))
            Text(label, style = MaterialTheme.typography.labelLarge, color = if (selected) Color.White else CordlyTextSecondary, maxLines = 1, overflow = TextOverflow.Ellipsis)
        }
    }
}

@Composable
fun WorldLanguagesTab() {
    val strings = LocalAppStrings.current
    val selectedCode = LocalSelectedLanguageCode.current
    val onLanguageSelected = LocalOnLanguageSelected.current
    var query by remember { mutableStateOf("") }
    val allLangs = remember { AppLanguageManager.allWorldLanguages }
    val filteredLangs = remember(query, allLangs) {
        val q = query.trim().lowercase()
        if (q.isBlank()) allLangs else allLangs.filter {
            it.nativeName.lowercase().contains(q) || it.englishName.lowercase().contains(q) || it.code.lowercase().contains(q)
        }
    }

    Column(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 14.dp)) {
        SettingsSectionCard(
            title = "${strings.languageTitle} (${allLangs.size} ${strings.languagesCountSuffix})",
            subtitle = strings.languageSubtitle,
            icon = Icons.Rounded.Language,
            iconTint = CordlyCyan
        ) {
            OutlinedTextField(
                value = query,
                onValueChange = { query = it },
                placeholder = { Text(strings.languageSearchPlaceholder) },
                leadingIcon = { Icon(Icons.Rounded.Search, contentDescription = null, tint = CordlyCyan) },
                singleLine = true,
                modifier = Modifier.fillMaxWidth().testTag("world_language_search_input")
            )
            LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth().height(320.dp)) {
                if (query.isBlank()) {
                    item(key = "lang_system") {
                        LanguageOptionRow(strings.deviceDefaultLanguage, "Auto System Locale", "system", false, selectedCode == "system") { onLanguageSelected("system") }
                    }
                }
                items(filteredLangs, key = { "lang_${it.code}" }) { lang ->
                    LanguageOptionRow(lang.nativeName, lang.englishName, lang.code, lang.isRtl, selectedCode.equals(lang.code, true)) { onLanguageSelected(lang.code) }
                }
            }
        }
    }
}

@Composable
private fun LanguageOptionRow(nativeName: String, englishName: String, code: String, isRtl: Boolean, selected: Boolean, onClick: () -> Unit) {
    Surface(
        color = if (selected) CordlyBlurple.copy(alpha = 0.22f) else CordlyCardSurface,
        shape = RoundedCornerShape(14.dp),
        border = BorderStroke(1.dp, if (selected) CordlyCyan else CordlyDivider),
        modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(14.dp)).clickable(onClick = onClick)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(horizontal = 14.dp, vertical = 11.dp)) {
            Column(modifier = Modifier.weight(1f)) {
                Text(nativeName, style = MaterialTheme.typography.titleSmall, color = CordlyTextPrimary, fontWeight = FontWeight.SemiBold)
                Text("$englishName • ${code.uppercase()}" + if (isRtl) " • RTL" else "", style = MaterialTheme.typography.labelSmall, color = CordlyTextSecondary)
            }
            if (selected) Icon(Icons.Rounded.CheckCircle, contentDescription = null, tint = CordlyCyan, modifier = Modifier.size(20.dp))
        }
    }
}

@Composable
fun WorldLanguagePickerDialog(selectedCode: String, onSelectLanguage: (String) -> Unit, onDismiss: () -> Unit) {
    val strings = LocalAppStrings.current
    var query by remember { mutableStateOf("") }
    val allLangs = remember { AppLanguageManager.allWorldLanguages }
    val filteredLangs = remember(query, allLangs) {
        val q = query.trim().lowercase()
        if (q.isBlank()) allLangs else allLangs.filter {
            it.nativeName.lowercase().contains(q) || it.englishName.lowercase().contains(q) || it.code.lowercase().contains(q)
        }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = CordlySurfaceDark,
        titleContentColor = CordlyTextPrimary,
        title = { Text("${strings.languageTitle} (${allLangs.size})", style = MaterialTheme.typography.titleLarge) },
        text = {
            Column(modifier = Modifier.fillMaxWidth()) {
                OutlinedTextField(value = query, onValueChange = { query = it }, placeholder = { Text(strings.languageSearchPlaceholder) }, leadingIcon = { Icon(Icons.Rounded.Search, contentDescription = null, tint = CordlyCyan) }, singleLine = true, modifier = Modifier.fillMaxWidth())
                Spacer(modifier = Modifier.height(10.dp))
                LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth().height(300.dp)) {
                    if (query.isBlank()) {
                        item { LanguageOptionRow(strings.deviceDefaultLanguage, "Auto", "system", false, selectedCode == "system") { onSelectLanguage("system"); onDismiss() } }
                    }
                    items(filteredLangs, key = { "dlg_${it.code}" }) { lang ->
                        LanguageOptionRow(lang.nativeName, lang.englishName, lang.code, lang.isRtl, selectedCode.equals(lang.code, true)) { onSelectLanguage(lang.code); onDismiss() }
                    }
                }
            }
        },
        confirmButton = { TextButton(onClick = onDismiss) { Text(strings.closeButton) } }
    )
}
