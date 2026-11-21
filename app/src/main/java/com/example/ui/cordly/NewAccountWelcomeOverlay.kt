package com.example.ui.cordly

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.scaleIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowForward
import androidx.compose.material.icons.rounded.AutoAwesome
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.Headphones
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.R
import com.example.ui.theme.CordlyAmber
import com.example.ui.theme.CordlyBlurple
import com.example.ui.theme.CordlyBlurpleBright
import com.example.ui.theme.CordlyCanvasBg
import com.example.ui.theme.CordlyCardSurface
import com.example.ui.theme.CordlyCyan
import com.example.ui.theme.CordlyEmerald
import com.example.ui.theme.CordlySurfaceDark
import com.example.ui.theme.CordlyTextPrimary
import com.example.ui.theme.CordlyTextSecondary
import kotlinx.coroutines.delay
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

private data class WelcomeBurstParticle(
    val angleRad: Float,
    val distanceFactor: Float,
    val radiusDp: Float,
    val colorIndex: Int,
    val phaseOffset: Float
)

@Composable
fun NewAccountWelcomeOverlay(
    displayName: String,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    val strings = LocalAppStrings.current
    val logoVariant = LocalAppLogoVariant.current

    BackHandler { onDismiss() }

    val cardScale = remember { Animatable(0.76f) }
    val cardAlpha = remember { Animatable(0f) }
    val emblemScale = remember { Animatable(0.32f) }
    val burstProgress = remember { Animatable(0f) }
    var showBonusBadge by remember { mutableStateOf(false) }
    var showActionRow by remember { mutableStateOf(false) }

    val particles = remember {
        List(22) { idx ->
            val angle = ((idx.toFloat() / 22f) * 2f * PI.toFloat()) + (idx % 3) * 0.14f
            WelcomeBurstParticle(
                angleRad = angle,
                distanceFactor = 0.42f + (idx % 5) * 0.12f,
                radiusDp = 3.2f + (idx % 4) * 1.4f,
                colorIndex = idx % 4,
                phaseOffset = (idx * 0.37f) % (2f * PI.toFloat())
            )
        }
    }

    val infiniteTransition = rememberInfiniteTransition(label = "newAccountWelcomeLoop")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 0.98f,
        targetValue = 1.05f,
        animationSpec = infiniteRepeatable(
            animation = tween(1700, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "emblemPulse"
    )
    val heroZoom by infiniteTransition.animateFloat(
        initialValue = 1.0f,
        targetValue = 1.07f,
        animationSpec = infiniteRepeatable(
            animation = tween(4200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "heroImageZoom"
    )
    val shimmerPhase by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = (2f * PI).toFloat(),
        animationSpec = infiniteRepeatable(
            animation = tween(3200, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "particleOrbit"
    )

    LaunchedEffect(displayName) {
        SoundEffectsManager.play(AppSoundEffect.NEW_ACCOUNT_CELEBRATION)
        cardAlpha.animateTo(1f, animationSpec = tween(260, easing = FastOutSlowInEasing))
        cardScale.animateTo(
            targetValue = 1f,
            animationSpec = spring(
                dampingRatio = 0.68f,
                stiffness = Spring.StiffnessMediumLow
            )
        )
        burstProgress.animateTo(1f, animationSpec = tween(900, easing = FastOutSlowInEasing))
    }

    LaunchedEffect(displayName) {
        delay(160L)
        emblemScale.animateTo(
            targetValue = 1f,
            animationSpec = spring(
                dampingRatio = Spring.DampingRatioMediumBouncy,
                stiffness = Spring.StiffnessMediumLow
            )
        )
        delay(240L)
        showBonusBadge = true
        SoundEffectsManager.play(AppSoundEffect.BONUS_SPARKLE)
        delay(220L)
        showActionRow = true
    }

    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier
            .fillMaxSize()
            .background(CordlyCanvasBg.copy(alpha = 0.90f))
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onDismiss
            )
            .statusBarsPadding()
            .navigationBarsPadding()
            .padding(horizontal = 18.dp, vertical = 20.dp)
    ) {
        // Full-backdrop celebration rings & confetti burst Canvas
        Canvas(modifier = Modifier.fillMaxSize()) {
            val center = Offset(size.width / 2f, size.height * 0.44f)
            val maxRadius = size.minDimension * 0.56f
            val progress = burstProgress.value

            val ringRadius1 = maxRadius * (0.28f + 0.58f * progress)
            val ringAlpha1 = ((1f - progress) * 0.45f).coerceIn(0f, 0.45f)
            if (ringAlpha1 > 0.01f) {
                drawCircle(
                    color = CordlyCyan.copy(alpha = ringAlpha1),
                    radius = ringRadius1,
                    center = center,
                    style = Stroke(width = 3.dp.toPx())
                )
            }

            val ringRadius2 = maxRadius * (0.15f + 0.72f * progress)
            val ringAlpha2 = ((1f - progress * 0.85f) * 0.30f).coerceIn(0f, 0.30f)
            if (ringAlpha2 > 0.01f) {
                drawCircle(
                    color = CordlyBlurpleBright.copy(alpha = ringAlpha2),
                    radius = ringRadius2,
                    center = center,
                    style = Stroke(width = 2.dp.toPx())
                )
            }

            val palette = listOf(CordlyCyan, CordlyBlurpleBright, CordlyAmber, CordlyEmerald)
            particles.forEach { p ->
                val radius = maxRadius * p.distanceFactor * (0.35f + 0.65f * progress)
                val drift = sin(shimmerPhase + p.phaseOffset) * 8.dp.toPx()
                val px = center.x + cos(p.angleRad) * radius + drift * 0.5f
                val py = center.y + sin(p.angleRad) * radius + drift
                val twinkle = 0.45f + 0.55f * ((sin(shimmerPhase * 1.4f + p.phaseOffset) + 1f) / 2f)
                drawCircle(
                    color = palette[p.colorIndex].copy(alpha = twinkle * cardAlpha.value * 0.85f),
                    radius = p.radiusDp.dp.toPx(),
                    center = Offset(px, py)
                )
            }
        }

        // Main Celebratory Showcase Card
        Surface(
            color = CordlySurfaceDark,
            shape = RoundedCornerShape(28.dp),
            border = BorderStroke(
                width = 1.6.dp,
                brush = Brush.linearGradient(
                    listOf(CordlyBlurpleBright, CordlyCyan, CordlyAmber)
                )
            ),
            tonalElevation = 12.dp,
            modifier = Modifier
                .widthIn(max = 430.dp)
                .fillMaxWidth()
                .scale(cardScale.value)
                .alpha(cardAlpha.value)
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null,
                    onClick = {}
                )
                .testTag("welcome_new_banner")
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.fillMaxWidth()
            ) {
                // Large Hero Image + Overlapping Big N-with-Headphones Emblem
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(238.dp)
                ) {
                    // Big Hero Artwork Container
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(192.dp)
                            .clip(RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp))
                    ) {
                        AsyncImage(
                            model = R.drawable.img_cordly_hero,
                            contentDescription = strings.welcomeNewTitle,
                            contentScale = ContentScale.Crop,
                            modifier = Modifier
                                .fillMaxSize()
                                .scale(heroZoom)
                        )
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(
                                    Brush.verticalGradient(
                                        colors = listOf(
                                            CordlyBlurple.copy(alpha = 0.28f),
                                            Color.Transparent,
                                            CordlySurfaceDark.copy(alpha = 0.92f)
                                        )
                                    )
                                )
                        )

                        // Close button top-end
                        IconButton(
                            onClick = onDismiss,
                            modifier = Modifier
                                .align(Alignment.TopEnd)
                                .padding(10.dp)
                                .minimumInteractiveComponentSize()
                                .clip(CircleShape)
                                .background(CordlyCanvasBg.copy(alpha = 0.55f))
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.Close,
                                contentDescription = strings.closeButton,
                                tint = CordlyTextPrimary
                            )
                        }
                    }

                    // Overlapping Large Animated "N with Headphones" Emblem
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .align(Alignment.BottomCenter)
                            .scale(emblemScale.value * pulseScale)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(116.dp)
                                .clip(CircleShape)
                                .background(
                                    Brush.radialGradient(
                                        listOf(
                                            CordlyCyan.copy(alpha = 0.55f),
                                            CordlyBlurple.copy(alpha = 0.25f),
                                            Color.Transparent
                                        )
                                    )
                                )
                        )
                        Surface(
                            color = CordlySurfaceDark,
                            shape = RoundedCornerShape(30.dp),
                            border = BorderStroke(
                                2.5.dp,
                                Brush.linearGradient(listOf(CordlyCyan, CordlyBlurpleBright))
                            ),
                            shadowElevation = 10.dp
                        ) {
                            Box(modifier = Modifier.padding(4.dp)) {
                                NeovBrandEmblem(
                                    variant = logoVariant,
                                    size = 94.dp,
                                    cornerRadius = 26.dp
                                )
                            }
                        }
                    }
                }

                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp, vertical = 14.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center,
                        modifier = Modifier
                            .clip(RoundedCornerShape(50))
                            .background(CordlyBlurple.copy(alpha = 0.16f))
                            .padding(horizontal = 12.dp, vertical = 5.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Headphones,
                            contentDescription = null,
                            tint = CordlyCyan,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "neov chat",
                            style = MaterialTheme.typography.labelMedium,
                            color = CordlyCyan,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Text(
                        text = strings.welcomeNewTitle,
                        style = MaterialTheme.typography.headlineSmall,
                        color = CordlyTextPrimary,
                        fontWeight = FontWeight.ExtraBold,
                        textAlign = TextAlign.Center
                    )

                    if (displayName.isNotBlank()) {
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = displayName,
                            style = MaterialTheme.typography.titleLarge,
                            color = CordlyCyan,
                            fontWeight = FontWeight.Bold,
                            textAlign = TextAlign.Center,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = strings.welcomeFirstTimeSubtitle,
                        style = MaterialTheme.typography.bodyMedium,
                        color = CordlyTextSecondary,
                        textAlign = TextAlign.Center,
                        lineHeight = 20.sp
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    AnimatedVisibility(
                        visible = showBonusBadge,
                        enter = scaleIn(
                            initialScale = 0.72f,
                            animationSpec = spring(
                                dampingRatio = Spring.DampingRatioMediumBouncy,
                                stiffness = Spring.StiffnessMediumLow
                            )
                        ) + fadeIn(tween(240))
                    ) {
                        Surface(
                            color = CordlyCardSurface,
                            shape = RoundedCornerShape(16.dp),
                            border = BorderStroke(1.2.dp, CordlyAmber.copy(alpha = 0.85f))
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.Center,
                                modifier = Modifier
                                    .background(
                                        Brush.horizontalGradient(
                                            listOf(
                                                CordlyAmber.copy(alpha = 0.16f),
                                                CordlyCyan.copy(alpha = 0.14f)
                                            )
                                        )
                                    )
                                    .padding(horizontal = 16.dp, vertical = 10.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Rounded.AutoAwesome,
                                    contentDescription = null,
                                    tint = CordlyAmber,
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = strings.welcomeBadge,
                                    style = MaterialTheme.typography.labelLarge,
                                    color = CordlyAmber,
                                    fontWeight = FontWeight.ExtraBold
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(18.dp))

                    AnimatedVisibility(
                        visible = showActionRow,
                        enter = slideInVertically { it / 2 } + fadeIn(tween(260))
                    ) {
                        Button(
                            onClick = onDismiss,
                            shape = RoundedCornerShape(16.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = CordlyBlurple,
                                contentColor = Color.White
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(50.dp)
                                .testTag("welcome_new_continue_button")
                        ) {
                            Text(
                                text = strings.welcomeStartButton,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Icon(
                                imageVector = Icons.AutoMirrored.Rounded.ArrowForward,
                                contentDescription = null,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(6.dp))
                }
            }
        }
    }
}
