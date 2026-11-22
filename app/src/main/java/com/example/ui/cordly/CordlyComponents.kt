package com.example.ui.cordly

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.Logout
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.AddTask
import androidx.compose.material.icons.rounded.ChevronLeft
import androidx.compose.material.icons.rounded.ChevronRight
import androidx.compose.material.icons.rounded.Explore
import androidx.compose.material.icons.rounded.Forum
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material.icons.rounded.Stars
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.vector.ImageVector
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.min
import kotlin.math.sin
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.data.model.Server
import com.example.data.model.UserPrivate
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
import com.example.ui.theme.CordlySurfaceDark
import com.example.ui.theme.CordlyTextMuted
import com.example.ui.theme.CordlyTextPrimary
import com.example.ui.theme.CordlyTextSecondary
import com.example.ui.theme.CordlyThemeState

fun statusColor(status: String): Color = when (status) {
    "online" -> CordlyEmerald
    "idle" -> CordlyAmber
    "dnd" -> CordlyCrimson
    else -> CordlyTextMuted
}

fun statusLabel(status: String, strings: AppStrings = EnglishStrings): String = when (status) {
    "online" -> strings.statusOnline
    "idle" -> strings.statusIdle
    "dnd" -> strings.statusDnd
    else -> strings.statusOffline
}

fun parseHexColor(hex: String, fallback: Color = CordlyBlurple): Color {
    return try {
        val clean = hex.removePrefix("#")
        val value = clean.toLong(16)
        when (clean.length) {
            6 -> Color(0xFF000000L or value)
            8 -> Color(value)
            else -> fallback
        }
    } catch (_: Exception) {
        fallback
    }
}

@Composable
fun WallpaperShapeCanvas(
    mode: AppWallpaperMode,
    isDark: Boolean,
    modifier: Modifier = Modifier
) {
    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height
        when (mode) {
            AppWallpaperMode.SHAPE_HEX -> {
                val bg = if (isDark) {
                    listOf(Color(0xFF040914), Color(0xFF0B1F3A), Color(0xFF071324))
                } else {
                    listOf(Color(0xFFE0F2FE), Color(0xFFF0F9FF), Color(0xFFDBEAFE))
                }
                drawRect(Brush.linearGradient(bg))
                val r = (min(w, h) * 0.11f).coerceIn(22f, 76f)
                val dx = r * 1.72f
                val dy = r * 1.50f
                val strokeColor = if (isDark) Color(0xFF22D3EE).copy(alpha = 0.24f) else Color(0xFF0284C7).copy(alpha = 0.20f)
                var row = 0
                var y = 0f
                while (y < h + r) {
                    var x = if (row % 2 == 0) 0f else dx * 0.5f
                    while (x < w + r) {
                        val path = Path().apply {
                            for (i in 0..5) {
                                val ang = (60 * i - 30) * PI / 180.0
                                val px = x + r * 0.86f * cos(ang).toFloat()
                                val py = y + r * 0.86f * sin(ang).toFloat()
                                if (i == 0) moveTo(px, py) else lineTo(px, py)
                            }
                            close()
                        }
                        drawPath(path, color = strokeColor, style = Stroke(width = 1.6f))
                        x += dx
                    }
                    y += dy
                    row++
                }
            }
            AppWallpaperMode.SHAPE_AURORA -> {
                val bg = if (isDark) {
                    listOf(Color(0xFF050816), Color(0xFF102A3B), Color(0xFF1E1B4B))
                } else {
                    listOf(Color(0xFFECFDF5), Color(0xFFE0E7FF), Color(0xFFF5F3FF))
                }
                drawRect(Brush.verticalGradient(bg))
                val wave1 = if (isDark) Color(0xFF10B981).copy(alpha = 0.24f) else Color(0xFF059669).copy(alpha = 0.16f)
                val wave2 = if (isDark) Color(0xFF6366F1).copy(alpha = 0.28f) else Color(0xFF4F46E5).copy(alpha = 0.16f)
                for (idx in 0..2) {
                    val y0 = h * (0.28f + idx * 0.22f)
                    val path = Path().apply {
                        moveTo(0f, h)
                        lineTo(0f, y0)
                        cubicTo(w * 0.35f, y0 - h * 0.16f, w * 0.65f, y0 + h * 0.18f, w, y0 - h * 0.08f)
                        lineTo(w, h)
                        close()
                    }
                    drawPath(path, color = if (idx % 2 == 0) wave1 else wave2)
                }
            }
            AppWallpaperMode.SHAPE_WAVES -> {
                val bg = if (isDark) {
                    listOf(Color(0xFF090614), Color(0xFF1E103C), Color(0xFF0C0524))
                } else {
                    listOf(Color(0xFFFDF2F8), Color(0xFFEDE9FE), Color(0xFFEEF2FF))
                }
                drawRect(Brush.linearGradient(bg))
                val ringColor = if (isDark) Color(0xFFEC4899) else Color(0xFF7C3AED)
                val maxR = min(w, h) * 0.9f
                for (ring in 1..6) {
                    val radius = (maxR / 6f) * ring
                    drawCircle(
                        color = ringColor.copy(alpha = if (isDark) 0.22f else 0.16f),
                        radius = radius,
                        center = Offset(w * 0.82f, h * 0.22f),
                        style = Stroke(width = 2.4f)
                    )
                    drawCircle(
                        color = CordlyBlurple.copy(alpha = if (isDark) 0.20f else 0.14f),
                        radius = radius * 0.72f,
                        center = Offset(w * 0.18f, h * 0.78f),
                        style = Stroke(width = 2.0f)
                    )
                }
            }
            AppWallpaperMode.SHAPE_GALAXY -> {
                val bg = if (isDark) {
                    listOf(Color(0xFF03030B), Color(0xFF0F172A), Color(0xFF170B3B))
                } else {
                    listOf(Color(0xFFF1F5F9), Color(0xFFE0E7FF), Color(0xFFE0F2FE))
                }
                drawRect(Brush.verticalGradient(bg))
                val gridColor = if (isDark) Color(0xFF38BDF8).copy(alpha = 0.16f) else Color(0xFF3B82F6).copy(alpha = 0.14f)
                val spacing = (min(w, h) * 0.12f).coerceIn(28f, 64f)
                var x = spacing
                while (x < w) {
                    drawLine(gridColor, Offset(x, 0f), Offset(x, h), strokeWidth = 1f)
                    x += spacing
                }
                var y = spacing
                while (y < h) {
                    drawLine(gridColor, Offset(0f, y), Offset(w, y), strokeWidth = 1f)
                    y += spacing
                }
                val diamondSize = min(w, h) * 0.24f
                val cx = w * 0.5f
                val cy = h * 0.42f
                val diamond = Path().apply {
                    moveTo(cx, cy - diamondSize)
                    lineTo(cx + diamondSize, cy)
                    lineTo(cx, cy + diamondSize)
                    lineTo(cx - diamondSize, cy)
                    close()
                }
                drawPath(diamond, color = CordlyBlurple.copy(alpha = if (isDark) 0.26f else 0.18f), style = Stroke(width = 2.5f))
            }
            else -> Unit
        }
    }
}

@Composable
fun AppWallpaperBackground(
    wallpaperState: WallpaperState,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(CordlyCanvasBg)
    ) {
        val presetRes = wallpaperState.mode.drawableRes()
        val scrimColor = if (CordlyThemeState.isDark) {
            Color(0xFF080A12).copy(alpha = 0.56f)
        } else {
            Color(0xFFF8FAFC).copy(alpha = 0.72f)
        }
        when {
            wallpaperState.mode == AppWallpaperMode.CUSTOM && wallpaperState.customFilePath != null -> {
                val customBitmap = rememberFileImageBitmap(
                    filePath = wallpaperState.customFilePath,
                    version = wallpaperState.customVersion
                )
                if (customBitmap != null) {
                    Image(
                        bitmap = customBitmap,
                        contentDescription = null,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                }
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(scrimColor)
                )
            }
            wallpaperState.mode.isShapeWallpaper() -> {
                WallpaperShapeCanvas(
                    mode = wallpaperState.mode,
                    isDark = CordlyThemeState.isDark,
                    modifier = Modifier.fillMaxSize()
                )
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            if (CordlyThemeState.isDark) Color.Black.copy(alpha = 0.22f)
                            else Color.White.copy(alpha = 0.18f)
                        )
                )
            }
            presetRes != null -> {
                AsyncImage(
                    model = presetRes,
                    contentDescription = null,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(scrimColor)
                )
            }
        }
    }
}

