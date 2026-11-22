package com.example.ui.cordly

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.AutoAwesome
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.Diamond
import androidx.compose.material.icons.rounded.Lock
import androidx.compose.material.icons.rounded.PlayCircleFilled
import androidx.compose.material.icons.rounded.ShoppingBag
import androidx.compose.material.icons.rounded.Stars
import androidx.compose.material.icons.rounded.Style
import androidx.compose.material.icons.rounded.VideoLibrary
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.data.model.PublicProfile
import com.example.data.model.UserPrivate
import com.example.data.repository.CordlyRepository
import com.example.ui.theme.CordlyAmber
import com.example.ui.theme.CordlyBlurple
import com.example.ui.theme.CordlyCardSurface
import com.example.ui.theme.CordlyCrimson
import com.example.ui.theme.CordlyCyan
import com.example.ui.theme.CordlyDivider
import com.example.ui.theme.CordlyElevatedSurface
import com.example.ui.theme.CordlyEmerald
import com.example.ui.theme.CordlyRailBg
import com.example.ui.theme.CordlySurfaceDark
import com.example.ui.theme.CordlyTextPrimary
import com.example.ui.theme.CordlyTextSecondary
import kotlinx.coroutines.launch

@Composable
fun PeyShopDialog(
    profile: UserPrivate?,
    isBusy: Boolean,
    onActivateFreeTrial: (tier: String) -> Unit,
    onPurchaseRank: (tier: String) -> Unit,
    onBuyFrame: (ProfileFrameItem) -> Unit,
    onEquipFrame: (String) -> Unit,
    onBuyCardFrame: (ProfileCardFrameItem) -> Unit,
    onEquipCardFrame: (String) -> Unit,
    onBuyProfileEffect: (ProfileVideoEffectItem, String?, Boolean) -> Unit,
    onEquipProfileEffect: (String, String?, Boolean) -> Unit,
    onPreviewOwnProfile: () -> Unit,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val strings = LocalAppStrings.current
    val langCode = LocalSelectedLanguageCode.current
    val isArabic = remember(langCode) {
        AppLanguageManager.resolveEffectiveCode(langCode).startsWith("ar")
    }

    var shopTab by remember { mutableIntStateOf(0) } // 0 = Ranks, 1 = Avatar Rings, 2 = Full Card Frames, 3 = Profile Videos
    var frameCategoryFilter by remember { mutableStateOf("all") }
    var cardFrameCategoryFilter by remember { mutableStateOf("all") }
    var processingVideoError by remember { mutableStateOf<String?>(null) }
    var isProcessingVideo by remember { mutableStateOf(false) }
    var pendingUploadEffectId by remember { mutableStateOf("custom_video") }
    var previewedCardFrame by remember { mutableStateOf<ProfileCardFrameItem?>(null) }

    val shopCardInfinite = rememberInfiniteTransition(label = "shopCardShowcase")
    val shopCardAngle by shopCardInfinite.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 10500, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "shopCardAngle"
    )
    val shopCardPulse by shopCardInfinite.animateFloat(
        initialValue = 0.25f,
        targetValue = 0.72f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 3200, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "shopCardPulse"
    )

    val balance = profile?.peyPoints ?: 200L
    val currentRank = remember(profile?.rank, profile?.rankExpiresAtMs) {
        CordlyRepository.effectiveRank(profile)
    }
    val ownedFrames = profile?.ownedFrames?.ifEmpty { listOf("none") } ?: listOf("none")
    val equippedFrame = profile?.equippedFrame ?: "none"
    val ownedCardFrames = profile?.ownedCardFrames?.ifEmpty { listOf("none") } ?: listOf("none")
    val equippedCardFrame = profile?.equippedCardFrame ?: "none"
    val ownedEffects = profile?.ownedProfileEffects?.ifEmpty { listOf("none") } ?: listOf("none")
    val equippedEffect = profile?.equippedProfileEffect ?: "none"
    val uid = profile?.userId ?: ""

    val videoPicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        val targetEffectId = pendingUploadEffectId
        if (uri != null && uid.isNotBlank()) {
            processingVideoError = null
            isProcessingVideo = true
            scope.launch {
                val targetEffect = PeyShopCatalog.findEffectById(targetEffectId)
                val processedResult = if (targetEffect.isUnlimitedDuration) {
                    VideoEffectHelper.processPickedFullVideo(context, uri, uid)
                } else {
                    VideoEffectHelper.processPickedVideoTo5Seconds(context, uri, uid)
                }
                isProcessingVideo = false
                processedResult
                    .onSuccess { processed ->
                        val alreadyOwned = ownedEffects.contains(targetEffect.id)
                        if (alreadyOwned) {
                            onEquipProfileEffect(targetEffect.id, processed.dataUri, !processed.cloudSynced)
                        } else {
                            onBuyProfileEffect(targetEffect, processed.dataUri, !processed.cloudSynced)
                        }
                    }
                    .onFailure { err ->
                        processingVideoError = err.localizedMessage ?: "Failed to load selected video."
                    }
            }
        }
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(
            usePlatformDefaultWidth = false,
            decorFitsSystemWindows = false
        )
    ) {
        SanitizeDialogWindowIds()
        Surface(
            color = CordlySurfaceDark,
            shape = RoundedCornerShape(24.dp),
            border = BorderStroke(1.dp, CordlyAmber.copy(alpha = 0.45f)),
            modifier = Modifier
                .systemBarsPadding()
                .fillMaxWidth(0.95f)
                .widthIn(max = 580.dp)
                .padding(vertical = 14.dp)
                .testTag("pey_shop_dialog")
        ) {
            Column(modifier = Modifier.fillMaxWidth()) {
                // Header
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(CordlyRailBg)
                        .padding(horizontal = 16.dp, vertical = 14.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                        Surface(
                            color = CordlyAmber.copy(alpha = 0.18f),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.size(40.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(Icons.Rounded.ShoppingBag, contentDescription = null, tint = CordlyAmber)
                            }
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = strings.tabPeyShop,
                                style = MaterialTheme.typography.titleLarge,
                                color = CordlyTextPrimary,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "$balance P E Y • ${rankDisplayTitle(currentRank, isArabic)}",
                                style = MaterialTheme.typography.labelSmall,
                                color = CordlyAmber,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                    OutlinedButton(
                        onClick = onPreviewOwnProfile,
                        shape = RoundedCornerShape(10.dp),
                        border = BorderStroke(1.dp, CordlyCyan),
                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp)
                    ) {
                        Icon(Icons.Rounded.PlayCircleFilled, contentDescription = null, tint = CordlyCyan, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(if (isArabic) "معاينة" else "Preview", color = CordlyCyan, style = MaterialTheme.typography.labelSmall)
                    }
                    Spacer(modifier = Modifier.width(4.dp))
                    IconButton(onClick = onDismiss, modifier = Modifier.minimumInteractiveComponentSize()) {
                        Icon(Icons.Rounded.Close, contentDescription = strings.closeButton, tint = CordlyTextSecondary)
                    }
                }

                // Isolated Shop Tabs: Ranks | Circular Avatar Rings | Full Profile Card Frames | Profile Videos
                Row(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(CordlyRailBg)
                        .horizontalScroll(rememberScrollState())
                        .padding(start = 14.dp, end = 14.dp, bottom = 12.dp)
                ) {
                    ShopTopTabPill(
                        icon = Icons.Rounded.Diamond,
                        label = strings.shopRanksTab,
                        selected = shopTab == 0,
                        testTag = "shop_tab_ranks",
                        onClick = { shopTab = 0 }
                    )
                    ShopTopTabPill(
                        icon = Icons.Rounded.Stars,
                        label = strings.shopAvatarFramesTab,
                        selected = shopTab == 1,
                        testTag = "shop_tab_frames",
                        onClick = { shopTab = 1 }
                    )
                    ShopTopTabPill(
                        icon = Icons.Rounded.Style,
                        label = strings.shopCardFramesTab,
                        selected = shopTab == 2,
                        testTag = "shop_tab_card_frames",
                        onClick = { shopTab = 2 }
                    )
                    ShopTopTabPill(
                        icon = Icons.Rounded.VideoLibrary,
                        label = strings.shopVideosTab,
                        selected = shopTab == 3,
                        testTag = "shop_tab_videos",
                        onClick = { shopTab = 3 }
                    )
                }

                HorizontalDivider(color = CordlyDivider)

                when (shopTab) {
                    0 -> RanksShopList(
                        profile = profile,
                        currentRank = currentRank,
                        balance = balance,
                        isBusy = isBusy,
                        isArabic = isArabic,
                        strings = strings,
                        onActivateFreeTrial = onActivateFreeTrial,
                        onPurchaseRank = onPurchaseRank
                    )
                    1 -> {
                        val filteredFrames = remember(frameCategoryFilter) {
                            when (frameCategoryFilter) {
                                "all" -> PeyShopCatalog.frames
                                else -> PeyShopCatalog.frames.filter { it.category == frameCategoryFilter }
                            }
                        }
                        LazyColumn(
                            contentPadding = PaddingValues(16.dp),
                            verticalArrangement = Arrangement.spacedBy(10.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            item {
                                Row(
                                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .horizontalScroll(rememberScrollState())
                                ) {
                                    listOf(
                                        "all" to (if (isArabic) "الكل" else "All"),
                                        "anime" to (if (isArabic) "أنمي HD (DBZ/JJK/BL)" else "Anime HD"),
                                        "gaming" to (if (isArabic) "ألعاب RGB" else "Gaming"),
                                        "standard" to (if (isArabic) "دائري" else "Classic")
                                    ).forEach { (key, title) ->
                                        FilterChip(
                                            selected = frameCategoryFilter == key,
                                            onClick = { frameCategoryFilter = key },
                                            label = { Text(title, maxLines = 1) },
                                            colors = FilterChipDefaults.filterChipColors(
                                                selectedContainerColor = CordlyBlurple,
                                                selectedLabelColor = Color.White
                                            )
                                        )
                                    }
                                }
                            }

                            items(filteredFrames, key = { it.id }) { frame ->
                                val isOwned = frame.id == "none" || ownedFrames.contains(frame.id)
                                val isEquipped = equippedFrame == frame.id
                                val canAfford = balance >= frame.price
                                val rankUnlocked = hasRequiredRank(currentRank, frame.requiredRank)
                                val rankTag = if (frame.requiredRank == CordlyRepository.RANK_NEO_PLUS) "NEO+" else "ULTRA"

                                val isAnimeAudioAvatar = SoundEffectsManager.isAnimeSoundAvatar(frame.id)
                                Surface(
                                    color = CordlyCardSurface,
                                    shape = RoundedCornerShape(16.dp),
                                    border = BorderStroke(
                                        width = if (isEquipped) 1.5.dp else 1.dp,
                                        color = if (isEquipped) CordlyEmerald else CordlyDivider
                                    ),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .then(
                                            if (isAnimeAudioAvatar) {
                                                Modifier.clickable {
                                                    SoundEffectsManager.playAvatarFrameSound(frame.id, context)
                                                }
                                            } else {
                                                Modifier
                                            }
                                        )
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        modifier = Modifier.padding(12.dp)
                                    ) {
                                        UserAvatar(
                                            displayName = profile?.displayName ?: "You",
                                            photoUrl = profile?.photoUrl,
                                            frameId = frame.id,
                                            size = 58.dp
                                        )
                                        Spacer(modifier = Modifier.width(12.dp))
                                        Column(modifier = Modifier.weight(1f)) {
                                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                                Text(
                                                    text = frame.title,
                                                    style = MaterialTheme.typography.titleSmall,
                                                    color = CordlyTextPrimary,
                                                    fontWeight = FontWeight.Bold,
                                                    maxLines = 1,
                                                    overflow = TextOverflow.Ellipsis,
                                                    modifier = Modifier.weight(1f, fill = false)
                                                )
                                                if (frame.requiredRank != "normal") {
                                                    Surface(
                                                        color = CordlyAmber.copy(alpha = 0.2f),
                                                        shape = RoundedCornerShape(6.dp)
                                                    ) {
                                                        Text(
                                                            text = rankTag,
                                                            style = MaterialTheme.typography.labelSmall,
                                                            color = CordlyAmber,
                                                            fontWeight = FontWeight.Bold,
                                                            modifier = Modifier.padding(horizontal = 5.dp, vertical = 1.dp)
                                                        )
                                                    }
                                                }
                                            }
                                            Text(
                                                text = frame.subtitle,
                                                style = MaterialTheme.typography.labelSmall,
                                                color = CordlyTextSecondary
                                            )
                                            Row(
                                                verticalAlignment = Alignment.CenterVertically,
                                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                                            ) {
                                                if (!isOwned) {
                                                    Text(
                                                        text = "${frame.price} P E Y",
                                                        style = MaterialTheme.typography.labelLarge,
                                                        color = CordlyAmber,
                                                        fontWeight = FontWeight.Bold
                                                    )
                                                }
                                                if (isAnimeAudioAvatar) {
                                                    val chipColor = frame.ringColors.firstOrNull() ?: CordlyCyan
                                                    Surface(
                                                        color = chipColor.copy(alpha = 0.16f),
                                                        shape = RoundedCornerShape(8.dp),
                                                        border = BorderStroke(1.dp, chipColor.copy(alpha = 0.55f)),
                                                        modifier = Modifier
                                                            .clip(RoundedCornerShape(8.dp))
                                                            .clickable {
                                                                SoundEffectsManager.playAvatarFrameSound(frame.id, context)
                                                            }
                                                    ) {
                                                        Row(
                                                            verticalAlignment = Alignment.CenterVertically,
                                                            modifier = Modifier.padding(horizontal = 7.dp, vertical = 2.dp)
                                                        ) {
                                                            Icon(
                                                                Icons.Rounded.PlayCircleFilled,
                                                                contentDescription = null,
                                                                tint = chipColor,
                                                                modifier = Modifier.size(13.dp)
                                                            )
                                                            Spacer(modifier = Modifier.width(3.dp))
                                                            Text(
                                                                text = if (isArabic) "صوت 🔊" else "SFX 🔊",
                                                                style = MaterialTheme.typography.labelSmall,
                                                                color = chipColor,
                                                                fontWeight = FontWeight.SemiBold
                                                            )
                                                        }
                                                    }
                                                }
                                            }
                                        }

                                        when {
                                            isEquipped -> {
                                                Surface(
                                                    color = CordlyEmerald.copy(alpha = 0.2f),
                                                    shape = RoundedCornerShape(10.dp),
                                                    border = BorderStroke(1.dp, CordlyEmerald)
                                                ) {
                                                    Text(
                                                        text = strings.equippedLabel,
                                                        style = MaterialTheme.typography.labelMedium,
                                                        color = CordlyEmerald,
                                                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)
                                                    )
                                                }
                                            }
                                            isOwned -> {
                                                OutlinedButton(
                                                    onClick = { onEquipFrame(frame.id) },
                                                    enabled = !isBusy,
                                                    shape = RoundedCornerShape(10.dp)
                                                ) {
                                                    Text(strings.equipButton, color = CordlyTextPrimary)
                                                }
                                            }
                                            !rankUnlocked -> {
                                                Surface(
                                                    color = CordlyElevatedSurface,
                                                    shape = RoundedCornerShape(10.dp),
                                                    border = BorderStroke(1.dp, CordlyAmber.copy(alpha = 0.5f)),
                                                    modifier = Modifier
                                                        .clip(RoundedCornerShape(10.dp))
                                                        .clickable { shopTab = 0 }
                                                ) {
                                                    Row(
                                                        verticalAlignment = Alignment.CenterVertically,
                                                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp)
                                                    ) {
                                                        Icon(Icons.Rounded.Lock, contentDescription = null, tint = CordlyAmber, modifier = Modifier.size(14.dp))
                                                        Spacer(modifier = Modifier.width(4.dp))
                                                        Text(rankTag, color = CordlyAmber, style = MaterialTheme.typography.labelSmall)
                                                    }
                                                }
                                            }
                                            else -> {
                                                Button(
                                                    onClick = { onBuyFrame(frame) },
                                                    enabled = canAfford && !isBusy,
                                                    colors = ButtonDefaults.buttonColors(
                                                        containerColor = if (canAfford) CordlyAmber else CordlyElevatedSurface,
                                                        contentColor = if (canAfford) Color(0xFF1A1200) else CordlyTextSecondary
                                                    ),
                                                    shape = RoundedCornerShape(10.dp),
                                                    modifier = Modifier.testTag("buy_frame_${frame.id}")
                                                ) {
                                                    Text(strings.buyButton)
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                    2 -> {
                        val filteredCardFrames = remember(cardFrameCategoryFilter) {
                            when (cardFrameCategoryFilter) {
                                "all" -> PeyShopCatalog.cardFrames
                                else -> PeyShopCatalog.cardFrames.filter { it.category == cardFrameCategoryFilter }
                            }
                        }
                        LazyColumn(
                            contentPadding = PaddingValues(16.dp),
                            verticalArrangement = Arrangement.spacedBy(10.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            item {
                                Row(
                                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .horizontalScroll(rememberScrollState())
                                ) {
                                    listOf(
                                        "all" to (if (isArabic) "الكل" else "All"),
                                        "anime" to (if (isArabic) "أنمي HD (DBZ/JJK/BL)" else "Anime HD"),
                                        "gaming" to (if (isArabic) "ألعاب RGB" else "Gaming"),
                                        "standard" to (if (isArabic) "كلاسيكي" else "Classic")
                                    ).forEach { (key, title) ->
                                        FilterChip(
                                            selected = cardFrameCategoryFilter == key,
                                            onClick = { cardFrameCategoryFilter = key },
                                            label = { Text(title, maxLines = 1) },
                                            colors = FilterChipDefaults.filterChipColors(
                                                selectedContainerColor = CordlyBlurple,
                                                selectedLabelColor = Color.White
                                            )
                                        )
                                    }
                                }
                            }

                            items(filteredCardFrames, key = { "cf_${it.id}" }) { cardFrame ->
                                val isOwned = cardFrame.id == "none" || ownedCardFrames.contains(cardFrame.id)
                                val isEquipped = equippedCardFrame == cardFrame.id
                                val canAfford = balance >= cardFrame.price
                                val rankUnlocked = hasRequiredRank(currentRank, cardFrame.requiredRank)
                                val rankTag = if (cardFrame.requiredRank == CordlyRepository.RANK_NEO_PLUS) "NEO+" else "ULTRA"
                                val cardShape = remember(cardFrame.id) { cardFrameShape(cardFrame.id) }
                                val animatedBorders = remember(cardFrame.borderColors, shopCardAngle, cardFrame.id) {
                                    if (cardFrame.id != "none" && cardFrame.borderColors.size >= 2) {
                                        val shift = ((shopCardAngle / 360f) * cardFrame.borderColors.size).toInt() % cardFrame.borderColors.size
                                        List(cardFrame.borderColors.size) { idx ->
                                            cardFrame.borderColors[(idx + shift) % cardFrame.borderColors.size]
                                        }
                                    } else {
                                        cardFrame.borderColors
                                    }
                                }

                                Surface(
                                    color = CordlyCardSurface,
                                    shape = RoundedCornerShape(16.dp),
                                    border = BorderStroke(
                                        width = if (isEquipped) 1.5.dp else 1.dp,
                                        color = if (isEquipped) CordlyEmerald else CordlyDivider
                                    ),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        modifier = Modifier.padding(12.dp)
                                    ) {
                                        // Mini Full Profile Card Preview with its OWN distinct architectural shape,
                                        // live continuous interior animation & animated external aura outside the border!
                                        Box(
                                            contentAlignment = Alignment.Center,
                                            modifier = Modifier
                                                .width(88.dp)
                                                .height(106.dp)
                                                .clickable { previewedCardFrame = cardFrame }
                                                .testTag("preview_card_frame_${cardFrame.id}")
                                        ) {
                                            if (cardFrame.id != "none") {
                                                ProfileCardExternalAuraOverlay(
                                                    cardFrameId = cardFrame.id,
                                                    borderColors = cardFrame.borderColors,
                                                    accentColor = cardFrame.accentColor,
                                                    animAngle = shopCardAngle,
                                                    pulse = shopCardPulse,
                                                    introActive = false,
                                                    introProgress = 0f,
                                                    insetDp = 10.dp,
                                                    modifier = Modifier.matchParentSize()
                                                )
                                            }
                                            Surface(
                                                color = CordlySurfaceDark,
                                                shape = cardShape,
                                                border = BorderStroke(
                                                    width = if (cardFrame.id != "none") 2.4.dp else 1.5.dp,
                                                    brush = Brush.linearGradient(animatedBorders)
                                                ),
                                                modifier = Modifier
                                                    .fillMaxSize()
                                                    .padding(10.dp)
                                            ) {
                                                Box(modifier = Modifier.fillMaxSize()) {
                                                    if (cardFrame.id == "card_bluelock_striker") {
                                                        Box(
                                                            modifier = Modifier
                                                                .fillMaxSize()
                                                                .background(
                                                                    Brush.verticalGradient(
                                                                        listOf(Color(0xFF03102A), Color(0xFF0C2A5A), Color(0xFF04142B))
                                                                    )
                                                                )
                                                        )
                                                    } else if (cardFrame.artworkResId != null) {
                                                        Image(
                                                            painter = painterResource(cardFrame.artworkResId),
                                                            contentDescription = cardFrame.title,
                                                            contentScale = ContentScale.Crop,
                                                            modifier = Modifier.fillMaxSize()
                                                        )
                                                        Box(
                                                            modifier = Modifier
                                                                .fillMaxSize()
                                                                .background(Color.Black.copy(alpha = 0.28f))
                                                        )
                                                    } else {
                                                        Box(
                                                            modifier = Modifier
                                                                .fillMaxWidth()
                                                                .height(28.dp)
                                                                .background(Brush.horizontalGradient(cardFrame.borderColors))
                                                        )
                                                    }

                                                    if (cardFrame.id != "none") {
                                                        ProfileCardAnimatedAmbientBackground(
                                                            cardFrameId = cardFrame.id,
                                                            accentColor = cardFrame.accentColor,
                                                            animAngle = shopCardAngle,
                                                            pulse = shopCardPulse,
                                                            modifier = Modifier.fillMaxSize()
                                                        )
                                                    }

                                                    if (isAnimeAnimatedCardFrame(cardFrame.id)) {
                                                        Surface(
                                                            color = Color.Black.copy(alpha = 0.78f),
                                                            shape = RoundedCornerShape(6.dp),
                                                            modifier = Modifier
                                                                .align(Alignment.TopEnd)
                                                                .padding(top = 8.dp, end = 6.dp)
                                                        ) {
                                                            Text(
                                                                text = cardEntranceDurationBadge(cardFrame.id),
                                                                style = MaterialTheme.typography.labelSmall,
                                                                color = cardFrame.accentColor,
                                                                fontWeight = FontWeight.Bold,
                                                                modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                                                            )
                                                        }
                                                    }
                                                    Box(
                                                        modifier = Modifier
                                                            .align(Alignment.CenterStart)
                                                            .padding(start = 10.dp, top = 6.dp)
                                                            .size(18.dp)
                                                            .clip(CircleShape)
                                                            .background(cardFrame.accentColor)
                                                    )
                                                    Box(
                                                        modifier = Modifier
                                                            .align(Alignment.BottomCenter)
                                                            .padding(horizontal = 10.dp, vertical = 10.dp)
                                                            .fillMaxWidth()
                                                            .height(10.dp)
                                                            .clip(RoundedCornerShape(4.dp))
                                                            .background(Color.Black.copy(alpha = 0.55f))
                                                    )
                                                }
                                            }
                                        }
                                        Spacer(modifier = Modifier.width(10.dp))
                                        Column(modifier = Modifier.weight(1f)) {
                                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                                Text(
                                                    text = cardFrame.title,
                                                    style = MaterialTheme.typography.titleSmall,
                                                    color = CordlyTextPrimary,
                                                    fontWeight = FontWeight.Bold,
                                                    maxLines = 1,
                                                    overflow = TextOverflow.Ellipsis,
                                                    modifier = Modifier.weight(1f, fill = false)
                                                )
                                                if (cardFrame.requiredRank != "normal") {
                                                    Surface(
                                                        color = CordlyAmber.copy(alpha = 0.2f),
                                                        shape = RoundedCornerShape(6.dp)
                                                    ) {
                                                        Text(
                                                            text = rankTag,
                                                            style = MaterialTheme.typography.labelSmall,
                                                            color = CordlyAmber,
                                                            fontWeight = FontWeight.Bold,
                                                            modifier = Modifier.padding(horizontal = 5.dp, vertical = 1.dp)
                                                        )
                                                    }
                                                }
                                            }
                                            Text(
                                                text = cardFrame.subtitle,
                                                style = MaterialTheme.typography.labelSmall,
                                                color = CordlyTextSecondary
                                            )
                                            Spacer(modifier = Modifier.height(4.dp))
                                            Row(
                                                verticalAlignment = Alignment.CenterVertically,
                                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                                            ) {
                                                if (!isOwned) {
                                                    Text(
                                                        text = "${cardFrame.price} P E Y",
                                                        style = MaterialTheme.typography.labelLarge,
                                                        color = CordlyAmber,
                                                        fontWeight = FontWeight.Bold
                                                    )
                                                }
                                                Surface(
                                                    color = cardFrame.accentColor.copy(alpha = 0.16f),
                                                    shape = RoundedCornerShape(8.dp),
                                                    border = BorderStroke(1.dp, cardFrame.accentColor.copy(alpha = 0.5f)),
                                                    modifier = Modifier
                                                        .clip(RoundedCornerShape(8.dp))
                                                        .clickable { previewedCardFrame = cardFrame }
                                                ) {
                                                    Row(
                                                        verticalAlignment = Alignment.CenterVertically,
                                                        modifier = Modifier.padding(horizontal = 7.dp, vertical = 3.dp)
                                                    ) {
                                                        Icon(
                                                            Icons.Rounded.PlayCircleFilled,
                                                            contentDescription = null,
                                                            tint = cardFrame.accentColor,
                                                            modifier = Modifier.size(13.dp)
                                                        )
                                                        Spacer(modifier = Modifier.width(3.dp))
                                                        Text(
                                                            text = if (isArabic) "معاينة" else "Preview",
                                                            style = MaterialTheme.typography.labelSmall,
                                                            color = cardFrame.accentColor,
                                                            fontWeight = FontWeight.SemiBold
                                                        )
                                                    }
                                                }
                                            }
                                        }
                                        Spacer(modifier = Modifier.width(8.dp))
                                        when {
                                            isEquipped -> {
                                                Surface(
                                                    color = CordlyEmerald.copy(alpha = 0.2f),
                                                    shape = RoundedCornerShape(10.dp),
                                                    border = BorderStroke(1.dp, CordlyEmerald)
                                                ) {
                                                    Text(
                                                        text = strings.equippedLabel,
                                                        style = MaterialTheme.typography.labelMedium,
                                                        color = CordlyEmerald,
                                                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)
                                                    )
                                                }
                                            }
                                            isOwned -> {
                                                OutlinedButton(
                                                    onClick = { onEquipCardFrame(cardFrame.id) },
                                                    enabled = !isBusy,
                                                    shape = RoundedCornerShape(10.dp)
                                                ) {
                                                    Text(strings.equipButton, color = CordlyTextPrimary)
                                                }
                                            }
                                            !rankUnlocked -> {
                                                Surface(
                                                    color = CordlyElevatedSurface,
                                                    shape = RoundedCornerShape(10.dp),
                                                    border = BorderStroke(1.dp, CordlyAmber.copy(alpha = 0.5f)),
                                                    modifier = Modifier
                                                        .clip(RoundedCornerShape(10.dp))
                                                        .clickable { shopTab = 0 }
                                                ) {
                                                    Row(
                                                        verticalAlignment = Alignment.CenterVertically,
                                                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp)
                                                    ) {
                                                        Icon(Icons.Rounded.Lock, contentDescription = null, tint = CordlyAmber, modifier = Modifier.size(14.dp))
                                                        Spacer(modifier = Modifier.width(4.dp))
                                                        Text(rankTag, color = CordlyAmber, style = MaterialTheme.typography.labelSmall)
                                                    }
                                                }
                                            }
                                            else -> {
                                                Button(
                                                    onClick = { onBuyCardFrame(cardFrame) },
                                                    enabled = canAfford && !isBusy,
                                                    colors = ButtonDefaults.buttonColors(
                                                        containerColor = if (canAfford) CordlyAmber else CordlyElevatedSurface,
                                                        contentColor = if (canAfford) Color(0xFF1A1200) else CordlyTextSecondary
                                                    ),
                                                    shape = RoundedCornerShape(10.dp),
                                                    modifier = Modifier.testTag("buy_card_frame_${cardFrame.id}")
                                                ) {
                                                    Text(strings.buyButton)
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                    else -> {
                        LazyColumn(
                            contentPadding = PaddingValues(16.dp),
                            verticalArrangement = Arrangement.spacedBy(10.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            if (processingVideoError != null) {
                                item {
                                    Surface(
                                        color = CordlyCrimson.copy(alpha = 0.16f),
                                        border = BorderStroke(1.dp, CordlyCrimson),
                                        shape = RoundedCornerShape(12.dp),
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Text(
                                            text = processingVideoError.orEmpty(),
                                            color = CordlyCrimson,
                                            style = MaterialTheme.typography.labelMedium,
                                            modifier = Modifier.padding(10.dp)
                                        )
                                    }
                                }
                            }

                            items(PeyShopCatalog.profileEffects, key = { "eff_${it.id}" }) { effect ->
                                val isOwned = effect.id == "none" || ownedEffects.contains(effect.id)
                                val isEquipped = equippedEffect == effect.id
                                val canAfford = balance >= effect.price
                                val rankUnlocked = hasRequiredRank(currentRank, effect.requiredRank)

                                Surface(
                                    color = CordlyCardSurface,
                                    shape = RoundedCornerShape(16.dp),
                                    border = BorderStroke(
                                        width = if (isEquipped) 1.5.dp else 1.dp,
                                        color = if (isEquipped) CordlyCyan else if (effect.isUnlimitedDuration) CordlyAmber.copy(alpha = 0.65f) else CordlyDivider
                                    ),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        modifier = Modifier.padding(12.dp)
                                    ) {
                                        Surface(
                                            shape = RoundedCornerShape(12.dp),
                                            border = BorderStroke(1.dp, CordlyDivider),
                                            modifier = Modifier
                                                .width(56.dp)
                                                .height(68.dp)
                                                .clip(RoundedCornerShape(12.dp))
                                                .clickable(onClick = onPreviewOwnProfile)
                                        ) {
                                            Box(
                                                contentAlignment = Alignment.Center,
                                                modifier = Modifier
                                                    .fillMaxSize()
                                                    .background(Brush.linearGradient(effect.accentColors))
                                            ) {
                                                Icon(
                                                    imageVector = if (effect.isCustomUpload) Icons.Rounded.VideoLibrary else Icons.Rounded.PlayCircleFilled,
                                                    contentDescription = null,
                                                    tint = Color.White,
                                                    modifier = Modifier.size(22.dp)
                                                )
                                                if (effect.isUnlimitedDuration) {
                                                    Surface(
                                                        color = CordlyAmber,
                                                        shape = RoundedCornerShape(4.dp),
                                                        modifier = Modifier
                                                            .align(Alignment.BottomCenter)
                                                            .padding(bottom = 3.dp)
                                                    ) {
                                                        Text(
                                                            text = "∞ FULL",
                                                            style = MaterialTheme.typography.labelSmall,
                                                            color = Color.Black,
                                                            fontWeight = FontWeight.Bold,
                                                            modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                                                        )
                                                    }
                                                }
                                            }
                                        }
                                        Spacer(modifier = Modifier.width(12.dp))
                                        Column(modifier = Modifier.weight(1f)) {
                                            Text(effect.title, style = MaterialTheme.typography.titleSmall, color = CordlyTextPrimary, fontWeight = FontWeight.Bold)
                                            Text(effect.subtitle, style = MaterialTheme.typography.labelSmall, color = CordlyTextSecondary)
                                            if (!isOwned) {
                                                Text("${effect.price} P E Y", style = MaterialTheme.typography.labelLarge, color = CordlyAmber, fontWeight = FontWeight.Bold)
                                            }
                                        }
                                        Spacer(modifier = Modifier.width(8.dp))
                                        if (effect.isCustomUpload) {
                                            Column(horizontalAlignment = Alignment.End, verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                                Button(
                                                    onClick = {
                                                        pendingUploadEffectId = effect.id
                                                        videoPicker.launch(
                                                            PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.VideoOnly)
                                                        )
                                                    },
                                                    enabled = !isBusy && !isProcessingVideo && (isOwned || canAfford),
                                                    colors = ButtonDefaults.buttonColors(
                                                        containerColor = if (isOwned) CordlyCyan else CordlyAmber,
                                                        contentColor = Color(0xFF0A101B)
                                                    ),
                                                    shape = RoundedCornerShape(10.dp),
                                                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp)
                                                ) {
                                                    Text(if (isOwned) strings.pickVideoButton else strings.unlockButton)
                                                }
                                                if (isOwned && !isEquipped) {
                                                    OutlinedButton(
                                                        onClick = { onEquipProfileEffect(effect.id, null, false) },
                                                        enabled = !isBusy,
                                                        shape = RoundedCornerShape(10.dp),
                                                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp)
                                                    ) {
                                                        Text(strings.equipButton, color = CordlyTextPrimary, style = MaterialTheme.typography.labelSmall)
                                                    }
                                                }
                                            }
                                        } else {
                                            when {
                                                isEquipped -> {
                                                    Surface(
                                                        color = CordlyCyan.copy(alpha = 0.2f),
                                                        shape = RoundedCornerShape(10.dp),
                                                        border = BorderStroke(1.dp, CordlyCyan)
                                                    ) {
                                                        Text(strings.activeLabel, color = CordlyCyan, style = MaterialTheme.typography.labelMedium, modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp))
                                                    }
                                                }
                                                isOwned -> {
                                                    OutlinedButton(onClick = { onEquipProfileEffect(effect.id, null, false) }, enabled = !isBusy, shape = RoundedCornerShape(10.dp)) {
                                                        Text(strings.equipButton, color = CordlyTextPrimary)
                                                    }
                                                }
                                                !rankUnlocked -> {
                                                    Surface(
                                                        color = CordlyElevatedSurface,
                                                        shape = RoundedCornerShape(10.dp),
                                                        border = BorderStroke(1.dp, CordlyAmber.copy(alpha = 0.5f)),
                                                        modifier = Modifier
                                                            .clip(RoundedCornerShape(10.dp))
                                                            .clickable { shopTab = 0 }
                                                    ) {
                                                        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp)) {
                                                            Icon(Icons.Rounded.Lock, contentDescription = null, tint = CordlyAmber, modifier = Modifier.size(14.dp))
                                                            Spacer(modifier = Modifier.width(4.dp))
                                                            Text("Ultra", color = CordlyAmber, style = MaterialTheme.typography.labelSmall)
                                                        }
                                                    }
                                                }
                                                else -> {
                                                    Button(
                                                        onClick = { onBuyProfileEffect(effect, null, false) },
                                                        enabled = canAfford && !isBusy,
                                                        colors = ButtonDefaults.buttonColors(containerColor = CordlyAmber, contentColor = Color(0xFF1A1200)),
                                                        shape = RoundedCornerShape(10.dp)
                                                    ) {
                                                        Text(strings.buyButton)
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
            }
        }
    }

    previewedCardFrame?.let { previewItem ->
        DiscordProfileSheetDialog(
            targetProfile = PublicProfile(
                id = uid.ifBlank { "shop_preview" },
                userId = uid.ifBlank { "shop_preview" },
                displayName = profile?.displayName?.takeIf { it.isNotBlank() } ?: "You",
                friendCode = profile?.friendCode ?: "",
                status = profile?.status ?: "online",
                photoUrl = profile?.photoUrl,
                bannerUrl = profile?.bannerUrl,
                customStatus = profile?.customStatus,
                equippedFrame = equippedFrame,
                equippedCardFrame = previewItem.id,
                equippedProfileEffect = equippedEffect,
                profileVideoData = profile?.profileVideoData,
                avatarVideoData = profile?.avatarVideoData,
                rank = currentRank,
                rankExpiresAtMs = profile?.rankExpiresAtMs ?: 0L,
                profileThemeColorHex = profile?.profileThemeColorHex
            ),
            fallbackName = profile?.displayName?.takeIf { it.isNotBlank() } ?: "You",
            fallbackPhoto = profile?.photoUrl,
            fallbackUserId = uid.ifBlank { "shop_preview" },
            isOwnProfile = true,
            isLoading = false,
            onMessageUser = null,
            onDismiss = { previewedCardFrame = null }
        )
    }
}

@Composable
private fun RanksShopList(
    profile: UserPrivate?,
    currentRank: String,
    balance: Long,
    isBusy: Boolean,
    isArabic: Boolean,
    strings: AppStrings,
    onActivateFreeTrial: (String) -> Unit,
    onPurchaseRank: (String) -> Unit
) {
    LazyColumn(
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        // 1. Normal Rank (0 P E Y)
        item {
            RankTierCard(
                title = strings.rankNormalTitle,
                priceText = "0 P E Y",
                colors = listOf(Color(0xFF334155), Color(0xFF1E293B)),
                accent = CordlyTextSecondary,
                isActive = currentRank == CordlyRepository.RANK_NORMAL,
                perks = if (isArabic) {
                    listOf(
                        "إضافة الأصدقاء والدردشة الفورية بالصور والملصقات والصوت",
                        "مكالمات صوتية وفيديو مجانية + فلاتر سناب شات",
                        "تغيير خلفية وألوان التطبيق والوضع الليلي والنهاري"
                    )
                } else {
                    listOf(
                        "Friends, servers, voice/video calls & Snapchat face filters",
                        "Voice notes, stickers, GIFs, and standard photo messages",
                        "Night/Day mode, accent colors & classic avatar frames"
                    )
                },
                primaryButtonLabel = if (currentRank == CordlyRepository.RANK_NORMAL) strings.activeLabel else null,
                primaryEnabled = false,
                onPrimaryClick = {},
                trialButtonLabel = null,
                onTrialClick = {}
            )
        }

        // 2. Ultra Rank (3500 P E Y / 30d + 3-Day Free Trial)
        item {
            val isUltraActive = currentRank == CordlyRepository.RANK_ULTRA
            val canTryUltra = !(profile?.ultraTrialUsed ?: false) && currentRank == CordlyRepository.RANK_NORMAL
            RankTierCard(
                title = "${strings.rankUltraTitle} (ULTRA)",
                priceText = "3500 P E Y / 30d",
                colors = listOf(Color(0xFF312E81), Color(0xFF5865F2), Color(0xFF06B6D4)),
                accent = CordlyCyan,
                isActive = isUltraActive,
                perks = if (isArabic) {
                    listOf(
                        "إطارات وبانرات الأنمي HD (دراغون بول، جوجوتسو كايسن، بلو لوك) + ألعاب + إطارات مربعة كاملة",
                        "إرسال الصور بجودة 4K + جودة مكالمات 720p/1080p/4K + تسجيل المكالمات ومغير الصوت",
                        "شارة ألترا بجانب اسمك + تغيير لون البروفايل بالكامل + الظهور في قائمة الألترا"
                    )
                } else {
                    listOf(
                        "HD Anime (Dragon Ball, Jujutsu Kaisen & Blue Lock) + Gaming + Square Full frames",
                        "Send 4K photos + 720p/1080p/4K video calls + WAV call recording & 5 Voice FX",
                        "ULTRA badge, Full Profile Theme Color, and Ultra Directory listing"
                    )
                },
                primaryButtonLabel = if (isUltraActive) strings.activeLabel else "${strings.buyButton} 3500 P E Y",
                primaryEnabled = !isUltraActive && !isBusy && balance >= CordlyRepository.ULTRA_COST_PEY,
                onPrimaryClick = { onPurchaseRank(CordlyRepository.RANK_ULTRA) },
                trialButtonLabel = if (canTryUltra) strings.freeTrial3Days else null,
                onTrialClick = { onActivateFreeTrial(CordlyRepository.RANK_ULTRA) },
                testTag = "buy_ultra_rank_button"
            )
        }

        // 3. Neo Plus Rank (6000 P E Y / 30d + 1-Day Free Trial)
        item {
            val isNeoPlusActive = currentRank == CordlyRepository.RANK_NEO_PLUS
            val canTryNeoPlus = !(profile?.neoPlusTrialUsed ?: false) && currentRank != CordlyRepository.RANK_NEO_PLUS
            RankTierCard(
                title = "${strings.rankNeoPlusTitle} (NEO+)",
                priceText = "6000 P E Y / 30d",
                colors = listOf(Color(0xFF451A03), Color(0xFFF59E0B), Color(0xFFEC4899)),
                accent = CordlyAmber,
                isActive = isNeoPlusActive,
                perks = if (isArabic) {
                    listOf(
                        "كل مميزات الألترا + تثبيت اسمك في أعلى قائمة الألترا + توهج ذهبي لرسائلك يراه أصدقاؤك",
                        "تغيير شعار التطبيق + تخصيص تخطيط التطبيق + فيديو لصورة البروفايل الشخصية",
                        "تحكم كامل في السيرفر (طرد أعضاء) + مكالمات جماعية متعددين + +100 P E Y إضافية مع كل مكافأة",
                        "إنشاء مهام مخصصة + التحدث مع المطور مباشرة"
                    )
                } else {
                    listOf(
                        "All Ultra perks + pinned top of Ultra Directory + golden message aura seen by friends",
                        "Switch App Launcher Icon + Custom App Layout + Video Avatar",
                        "Full server control (kick members) + Multi-person call invites + +100 P E Y bonus on every reward",
                        "Create Custom Quests for everyone + Direct Developer Contact"
                    )
                },
                primaryButtonLabel = if (isNeoPlusActive) strings.activeLabel else "${strings.buyButton} 6000 P E Y",
                primaryEnabled = !isNeoPlusActive && !isBusy && balance >= CordlyRepository.NEO_PLUS_COST_PEY,
                onPrimaryClick = { onPurchaseRank(CordlyRepository.RANK_NEO_PLUS) },
                trialButtonLabel = if (canTryNeoPlus) strings.freeTrial1Day else null,
                onTrialClick = { onActivateFreeTrial(CordlyRepository.RANK_NEO_PLUS) },
                testTag = "buy_neo_plus_rank_button"
            )
        }
    }
}

@Composable
private fun RankTierCard(
    title: String,
    priceText: String,
    colors: List<Color>,
    accent: Color,
    isActive: Boolean,
    perks: List<String>,
    primaryButtonLabel: String?,
    primaryEnabled: Boolean,
    onPrimaryClick: () -> Unit,
    trialButtonLabel: String?,
    onTrialClick: () -> Unit,
    testTag: String = ""
) {
    Surface(
        color = CordlyCardSurface,
        shape = RoundedCornerShape(18.dp),
        border = BorderStroke(if (isActive) 2.dp else 1.dp, if (isActive) accent else CordlyDivider),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Brush.horizontalGradient(colors))
                    .padding(14.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                        Icon(Icons.Rounded.AutoAwesome, contentDescription = null, tint = Color.White, modifier = Modifier.size(20.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(title, style = MaterialTheme.typography.titleMedium, color = Color.White, fontWeight = FontWeight.Bold)
                    }
                    Surface(color = Color.Black.copy(alpha = 0.4f), shape = RoundedCornerShape(8.dp)) {
                        Text(priceText, color = accent, style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp))
                    }
                }
            }
            Column(verticalArrangement = Arrangement.spacedBy(6.dp), modifier = Modifier.padding(14.dp)) {
                perks.forEach { line ->
                    Row(verticalAlignment = Alignment.Top) {
                        Text("• ", color = accent, fontWeight = FontWeight.Bold)
                        Text(line, style = MaterialTheme.typography.bodySmall, color = CordlyTextSecondary)
                    }
                }
                if (primaryButtonLabel != null || trialButtonLabel != null) {
                    Spacer(modifier = Modifier.height(6.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                        if (primaryButtonLabel != null) {
                            Button(
                                onClick = onPrimaryClick,
                                enabled = primaryEnabled,
                                colors = ButtonDefaults.buttonColors(containerColor = accent, contentColor = Color.Black),
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier
                                    .weight(1f)
                                    .then(if (testTag.isNotBlank()) Modifier.testTag(testTag) else Modifier)
                            ) {
                                Text(primaryButtonLabel, fontWeight = FontWeight.Bold)
                            }
                        }
                        if (trialButtonLabel != null) {
                            OutlinedButton(
                                onClick = onTrialClick,
                                border = BorderStroke(1.dp, accent),
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier.weight(1f)
                            ) {
                                Text(trialButtonLabel, color = accent, style = MaterialTheme.typography.labelMedium, maxLines = 1, overflow = TextOverflow.Ellipsis)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ShopTopTabPill(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
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
            .height(42.dp)
            .clip(RoundedCornerShape(12.dp))
            .clickable(onClick = onClick)
            .testTag(testTag)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center,
            modifier = Modifier.padding(horizontal = 8.dp)
        ) {
            Icon(icon, contentDescription = null, tint = if (selected) Color.White else CordlyTextSecondary, modifier = Modifier.size(16.dp))
            Spacer(modifier = Modifier.width(5.dp))
            Text(label, style = MaterialTheme.typography.labelMedium, color = if (selected) Color.White else CordlyTextSecondary, maxLines = 1, overflow = TextOverflow.Ellipsis)
        }
    }
}

fun hasRequiredRank(currentRank: String, requiredRank: String): Boolean {
    return when (requiredRank) {
        CordlyRepository.RANK_NEO_PLUS -> currentRank == CordlyRepository.RANK_NEO_PLUS
        CordlyRepository.RANK_ULTRA -> currentRank == CordlyRepository.RANK_ULTRA || currentRank == CordlyRepository.RANK_NEO_PLUS
        else -> true
    }
}

fun rankDisplayTitle(rank: String, isArabic: Boolean): String = when (rank) {
    CordlyRepository.RANK_NEO_PLUS -> if (isArabic) "رتبة نيو بلس" else "NEO PLUS"
    CordlyRepository.RANK_ULTRA -> if (isArabic) "رتبة ألترا" else "ULTRA"
    else -> if (isArabic) "الرتبة العادية" else "NORMAL"
}
