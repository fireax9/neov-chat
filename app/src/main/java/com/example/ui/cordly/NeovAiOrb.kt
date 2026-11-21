package com.example.ui.cordly

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.AutoAwesome
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

val NeovAiRed = Color(0xFFEF233C)
val NeovAiCrimson = Color(0xFFFF2E54)
val NeovAiAmber = Color(0xFFF59E0B)
val NeovAiYellow = Color(0xFFFACC15)

/**
 * Circular animated red & yellow Neov AI badge icon (Meta-AI inspired ring).
 */
@Composable
fun NeovAiOrbGlyph(
    size: Dp = 54.dp,
    pulseIntensity: Float = 1f,
    modifier: Modifier = Modifier
) {
    val infinite = rememberInfiniteTransition(label = "neovAiOrb")
    val angle by infinite.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 3600, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "neovAiOrbitAngle"
    )
    val pulse by infinite.animateFloat(
        initialValue = 0.86f,
        targetValue = 1.06f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1500),
            repeatMode = RepeatMode.Reverse
        ),
        label = "neovAiPulse"
    )

    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier.size(size)
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val strokePx = (this.size.minDimension * 0.135f).coerceAtLeast(4f)
            val inset = strokePx / 2f + 1.5f
            val arcSize = Size(this.size.width - inset * 2f, this.size.height - inset * 2f)
            val topLeft = Offset(inset, inset)

            // Soft red-gold outer halo
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        NeovAiRed.copy(alpha = 0.34f * pulseIntensity),
                        NeovAiYellow.copy(alpha = 0.18f * pulseIntensity),
                        Color.Transparent
                    )
                ),
                radius = (this.size.minDimension / 2f) * pulse
            )

            // Dark metallic core
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(Color(0xFF280B0E), Color(0xFF12080A))
                ),
                radius = (this.size.minDimension / 2f) - strokePx * 0.65f
            )

            // Rotating multi-layered Red & Yellow luminous ring
            rotate(degrees = angle) {
                drawArc(
                    brush = Brush.sweepGradient(
                        0.0f to NeovAiRed,
                        0.28f to NeovAiYellow,
                        0.52f to NeovAiCrimson,
                        0.78f to NeovAiAmber,
                        1.0f to NeovAiRed
                    ),
                    startAngle = 0f,
                    sweepAngle = 360f,
                    useCenter = false,
                    topLeft = topLeft,
                    size = arcSize,
                    style = Stroke(width = strokePx, cap = StrokeCap.Round)
                )
            }

            // Inner glowing yellow-red accent highlight arc rotating counter-clockwise
            rotate(degrees = -angle * 1.35f) {
                drawArc(
                    color = NeovAiYellow.copy(alpha = 0.85f),
                    startAngle = 15f,
                    sweepAngle = 95f,
                    useCenter = false,
                    topLeft = Offset(inset + strokePx * 0.35f, inset + strokePx * 0.35f),
                    size = Size(arcSize.width - strokePx * 0.7f, arcSize.height - strokePx * 0.7f),
                    style = Stroke(width = strokePx * 0.35f, cap = StrokeCap.Round)
                )
            }
        }

        // Brand-new Red & Yellow emblem inside the ring
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .size(size * 0.50f)
                .clip(CircleShape)
                .background(
                    Brush.linearGradient(
                        colors = listOf(NeovAiRed, NeovAiAmber, NeovAiYellow)
                    )
                )
        ) {
            Icon(
                imageVector = Icons.Rounded.AutoAwesome,
                contentDescription = "Neov AI",
                tint = Color.White,
                modifier = Modifier.size(size * 0.28f)
            )
        }
    }
}

/**
 * Floating bottom-right orb + "Neov AI" red/yellow pill badge.
 */
@Composable
fun FloatingNeovAiOrbButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(4.dp),
        modifier = modifier
            .clip(RoundedCornerShape(24.dp))
            .clickable(onClick = onClick)
            .padding(2.dp)
            .testTag("neov_ai_fab")
    ) {
        Surface(
            color = Color(0xFF14090B),
            shape = CircleShape,
            border = BorderStroke(1.5.dp, Brush.linearGradient(listOf(NeovAiRed, NeovAiYellow))),
            modifier = Modifier
                .size(58.dp)
                .shadow(14.dp, CircleShape, ambientColor = NeovAiRed, spotColor = NeovAiYellow)
        ) {
            Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
                NeovAiOrbGlyph(size = 54.dp)
            }
        }

        // New red & yellow "Neov AI" badge
        Surface(
            shape = RoundedCornerShape(999.dp),
            border = BorderStroke(1.dp, NeovAiYellow.copy(alpha = 0.85f)),
            color = Color.Transparent,
            modifier = Modifier.shadow(6.dp, RoundedCornerShape(999.dp))
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .background(
                        Brush.horizontalGradient(
                            colors = listOf(Color(0xFFD90429), Color(0xFFEA580C), Color(0xFFEAB308))
                        )
                    )
                    .padding(horizontal = 8.dp, vertical = 2.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(6.dp)
                        .clip(CircleShape)
                        .background(Color(0xFFFEF08A))
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = "Neov AI",
                    style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                    color = Color.White,
                    fontWeight = FontWeight.ExtraBold,
                    maxLines = 1
                )
            }
        }
    }
}