@Composable
fun UserAvatar(
    displayName: String,
    photoUrl: String?,
    status: String? = null,
    frameId: String = "none",
    size: Dp = 42.dp,
    modifier: Modifier = Modifier
) {
    val initial = displayName.trim().firstOrNull()?.uppercaseChar()?.toString() ?: "?"
    val avatarBg = rememberAvatarColor(displayName)
    val frame = remember(frameId) { PeyShopCatalog.findById(frameId) }
    val hasSpecialFrame = frame.id != "none"

    val rotationAngle: Float
    val counterRotation: Float
    val pulseScale: Float
    if (hasSpecialFrame && frame.animated) {
        val transition = rememberInfiniteTransition(label = "avatarFrame_${frame.id}")
        val angle by transition.animateFloat(
            initialValue = 0f,
            targetValue = 360f,
            animationSpec = infiniteRepeatable(
                animation = tween(durationMillis = 10000, easing = LinearEasing),
                repeatMode = RepeatMode.Restart
            ),
            label = "frameRotation"
        )
        val reverseAngle by transition.animateFloat(
            initialValue = 360f,
            targetValue = 0f,
            animationSpec = infiniteRepeatable(
                animation = tween(durationMillis = 14000, easing = LinearEasing),
                repeatMode = RepeatMode.Restart
            ),
            label = "frameCounterRotation"
        )
        val pulse by transition.animateFloat(
            initialValue = 0.25f,
            targetValue = 0.65f,
            animationSpec = infiniteRepeatable(
                animation = tween(durationMillis = 3200, easing = LinearEasing),
                repeatMode = RepeatMode.Reverse
            ),
            label = "framePulse"
        )
        rotationAngle = angle
        counterRotation = reverseAngle
        pulseScale = pulse
    } else {
        rotationAngle = 0f
        counterRotation = 0f
        pulseScale = 0f
    }

    val ringPadding = if (hasSpecialFrame) (frame.strokeDp + 2.2f).dp else 0.dp
    val primaryGlow = frame.ringColors.firstOrNull() ?: CordlyCyan
    val secondaryGlow = frame.ringColors.getOrElse(1) { primaryGlow }

    Box(
        modifier = modifier
            .size(size)
            .then(
                if (hasSpecialFrame) {
                    Modifier.drawBehind {
                        val strokePx = frame.strokeDp.dp.toPx()
                        val ringRadius = (this.size.minDimension - strokePx) / 2f
                        val centerOffset = Offset(
                            this.size.width / 2f,
                            this.size.height / 2f
                        )
                        val sweepBrush = Brush.sweepGradient(frame.ringColors)

                        // Soft pulsing outer halo
                        if (frame.animated) {
                            drawCircle(
                                color = primaryGlow.copy(alpha = 0.24f * pulseScale),
                                radius = ringRadius + strokePx * 0.7f,
                                style = Stroke(width = strokePx * 1.3f)
                            )
                        }

                        when (frame.shapeStyle) {
                            FrameShapeStyle.OCTAGON_CYBER -> {
                                val octPath = Path().apply {
                                    for (i in 0..7) {
                                        val ang = (45.0 * i + 22.5) * PI / 180.0
                                        val px = centerOffset.x + ringRadius * cos(ang).toFloat()
                                        val py = centerOffset.y + ringRadius * sin(ang).toFloat()
                                        if (i == 0) moveTo(px, py) else lineTo(px, py)
                                    }
                                    close()
                                }
                                rotate(rotationAngle * 0.35f) {
                                    drawPath(
                                        path = octPath,
                                        brush = sweepBrush,
                                        style = Stroke(width = strokePx)
                                    )
                                }
                            }
                            FrameShapeStyle.PRISM_DIAMOND -> {
                                val diamondSide = ringRadius * 1.44f
                                val boxTopLeft = Offset(
                                    centerOffset.x - diamondSide / 2f,
                                    centerOffset.y - diamondSide / 2f
                                )
                                val boxSize = Size(diamondSide, diamondSide)
                                val cornerRad = CornerRadius(strokePx * 1.8f, strokePx * 1.8f)
                                rotate(rotationAngle * 0.5f) {
                                    drawRoundRect(
                                        brush = sweepBrush,
                                        topLeft = boxTopLeft,
                                        size = boxSize,
                                        cornerRadius = cornerRad,
                                        style = Stroke(width = strokePx * 0.92f)
                                    )
                                }
                                rotate(counterRotation * 0.5f + 45f) {
                                    drawRoundRect(
                                        color = secondaryGlow.copy(alpha = 0.85f),
                                        topLeft = boxTopLeft,
                                        size = boxSize,
                                        cornerRadius = cornerRad,
                                        style = Stroke(width = strokePx * 0.72f)
                                    )
                                }
                            }
                            FrameShapeStyle.DOUBLE_STARBURST -> {
                                val starPath = Path().apply {
                                    val points = 12
                                    for (i in 0 until points * 2) {
                                        val rad = if (i % 2 == 0) ringRadius else ringRadius * 0.84f
                                        val ang = (180.0 / points * i) * PI / 180.0
                                        val px = centerOffset.x + rad * cos(ang).toFloat()
                                        val py = centerOffset.y + rad * sin(ang).toFloat()
                                        if (i == 0) moveTo(px, py) else lineTo(px, py)
                                    }
                                    close()
                                }
                                rotate(rotationAngle * 0.5f) {
                                    drawPath(starPath, brush = sweepBrush, style = Stroke(width = strokePx * 0.9f))
                                }
                                if (frame.animated) {
                                    rotate(counterRotation) {
                                        drawCircle(
                                            color = Color.White.copy(alpha = 0.72f),
                                            radius = ringRadius * 0.86f,
                                            style = Stroke(
                                                width = strokePx * 0.45f,
                                                pathEffect = PathEffect.dashPathEffect(floatArrayOf(12f, 12f))
                                            )
                                        )
                                    }
                                }
                            }
                            FrameShapeStyle.FLAME_CROWN -> {
                                rotate(rotationAngle * 0.65f) {
                                    drawCircle(
                                        brush = sweepBrush,
                                        radius = ringRadius,
                                        style = Stroke(width = strokePx)
                                    )
                                }
                                val flareCount = 6
                                rotate(counterRotation * 0.35f) {
                                    for (i in 0 until flareCount) {
                                        val ang = (360.0 / flareCount * i) * PI / 180.0
                                        val pInner = Offset(
                                            centerOffset.x + (ringRadius - strokePx * 0.4f) * cos(ang).toFloat(),
                                            centerOffset.y + (ringRadius - strokePx * 0.4f) * sin(ang).toFloat()
                                        )
                                        val pOuter = Offset(
                                            centerOffset.x + (ringRadius + strokePx * 1.45f) * cos(ang).toFloat(),
                                            centerOffset.y + (ringRadius + strokePx * 1.45f) * sin(ang).toFloat()
                                        )
                                        drawLine(
                                            color = if (i % 2 == 0) primaryGlow else Color.White,
                                            start = pInner,
                                            end = pOuter,
                                            strokeWidth = strokePx * 0.85f,
                                            cap = StrokeCap.Round
                                        )
                                    }
                                }
                            }
                            FrameShapeStyle.MATRIX_DASHED -> {
                                rotate(rotationAngle) {
                                    drawCircle(
                                        brush = sweepBrush,
                                        radius = ringRadius,
                                        style = Stroke(
                                            width = strokePx,
                                            cap = StrokeCap.Round,
                                            pathEffect = PathEffect.dashPathEffect(floatArrayOf(18f, 10f))
                                        )
                                    )
                                }
                                if (frame.animated) {
                                    rotate(counterRotation) {
                                        drawCircle(
                                            color = Color.White.copy(alpha = 0.8f),
                                            radius = ringRadius * 0.90f,
                                            style = Stroke(
                                                width = strokePx * 0.45f,
                                                cap = StrokeCap.Round,
                                                pathEffect = PathEffect.dashPathEffect(floatArrayOf(8f, 16f))
                                            )
                                        )
                                    }
                                }
                            }
                            FrameShapeStyle.LEGENDARY_NEON -> {
                                // Stationary sculpted futuristic neon chassis + cyber wings + breathing plasma bloom (no wheel spin)
                                val neonCyan = Color(0xFF00F5FF)
                                val neonIndigo = Color(0xFF6366F1)
                                val neonPink = Color(0xFFEC4899)
                                val phaseRad = rotationAngle * PI.toFloat() / 180f
                                val breath = 0.55f + 0.45f * sin(phaseRad * 2f)

                                // 1. Soft Neon Corona Aura Halo
                                drawCircle(
                                    brush = Brush.radialGradient(
                                        colors = listOf(
                                            neonCyan.copy(alpha = 0.36f + 0.14f * breath),
                                            neonPink.copy(alpha = 0.22f + 0.10f * pulseScale),
                                            Color.Transparent
                                        ),
                                        center = centerOffset,
                                        radius = ringRadius + strokePx * 2.2f
                                    ),
                                    radius = ringRadius + strokePx * 2.2f
                                )

                                // 2. Sculpted Chamfered Cyber-Hex Shield Chassis (stationary geometry, liquid sweeping gradient)
                                val chassisPath = Path().apply {
                                    val points = 12
                                    for (i in 0 until points) {
                                        val deg = i * (360.0 / points) - 90.0
                                        val ang = deg * PI / 180.0
                                        val r = if (i % 3 == 0) ringRadius * 1.07f else ringRadius * 0.97f
                                        val px = centerOffset.x + r * cos(ang).toFloat()
                                        val py = centerOffset.y + r * sin(ang).toFloat()
                                        if (i == 0) moveTo(px, py) else lineTo(px, py)
                                    }
                                    close()
                                }
                                val liquidGrad = Brush.linearGradient(
                                    colors = listOf(neonCyan, neonIndigo, neonPink, Color(0xFF22D3EE), neonCyan),
                                    start = Offset(
                                        centerOffset.x - ringRadius * cos(phaseRad),
                                        centerOffset.y - ringRadius * sin(phaseRad)
                                    ),
                                    end = Offset(
                                        centerOffset.x + ringRadius * cos(phaseRad),
                                        centerOffset.y + ringRadius * sin(phaseRad)
                                    )
                                )
                                // Outer diffused neon tube halo
                                drawPath(
                                    path = chassisPath,
                                    brush = liquidGrad,
                                    alpha = 0.38f + 0.16f * breath,
                                    style = Stroke(width = strokePx * 2.15f, join = StrokeJoin.Round)
                                )
                                // Vivid main neon tube body
                                drawPath(
                                    path = chassisPath,
                                    brush = liquidGrad,
                                    style = Stroke(width = strokePx * 0.98f, join = StrokeJoin.Round)
                                )
                                // Razor-sharp white-hot glass core filament
                                drawPath(
                                    path = chassisPath,
                                    color = Color.White.copy(alpha = 0.78f + 0.18f * breath),
                                    style = Stroke(width = (strokePx * 0.28f).coerceAtLeast(1f), join = StrokeJoin.Round)
                                )

                                // 3. Four Stationary Cyber Energy Wings at Top, Bottom, Left, Right
                                val wingReach = strokePx * (1.55f + 0.35f * breath)
                                val wingHalfSpread = 0.24f
                                for (k in 0 until 4) {
                                    val baseAng = k * (PI.toFloat() * 0.5f) - (PI.toFloat() * 0.5f)
                                    val wingTip = Offset(
                                        centerOffset.x + (ringRadius + wingReach) * cos(baseAng),
                                        centerOffset.y + (ringRadius + wingReach) * sin(baseAng)
                                    )
                                    val wingL = Offset(
                                        centerOffset.x + (ringRadius * 0.99f) * cos(baseAng - wingHalfSpread),
                                        centerOffset.y + (ringRadius * 0.99f) * sin(baseAng - wingHalfSpread)
                                    )
                                    val wingR = Offset(
                                        centerOffset.x + (ringRadius * 0.99f) * cos(baseAng + wingHalfSpread),
                                        centerOffset.y + (ringRadius * 0.99f) * sin(baseAng + wingHalfSpread)
                                    )
                                    val wingCol = if (k % 2 == 0) neonCyan else neonPink
                                    val wingPath = Path().apply {
                                        moveTo(wingL.x, wingL.y)
                                        lineTo(wingTip.x, wingTip.y)
                                        lineTo(wingR.x, wingR.y)
                                    }
                                    drawPath(
                                        path = wingPath,
                                        color = wingCol.copy(alpha = 0.40f + 0.20f * breath),
                                        style = Stroke(width = strokePx * 1.15f, cap = StrokeCap.Round, join = StrokeJoin.Round)
                                    )
                                    drawPath(
                                        path = wingPath,
                                        color = Color.White.copy(alpha = 0.90f),
                                        style = Stroke(width = (strokePx * 0.34f).coerceAtLeast(1.1f), cap = StrokeCap.Round, join = StrokeJoin.Round)
                                    )
                                }
                            }
                            FrameShapeStyle.CIRCLE_ORBIT -> {
                                rotate(rotationAngle) {
                                    drawCircle(
                                        brush = sweepBrush,
                                        radius = ringRadius,
                                        style = Stroke(width = strokePx)
                                    )
                                }
                                if (frame.animated) {
                                    val arcTopLeft = Offset(
                                        centerOffset.x - ringRadius,
                                        centerOffset.y - ringRadius
                                    )
                                    val arcSize = Size(ringRadius * 2f, ringRadius * 2f)
                                    rotate(counterRotation * 0.45f) {
                                        drawArc(
                                            color = Color.White.copy(alpha = 0.55f),
                                            startAngle = 0f,
                                            sweepAngle = 42f,
                                            useCenter = false,
                                            topLeft = arcTopLeft,
                                            size = arcSize,
                                            style = Stroke(
                                                width = strokePx * 0.55f,
                                                cap = StrokeCap.Round
                                            )
                                        )
                                        drawArc(
                                            color = secondaryGlow.copy(alpha = 0.65f),
                                            startAngle = 180f,
                                            sweepAngle = 42f,
                                            useCenter = false,
                                            topLeft = arcTopLeft,
                                            size = arcSize,
                                            style = Stroke(
                                                width = strokePx * 0.55f,
                                                cap = StrokeCap.Round
                                            )
                                        )
                                    }
                                }
                            }
                            FrameShapeStyle.SUKUNA_SHRINE_HANDS -> {
                                // Domain Expansion: Malevolent Shrine animated fire tongues + dark curse slash ring
                                val flameColors = listOf(
                                    Color(0xFFFF4500),
                                    Color(0xFFEF4444),
                                    Color(0xFFF59E0B),
                                    Color(0xFF991B1B),
                                    Color(0xFFFF6B00)
                                )
                                drawCircle(
                                    color = Color(0xFFDC2626).copy(alpha = 0.28f + 0.24f * pulseScale),
                                    radius = ringRadius + strokePx * 1.45f,
                                    style = Stroke(width = strokePx * 1.7f)
                                )
                                rotate(rotationAngle * 0.45f) {
                                    drawCircle(
                                        brush = Brush.sweepGradient(flameColors),
                                        radius = ringRadius,
                                        style = Stroke(width = strokePx * 1.15f)
                                    )
                                }
                                // Flickering animated fire tongues around the entire rim
                                val tongueCount = 12
                                for (i in 0 until tongueCount) {
                                    val baseDeg = i * (360f / tongueCount) + (rotationAngle * 0.28f)
                                    val ang = baseDeg * PI / 180.0
                                    val flameLen = strokePx * (1.5f + 0.8f * sin(ang * 3.0 + pulseScale * PI).toFloat())
                                    val leftAng = (baseDeg - 9f) * PI / 180.0
                                    val rightAng = (baseDeg + 9f) * PI / 180.0
                                    val baseL = Offset(
                                        centerOffset.x + (ringRadius - strokePx * 0.2f) * cos(leftAng).toFloat(),
                                        centerOffset.y + (ringRadius - strokePx * 0.2f) * sin(leftAng).toFloat()
                                    )
                                    val baseR = Offset(
                                        centerOffset.x + (ringRadius - strokePx * 0.2f) * cos(rightAng).toFloat(),
                                        centerOffset.y + (ringRadius - strokePx * 0.2f) * sin(rightAng).toFloat()
                                    )
                                    val tip = Offset(
                                        centerOffset.x + (ringRadius + flameLen) * cos(ang).toFloat(),
                                        centerOffset.y + (ringRadius + flameLen) * sin(ang).toFloat()
                                    )
                                    val flamePath = Path().apply {
                                        moveTo(baseL.x, baseL.y)
                                        lineTo(tip.x, tip.y)
                                        lineTo(baseR.x, baseR.y)
                                        close()
                                    }
                                    drawPath(
                                        path = flamePath,
                                        color = if (i % 3 == 0) Color(0xFFFDE047).copy(alpha = 0.90f)
                                        else if (i % 2 == 0) Color(0xFFFF4500).copy(alpha = 0.88f)
                                        else Color(0xFFDC2626).copy(alpha = 0.86f)
                                    )
                                }
                            }
                            FrameShapeStyle.DBZ_SUPER_SAIYAN -> {
                                val ssjBrush = Brush.radialGradient(
                                    colors = listOf(
                                        Color(0xFFFDE047).copy(alpha = 0.34f + 0.20f * pulseScale),
                                        Color(0xFFF59E0B).copy(alpha = 0.20f),
                                        Color.Transparent
                                    ),
                                    center = centerOffset,
                                    radius = ringRadius + strokePx * 2.8f
                                )
                                drawCircle(brush = ssjBrush, radius = ringRadius + strokePx * 2.4f, center = centerOffset)
                                rotate(rotationAngle * 0.45f) {
                                    drawCircle(
                                        brush = sweepBrush,
                                        radius = ringRadius,
                                        style = Stroke(width = strokePx * 1.1f)
                                    )
                                }
                                val spikeCount = 14
                                for (i in 0 until spikeCount) {
                                    val deg = i * (360f / spikeCount) - 90f
                                    val isTopCrown = i == 0 || i == 1 || i == spikeCount - 1
                                    val spikeLen = strokePx * (if (isTopCrown) 2.6f else 1.55f + 0.45f * pulseScale)
                                    val rad = deg * PI / 180.0
                                    val lRad = (deg - 10f) * PI / 180.0
                                    val rRad = (deg + 10f) * PI / 180.0
                                    val path = Path().apply {
                                        moveTo(
                                            centerOffset.x + (ringRadius - strokePx * 0.2f) * cos(lRad).toFloat(),
                                            centerOffset.y + (ringRadius - strokePx * 0.2f) * sin(lRad).toFloat()
                                        )
                                        lineTo(
                                            centerOffset.x + (ringRadius + spikeLen) * cos(rad).toFloat(),
                                            centerOffset.y + (ringRadius + spikeLen) * sin(rad).toFloat()
                                        )
                                        lineTo(
                                            centerOffset.x + (ringRadius - strokePx * 0.2f) * cos(rRad).toFloat(),
                                            centerOffset.y + (ringRadius - strokePx * 0.2f) * sin(rRad).toFloat()
                                        )
                                        close()
                                    }
                                    drawPath(
                                        path = path,
                                        color = if (isTopCrown || i % 2 == 0) Color(0xFFFDE047).copy(alpha = 0.92f)
                                        else Color(0xFFF59E0B).copy(alpha = 0.85f)
                                    )
                                }
                            }
                            FrameShapeStyle.DBZ_KAMEHAMEHA -> {
                                val auraBrush = Brush.radialGradient(
                                    colors = listOf(
                                        Color(0xFF22D3EE).copy(alpha = 0.36f + 0.20f * pulseScale),
                                        Color(0xFF0284C7).copy(alpha = 0.22f),
                                        Color.Transparent
                                    ),
                                    center = centerOffset,
                                    radius = ringRadius + strokePx * 2.6f
                                )
                                drawCircle(brush = auraBrush, radius = ringRadius + strokePx * 2.2f, center = centerOffset)
                                rotate(rotationAngle * 0.6f) {
                                    drawCircle(
                                        brush = sweepBrush,
                                        radius = ringRadius,
                                        style = Stroke(width = strokePx * 1.15f)
                                    )
                                }
                                rotate(counterRotation * 0.7f) {
                                    drawCircle(
                                        color = Color(0xFFE0F2FE).copy(alpha = 0.72f),
                                        radius = ringRadius + strokePx * 0.65f,
                                        style = Stroke(
                                            width = strokePx * 0.42f,
                                            pathEffect = PathEffect.dashPathEffect(floatArrayOf(14f, 10f))
                                        )
                                    )
                                }
                            }
                            FrameShapeStyle.JJK_GOJO_VOID -> {
                                val voidBrush = Brush.radialGradient(
                                    colors = listOf(
                                        Color(0xFFA855F7).copy(alpha = 0.34f + 0.18f * pulseScale),
                                        Color(0xFF38BDF8).copy(alpha = 0.20f),
                                        Color.Transparent
                                    ),
                                    center = centerOffset,
                                    radius = ringRadius + strokePx * 2.6f
                                )
                                drawCircle(brush = voidBrush, radius = ringRadius + strokePx * 2.2f, center = centerOffset)
                                rotate(rotationAngle * 0.55f) {
                                    drawCircle(
                                        brush = sweepBrush,
                                        radius = ringRadius,
                                        style = Stroke(width = strokePx * 1.12f)
                                    )
                                }
                                rotate(counterRotation * 0.85f) {
                                    val arcRectSize = Size(ringRadius * 2.08f, ringRadius * 2.08f)
                                    val arcTopLeft = Offset(
                                        centerOffset.x - ringRadius * 1.04f,
                                        centerOffset.y - ringRadius * 1.04f
                                    )
                                    drawArc(
                                        color = Color(0xFF38BDF8).copy(alpha = 0.90f),
                                        startAngle = 10f,
                                        sweepAngle = 70f,
                                        useCenter = false,
                                        topLeft = arcTopLeft,
                                        size = arcRectSize,
                                        style = Stroke(width = strokePx * 0.55f, cap = StrokeCap.Round)
                                    )
                                    drawArc(
                                        color = Color(0xFFEF4444).copy(alpha = 0.90f),
                                        startAngle = 190f,
                                        sweepAngle = 70f,
                                        useCenter = false,
                                        topLeft = arcTopLeft,
                                        size = arcRectSize,
                                        style = Stroke(width = strokePx * 0.55f, cap = StrokeCap.Round)
                                    )
                                }
                            }
                            FrameShapeStyle.BLUELOCK_EGOIST -> {
                                val egoBrush = Brush.radialGradient(
                                    colors = listOf(
                                        Color(0xFF22D3EE).copy(alpha = 0.30f + 0.20f * pulseScale),
                                        Color(0xFF1D4ED8).copy(alpha = 0.22f),
                                        Color.Transparent
                                    ),
                                    center = centerOffset,
                                    radius = ringRadius + strokePx * 2.5f
                                )
                                drawCircle(brush = egoBrush, radius = ringRadius + strokePx * 2.1f, center = centerOffset)
                                val hexPath = Path().apply {
                                    for (i in 0..7) {
                                        val ang = (45.0 * i + 22.5) * PI / 180.0
                                        val px = centerOffset.x + ringRadius * cos(ang).toFloat()
                                        val py = centerOffset.y + ringRadius * sin(ang).toFloat()
                                        if (i == 0) moveTo(px, py) else lineTo(px, py)
                                    }
                                    close()
                                }
                                rotate(rotationAngle * 0.30f) {
                                    drawPath(hexPath, brush = sweepBrush, style = Stroke(width = strokePx * 1.15f))
                                }
                                // Blazing cyan egoist aura flames on top/sides
                                for (i in 0 until 8) {
                                    val deg = i * 45f - 90f + sin((rotationAngle * PI / 180.0)).toFloat() * 6f
                                    val rad = deg * PI / 180.0
                                    val lRad = (deg - 11f) * PI / 180.0
                                    val rRad = (deg + 11f) * PI / 180.0
                                    val flameTip = ringRadius + strokePx * (1.4f + 0.55f * pulseScale)
                                    val p = Path().apply {
                                        moveTo(
                                            centerOffset.x + (ringRadius - strokePx * 0.15f) * cos(lRad).toFloat(),
                                            centerOffset.y + (ringRadius - strokePx * 0.15f) * sin(lRad).toFloat()
                                        )
                                        lineTo(
                                            centerOffset.x + flameTip * cos(rad).toFloat(),
                                            centerOffset.y + flameTip * sin(rad).toFloat()
                                        )
                                        lineTo(
                                            centerOffset.x + (ringRadius - strokePx * 0.15f) * cos(rRad).toFloat(),
                                            centerOffset.y + (ringRadius - strokePx * 0.15f) * sin(rRad).toFloat()
                                        )
                                        close()
                                    }
                                    drawPath(p, color = if (i % 2 == 0) Color(0xFF22D3EE).copy(alpha = 0.88f) else Color(0xFF2563EB).copy(alpha = 0.85f))
                                }
                            }
                            FrameShapeStyle.BLUELOCK_SPATIAL_FLOW -> {
                                val spatialBrush = Brush.radialGradient(
                                    colors = listOf(
                                        Color(0xFF10B981).copy(alpha = 0.28f + 0.18f * pulseScale),
                                        Color(0xFF22D3EE).copy(alpha = 0.18f),
                                        Color.Transparent
                                    ),
                                    center = centerOffset,
                                    radius = ringRadius + strokePx * 2.5f
                                )
                                drawCircle(brush = spatialBrush, radius = ringRadius + strokePx * 2.0f, center = centerOffset)
                                rotate(rotationAngle * 0.55f) {
                                    drawCircle(
                                        brush = sweepBrush,
                                        radius = ringRadius,
                                        style = Stroke(
                                            width = strokePx * 1.05f,
                                            cap = StrokeCap.Round,
                                            pathEffect = PathEffect.dashPathEffect(floatArrayOf(20f, 8f))
                                        )
                                    )
                                }
                                rotate(counterRotation * 0.65f) {
                                    drawCircle(
                                        color = Color(0xFF6EE7B7).copy(alpha = 0.82f),
                                        radius = ringRadius + strokePx * 0.55f,
                                        style = Stroke(
                                            width = strokePx * 0.45f,
                                            cap = StrokeCap.Round,
                                            pathEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 14f))
                                        )
                                    )
                                }
                            }
                        }
                    }
                } else {
                    Modifier
                }
            )
    ) {
        Surface(
            color = avatarBg,
            shape = CircleShape,
            modifier = Modifier
                .fillMaxSize()
                .padding(ringPadding)
        ) {
            Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
                Text(
                    text = initial,
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = (size.value * 0.40f).sp
                )
                if (!photoUrl.isNullOrBlank()) {
                    CordlyRemoteOrDataImage(
                        source = photoUrl,
                        contentDescription = "$displayName avatar",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .fillMaxSize()
                            .clip(CircleShape)
                    )
                }
            }
        }
        when (frame.shapeStyle) {
            FrameShapeStyle.SUKUNA_SHRINE_HANDS -> {
                // Foreground Malevolent Shrine fire crest + Sukuna Domain Expansion folded hands integrated into the frame
                Canvas(modifier = Modifier.fillMaxSize()) {
                    val w = this.size.width
                    val h = this.size.height
                    val cx = w * 0.5f
                    val strokePx = frame.strokeDp.dp.toPx()

                    // Lower rim blaze enveloping the wrists
                    val blazeBrush = Brush.verticalGradient(
                        colors = listOf(Color(0xFFFDE047), Color(0xFFFF4500), Color(0xFF991B1B)),
                        startY = h * 0.64f,
                        endY = h
                    )
                    drawArc(
                        brush = blazeBrush,
                        startAngle = 22f,
                        sweepAngle = 136f,
                        useCenter = false,
                        topLeft = Offset(strokePx * 0.45f, strokePx * 0.45f),
                        size = Size(w - strokePx * 0.9f, h - strokePx * 0.9f),
                        style = Stroke(width = strokePx * (1.0f + 0.25f * pulseScale), cap = StrokeCap.Round)
                    )

                    // Sukuna Domain Expansion hands (symmetric interlocking steeple mudra at bottom center of frame)
                    val handsY = h * 0.94f
                    val span = w * 0.23f
                    val spireH = h * 0.30f
                    val inkColor = Color(0xFF14090A)
                    val handFill = Color(0xFFFFF5EB)
                    val fireAccent = Color(0xFFFF4500)

                    val glowCircleRadius = span * (0.95f + 0.16f * pulseScale)
                    drawCircle(
                        color = fireAccent.copy(alpha = 0.36f + 0.18f * pulseScale),
                        radius = glowCircleRadius,
                        center = Offset(cx, handsY - spireH * 0.38f)
                    )

                    // Left hand & raised middle fingers
                    val leftHand = Path().apply {
                        moveTo(cx - span * 1.15f, handsY)
                        lineTo(cx - span * 0.56f, handsY - spireH * 0.56f)
                        lineTo(cx - span * 0.04f, handsY - spireH)
                        lineTo(cx + span * 0.02f, handsY - spireH * 0.54f)
                        lineTo(cx - span * 0.28f, handsY - spireH * 0.14f)
                        lineTo(cx - span * 0.85f, handsY + h * 0.02f)
                        close()
                    }
                    // Right hand & raised middle fingers
                    val rightHand = Path().apply {
                        moveTo(cx + span * 1.15f, handsY)
                        lineTo(cx + span * 0.56f, handsY - spireH * 0.56f)
                        lineTo(cx + span * 0.04f, handsY - spireH)
                        lineTo(cx - span * 0.02f, handsY - spireH * 0.54f)
                        lineTo(cx + span * 0.28f, handsY - spireH * 0.14f)
                        lineTo(cx + span * 0.85f, handsY + h * 0.02f)
                        close()
                    }

                    drawPath(leftHand, color = handFill)
                    drawPath(rightHand, color = handFill)
                    val inkStroke = Stroke(
                        width = (w * 0.028f).coerceAtLeast(1.3f),
                        cap = StrokeCap.Round,
                        join = StrokeJoin.Round
                    )
                    drawPath(leftHand, color = inkColor, style = inkStroke)
                    drawPath(rightHand, color = inkColor, style = inkStroke)

                    // Sukuna curse wrist markings (double black bands)
                    drawLine(
                        color = inkColor,
                        start = Offset(cx - span * 0.98f, handsY - spireH * 0.12f),
                        end = Offset(cx - span * 0.62f, handsY - spireH * 0.03f),
                        strokeWidth = (w * 0.028f).coerceAtLeast(1.3f),
                        cap = StrokeCap.Round
                    )
                    drawLine(
                        color = inkColor,
                        start = Offset(cx + span * 0.98f, handsY - spireH * 0.12f),
                        end = Offset(cx + span * 0.62f, handsY - spireH * 0.03f),
                        strokeWidth = (w * 0.028f).coerceAtLeast(1.3f),
                        cap = StrokeCap.Round
                    )
                }
            }
            FrameShapeStyle.DBZ_SUPER_SAIYAN -> {
                // Integrated Super Saiyan golden ki flames, electric SSJ2 lightning bolts & center ki burst
                Canvas(modifier = Modifier.fillMaxSize()) {
                    val w = this.size.width
                    val h = this.size.height
                    val cx = w * 0.5f

                    // Translucent golden ki flames rising across bottom/sides into the center
                    val leftKiFlame = Path().apply {
                        moveTo(w * 0.14f, h * 0.84f)
                        quadraticTo(w * 0.08f, h * 0.45f, w * 0.24f, h * 0.18f)
                        quadraticTo(w * 0.22f, h * 0.48f, w * 0.36f, h * 0.72f)
                        close()
                    }
                    val rightKiFlame = Path().apply {
                        moveTo(w * 0.86f, h * 0.84f)
                        quadraticTo(w * 0.92f, h * 0.45f, w * 0.76f, h * 0.18f)
                        quadraticTo(w * 0.78f, h * 0.48f, w * 0.64f, h * 0.72f)
                        close()
                    }
                    val kiBrush = Brush.verticalGradient(
                        colors = listOf(Color(0xFFFDE047).copy(alpha = 0.68f), Color(0xFFF59E0B).copy(alpha = 0.34f)),
                        startY = h * 0.15f,
                        endY = h * 0.9f
                    )
                    drawPath(leftKiFlame, brush = kiBrush)
                    drawPath(rightKiFlame, brush = kiBrush)

                    // Top golden Super Saiyan spiked hair/aura silhouette crest integrated at upper center
                    val crownCrest = Path().apply {
                        moveTo(cx - w * 0.28f, h * 0.18f)
                        lineTo(cx - w * 0.17f, h * 0.02f)
                        lineTo(cx - w * 0.07f, h * 0.12f)
                        lineTo(cx, -h * 0.02f)
                        lineTo(cx + w * 0.07f, h * 0.12f)
                        lineTo(cx + w * 0.17f, h * 0.02f)
                        lineTo(cx + w * 0.28f, h * 0.18f)
                        quadraticTo(cx, h * 0.10f, cx - w * 0.28f, h * 0.18f)
                        close()
                    }
                    drawPath(crownCrest, color = Color(0xFFFDE047).copy(alpha = 0.92f))
                    drawPath(
                        crownCrest,
                        color = Color(0xFF92400E),
                        style = Stroke(width = (w * 0.02f).coerceAtLeast(1.1f), join = StrokeJoin.Round)
                    )

                    // Crackling cyan-white SSJ2 lightning arcs across the avatar foreground
                    val boltL = Path().apply {
                        moveTo(w * 0.15f, h * 0.46f)
                        lineTo(w * 0.29f, h * 0.38f)
                        lineTo(w * 0.24f, h * 0.56f)
                        lineTo(w * 0.39f, h * 0.50f)
                    }
                    val boltR = Path().apply {
                        moveTo(w * 0.85f, h * 0.38f)
                        lineTo(w * 0.70f, h * 0.46f)
                        lineTo(w * 0.76f, h * 0.60f)
                        lineTo(w * 0.61f, h * 0.66f)
                    }
                    val boltGlow = Stroke(width = (w * 0.045f).coerceAtLeast(2.2f), cap = StrokeCap.Round, join = StrokeJoin.Round)
                    val boltCore = Stroke(width = (w * 0.020f).coerceAtLeast(1.0f), cap = StrokeCap.Round, join = StrokeJoin.Round)
                    drawPath(boltL, color = Color(0xFF22D3EE).copy(alpha = 0.82f), style = boltGlow)
                    drawPath(boltL, color = Color.White, style = boltCore)
                    drawPath(boltR, color = Color(0xFF22D3EE).copy(alpha = 0.82f), style = boltGlow)
                    drawPath(boltR, color = Color.White, style = boltCore)

                    // Center-bottom pulsing golden Ki burst star
                    val starCy = h * 0.78f
                    val outerR = w * (0.14f + 0.03f * pulseScale)
                    val innerR = outerR * 0.34f
                    val starPath = Path().apply {
                        for (i in 0 until 8) {
                            val r = if (i % 2 == 0) outerR else innerR
                            val a = (i * 45.0) * PI / 180.0
                            val px = cx + r * cos(a).toFloat()
                            val py = starCy + r * sin(a).toFloat()
                            if (i == 0) moveTo(px, py) else lineTo(px, py)
                        }
                        close()
                    }
                    drawCircle(
                        brush = Brush.radialGradient(
                            colors = listOf(Color(0xFFFDE047).copy(alpha = 0.75f), Color.Transparent),
                            center = Offset(cx, starCy),
                            radius = outerR * 1.9f
                        ),
                        radius = outerR * 1.9f,
                        center = Offset(cx, starCy)
                    )
                    drawPath(starPath, color = Color.White)
                    drawPath(starPath, color = Color(0xFFF59E0B), style = Stroke(width = (w * 0.022f).coerceAtLeast(1.2f)))
                }
            }
            FrameShapeStyle.DBZ_KAMEHAMEHA -> {
                // Integrated Kamehameha surging blast beam, cupped energy hands & blazing blue-white core orb
                Canvas(modifier = Modifier.fillMaxSize()) {
                    val w = this.size.width
                    val h = this.size.height
                    val cx = w * 0.5f
                    val orbCy = h * 0.72f
                    val orbCenter = Offset(cx, orbCy)

                    // Surging upward Kamehameha plasma beam flaring from lower orb through center of avatar
                    val beamPath = Path().apply {
                        moveTo(cx - w * 0.09f, orbCy)
                        lineTo(cx - w * 0.30f, h * 0.12f)
                        quadraticTo(cx, h * 0.06f, cx + w * 0.30f, h * 0.12f)
                        lineTo(cx + w * 0.09f, orbCy)
                        close()
                    }
                    drawPath(
                        path = beamPath,
                        brush = Brush.verticalGradient(
                            colors = listOf(
                                Color(0xFF38BDF8).copy(alpha = 0.08f),
                                Color(0xFF22D3EE).copy(alpha = 0.34f),
                                Color.White.copy(alpha = 0.56f)
                            ),
                            startY = h * 0.10f,
                            endY = orbCy
                        )
                    )

                    // Twin kinetic shockwave ellipses tilted around the Kamehameha core
                    val waveW = w * (0.48f + 0.06f * pulseScale)
                    val waveH = h * (0.16f + 0.02f * pulseScale)
                    val waveTopLeft = Offset(cx - waveW / 2f, orbCy - waveH / 2f)
                    rotate(22f, pivot = orbCenter) {
                        drawOval(
                            color = Color(0xFF67E8F9).copy(alpha = 0.78f),
                            topLeft = waveTopLeft,
                            size = Size(waveW, waveH),
                            style = Stroke(width = (w * 0.026f).coerceAtLeast(1.4f))
                        )
                    }
                    rotate(-22f, pivot = orbCenter) {
                        drawOval(
                            color = Color.White.copy(alpha = 0.85f),
                            topLeft = waveTopLeft,
                            size = Size(waveW, waveH),
                            style = Stroke(width = (w * 0.022f).coerceAtLeast(1.2f))
                        )
                    }

                    // Stylized cupped hands charging the Kamehameha orb along the bottom edge
                    val inkColor = Color(0xFF0C192C)
                    val handColor = Color(0xFFE0F2FE)
                    val palmSpan = w * 0.24f
                    val palmLeft = Path().apply {
                        moveTo(cx - palmSpan * 1.18f, h * 0.86f)
                        quadraticTo(cx - palmSpan * 0.72f, h * 0.62f, cx - palmSpan * 0.35f, h * 0.68f)
                        lineTo(cx - palmSpan * 0.24f, h * 0.88f)
                        close()
                    }
                    val palmRight = Path().apply {
                        moveTo(cx + palmSpan * 1.18f, h * 0.86f)
                        quadraticTo(cx + palmSpan * 0.72f, h * 0.62f, cx + palmSpan * 0.35f, h * 0.68f)
                        lineTo(cx + palmSpan * 0.24f, h * 0.88f)
                        close()
                    }
                    drawPath(palmLeft, color = handColor)
                    drawPath(palmRight, color = handColor)
                    val handInk = Stroke(width = (w * 0.024f).coerceAtLeast(1.2f), join = StrokeJoin.Round)
                    drawPath(palmLeft, color = inkColor, style = handInk)
                    drawPath(palmRight, color = inkColor, style = handInk)

                    // Luminous Kamehameha plasma core sphere in the lower-center foreground
                    val orbRadius = w * (0.16f + 0.025f * pulseScale)
                    drawCircle(
                        brush = Brush.radialGradient(
                            colors = listOf(
                                Color.White,
                                Color(0xFF67E8F9),
                                Color(0xFF0284C7).copy(alpha = 0.85f),
                                Color.Transparent
                            ),
                            center = orbCenter,
                            radius = orbRadius * 1.9f
                        ),
                        radius = orbRadius * 1.9f,
                        center = orbCenter
                    )
                    drawCircle(color = Color.White, radius = orbRadius * 0.68f, center = orbCenter)
                }
            }
            FrameShapeStyle.JJK_GOJO_VOID -> {
                // Integrated Gojo Six Eyes luminescence, Blue + Red clash orbs & Hollow Purple center core
                Canvas(modifier = Modifier.fillMaxSize()) {
                    val w = this.size.width
                    val h = this.size.height
                    val cx = w * 0.5f

                    // Upper Unlimited Void cosmic veil & twin luminous Six Eyes glints
                    val eyeY = h * 0.28f
                    val eyeOffset = w * 0.165f
                    drawOval(
                        color = Color(0xFF0F0720).copy(alpha = 0.34f),
                        topLeft = Offset(w * 0.16f, h * 0.17f),
                        size = Size(w * 0.68f, h * 0.21f)
                    )
                    for (side in listOf(-1f, 1f)) {
                        val ex = cx + side * eyeOffset
                        val eyeCenter = Offset(ex, eyeY)
                        drawCircle(
                            brush = Brush.radialGradient(
                                colors = listOf(Color(0xFF7DD3FC).copy(alpha = 0.90f), Color.Transparent),
                                center = eyeCenter,
                                radius = w * 0.12f
                            ),
                            radius = w * 0.12f,
                            center = eyeCenter
                        )
                        // Sharp glowing anime eye almond silhouette
                        val eyePath = Path().apply {
                            moveTo(ex - w * 0.09f, eyeY)
                            quadraticTo(ex, eyeY - h * 0.065f, ex + w * 0.09f, eyeY)
                            quadraticTo(ex, eyeY + h * 0.065f, ex - w * 0.09f, eyeY)
                            close()
                        }
                        drawPath(eyePath, color = Color(0xFF38BDF8))
                        drawCircle(color = Color.White, radius = w * 0.032f, center = eyeCenter)
                    }

                    // Lower collision: Lapse Blue (left) + Reversal Red (right) -> Hollow Purple (center)
                    val orbY = h * 0.77f
                    val blueCenter = Offset(cx - w * 0.24f, orbY + h * 0.02f)
                    val redCenter = Offset(cx + w * 0.24f, orbY + h * 0.02f)
                    val purpleCenter = Offset(cx, orbY - h * 0.02f)

                    // Merging attraction arcs
                    drawArc(
                        color = Color(0xFF38BDF8).copy(alpha = 0.85f),
                        startAngle = 140f,
                        sweepAngle = 120f,
                        useCenter = false,
                        topLeft = Offset(cx - w * 0.28f, orbY - h * 0.14f),
                        size = Size(w * 0.56f, h * 0.24f),
                        style = Stroke(width = (w * 0.022f).coerceAtLeast(1.2f), cap = StrokeCap.Round)
                    )
                    drawCircle(
                        brush = Brush.radialGradient(
                            colors = listOf(Color.White, Color(0xFF38BDF8), Color.Transparent),
                            center = blueCenter,
                            radius = w * 0.13f
                        ),
                        radius = w * 0.13f,
                        center = blueCenter
                    )
                    drawCircle(
                        brush = Brush.radialGradient(
                            colors = listOf(Color.White, Color(0xFFEF4444), Color.Transparent),
                            center = redCenter,
                            radius = w * 0.13f
                        ),
                        radius = w * 0.13f,
                        center = redCenter
                    )

                    // Hollow Purple central singularity orb
                    val purpleR = w * (0.16f + 0.025f * pulseScale)
                    drawCircle(
                        brush = Brush.radialGradient(
                            colors = listOf(
                                Color.White,
                                Color(0xFFD946EF),
                                Color(0xFF7E22CE),
                                Color.Transparent
                            ),
                            center = purpleCenter,
                            radius = purpleR * 1.65f
                        ),
                        radius = purpleR * 1.65f,
                        center = purpleCenter
                    )
                    drawCircle(
                        color = Color(0xFFFAF5FF),
                        radius = purpleR * 0.56f,
                        center = purpleCenter
                    )
                    drawCircle(
                        color = Color(0xFFA855F7),
                        radius = purpleR * 0.92f,
                        center = purpleCenter,
                        style = Stroke(width = (w * 0.025f).coerceAtLeast(1.3f))
                    )
                }
            }
            FrameShapeStyle.BLUELOCK_EGOIST -> {
                // Integrated Blue Lock glowing cyan striker speed arcs & blazing soccer ball in foreground (no eye!)
                Canvas(modifier = Modifier.fillMaxSize()) {
                    val w = this.size.width
                    val h = this.size.height
                    val cx = w * 0.5f

                    // Blazing cyan top striker speed-arc crown swooping toward the ball
                    drawArc(
                        brush = Brush.horizontalGradient(
                            listOf(Color(0xFF1D4ED8), Color(0xFF22D3EE), Color.White)
                        ),
                        startAngle = 200f,
                        sweepAngle = 140f,
                        useCenter = false,
                        topLeft = Offset(w * 0.14f, h * 0.08f),
                        size = Size(w * 0.72f, h * 0.48f),
                        style = Stroke(width = (w * 0.042f).coerceAtLeast(2f), cap = StrokeCap.Round)
                    )

                    // Blazing Blue Lock striker soccer ball at bottom center foreground
                    val ballCy = h * 0.77f
                    val ballCenter = Offset(cx, ballCy)
                    val ballR = w * 0.165f

                    // Roaring cyan aura flames wrapping left & right around the ball
                    val flameWrap = Path().apply {
                        moveTo(cx - ballR * 2.1f, ballCy - ballR * 0.35f)
                        quadraticTo(cx - ballR * 1.1f, ballCy + ballR * 1.15f, cx, ballCy + ballR * 1.05f)
                        quadraticTo(cx + ballR * 1.1f, ballCy + ballR * 1.15f, cx + ballR * 2.1f, ballCy - ballR * 0.35f)
                        quadraticTo(cx + ballR * 1.2f, ballCy + ballR * 0.55f, cx, ballCy + ballR * 0.35f)
                        quadraticTo(cx - ballR * 1.2f, ballCy + ballR * 0.55f, cx - ballR * 2.1f, ballCy - ballR * 0.35f)
                        close()
                    }
                    drawPath(
                        flameWrap,
                        color = Color(0xFF22D3EE).copy(alpha = 0.85f)
                    )

                    drawCircle(
                        brush = Brush.radialGradient(
                            colors = listOf(Color(0xFF22D3EE).copy(alpha = 0.62f), Color.Transparent),
                            center = ballCenter,
                            radius = ballR * 1.9f
                        ),
                        radius = ballR * 1.9f,
                        center = ballCenter
                    )

                    // Soccer ball body & crisp dark pentagon seams
                    drawCircle(color = Color(0xFFF8FAFC), radius = ballR, center = ballCenter)
                    drawCircle(
                        color = Color(0xFF0F172A),
                        radius = ballR,
                        center = ballCenter,
                        style = Stroke(width = (w * 0.024f).coerceAtLeast(1.2f))
                    )
                    val pentagon = Path().apply {
                        val pr = ballR * 0.44f
                        for (i in 0..4) {
                            val a = (i * 72.0 - 90.0) * PI / 180.0
                            val px = cx + pr * cos(a).toFloat()
                            val py = ballCy + pr * sin(a).toFloat()
                            if (i == 0) moveTo(px, py) else lineTo(px, py)
                        }
                        close()
                    }
                    drawPath(pentagon, color = Color(0xFF0F172A))
                    val seamStroke = (w * 0.018f).coerceAtLeast(1.0f)
                    for (i in 0..4) {
                        val a = (i * 72.0 - 90.0) * PI / 180.0
                        drawLine(
                            color = Color(0xFF0F172A),
                            start = Offset(cx + ballR * 0.44f * cos(a).toFloat(), ballCy + ballR * 0.44f * sin(a).toFloat()),
                            end = Offset(cx + ballR * 0.96f * cos(a).toFloat(), ballCy + ballR * 0.96f * sin(a).toFloat()),
                            strokeWidth = seamStroke
                        )
                    }
                }
            }
            FrameShapeStyle.BLUELOCK_SPATIAL_FLOW -> {
                // Integrated Isagi Spatial Awareness puzzle pieces & Metavision grid across center/foreground
                Canvas(modifier = Modifier.fillMaxSize()) {
                    val w = this.size.width
                    val h = this.size.height
                    val cx = w * 0.5f
                    val cy = h * 0.5f

                    // Subtle Metavision spatial trajectory arcs across center
                    val lineStroke = (w * 0.018f).coerceAtLeast(1.0f)
                    drawArc(
                        color = Color(0xFF10B981).copy(alpha = 0.58f),
                        startAngle = -35f,
                        sweepAngle = 150f,
                        useCenter = false,
                        topLeft = Offset(w * 0.14f, h * 0.20f),
                        size = Size(w * 0.72f, h * 0.62f),
                        style = Stroke(width = lineStroke, pathEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 8f)))
                    )
                    drawLine(
                        color = Color(0xFF22D3EE).copy(alpha = 0.46f),
                        start = Offset(w * 0.16f, h * 0.66f),
                        end = Offset(w * 0.84f, h * 0.30f),
                        strokeWidth = lineStroke
                    )

                    // Helper to draw a sharp jigsaw puzzle piece centered at (px, py)
                    fun drawPuzzlePiece(
                        px: Float,
                        py: Float,
                        pieceSize: Float,
                        deg: Float,
                        fillColor: Color,
                        edgeColor: Color
                    ) {
                        val half = pieceSize * 0.5f
                        val tabR = pieceSize * 0.18f
                        val p = Path().apply {
                            moveTo(px - half, py - half)
                            // Top edge with outward tab
                            lineTo(px - tabR, py - half)
                            cubicTo(
                                px - tabR * 1.1f, py - half - tabR * 1.4f,
                                px + tabR * 1.1f, py - half - tabR * 1.4f,
                                px + tabR, py - half
                            )
                            lineTo(px + half, py - half)
                            // Right edge with outward tab
                            lineTo(px + half, py - tabR)
                            cubicTo(
                                px + half + tabR * 1.4f, py - tabR * 1.1f,
                                px + half + tabR * 1.4f, py + tabR * 1.1f,
                                px + half, py + tabR
                            )
                            lineTo(px + half, py + half)
                            // Bottom straight edge
                            lineTo(px - half, py + half)
                            // Left edge with inward notch
                            lineTo(px - half, py + tabR)
                            cubicTo(
                                px - half + tabR * 1.1f, py + tabR * 0.9f,
                                px - half + tabR * 1.1f, py - tabR * 0.9f,
                                px - half, py - tabR
                            )
                            close()
                        }
                        rotate(deg, pivot = Offset(px, py)) {
                            drawPath(p, color = fillColor)
                            drawPath(
                                p,
                                color = edgeColor,
                                style = Stroke(width = (w * 0.024f).coerceAtLeast(1.2f), join = StrokeJoin.Round)
                            )
                        }
                    }

                    // Bottom-center anchor emerald/cyan puzzle piece locking in
                    drawPuzzlePiece(
                        px = cx,
                        py = h * 0.77f,
                        pieceSize = w * 0.25f,
                        deg = 0f,
                        fillColor = Color(0xFF047857).copy(alpha = 0.92f),
                        edgeColor = Color(0xFF6EE7B7)
                    )
                    // Upper-left flying spatial piece
                    drawPuzzlePiece(
                        px = cx - w * 0.24f,
                        py = h * 0.30f,
                        pieceSize = w * 0.19f,
                        deg = -18f + sin((rotationAngle * PI / 180.0)).toFloat() * 5f,
                        fillColor = Color(0xFF0284C7).copy(alpha = 0.88f),
                        edgeColor = Color(0xFF67E8F9)
                    )
                    // Mid-right assembling spatial piece
                    drawPuzzlePiece(
                        px = cx + w * 0.25f,
                        py = h * 0.56f,
                        pieceSize = w * 0.20f,
                        deg = 16f - sin((rotationAngle * PI / 180.0)).toFloat() * 5f,
                        fillColor = Color(0xFF059669).copy(alpha = 0.88f),
                        edgeColor = Color(0xFFA7F3D0)
                    )

                    // Subtle glowing metavision center target ring
                    drawCircle(
                        color = Color(0xFF6EE7B7).copy(alpha = 0.55f + 0.20f * pulseScale),
                        radius = w * 0.07f,
                        center = Offset(cx, cy),
                        style = Stroke(width = (w * 0.02f).coerceAtLeast(1.1f))
                    )
                }
            }
            FrameShapeStyle.LEGENDARY_NEON -> {
                // Crisp 4-point neon starlight flares & diagonal rim gleam outside the center portrait
                Canvas(modifier = Modifier.fillMaxSize()) {
                    val w = this.size.width
                    val h = this.size.height
                    val cx = w * 0.5f
                    val cy = h * 0.5f
                    val rimR = min(w, h) * 0.46f
                    val phase = rotationAngle * PI.toFloat() / 180f

                    val starPositions = listOf(45f, 135f, 225f, 315f)
                    starPositions.forEachIndexed { idx, deg ->
                        val ang = deg * PI.toFloat() / 180f
                        val pt = Offset(cx + rimR * cos(ang), cy + rimR * sin(ang))
                        val twinkle = 0.55f + 0.45f * sin(phase * 2f + idx * 1.4f)
                        val r = (w * 0.068f * (0.75f + 0.35f * twinkle)).coerceAtLeast(2.2f)
                        val starColor = if (idx % 2 == 0) Color(0xFF00F5FF) else Color(0xFFEC4899)
                        drawCircle(
                            brush = Brush.radialGradient(
                                colors = listOf(starColor.copy(alpha = 0.55f * twinkle), Color.Transparent),
                                center = pt,
                                radius = r * 2.1f
                            ),
                            radius = r * 2.1f,
                            center = pt
                        )
                        drawLine(
                            color = Color.White.copy(alpha = 0.92f * twinkle),
                            start = Offset(pt.x - r, pt.y),
                            end = Offset(pt.x + r, pt.y),
                            strokeWidth = (w * 0.022f).coerceAtLeast(1.1f),
                            cap = StrokeCap.Round
                        )
                        drawLine(
                            color = Color.White.copy(alpha = 0.92f * twinkle),
                            start = Offset(pt.x, pt.y - r),
                            end = Offset(pt.x, pt.y + r),
                            strokeWidth = (w * 0.022f).coerceAtLeast(1.1f),
                            cap = StrokeCap.Round
                        )
                    }
                }
            }
            else -> Unit
        }
        val artRes = frame.artworkResId
        if (artRes != null && size >= 38.dp) {
            val emblemSize = (size.value * 0.36f).coerceIn(15f, 32f).dp
            Surface(
                color = CordlySurfaceDark,
                shape = CircleShape,
                border = BorderStroke(1.5.dp, primaryGlow),
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .offset(x = 2.dp, y = (-2).dp)
                    .size(emblemSize)
            ) {
                Image(
                    painter = painterResource(artRes),
                    contentDescription = frame.title,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .fillMaxSize()
                        .clip(CircleShape)
                )
            }
        }
        if (status != null) {
            Box(
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .offset(x = 1.dp, y = 1.dp)
                    .size(13.dp)
                    .clip(CircleShape)
                    .background(CordlySurfaceDark)
                    .padding(2.dp)
                    .clip(CircleShape)
                    .background(statusColor(status))
            )
        }
    }
}

