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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.AutoAwesome
import androidx.compose.material.icons.rounded.Celebration
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.Diamond
import androidx.compose.material.icons.rounded.EmojiEvents
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
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.repository.PromoRedeemResult
import com.example.ui.theme.CordlyAmber
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
import kotlin.math.roundToInt
import kotlin.math.sin

private data class LegendaryConfettiPiece(
    val angleRad: Float,
    val speedFactor: Float,
    val sizeDp: Float,
    val colorIndex: Int,
    val phaseOffset: Float,
    val isDiamond: Boolean
)

/**
 * Full-screen Legendary Celebration Overlay triggered when one of the first 50 users
 * enters the secret promo code `12340` in the Friends section and wins 20,000 P E Y Points.
 */
@Composable
fun SecretCodeCelebrationOverlay(
    result: PromoRedeemResult,
    isArabic: Boolean,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    BackHandler { onDismiss() }

    val cardScale = remember { Animatable(0.55f) }
    val cardAlpha = remember { Animatable(0f) }
    val burstProgress = remember { Animatable(0f) }
    val pointsCounter = remember { Animatable(0f) }
    var showRewardCard by remember { mutableStateOf(false) }
    var showClaimButton by remember { mutableStateOf(false) }

    val confetti = remember {
        List(56) { idx ->
            val angle = ((idx.toFloat() / 56f) * 2f * PI.toFloat()) + (idx % 5) * 0.11f
            LegendaryConfettiPiece(
                angleRad = angle,
                speedFactor = 0.28f + (idx % 7) * 0.11f,
                sizeDp = 4.0f + (idx % 4) * 2.2f,
                colorIndex = idx % 5,
                phaseOffset = (idx * 0.43f) % (2f * PI.toFloat()),
                isDiamond = idx % 2 == 0
            )
        }
    }

    val infiniteTransition = rememberInfiniteTransition(label = "secretCodeLegendaryLoop")
    val rayRotation by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(14000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "legendaryRaysSpin"
    )
    val crownPulse by infiniteTransition.animateFloat(
        initialValue = 0.94f,
        targetValue = 1.09f,
        animationSpec = infiniteRepeatable(
            animation = tween(1100, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "legendaryCrownPulse"
    )
    val driftPhase by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = (2f * PI).toFloat(),
        animationSpec = infiniteRepeatable(
            animation = tween(3600, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "legendaryDrift"
    )

    LaunchedEffect(result.code, result.claimRank) {
        SoundEffectsManager.play(AppSoundEffect.NEW_ACCOUNT_CELEBRATION)
        cardAlpha.animateTo(1f, tween(220))
        cardScale.animateTo(
            targetValue = 1f,
            animationSpec = spring(
                dampingRatio = 0.56f,
                stiffness = Spring.StiffnessMediumLow
            )
        )
    }

    LaunchedEffect(result.code, result.claimRank) {
        burstProgress.animateTo(1f, tween(1350, easing = FastOutSlowInEasing))
    }

    LaunchedEffect(result.code, result.claimRank) {
        delay(220L)
        showRewardCard = true
        pointsCounter.animateTo(
            targetValue = result.awardedPoints.toFloat(),
            animationSpec = tween(durationMillis = 1250, easing = FastOutSlowInEasing)
        )
        SoundEffectsManager.play(AppSoundEffect.BONUS_SPARKLE)
        delay(160L)
        showClaimButton = true
    }

    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFF04060D).copy(alpha = 0.94f))
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onDismiss
            )
            .statusBarsPadding()
            .navigationBarsPadding()
            .padding(horizontal = 16.dp, vertical = 20.dp)
            .testTag("secret_code_celebration_overlay")
    ) {
        // Rotating Legendary Golden Rays + Shockwaves + Multi-color Confetti Explosion
        Canvas(modifier = Modifier.fillMaxSize()) {
            val center = Offset(size.width * 0.5f, size.height * 0.42f)
            val maxR = size.minDimension * 0.68f
            val p = burstProgress.value

            // Rotating Celestial Sunburst Light Rays behind card
            val rayCount = 12
            rotate(rayRotation, pivot = center) {
                for (i in 0 until rayCount) {
                    val startA = (i * (360f / rayCount)) * PI.toFloat() / 180f
                    val halfSpan = 0.10f
                    val rayPath = Path().apply {
                        moveTo(center.x, center.y)
                        lineTo(center.x + cos(startA - halfSpan) * maxR * 1.4f, center.y + sin(startA - halfSpan) * maxR * 1.4f)
                        lineTo(center.x + cos(startA + halfSpan) * maxR * 1.4f, center.y + sin(startA + halfSpan) * maxR * 1.4f)
                        close()
                    }
                    drawPath(
                        rayPath,
                        brush = Brush.radialGradient(
                            colors = listOf(
                                (if (i % 2 == 0) Color(0xFFFDE047) else CordlyCyan).copy(alpha = 0.20f * cardAlpha.value),
                                Color.Transparent
                            ),
                            center = center,
                            radius = maxR * 1.35f
                        )
                    )
                }
            }

            // Triple Expanding Golden & Neon Shockwave Rings
            listOf(
                Triple(0f, Color(0xFFFDE047), 4.5f),
                Triple(0.18f, CordlyCyan, 3.2f),
                Triple(0.36f, Color(0xFFEC4899), 2.8f)
            ).forEach { (delayFraction, color, strokeWidthDp) ->
                val ringP = ((p - delayFraction) / (1f - delayFraction)).coerceIn(0f, 1f)
                if (ringP > 0f && ringP < 1f) {
                    drawCircle(
                        color = color.copy(alpha = (1f - ringP) * 0.70f),
                        radius = maxR * (0.14f + 0.95f * ringP),
                        center = center,
                        style = Stroke(width = strokeWidthDp.dp.toPx() * (1f - ringP * 0.4f))
                    )
                }
            }

            // 56 Drifting Diamond & Circular Confetti Particles
            val palette = listOf(
                Color(0xFFFDE047),
                CordlyAmber,
                CordlyCyan,
                Color(0xFFEC4899),
                CordlyEmerald
            )
            confetti.forEach { piece ->
                val dist = maxR * piece.speedFactor * (0.28f + 0.72f * p)
                val bobX = cos(driftPhase * 1.1f + piece.phaseOffset) * 10.dp.toPx()
                val bobY = sin(driftPhase * 1.3f + piece.phaseOffset) * 14.dp.toPx()
                val px = center.x + cos(piece.angleRad) * dist + bobX
                val py = center.y + sin(piece.angleRad) * dist + bobY
                val twinkle = 0.55f + 0.45f * sin(driftPhase * 2f + piece.phaseOffset)
                val color = palette[piece.colorIndex].copy(alpha = twinkle * cardAlpha.value * 0.92f)
                val s = piece.sizeDp.dp.toPx()
                if (piece.isDiamond) {
                    rotate((rayRotation * 1.8f + piece.colorIndex * 35f), pivot = Offset(px, py)) {
                        drawRect(
                            color = color,
                            topLeft = Offset(px - s * 0.5f, py - s * 0.5f),
                            size = Size(s, s)
                        )
                    }
                } else {
                    drawCircle(color = color, radius = s * 0.55f, center = Offset(px, py))
                }
            }
        }

        // Main Legendary Winner Card
        Surface(
            color = CordlySurfaceDark,
            shape = RoundedCornerShape(30.dp),
            border = BorderStroke(
                width = 2.4.dp,
                brush = Brush.linearGradient(
                    listOf(Color(0xFFFDE047), CordlyAmber, CordlyCyan, Color(0xFFEC4899), Color(0xFFFDE047))
                )
            ),
            shadowElevation = 24.dp,
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
        ) {
            Box(modifier = Modifier.fillMaxWidth()) {
                Box(
                    modifier = Modifier
                        .matchParentSize()
                        .background(
                            Brush.verticalGradient(
                                colors = listOf(
                                    Color(0xFFF59E0B).copy(alpha = 0.20f),
                                    CordlyBlurpleBright.copy(alpha = 0.12f),
                                    CordlySurfaceDark
                                )
                            )
                        )
                )

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
                        contentDescription = if (isArabic) "إغلاق" else "Close",
                        tint = CordlyTextPrimary
                    )
                }

                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp, vertical = 26.dp)
                ) {
                    // Floating Golden Crown + Trophy Emblem
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier.scale(crownPulse)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(116.dp)
                                .clip(CircleShape)
                                .background(
                                    Brush.radialGradient(
                                        listOf(
                                            Color(0xFFFDE047).copy(alpha = 0.58f),
                                            CordlyAmber.copy(alpha = 0.24f),
                                            Color.Transparent
                                        )
                                    )
                                )
                        )
                        Surface(
                            color = Color(0xFF1F1604),
                            shape = CircleShape,
                            border = BorderStroke(
                                3.dp,
                                Brush.linearGradient(listOf(Color(0xFFFDE047), CordlyAmber, CordlyCyan))
                            ),
                            shadowElevation = 12.dp,
                            modifier = Modifier.size(86.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Rounded.EmojiEvents,
                                    contentDescription = null,
                                    tint = Color(0xFFFDE047),
                                    modifier = Modifier.size(46.dp)
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Exclusive First 50 Winners Pill
                    Surface(
                        color = Color(0xFFFDE047).copy(alpha = 0.18f),
                        shape = RoundedCornerShape(50),
                        border = BorderStroke(1.dp, Color(0xFFFDE047).copy(alpha = 0.7f))
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.Celebration,
                                contentDescription = null,
                                tint = Color(0xFFFDE047),
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = if (isArabic) {
                                    "الفائز رقم #${result.claimRank} من أصل ${result.maxClaims}!"
                                } else {
                                    "LEGENDARY WINNER #${result.claimRank} OF ${result.maxClaims}!"
                                },
                                style = MaterialTheme.typography.labelMedium,
                                color = Color(0xFFFDE047),
                                fontWeight = FontWeight.ExtraBold
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    Text(
                        text = if (isArabic) {
                            "تهانينا! اكتشفت الكود السري 👑"
                        } else {
                            "Congratulations! Secret Code Cracked 👑"
                        },
                        style = MaterialTheme.typography.headlineSmall,
                        color = CordlyTextPrimary,
                        fontWeight = FontWeight.ExtraBold,
                        textAlign = TextAlign.Center
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    val formattedReward = remember(result.awardedPoints) {
                        java.text.NumberFormat.getIntegerInstance(java.util.Locale.US).format(result.awardedPoints)
                    }
                    Text(
                        text = if (isArabic) {
                            "لقد استطعت معرفة الكود السري (${result.code}) وحصلت على $formattedReward نقطة P E Y مجاناً في رصيدك!"
                        } else {
                            "You discovered the secret code (${result.code}) and won $formattedReward free P E Y Points!"
                        },
                        style = MaterialTheme.typography.bodyMedium,
                        color = CordlyTextSecondary,
                        textAlign = TextAlign.Center,
                        lineHeight = 22.sp
                    )

                    Spacer(modifier = Modifier.height(18.dp))

                    AnimatedVisibility(
                        visible = showRewardCard,
                        enter = scaleIn(
                            initialScale = 0.65f,
                            animationSpec = spring(
                                dampingRatio = Spring.DampingRatioMediumBouncy,
                                stiffness = Spring.StiffnessMediumLow
                            )
                        ) + fadeIn(tween(260))
                    ) {
                        Surface(
                            color = CordlyCardSurface,
                            shape = RoundedCornerShape(20.dp),
                            border = BorderStroke(
                                2.dp,
                                Brush.horizontalGradient(listOf(Color(0xFFFDE047), CordlyAmber, CordlyCyan))
                            ),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.Center,
                                modifier = Modifier
                                    .background(
                                        Brush.horizontalGradient(
                                            listOf(
                                                Color(0xFFFDE047).copy(alpha = 0.18f),
                                                CordlyCyan.copy(alpha = 0.14f)
                                            )
                                        )
                                    )
                                    .padding(horizontal = 20.dp, vertical = 16.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Rounded.Diamond,
                                    contentDescription = null,
                                    tint = Color(0xFFFDE047),
                                    modifier = Modifier.size(30.dp)
                                )
                                Spacer(modifier = Modifier.width(12.dp))
                                Text(
                                    text = "+${pointsCounter.value.roundToInt()} P E Y",
                                    style = MaterialTheme.typography.headlineMedium,
                                    color = Color(0xFFFDE047),
                                    fontWeight = FontWeight.Black
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Icon(
                                    imageVector = Icons.Rounded.AutoAwesome,
                                    contentDescription = null,
                                    tint = CordlyCyan,
                                    modifier = Modifier.size(22.dp)
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    AnimatedVisibility(
                        visible = showClaimButton,
                        enter = slideInVertically { it / 2 } + fadeIn(tween(240))
                    ) {
                        Button(
                            onClick = onDismiss,
                            shape = RoundedCornerShape(16.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color(0xFFF59E0B),
                                contentColor = Color(0xFF1A1000)
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(52.dp)
                                .testTag("secret_code_celebration_collect_btn")
                        ) {
                            Text(
                                text = if (isArabic) "استلام النقاط والانطلاق!" else "Awesome! Collect & Continue",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.ExtraBold
                            )
                        }
                    }
                }
            }
        }
    }
}