@Composable
fun ServerIconView(
    serverName: String,
    iconEmoji: String,
    iconUrl: String?,
    accentColor: Color,
    size: Dp = 44.dp,
    cornerRadius: Dp = 14.dp,
    modifier: Modifier = Modifier
) {
    Surface(
        color = accentColor.copy(alpha = 0.25f),
        shape = RoundedCornerShape(cornerRadius),
        modifier = modifier.size(size)
    ) {
        Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
            Text(
                text = iconEmoji,
                fontSize = (size.value * 0.48f).sp
            )
            if (!iconUrl.isNullOrBlank()) {
                CordlyRemoteOrDataImage(
                    source = iconUrl,
                    contentDescription = "$serverName icon",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .fillMaxSize()
                        .clip(RoundedCornerShape(cornerRadius))
                )
            }
        }
    }
}

private fun rememberAvatarColor(seed: String): Color {
    val palette = listOf(
        CordlyBlurple,
        Color(0xFF0EA5E9),
        Color(0xFF10B981),
        Color(0xFFF59E0B),
        Color(0xFFEC4899),
        Color(0xFF8B5CF6)
    )
    val idx = (seed.hashCode().let { if (it < 0) -it else it }) % palette.size
    return palette[idx]
}

@Composable
fun ServerRail(
    servers: List<Server>,
    activeDestination: ActiveDestination,
    pendingRequestCount: Int,
    hasWallpaper: Boolean = false,
    railOnRight: Boolean = false,
    onHideRail: (() -> Unit)? = null,
    onSelectHome: () -> Unit,
    onOpenQuestsClick: () -> Unit = {},
    onSelectServer: (Server) -> Unit,
    onCreateServerClick: () -> Unit,
    onJoinServerClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val strings = LocalAppStrings.current
    val isHomeSelected = activeDestination is ActiveDestination.Home

    Column(
        modifier = modifier
            .width(72.dp)
            .fillMaxHeight()
            .background(CordlyRailBg.copy(alpha = if (hasWallpaper) 0.80f else 1f))
            .then(
                if (onHideRail != null) {
                    Modifier.pointerInput(railOnRight) {
                        var totalDrag = 0f
                        detectHorizontalDragGestures(
                            onDragStart = { totalDrag = 0f },
                            onHorizontalDrag = { _, dragAmount ->
                                totalDrag += dragAmount
                                val shouldHide = if (railOnRight) totalDrag > 42f else totalDrag < -42f
                                if (shouldHide) {
                                    totalDrag = 0f
                                    onHideRail()
                                }
                            }
                        )
                    }
                } else {
                    Modifier
                }
            )
            .statusBarsPadding()
            .navigationBarsPadding()
            .padding(vertical = 10.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Home / DMs Hub Button
        RailIconButton(
            selected = isHomeSelected,
            accentColor = CordlyBlurple,
            badgeCount = pendingRequestCount,
            testTag = "rail_home_button",
            onClick = onSelectHome
        ) {
            Icon(
                imageVector = Icons.Rounded.Forum,
                contentDescription = strings.messagesTab,
                tint = if (isHomeSelected) Color.White else CordlyTextSecondary,
                modifier = Modifier.size(24.dp)
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Dedicated Quests & Ultra Directory Button ("المهام")
        RailActionButton(
            icon = Icons.Rounded.AddTask,
            contentDescription = strings.questsTabTitle,
            tint = CordlyAmber,
            testTag = "rail_quests_button",
            onClick = onOpenQuestsClick
        )

        HorizontalDivider(
            color = CordlyDivider,
            thickness = 1.dp,
            modifier = Modifier
                .padding(vertical = 10.dp)
                .width(32.dp)
        )

        LazyColumn(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(10.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            items(servers, key = { it.id }) { server ->
                val isSelected = activeDestination is ActiveDestination.ServerSpace &&
                    activeDestination.serverId == server.id
                val serverAccent = parseHexColor(server.accentHex)

                RailIconButton(
                    selected = isSelected,
                    accentColor = serverAccent,
                    badgeCount = 0,
                    testTag = "rail_server_${server.id}",
                    onClick = { onSelectServer(server) }
                ) {
                    Text(
                        text = server.iconEmoji,
                        fontSize = 22.sp
                    )
                    if (!server.iconUrl.isNullOrBlank()) {
                        CordlyRemoteOrDataImage(
                            source = server.iconUrl,
                            contentDescription = "${server.name} icon",
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize()
                        )
                    }
                }
            }

            item {
                RailActionButton(
                    icon = Icons.Rounded.Add,
                    contentDescription = strings.createServerTitle,
                    tint = CordlyEmerald,
                    testTag = "rail_create_server_button",
                    onClick = onCreateServerClick
                )
            }

            item {
                RailActionButton(
                    icon = Icons.Rounded.Explore,
                    contentDescription = strings.joinServerTitle,
                    tint = CordlyCyan,
                    testTag = "rail_join_server_button",
                    onClick = onJoinServerClick
                )
            }
        }

        if (onHideRail != null) {
            HorizontalDivider(
                color = CordlyDivider,
                thickness = 1.dp,
                modifier = Modifier
                    .padding(vertical = 8.dp)
                    .width(32.dp)
            )
            RailActionButton(
                icon = if (railOnRight) Icons.Rounded.ChevronRight else Icons.Rounded.ChevronLeft,
                contentDescription = "Hide server rail",
                tint = CordlyTextSecondary,
                testTag = "rail_hide_button",
                onClick = onHideRail
            )
        }
    }
}

@Composable
private fun RailIconButton(
    selected: Boolean,
    accentColor: Color,
    badgeCount: Int,
    testTag: String,
    onClick: () -> Unit,
    content: @Composable () -> Unit
) {
    val cornerRadius by animateDpAsState(
        targetValue = if (selected) 16.dp else 25.dp,
        animationSpec = spring(),
        label = "railCorner"
    )
    val pillHeight by animateDpAsState(
        targetValue = if (selected) 34.dp else 0.dp,
        animationSpec = spring(),
        label = "railPill"
    )
    val bgColor by animateColorAsState(
        targetValue = if (selected) accentColor else CordlyCardSurface,
        label = "railBg"
    )

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(50.dp),
        contentAlignment = Alignment.Center
    ) {
        if (pillHeight > 1.dp) {
            Box(
                modifier = Modifier
                    .align(Alignment.CenterStart)
                    .width(4.dp)
                    .height(pillHeight)
                    .clip(RoundedCornerShape(topEnd = 4.dp, bottomEnd = 4.dp))
                    .background(CordlyTextPrimary)
            )
        }

        Surface(
            color = bgColor,
            shape = RoundedCornerShape(cornerRadius),
            modifier = Modifier
                .size(48.dp)
                .clip(RoundedCornerShape(cornerRadius))
                .clickable(onClick = onClick)
                .testTag(testTag)
        ) {
            Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
                content()
            }
        }

        if (badgeCount > 0) {
            Surface(
                color = CordlyCrimson,
                shape = CircleShape,
                border = BorderStroke(2.dp, CordlyRailBg),
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(end = 8.dp)
                    .size(20.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Text(
                        text = if (badgeCount > 9) "9+" else badgeCount.toString(),
                        style = MaterialTheme.typography.labelSmall,
                        color = Color.White,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

@Composable
private fun RailActionButton(
    icon: ImageVector,
    contentDescription: String,
    tint: Color,
    testTag: String,
    onClick: () -> Unit
) {
    Surface(
        color = CordlyCardSurface,
        shape = CircleShape,
        border = BorderStroke(1.dp, tint.copy(alpha = 0.35f)),
        modifier = Modifier
            .size(48.dp)
            .clip(CircleShape)
            .clickable(onClick = onClick)
            .testTag(testTag)
    ) {
        Box(contentAlignment = Alignment.Center) {
            Icon(
                imageVector = icon,
                contentDescription = contentDescription,
                tint = tint,
                modifier = Modifier.size(22.dp)
            )
        }
    }
}

@Composable
fun UserDockBar(
    profile: UserPrivate?,
    onEditStatusClick: () -> Unit,
    onOpenShopClick: () -> Unit,
    onSignOutClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val strings = LocalAppStrings.current
    Surface(
        color = CordlyRailBg.copy(alpha = 0.92f),
        border = BorderStroke(1.dp, CordlyDivider.copy(alpha = 0.75f)),
        shape = RoundedCornerShape(18.dp),
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 8.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxWidth()
                .clickable(onClick = onEditStatusClick)
                .padding(horizontal = 10.dp, vertical = 8.dp)
                .testTag("user_dock_bar")
        ) {
            UserAvatar(
                displayName = profile?.displayName ?: "neov user",
                photoUrl = profile?.photoUrl,
                status = profile?.status ?: "online",
                frameId = profile?.equippedFrame ?: "none",
                size = 40.dp
            )
            Spacer(modifier = Modifier.width(10.dp))
            Column(modifier = Modifier.weight(1f)) {
                val effectiveRank = com.example.data.repository.CordlyRepository.effectiveRank(profile)
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(5.dp)
                ) {
                    Text(
                        text = profile?.displayName ?: "Loading...",
                        style = MaterialTheme.typography.titleMedium,
                        color = CordlyTextPrimary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f, fill = false)
                    )
                    RankBadgePill(rank = effectiveRank)
                }
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Surface(
                        color = CordlyAmber.copy(alpha = 0.16f),
                        shape = RoundedCornerShape(8.dp),
                        border = BorderStroke(1.dp, CordlyAmber.copy(alpha = 0.45f)),
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .clickable(onClick = onOpenShopClick)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.Stars,
                                contentDescription = strings.tabPeyShop,
                                tint = CordlyAmber,
                                modifier = Modifier.size(12.dp)
                            )
                            Spacer(modifier = Modifier.width(3.dp))
                            Text(
                                text = "${profile?.peyPoints ?: 200} P E Y",
                                style = MaterialTheme.typography.labelSmall,
                                color = CordlyAmber,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                    Text(
                        text = profile?.customStatus?.takeIf { it.isNotBlank() }
                            ?: statusLabel(profile?.status ?: "online", strings),
                        style = MaterialTheme.typography.labelSmall,
                        color = CordlyTextSecondary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
            IconButton(
                onClick = onEditStatusClick,
                modifier = Modifier
                    .minimumInteractiveComponentSize()
                    .testTag("edit_profile_button")
            ) {
                Icon(
                    imageVector = Icons.Rounded.Settings,
                    contentDescription = strings.settingsTitle,
                    tint = CordlyTextSecondary
                )
            }
            IconButton(
                onClick = onSignOutClick,
                modifier = Modifier
                    .minimumInteractiveComponentSize()
                    .testTag("sign_out_button")
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Rounded.Logout,
                    contentDescription = strings.signOutButton,
                    tint = CordlyCrimson
                )
            }
        }
    }
}

@Composable
fun EmptyPlaceholder(
    icon: ImageVector,
    title: String,
    subtitle: String,
    actionLabel: String? = null,
    onAction: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(28.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Surface(
            color = CordlyElevatedSurface.copy(alpha = 0.9f),
            shape = CircleShape,
            modifier = Modifier.size(64.dp)
        ) {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier.background(
                    Brush.radialGradient(
                        listOf(CordlyBlurple.copy(alpha = 0.25f), Color.Transparent)
                    )
                )
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = CordlyCyan,
                    modifier = Modifier.size(30.dp)
                )
            }
        }
        Spacer(modifier = Modifier.height(14.dp))
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            color = CordlyTextPrimary,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(6.dp))
        Text(
            text = subtitle,
            style = MaterialTheme.typography.bodyMedium,
            color = CordlyTextSecondary,
            textAlign = TextAlign.Center
        )
        if (actionLabel != null && onAction != null) {
            Spacer(modifier = Modifier.height(16.dp))
            Surface(
                color = CordlyBlurple,
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier
                    .clip(RoundedCornerShape(14.dp))
                    .clickable(onClick = onAction)
                    .minimumInteractiveComponentSize()
            ) {
                Text(
                    text = actionLabel,
                    style = MaterialTheme.typography.labelLarge,
                    color = Color.White,
                    modifier = Modifier.padding(horizontal = 18.dp, vertical = 12.dp)
                )
            }
        }
    }
}

@Composable
fun RankBadgePill(
    rank: String?,
    modifier: Modifier = Modifier
) {
    when (rank) {
        com.example.data.repository.CordlyRepository.RANK_NEO_PLUS -> {
            Surface(
                color = CordlyAmber,
                shape = RoundedCornerShape(6.dp),
                modifier = modifier
            ) {
                Text(
                    text = "NEO+",
                    style = MaterialTheme.typography.labelSmall,
                    color = Color.Black,
                    fontWeight = FontWeight.ExtraBold,
                    modifier = Modifier.padding(horizontal = 5.dp, vertical = 1.dp)
                )
            }
        }
        com.example.data.repository.CordlyRepository.RANK_ULTRA -> {
            Surface(
                color = CordlyCyan.copy(alpha = 0.22f),
                shape = RoundedCornerShape(6.dp),
                border = BorderStroke(1.dp, CordlyCyan.copy(alpha = 0.7f)),
                modifier = modifier
            ) {
                Text(
                    text = "ULTRA",
                    style = MaterialTheme.typography.labelSmall,
                    color = CordlyCyan,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(horizontal = 5.dp, vertical = 1.dp)
                )
            }
        }
    }
}

val LocalAppLogoVariant = staticCompositionLocalOf { AppLogoVariant.DEFAULT }
val LocalOnAppLogoVariantSelected = staticCompositionLocalOf<(AppLogoVariant) -> Unit> { {} }

@Composable
fun NeovBrandEmblem(
    variant: AppLogoVariant,
    size: Dp = 32.dp,
    cornerRadius: Dp = 10.dp,
    modifier: Modifier = Modifier
) {
    val artworkRes = variant.artworkResId
    val gradientColors = remember(variant) {
        when (variant) {
            AppLogoVariant.DEFAULT -> listOf(Color(0xFF5865F2), Color(0xFF404EED), Color(0xFF3139C8))
            AppLogoVariant.GOLD -> listOf(Color(0xFFFDE047), Color(0xFFF59E0B), Color(0xFFB45309))
            AppLogoVariant.CRIMSON -> listOf(Color(0xFFF43F5E), Color(0xFFE11D48), Color(0xFF881337))
            AppLogoVariant.EMERALD -> listOf(Color(0xFF34D399), Color(0xFF10B981), Color(0xFF047857))
            else -> listOf(variant.badgeColor, CordlySurfaceDark)
        }
    }
    Surface(
        color = Color.Transparent,
        shape = RoundedCornerShape(cornerRadius),
        border = BorderStroke(
            width = if (size >= 60.dp) 1.5.dp else 1.dp,
            color = Color.White.copy(alpha = 0.34f)
        ),
        shadowElevation = if (size >= 60.dp) 16.dp else 2.dp,
        modifier = modifier
            .size(size)
            .clip(RoundedCornerShape(cornerRadius))
    ) {
        if (artworkRes != null) {
            Image(
                painter = painterResource(id = artworkRes),
                contentDescription = variant.titleEn,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )
        } else {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .fillMaxSize()
                    .background(Brush.linearGradient(gradientColors))
            ) {
                Canvas(modifier = Modifier.fillMaxSize()) {
                    val w = this.size.width
                    val h = this.size.height
                    // Subtle diagonal highlight sheen
                    drawCircle(
                        color = Color.White.copy(alpha = 0.16f),
                        radius = w * 0.48f,
                        center = Offset(w * 0.24f, h * 0.20f)
                    )
                    // Sleek headphones headband + earcups
                    val bandStroke = w * 0.08f
                    val bandPath = Path().apply {
                        moveTo(w * 0.24f, h * 0.54f)
                        cubicTo(
                            w * 0.24f, h * 0.19f,
                            w * 0.76f, h * 0.19f,
                            w * 0.76f, h * 0.54f
                        )
                    }
                    drawPath(
                        path = bandPath,
                        color = Color.White.copy(alpha = 0.95f),
                        style = Stroke(width = bandStroke, cap = StrokeCap.Round)
                    )
                    // Left & right earcups
                    drawRoundRect(
                        color = Color.White,
                        topLeft = Offset(w * 0.15f, h * 0.48f),
                        size = androidx.compose.ui.geometry.Size(w * 0.13f, h * 0.24f),
                        cornerRadius = androidx.compose.ui.geometry.CornerRadius(w * 0.06f, w * 0.06f)
                    )
                    drawRoundRect(
                        color = Color.White,
                        topLeft = Offset(w * 0.72f, h * 0.48f),
                        size = androidx.compose.ui.geometry.Size(w * 0.13f, h * 0.24f),
                        cornerRadius = androidx.compose.ui.geometry.CornerRadius(w * 0.06f, w * 0.06f)
                    )
                    // Bold geometric 'N' glyph
                    val strokeW = w * 0.125f
                    val leftX = w * 0.37f
                    val rightX = w * 0.63f
                    val topY = h * 0.39f
                    val bottomY = h * 0.72f

                    val nPath = Path().apply {
                        moveTo(leftX, bottomY)
                        lineTo(leftX, topY)
                        lineTo(rightX, bottomY)
                        lineTo(rightX, topY)
                    }
                    drawPath(
                        path = nPath,
                        color = Color.White,
                        style = Stroke(
                            width = strokeW,
                            cap = StrokeCap.Round,
                            join = StrokeJoin.Round
                        )
                    )
                }
            }
        }
    }
}


