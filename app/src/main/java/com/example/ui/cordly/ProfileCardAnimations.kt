package com.example.ui.cordly

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.shape.GenericShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.min
import kotlin.math.sin

fun isAnimeAnimatedCardFrame(cardFrameId: String): Boolean {
    return cardFrameId == "card_jjk_gojo_void" ||
        cardFrameId == "card_jjk_gojo_domain" ||
        cardFrameId == "card_dbz_kamehameha" ||
        cardFrameId == "card_bluelock_striker" ||
        cardFrameId == "card_jjk_sukuna_shrine"
}

fun cardEntranceDurationMs(cardFrameId: String): Int = when (cardFrameId) {
    "card_jjk_gojo_domain" -> 5500
    "card_jjk_gojo_void" -> 19000
    "card_jjk_sukuna_shrine" -> 17800
    else -> 5000
}

fun cardEntranceDurationBadge(cardFrameId: String): String = when (cardFrameId) {
    "card_jjk_gojo_domain" -> "5.5s"
    "card_jjk_gojo_void" -> "19s"
    "card_jjk_sukuna_shrine" -> "18s"
    else -> "5s"
}

/**
 * Distinct, custom architectural silhouette Path for EVERY Profile Card Frame so
 * no two cards in the shop or profile viewer share the same outline shape.
 */
fun buildCardFramePath(
    cardFrameId: String,
    left: Float,
    top: Float,
    right: Float,
    bottom: Float
): Path {
    val w = (right - left).coerceAtLeast(4f)
    val h = (bottom - top).coerceAtLeast(4f)
    val m = min(w, h)
    val cx = (left + right) * 0.5f
    val cy = (top + bottom) * 0.5f

    return Path().apply {
        when (cardFrameId) {
            // 1. Cyber Tech Asymmetrical Slanted Bevel + Deep Side HUD Notches
            "card_neon_blurple" -> {
                val bigCut = m * 0.17f
                val smallCut = m * 0.04f
                val notchIn = m * 0.052f
                moveTo(left + smallCut, top)
                lineTo(right - bigCut, top)
                lineTo(right, top + bigCut)
                lineTo(right, cy - h * 0.11f)
                lineTo(right - notchIn, cy - h * 0.06f)
                lineTo(right - notchIn, cy + h * 0.06f)
                lineTo(right, cy + h * 0.11f)
                lineTo(right, bottom - smallCut)
                lineTo(right - smallCut, bottom)
                lineTo(left + bigCut, bottom)
                lineTo(left, bottom - bigCut)
                lineTo(left, cy + h * 0.11f)
                lineTo(left + notchIn, cy + h * 0.06f)
                lineTo(left + notchIn, cy - h * 0.06f)
                lineTo(left, cy - h * 0.11f)
                lineTo(left, top + smallCut)
                close()
            }

            // 2. Crowned Royal Imperial Crest + Pointed Heraldic Shield Base
            "card_royal_gold" -> {
                val shoulderY = top + m * 0.11f
                val crownMidY = top + m * 0.035f
                val sideInset = m * 0.04f
                moveTo(left + sideInset + m * 0.06f, shoulderY)
                lineTo(cx - m * 0.20f, shoulderY)
                lineTo(cx - m * 0.11f, crownMidY)
                lineTo(cx, top)
                lineTo(cx + m * 0.11f, crownMidY)
                lineTo(cx + m * 0.20f, shoulderY)
                lineTo(right - sideInset - m * 0.06f, shoulderY)
                lineTo(right - sideInset, shoulderY + m * 0.07f)
                lineTo(right - sideInset, bottom - m * 0.16f)
                quadraticTo(right - w * 0.10f, bottom - m * 0.05f, cx, bottom)
                quadraticTo(left + w * 0.10f, bottom - m * 0.05f, left + sideInset, bottom - m * 0.16f)
                lineTo(left + sideInset, shoulderY + m * 0.07f)
                close()
            }

            // 3. Tactical Octagonal Diamond-Wing HUD Shield
            "card_emerald_cyber" -> {
                val cut = m * 0.15f
                val wingOut = m * 0.055f
                moveTo(left + wingOut + cut, top)
                lineTo(right - wingOut - cut, top)
                lineTo(right - wingOut, top + cut)
                lineTo(right - wingOut, cy - h * 0.14f)
                lineTo(right, cy - h * 0.065f)
                lineTo(right, cy + h * 0.065f)
                lineTo(right - wingOut, cy + h * 0.14f)
                lineTo(right - wingOut, bottom - cut)
                lineTo(right - wingOut - cut, bottom)
                lineTo(left + wingOut + cut, bottom)
                lineTo(left + wingOut, bottom - cut)
                lineTo(left + wingOut, cy + h * 0.14f)
                lineTo(left, cy + h * 0.065f)
                lineTo(left, cy - h * 0.065f)
                lineTo(left + wingOut, cy - h * 0.14f)
                lineTo(left + wingOut, top + cut)
                close()
            }

            // 4. Jagged Esports Twin-Wing Tournament Blade
            "card_gaming_rgb" -> {
                val wingDrop = m * 0.105f
                val bladeIn = m * 0.065f
                moveTo(left, top + wingDrop)
                lineTo(cx - m * 0.18f, top)
                lineTo(cx, top + wingDrop * 0.85f)
                lineTo(cx + m * 0.18f, top)
                lineTo(right, top + wingDrop)
                lineTo(right - bladeIn, top + h * 0.28f)
                lineTo(right, top + h * 0.37f)
                lineTo(right, bottom - h * 0.20f)
                lineTo(right - bladeIn * 1.6f, bottom)
                lineTo(left + bladeIn * 1.6f, bottom)
                lineTo(left, bottom - h * 0.20f)
                lineTo(left, top + h * 0.37f)
                lineTo(left + bladeIn, top + h * 0.28f)
                close()
            }

            // 5. Dragon Ball: Super Saiyan Spiky Aura Crown + V-Energy Point
            "card_dbz_kamehameha" -> {
                val baseTop = top + m * 0.105f
                val sideCut = m * 0.06f
                moveTo(left + sideCut, baseTop)
                lineTo(left + w * 0.20f, top + m * 0.015f)
                lineTo(left + w * 0.33f, baseTop - m * 0.015f)
                lineTo(cx, top)
                lineTo(right - w * 0.33f, baseTop - m * 0.015f)
                lineTo(right - w * 0.20f, top + m * 0.015f)
                lineTo(right - sideCut, baseTop)
                lineTo(right, baseTop + m * 0.08f)
                lineTo(right, bottom - m * 0.12f)
                lineTo(right - w * 0.22f, bottom - m * 0.025f)
                lineTo(cx, bottom)
                lineTo(left + w * 0.22f, bottom - m * 0.025f)
                lineTo(left, bottom - m * 0.12f)
                lineTo(left, baseTop + m * 0.08f)
                close()
            }

            // 6. Gojo Hollow Purple: Smooth Infinity Hourglass Silhouette
            "card_jjk_gojo_void" -> {
                val waistIn = m * 0.065f
                val domeH = m * 0.085f
                moveTo(left + m * 0.13f, top + domeH)
                quadraticTo(cx, top, right - m * 0.13f, top + domeH)
                quadraticTo(right, top + domeH * 1.35f, right - waistIn * 0.45f, cy - h * 0.18f)
                quadraticTo(right - waistIn * 1.35f, cy, right - waistIn * 0.45f, cy + h * 0.18f)
                quadraticTo(right, bottom - domeH * 1.35f, right - m * 0.13f, bottom - domeH)
                quadraticTo(cx, bottom, left + m * 0.13f, bottom - domeH)
                quadraticTo(left, bottom - domeH * 1.35f, left + waistIn * 0.45f, cy + h * 0.18f)
                quadraticTo(left + waistIn * 1.35f, cy, left + waistIn * 0.45f, cy - h * 0.18f)
                quadraticTo(left, top + domeH * 1.35f, left + m * 0.13f, top + domeH)
                close()
            }

            // 6b. Gojo Unlimited Void Domain Expansion: Celestial Halo Gate with Twin Eclipse Wings
            "card_jjk_gojo_domain" -> {
                val wingW = m * 0.065f
                val bodyL = left + wingW
                val bodyR = right - wingW
                val archH = m * 0.11f
                moveTo(bodyL + m * 0.08f, top + archH)
                quadraticTo(cx, top, bodyR - m * 0.08f, top + archH)
                lineTo(bodyR, top + archH + m * 0.05f)
                // Right crescent wing flare
                lineTo(bodyR, cy - h * 0.16f)
                quadraticTo(right, cy, bodyR, cy + h * 0.16f)
                lineTo(bodyR, bottom - m * 0.14f)
                // Deep cosmic prism base
                lineTo(cx, bottom)
                lineTo(bodyL, bottom - m * 0.14f)
                // Left crescent wing flare
                lineTo(bodyL, cy + h * 0.16f)
                quadraticTo(left, cy, bodyL, cy - h * 0.16f)
                lineTo(bodyL, top + archH + m * 0.05f)
                close()
            }

            // 7. Blue Lock: Hexagonal Egoist Frame with Interlocking Jigsaw Puzzle Tabs on Sides
            "card_bluelock_striker" -> {
                val hexInset = m * 0.095f
                val tabOut = m * 0.068f
                val bodyL = left + tabOut
                val bodyR = right - tabOut
                moveTo(bodyL + hexInset, top)
                lineTo(bodyR - hexInset, top)
                lineTo(bodyR, top + hexInset)
                // Right edge puzzle knob sticking OUT to `right`
                lineTo(bodyR, cy - h * 0.085f)
                cubicTo(
                    right, cy - h * 0.115f,
                    right, cy + h * 0.115f,
                    bodyR, cy + h * 0.085f
                )
                lineTo(bodyR, bottom - hexInset)
                lineTo(bodyR - hexInset, bottom)
                lineTo(bodyL + hexInset, bottom)
                lineTo(bodyL, bottom - hexInset)
                // Left edge puzzle knob sticking OUT to `left`
                lineTo(bodyL, cy + h * 0.085f)
                cubicTo(
                    left, cy + h * 0.115f,
                    left, cy - h * 0.115f,
                    bodyL, cy - h * 0.085f
                )
                lineTo(bodyL, top + hexInset)
                close()
            }

            // 8. Sukuna: Demon Shrine Torii Horned Roof + Serrated Cursed Flame Base
            "card_jjk_sukuna_shrine" -> {
                val hornInsetX = m * 0.065f
                val eaveY = top + m * 0.105f
                val toothY = bottom - m * 0.065f
                moveTo(left, top)
                quadraticTo(cx, eaveY, right, top)
                lineTo(right - hornInsetX, eaveY + m * 0.06f)
                lineTo(right - hornInsetX, bottom - m * 0.09f)
                lineTo(right - w * 0.25f, toothY)
                lineTo(right - w * 0.35f, bottom)
                lineTo(cx, toothY - m * 0.01f)
                lineTo(left + w * 0.35f, bottom)
                lineTo(left + w * 0.25f, toothY)
                lineTo(left + hornInsetX, bottom - m * 0.09f)
                lineTo(left + hornInsetX, eaveY + m * 0.06f)
                close()
            }

            // 9. Default Clean Card
            else -> {
                val r = m * 0.11f
                addRoundRect(
                    androidx.compose.ui.geometry.RoundRect(
                        rect = Rect(left, top, right, bottom),
                        radiusX = r,
                        radiusY = r
                    )
                )
            }
        }
    }
}

fun cardFrameShape(cardFrameId: String): Shape {
    if (cardFrameId == "none") return RoundedCornerShape(24.dp)
    return GenericShape { size, _ ->
        val p = buildCardFramePath(cardFrameId, 0f, 0f, size.width, size.height)
        addPath(p)
    }
}

/**
 * Draws rich, continuous animated visual effects OUTSIDE the profile card's unique shape border
 * for EVERY equipped card frame (anime, gaming RGB, gold, emerald, blurple, or custom accent).
 */
@Composable
fun ProfileCardExternalAuraOverlay(
    cardFrameId: String,
    borderColors: List<Color>,
    accentColor: Color,
    animAngle: Float,
    pulse: Float,
    introActive: Boolean,
    introProgress: Float,
    insetDp: Dp = 18.dp,
    modifier: Modifier = Modifier
) {
    Canvas(modifier = modifier) {
        val inset = insetDp.toPx()
        val left = inset
        val top = inset
        val right = (size.width - inset).coerceAtLeast(left + 4f)
        val bottom = (size.height - inset).coerceAtLeast(top + 4f)
        val cx = size.width * 0.5f
        val cy = size.height * 0.5f
        val surgeBoost = if (introActive) (1f + 0.50f * sin(introProgress * PI).toFloat()) else 1f
        val radPhase = animAngle * PI.toFloat() / 180f
        val gradColors = if (borderColors.size >= 2) borderColors else listOf(accentColor, accentColor.copy(alpha = 0.45f), accentColor)

        // 1. Outer soft halo conforming closely to this card's exact shape!
        val haloExpand = inset * 0.42f * (0.85f + 0.22f * pulse) * surgeBoost
        val haloPath = buildCardFramePath(
            cardFrameId = cardFrameId,
            left = left - haloExpand,
            top = top - haloExpand,
            right = right + haloExpand,
            bottom = bottom + haloExpand
        )
        drawPath(
            path = haloPath,
            brush = Brush.sweepGradient(gradColors, center = Offset(cx, cy)),
            alpha = (0.36f + 0.22f * pulse).coerceAtMost(0.75f),
            style = Stroke(width = inset * 0.48f * surgeBoost, join = StrokeJoin.Round)
        )

        // 2. Crisp orbiting neon dashed aura path floating outside non-custom cards (skip for card_neon_blurple which renders twin smooth plasma tubes)
        if (cardFrameId != "card_neon_blurple") {
            val floatGap = inset * 0.34f
            val orbitPath = buildCardFramePath(
                cardFrameId = cardFrameId,
                left = left - floatGap,
                top = top - floatGap,
                right = right + floatGap,
                bottom = bottom + floatGap
            )
            drawPath(
                path = orbitPath,
                brush = Brush.linearGradient(
                    colors = listOf(accentColor, Color.White, gradColors.getOrElse(1) { accentColor }),
                    start = Offset(0f, cy + sin(radPhase) * (bottom - top) * 0.4f),
                    end = Offset(size.width, cy - sin(radPhase) * (bottom - top) * 0.4f)
                ),
                alpha = 0.65f + 0.25f * pulse,
                style = Stroke(
                    width = (inset * 0.13f).coerceAtLeast(1.8.dp.toPx()),
                    pathEffect = PathEffect.dashPathEffect(floatArrayOf(38f, 16f, 14f, 16f), animAngle * 2.2f),
                    cap = StrokeCap.Round,
                    join = StrokeJoin.Round
                )
            )
        }

        // Helper for external perimeter coordinates
        fun perimeterPoint(t: Float, extraOut: Float): Offset {
            val norm = ((t % 1f) + 1f) % 1f
            val pL = left - extraOut
            val pR = right + extraOut
            val pT = top - extraOut
            val pB = bottom + extraOut
            return when {
                norm < 0.25f -> Offset(pL + (pR - pL) * (norm / 0.25f), pT)
                norm < 0.50f -> Offset(pR, pT + (pB - pT) * ((norm - 0.25f) / 0.25f))
                norm < 0.75f -> Offset(pR - (pR - pL) * ((norm - 0.50f) / 0.25f), pB)
                else -> Offset(pL, pB - (pB - pT) * ((norm - 0.75f) / 0.25f))
            }
        }

        val phaseNorm = animAngle / 360f
        // 3. Gentle external shimmer/artistry outside each card's perimeter (no orbiting balls!)
        when (cardFrameId) {
            "card_jjk_sukuna_shrine" -> {
                val flameCount = 16
                for (i in 0 until flameCount) {
                    val t = i.toFloat() / flameCount
                    val base = perimeterPoint(t + 0.01f * sin(radPhase + i), inset * 0.14f)
                    val outDir = Offset(base.x - cx, base.y - cy).let { v ->
                        val len = kotlin.math.hypot(v.x, v.y).coerceAtLeast(1f)
                        Offset(v.x / len, v.y / len)
                    }
                    val flameH = inset * (0.64f + 0.24f * abs(sin(radPhase + i * 1.3f))) * surgeBoost
                    val perp = Offset(-outDir.y, outDir.x) * (inset * 0.30f)
                    val tip = Offset(base.x + outDir.x * flameH, base.y + outDir.y * flameH)
                    val flamePath = Path().apply {
                        moveTo(base.x - perp.x, base.y - perp.y)
                        lineTo(tip.x, tip.y)
                        lineTo(base.x + perp.x, base.y + perp.y)
                        close()
                    }
                    drawPath(
                        flamePath,
                        brush = Brush.linearGradient(listOf(Color(0xFFDC2626), Color(0xFFFF4500), Color(0xFFFDE047)), base, tip),
                        alpha = 0.72f
                    )
                }
            }
            "card_jjk_gojo_void" -> {
                // Soft static-positioned Blue/Red/Purple outer veil glow pulsing gently
                val softGlowPath = buildCardFramePath(cardFrameId, left - inset * 0.55f, top - inset * 0.55f, right + inset * 0.55f, bottom + inset * 0.55f)
                drawPath(
                    softGlowPath,
                    brush = Brush.linearGradient(listOf(Color(0xFF38BDF8), Color(0xFFA855F7), Color(0xFFEF4444))),
                    alpha = 0.32f + 0.16f * pulse,
                    style = Stroke(width = (inset * 0.22f).coerceAtLeast(2.dp.toPx()), join = StrokeJoin.Round)
                )
            }
            "card_jjk_gojo_domain" -> {
                // Gentle cosmic diamond sparkles along corners (no orbiting balls)
                val corners = listOf(
                    Offset(left - inset * 0.30f, top - inset * 0.30f),
                    Offset(right + inset * 0.30f, top - inset * 0.30f),
                    Offset(left - inset * 0.30f, bottom + inset * 0.30f),
                    Offset(right + inset * 0.30f, bottom + inset * 0.30f)
                )
                corners.forEachIndexed { idx, pt ->
                    val r = inset * (0.28f + 0.08f * sin(radPhase + idx)) * surgeBoost
                    drawLine(Color.White, Offset(pt.x - r, pt.y), Offset(pt.x + r, pt.y), 1.4.dp.toPx(), StrokeCap.Round, alpha = 0.72f)
                    drawLine(Color.White, Offset(pt.x, pt.y - r), Offset(pt.x, pt.y + r), 1.4.dp.toPx(), StrokeCap.Round, alpha = 0.72f)
                }
            }
            "card_dbz_kamehameha" -> {
                for (i in 0 until 14) {
                    val base = perimeterPoint(i / 14f, inset * 0.14f)
                    val outDir = Offset(base.x - cx, base.y - cy).let { v ->
                        val len = kotlin.math.hypot(v.x, v.y).coerceAtLeast(1f)
                        Offset(v.x / len, v.y / len)
                    }
                    val spikeLen = inset * (0.66f + 0.34f * abs(sin(radPhase * 2f + i))) * surgeBoost
                    val perp = Offset(-outDir.y, outDir.x) * (inset * 0.28f)
                    val tip = Offset(base.x + outDir.x * spikeLen, base.y + outDir.y * spikeLen)
                    val p = Path().apply {
                        moveTo(base.x - perp.x, base.y - perp.y)
                        lineTo(tip.x, tip.y)
                        lineTo(base.x + perp.x, base.y + perp.y)
                        close()
                    }
                    drawPath(p, color = if (i % 2 == 0) Color(0xFFFDE047) else Color(0xFF22D3EE), alpha = 0.86f)
                }
            }
            "card_bluelock_striker" -> {
                val corners = listOf(
                    Offset(left - inset * 0.35f, top - inset * 0.35f),
                    Offset(right + inset * 0.35f, top - inset * 0.35f),
                    Offset(left - inset * 0.35f, bottom + inset * 0.35f),
                    Offset(right + inset * 0.35f, bottom + inset * 0.35f)
                )
                corners.forEachIndexed { idx, pt ->
                    val drift = Offset(cos(radPhase + idx * 1.5f) * inset * 0.16f, sin(radPhase + idx * 1.5f) * inset * 0.16f)
                    drawPuzzleGlyph(
                        center = pt + drift,
                        pieceSize = (inset * 0.72f) * surgeBoost,
                        angleDeg = (animAngle * if (idx % 2 == 0) 0.5f else -0.5f) + idx * 24f,
                        fillColor = Color(0xFF0284C7).copy(alpha = 0.90f),
                        edgeColor = Color(0xFF67E8F9)
                    )
                }
            }
            "card_neon_blurple" -> {
                // Legendary Neon Cyber: Multi-layer glowing glass tube halo, holographic side wings & 4-point star flares
                val neonCyan = Color(0xFF00F5FF)
                val neonIndigo = Color(0xFF6366F1)
                val neonPink = Color(0xFFEC4899)
                val tubeGap = inset * 0.34f
                val tubePath = buildCardFramePath(
                    cardFrameId = cardFrameId,
                    left = left - tubeGap,
                    top = top - tubeGap,
                    right = right + tubeGap,
                    bottom = bottom + tubeGap
                )
                val sweepGrad = Brush.linearGradient(
                    colors = listOf(neonCyan, neonIndigo, neonPink, Color(0xFF22D3EE), neonCyan),
                    start = Offset(cx - (right - left) * 0.6f * cos(radPhase), top),
                    end = Offset(cx + (right - left) * 0.6f * cos(radPhase), bottom)
                )
                // Soft outer bloom tube
                drawPath(
                    path = tubePath,
                    brush = sweepGrad,
                    alpha = 0.42f + 0.20f * pulse,
                    style = Stroke(width = (inset * 0.46f).coerceAtLeast(4.dp.toPx()), join = StrokeJoin.Round)
                )
                // White-hot neon glass core
                drawPath(
                    path = tubePath,
                    color = Color.White.copy(alpha = 0.85f),
                    style = Stroke(width = (inset * 0.11f).coerceAtLeast(1.3.dp.toPx()), join = StrokeJoin.Round)
                )

                // Twin Cyber Wing Blades outside left & right HUD notches
                val wingSpanY = (bottom - top) * 0.16f
                val wingOut = inset * (0.82f + 0.14f * pulse)
                listOf(-1f, 1f).forEach { side ->
                    val anchorX = if (side < 0f) left - tubeGap else right + tubeGap
                    val tipX = if (side < 0f) left - wingOut else right + wingOut
                    val col = if (side < 0f) neonCyan else neonPink
                    val wingPath = Path().apply {
                        moveTo(anchorX, cy - wingSpanY)
                        lineTo(tipX, cy)
                        lineTo(anchorX, cy + wingSpanY)
                    }
                    drawPath(
                        wingPath,
                        color = col.copy(alpha = 0.55f + 0.20f * pulse),
                        style = Stroke(width = (inset * 0.24f).coerceAtLeast(2.4.dp.toPx()), cap = StrokeCap.Round, join = StrokeJoin.Round)
                    )
                    drawPath(
                        wingPath,
                        color = Color.White.copy(alpha = 0.92f),
                        style = Stroke(width = (inset * 0.08f).coerceAtLeast(1.1.dp.toPx()), cap = StrokeCap.Round, join = StrokeJoin.Round)
                    )
                }

                // 4 Corner Neon Starlight Flares
                val flareCorners = listOf(
                    Offset(left - inset * 0.36f, top - inset * 0.36f) to neonCyan,
                    Offset(right + inset * 0.36f, top - inset * 0.36f) to neonPink,
                    Offset(left - inset * 0.36f, bottom + inset * 0.36f) to neonPink,
                    Offset(right + inset * 0.36f, bottom + inset * 0.36f) to neonCyan
                )
                flareCorners.forEachIndexed { idx, (pt, glowCol) ->
                    val tw = 0.60f + 0.40f * sin(radPhase * 2f + idx * 1.4f)
                    val r = inset * (0.38f + 0.12f * tw)
                    drawCircle(
                        brush = Brush.radialGradient(listOf(glowCol.copy(alpha = 0.62f * tw), Color.Transparent), pt, r * 2.2f),
                        radius = r * 2.2f,
                        center = pt
                    )
                    drawLine(Color.White.copy(alpha = tw), Offset(pt.x - r, pt.y), Offset(pt.x + r, pt.y), (inset * 0.09f).coerceAtLeast(1.2.dp.toPx()), StrokeCap.Round)
                    drawLine(Color.White.copy(alpha = tw), Offset(pt.x, pt.y - r), Offset(pt.x, pt.y + r), (inset * 0.09f).coerceAtLeast(1.2.dp.toPx()), StrokeCap.Round)
                }
            }
            else -> {
                // External laser corner brackets & diamond stars outside the card
                val bracketLen = inset * 0.9f
                val off = inset * 0.45f
                val bW = (inset * 0.14f).coerceAtLeast(1.8.dp.toPx())
                val bColor = accentColor.copy(alpha = 0.78f + 0.22f * pulse)
                drawLine(bColor, Offset(left - off, top - off + bracketLen), Offset(left - off, top - off), bW, StrokeCap.Round)
                drawLine(bColor, Offset(left - off, top - off), Offset(left - off + bracketLen, top - off), bW, StrokeCap.Round)
                drawLine(bColor, Offset(right + off - bracketLen, top - off), Offset(right + off, top - off), bW, StrokeCap.Round)
                drawLine(bColor, Offset(right + off, top - off), Offset(right + off, top - off + bracketLen), bW, StrokeCap.Round)
                drawLine(bColor, Offset(left - off, bottom + off - bracketLen), Offset(left - off, bottom + off), bW, StrokeCap.Round)
                drawLine(bColor, Offset(left - off, bottom + off), Offset(left - off + bracketLen, bottom + off), bW, StrokeCap.Round)
                drawLine(bColor, Offset(right + off - bracketLen, bottom + off), Offset(right + off, bottom + off), bW, StrokeCap.Round)
                drawLine(bColor, Offset(right + off, bottom + off - bracketLen), Offset(right + off, bottom + off), bW, StrokeCap.Round)
            }
        }
    }
}

/**
 * Continuous animated layer INSIDE EVERY card frame (works both inside full DiscordProfileSheetDialog
 * AND inside PEY Shop live card thumbnails!).
 */
@Composable
fun ProfileCardAnimatedAmbientBackground(
    cardFrameId: String,
    accentColor: Color,
    animAngle: Float,
    pulse: Float,
    modifier: Modifier = Modifier
) {
    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height
        val cx = w * 0.5f
        val rad = animAngle * PI.toFloat() / 180f

        // Sweeping diagonal light sheen across every frame
        val sweepOffset = ((animAngle / 360f) * (w + h)) - h * 0.4f
        drawLine(
            color = accentColor.copy(alpha = 0.10f + 0.08f * pulse),
            start = Offset(sweepOffset, 0f),
            end = Offset(sweepOffset - h * 0.55f, h),
            strokeWidth = w * 0.16f
        )

        when (cardFrameId) {
            "card_jjk_sukuna_shrine" -> {
                drawRect(
                    brush = Brush.verticalGradient(
                        colors = listOf(Color.Transparent, Color(0xFFEF4444).copy(alpha = 0.18f + 0.12f * pulse), Color(0xFF991B1B).copy(alpha = 0.42f)),
                        startY = h * 0.46f,
                        endY = h
                    )
                )
                for (i in 0..9) {
                    val t = ((animAngle / 360f + i * 0.1f) % 1f)
                    val ex = w * (0.08f + i * 0.09f) + sin(rad * 2f + i) * w * 0.03f
                    val ey = h * (0.98f - t * 0.85f)
                    drawCircle(
                        color = if (i % 2 == 0) Color(0xFFFDE047) else Color(0xFFFF4500),
                        radius = (w * 0.016f * (1f - t * 0.4f)).coerceAtLeast(1.4f),
                        center = Offset(ex, ey),
                        alpha = (1f - t) * 0.88f
                    )
                }
                val slashAlpha = (sin(rad * 2f).coerceAtLeast(0f)) * 0.52f
                drawLine(Color(0xFFFF4500), Offset(w * 0.06f, h * 0.22f), Offset(w * 0.36f, h * 0.09f), (w * 0.012f).coerceAtLeast(1.4f), alpha = slashAlpha)
                drawLine(Color.White, Offset(w * 0.65f, h * 0.86f), Offset(w * 0.94f, h * 0.72f), (w * 0.01f).coerceAtLeast(1.2f), alpha = slashAlpha)
            }
            "card_jjk_gojo_void" -> {
                for (i in 0..12) {
                    val sx = w * ((0.10f + i * 0.17f) % 0.90f)
                    val sy = h * ((0.12f + i * 0.21f) % 0.88f)
                    val starPulse = 0.35f + 0.65f * abs(sin(rad + i))
                    drawCircle(Color(0xFF7DD3FC), radius = (w * 0.014f).coerceAtLeast(1.2f), center = Offset(sx, sy), alpha = starPulse * 0.75f)
                }
                val bPos = Offset(cx + cos(rad) * w * 0.30f, h * 0.22f + sin(rad) * h * 0.04f)
                val rPos = Offset(cx - cos(rad) * w * 0.30f, h * 0.22f - sin(rad) * h * 0.04f)
                drawCircle(Brush.radialGradient(listOf(Color(0xFF38BDF8), Color.Transparent), bPos, w * 0.12f), w * 0.12f, bPos, alpha = 0.65f)
                drawCircle(Brush.radialGradient(listOf(Color(0xFFEF4444), Color.Transparent), rPos, w * 0.12f), w * 0.12f, rPos, alpha = 0.65f)
            }
            "card_jjk_gojo_domain" -> {
                // Deep Unlimited Void cosmic horizon + white event-horizon ring + shooting hyperspace stars
                drawRect(
                    brush = Brush.radialGradient(
                        colors = listOf(Color(0xFF0B1330), Color(0xFF040610), Color(0xFF010207)),
                        center = Offset(cx, h * 0.28f),
                        radius = h * 0.85f
                    ),
                    alpha = 0.88f
                )
                val horizonC = Offset(cx, h * 0.26f)
                val holeR = w * 0.14f
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(Color.Black, Color.Black, Color.White, Color(0xFF38BDF8).copy(alpha = 0.7f), Color(0xFF6366F1).copy(alpha = 0.3f), Color.Transparent),
                        center = horizonC,
                        radius = holeR * 2.5f
                    ),
                    radius = holeR * 2.5f,
                    center = horizonC
                )
                drawCircle(Color.Black, holeR * 0.72f, horizonC)
                drawCircle(Color.White.copy(alpha = 0.85f + 0.15f * pulse), holeR * 0.86f, horizonC, style = Stroke((w * 0.015f).coerceAtLeast(1.4f)))
                for (s in 0 until 14) {
                    val ang = (s * (360f / 14f) + animAngle * 0.45f) * PI.toFloat() / 180f
                    val dist = w * (0.22f + ((s * 0.11f + animAngle / 360f) % 0.65f))
                    val p1 = Offset(horizonC.x + cos(ang) * dist, horizonC.y + sin(ang) * dist * 0.85f)
                    val p2 = Offset(horizonC.x + cos(ang) * (dist + w * 0.08f), horizonC.y + sin(ang) * (dist + w * 0.08f) * 0.85f)
                    drawLine(Color(0xFFE0F2FE), p1, p2, (w * 0.008f).coerceAtLeast(1f), StrokeCap.Round, alpha = 0.68f)
                }
            }
            "card_dbz_kamehameha" -> {
                for (i in 0..9) {
                    val t = ((animAngle / 360f + i * 0.1f) % 1f)
                    val kx = w * (0.08f + i * 0.09f)
                    val ky = h * (0.96f - t * 0.82f)
                    drawCircle(
                        color = if (i % 2 == 0) Color(0xFFFDE047) else Color(0xFF22D3EE),
                        radius = (w * 0.018f * (1f - t * 0.45f)).coerceAtLeast(1.4f),
                        center = Offset(kx, ky),
                        alpha = (1f - t) * 0.88f
                    )
                }
                drawCircle(
                    color = Color(0xFF22D3EE).copy(alpha = 0.20f + 0.14f * pulse),
                    radius = w * 0.38f,
                    center = Offset(cx, h * 0.24f),
                    style = Stroke(width = (w * 0.012f).coerceAtLeast(1.3f), pathEffect = PathEffect.dashPathEffect(floatArrayOf(18f, 12f), animAngle))
                )
            }
            "card_bluelock_striker" -> {
                val gridAlpha = 0.18f + 0.10f * pulse
                for (g in 1..4) {
                    drawLine(Color(0xFF22D3EE), Offset(w * g * 0.2f, 0f), Offset(w * g * 0.2f, h), (w * 0.006f).coerceAtLeast(0.8f), alpha = gridAlpha)
                }
                drawPuzzleGlyph(
                    center = Offset(w * 0.78f, h * 0.30f + sin(rad) * h * 0.03f),
                    pieceSize = w * 0.18f,
                    angleDeg = 14f + sin(rad) * 8f,
                    fillColor = Color(0xFF0284C7).copy(alpha = 0.55f),
                    edgeColor = Color(0xFF67E8F9).copy(alpha = 0.85f)
                )
                drawPuzzleGlyph(
                    center = Offset(w * 0.22f, h * 0.72f - sin(rad) * h * 0.03f),
                    pieceSize = w * 0.16f,
                    angleDeg = -18f - sin(rad) * 8f,
                    fillColor = Color(0xFF059669).copy(alpha = 0.55f),
                    edgeColor = Color(0xFF6EE7B7).copy(alpha = 0.85f)
                )
                drawBlueLockSoccerBall(
                    center = Offset(w * 0.75f, h * 0.21f),
                    radius = w * 0.115f,
                    spinDeg = animAngle,
                    alpha = 0.78f + 0.18f * pulse
                )
            }
            "card_gaming_rgb" -> {
                val scanY = h * ((animAngle / 360f) % 1f)
                drawLine(Color(0xFF22D3EE).copy(alpha = 0.35f), Offset(0f, scanY), Offset(w, scanY), (h * 0.018f).coerceAtLeast(1.5f))
                drawCircle(Color(0xFFEF4444).copy(alpha = 0.22f + 0.18f * pulse), w * 0.14f, Offset(w * 0.82f, h * 0.20f))
                drawCircle(Color(0xFF10B981).copy(alpha = 0.22f + 0.18f * pulse), w * 0.12f, Offset(w * 0.20f, h * 0.80f))
            }
            "card_neon_blurple" -> {
                val neonCyan = Color(0xFF00F5FF)
                val neonPink = Color(0xFFEC4899)
                drawCircle(
                    brush = Brush.radialGradient(listOf(neonCyan.copy(alpha = 0.24f + 0.12f * pulse), Color.Transparent), Offset(w * 0.16f, h * 0.20f), w * 0.42f),
                    radius = w * 0.42f,
                    center = Offset(w * 0.16f, h * 0.20f)
                )
                drawCircle(
                    brush = Brush.radialGradient(listOf(neonPink.copy(alpha = 0.24f + 0.12f * pulse), Color.Transparent), Offset(w * 0.84f, h * 0.78f), w * 0.42f),
                    radius = w * 0.42f,
                    center = Offset(w * 0.84f, h * 0.78f)
                )
                // Futuristic neon laser ribbons & micro HUD grid accents
                val beamShift = ((animAngle / 360f) * w * 1.4f) - w * 0.2f
                drawLine(
                    brush = Brush.linearGradient(listOf(Color.Transparent, neonCyan.copy(alpha = 0.42f), Color.White.copy(alpha = 0.55f), neonPink.copy(alpha = 0.42f), Color.Transparent)),
                    start = Offset(beamShift, 0f),
                    end = Offset(beamShift - w * 0.35f, h),
                    strokeWidth = (w * 0.018f).coerceAtLeast(1.5f)
                )
                for (i in 0..7) {
                    val sx = w * (0.12f + i * 0.11f)
                    val sy = h * (0.16f + ((i * 0.21f) % 0.72f))
                    val starA = 0.42f + 0.45f * abs(sin(rad + i * 1.1f))
                    val col = if (i % 2 == 0) neonCyan else neonPink
                    val sr = (w * 0.024f).coerceAtLeast(1.8f)
                    drawLine(col.copy(alpha = starA), Offset(sx - sr, sy), Offset(sx + sr, sy), 1.3f, StrokeCap.Round)
                    drawLine(col.copy(alpha = starA), Offset(sx, sy - sr), Offset(sx, sy + sr), 1.3f, StrokeCap.Round)
                }
            }
            "card_royal_gold" -> {
                for (i in 0..5) {
                    val gx = w * (0.16f + i * 0.13f)
                    val gy = h * (0.20f + ((i * 0.17f) % 0.65f)) - (sin(rad + i) * h * 0.03f)
                    drawCircle(Color(0xFFFDE047), radius = (w * 0.018f).coerceAtLeast(1.4f), center = Offset(gx, gy), alpha = 0.45f + 0.35f * pulse)
                }
            }
            else -> {
                for (i in 0..6) {
                    val sx = w * (0.14f + (i * 0.12f))
                    val sy = h * (0.18f + ((i * 0.23f) % 0.70f))
                    drawCircle(
                        color = accentColor,
                        radius = (w * 0.015f * (0.75f + 0.45f * pulse)).coerceAtLeast(1.3f),
                        center = Offset(sx, sy),
                        alpha = 0.35f + 0.35f * abs(sin(rad + i))
                    )
                }
            }
        }
    }
}

/**
 * 5-Second (0f -> 1f) full-card anime cinematic entrance sequence with synchronized sound.
 */
@Composable
fun ProfileCardFiveSecondEntranceOverlay(
    cardFrameId: String,
    progress: Float,
    animAngle: Float,
    modifier: Modifier = Modifier
) {
    val p = progress.coerceIn(0f, 1f)
    val masterAlpha = if (p > 0.86f) ((1f - p) / 0.14f).coerceIn(0f, 1f) else 1f
    val rad = animAngle * PI.toFloat() / 180f

    Canvas(modifier = modifier) {
        if (masterAlpha <= 0.01f) return@Canvas
        val w = size.width
        val h = size.height
        val cx = w * 0.5f
        val cy = h * 0.44f

        when (cardFrameId) {
            "card_jjk_gojo_void" -> {
                // Full 19s Hollow Purple ("Jutsushiki Hanten: Aka" -> "Kyoshiki... Murasaki!" Explosion)
                drawRect(color = Color(0xFF060312).copy(alpha = 0.88f * masterAlpha))
                for (rIdx in 1..3) {
                    rotate(animAngle * if (rIdx % 2 == 0) 0.9f else -0.7f, pivot = Offset(cx, cy)) {
                        drawCircle(
                            brush = Brush.sweepGradient(listOf(Color(0xFF38BDF8), Color(0xFFEF4444), Color(0xFFA855F7), Color(0xFFEC4899), Color(0xFF38BDF8))),
                            radius = w * (0.18f * rIdx + 0.06f * sin(rad * 1.5f)),
                            center = Offset(cx, cy),
                            alpha = 0.44f * masterAlpha,
                            style = Stroke(width = 3.dp.toPx(), pathEffect = PathEffect.dashPathEffect(floatArrayOf(28f, 14f)))
                        )
                    }
                }
                val eyeAlpha = when {
                    p < 0.08f -> (p / 0.08f)
                    p < 0.62f -> 1f
                    else -> ((0.80f - p) / 0.18f).coerceIn(0f, 1f)
                } * masterAlpha
                if (eyeAlpha > 0.01f) {
                    val eyeY = h * 0.24f
                    val eyeSep = w * 0.18f
                    for (side in listOf(-1f, 1f)) {
                        val ex = cx + side * eyeSep
                        drawCircle(
                            brush = Brush.radialGradient(listOf(Color(0xFFE0F2FE), Color(0xFF38BDF8), Color.Transparent), Offset(ex, eyeY), w * 0.17f),
                            radius = w * 0.17f,
                            center = Offset(ex, eyeY),
                            alpha = eyeAlpha
                        )
                        val almond = Path().apply {
                            moveTo(ex - w * 0.11f, eyeY)
                            quadraticTo(ex, eyeY - h * 0.055f, ex + w * 0.11f, eyeY)
                            quadraticTo(ex, eyeY + h * 0.055f, ex - w * 0.11f, eyeY)
                            close()
                        }
                        drawPath(almond, color = Color(0xFF7DD3FC), alpha = eyeAlpha)
                        drawCircle(color = Color.White, radius = w * 0.036f, center = Offset(ex, eyeY), alpha = eyeAlpha)
                    }
                }
                // Stage 1 (0.00..0.24): "Jutsushiki Hanten: Aka" — fierce Crimson Reversal Red ignites!
                if (p < 0.24f) {
                    val redIn = (p / 0.14f).coerceIn(0f, 1f)
                    val redPos = Offset(cx + w * 0.18f * (1f - (p / 0.24f) * 0.35f), cy + h * 0.05f)
                    val redR = w * (0.09f + 0.12f * redIn + 0.025f * sin(rad * 3f))
                    drawCircle(
                        brush = Brush.radialGradient(listOf(Color.White, Color(0xFFFF6B6B), Color(0xFFDC2626), Color.Transparent), redPos, redR * 2.2f),
                        radius = redR * 2.2f,
                        center = redPos,
                        alpha = masterAlpha
                    )
                    drawCircle(
                        color = Color(0xFFFCA5A5),
                        radius = redR * (1.25f + 0.45f * (p % 0.12f) / 0.12f),
                        center = redPos,
                        alpha = 0.65f * masterAlpha,
                        style = Stroke(width = 2.6.dp.toPx())
                    )
                }
                // Stage 2 (0.20..0.60): Reversal Red + Lapse Blue spiral closer with violet lightning arcs!
                if (p in 0.20f..0.62f) {
                    val converge = ((p - 0.20f) / 0.40f).coerceIn(0f, 1f)
                    val orbitA = animAngle * 2.2f * PI.toFloat() / 180f
                    val sep = w * 0.34f * (1f - converge)
                    val blueCenter = Offset(cx - cos(orbitA) * sep, cy - sin(orbitA) * sep * 0.45f)
                    val redCenter = Offset(cx + cos(orbitA) * sep, cy + sin(orbitA) * sep * 0.45f)
                    val orbR = w * (0.11f + 0.06f * converge)
                    drawCircle(Brush.radialGradient(listOf(Color.White, Color(0xFF38BDF8), Color(0xFF0284C7), Color.Transparent), blueCenter, orbR * 2.1f), orbR * 2.1f, blueCenter, alpha = masterAlpha)
                    drawCircle(Brush.radialGradient(listOf(Color.White, Color(0xFFF87171), Color(0xFFDC2626), Color.Transparent), redCenter, orbR * 2.1f), orbR * 2.1f, redCenter, alpha = masterAlpha)
                    drawLine(
                        color = Color(0xFFD946EF),
                        start = blueCenter,
                        end = redCenter,
                        strokeWidth = (2.5f + 4f * converge).dp.toPx(),
                        cap = StrokeCap.Round,
                        alpha = (0.35f + 0.55f * converge) * masterAlpha
                    )
                }
                // Stage 3 (0.56..1.00): "Kyoshiki... Murasaki!" — Massive Hollow Purple singularity & beam erasure!
                if (p >= 0.56f) {
                    val blast = ((p - 0.56f) / 0.44f).coerceIn(0f, 1f)
                    val pulseScale = 1f + 0.08f * sin(rad * 4f)
                    val blastR = w * (0.14f + blast * 1.22f) * pulseScale
                    drawCircle(
                        brush = Brush.radialGradient(
                            colors = listOf(Color.White, Color(0xFFF0ABFC), Color(0xFFA855F7), Color(0xFF6B21A8).copy(alpha = 0.72f), Color.Transparent),
                            center = Offset(cx, cy),
                            radius = blastR
                        ),
                        radius = blastR,
                        center = Offset(cx, cy),
                        alpha = masterAlpha
                    )
                    for (ring in 0..2) {
                        val ringPhase = ((blast * 2.4f + ring * 0.33f) % 1f)
                        drawCircle(
                            color = Color.White,
                            radius = blastR * (0.35f + 0.75f * ringPhase),
                            center = Offset(cx, cy),
                            alpha = (1f - ringPhase) * 0.85f * masterAlpha,
                            style = Stroke(width = (4.5f - 2f * ringPhase).dp.toPx())
                        )
                    }
                }
            }

            "card_jjk_gojo_domain" -> {
                // Real 5-Second Gojo Unlimited Void ("Ryoiki Tenkai: Muryokusho") Domain Expansion!
                drawRect(color = Color(0xFF010207).copy(alpha = 0.94f * masterAlpha))

                // Phase 1 (0..0.28): Gojo Crossed-Fingers Hand Sign (Taishakuten mudra) + spatial fractures
                if (p <= 0.38f) {
                    val handAlpha = if (p < 0.24f) (p / 0.10f).coerceIn(0f, 1f) else ((0.38f - p) / 0.14f).coerceIn(0f, 1f)
                    val hx = cx
                    val hy = h * 0.76f
                    val fLen = h * 0.28f
                    val fW = w * 0.042f
                    // Glow behind crossed fingers
                    drawCircle(
                        brush = Brush.radialGradient(listOf(Color(0xFF38BDF8).copy(alpha = 0.75f), Color.Transparent), Offset(hx, hy - fLen * 0.45f), w * 0.28f),
                        radius = w * 0.28f,
                        center = Offset(hx, hy - fLen * 0.45f),
                        alpha = handAlpha * masterAlpha
                    )
                    // Back middle finger leaning right-up, crossing index finger leaning left-up (Gojo's iconic hand sign!)
                    drawLine(Color(0xFF0F172A), Offset(hx - fW * 0.4f, hy), Offset(hx + fW * 1.15f, hy - fLen * 0.92f), fW * 1.15f, StrokeCap.Round, alpha = handAlpha * masterAlpha)
                    drawLine(Color(0xFFFDF2EB), Offset(hx - fW * 0.4f, hy), Offset(hx + fW * 1.15f, hy - fLen * 0.92f), fW * 0.78f, StrokeCap.Round, alpha = handAlpha * masterAlpha)
                    drawLine(Color(0xFF0F172A), Offset(hx + fW * 0.35f, hy), Offset(hx - fW * 0.75f, hy - fLen), fW * 1.25f, StrokeCap.Round, alpha = handAlpha * masterAlpha)
                    drawLine(Color(0xFFFFF8F2), Offset(hx + fW * 0.35f, hy), Offset(hx - fW * 0.75f, hy - fLen), fW * 0.85f, StrokeCap.Round, alpha = handAlpha * masterAlpha)
                    // Spatial shatter crack lines radiating outward during activation
                    if (p >= 0.16f) {
                        val crackP = ((p - 0.16f) / 0.16f).coerceIn(0f, 1f)
                        for (c in 0 until 8) {
                            val a = (c * 45f + 12f) * PI.toFloat() / 180f
                            val start = Offset(cx + cos(a) * w * 0.08f, cy + sin(a) * w * 0.08f)
                            val end = Offset(cx + cos(a) * w * 0.65f * crackP, cy + sin(a) * w * 0.65f * crackP)
                            drawLine(Color.White, start, end, 2.2.dp.toPx(), StrokeCap.Round, alpha = (1f - crackP * 0.4f) * masterAlpha)
                        }
                    }
                }

                // Phase 2 (0.20..0.44): Explosive White-Black Domain Shockwave Expanding
                if (p in 0.20f..0.44f) {
                    val shockP = ((p - 0.20f) / 0.24f).coerceIn(0f, 1f)
                    val shockR = w * (0.06f + shockP * 1.15f)
                    drawCircle(
                        color = Color.White,
                        radius = shockR,
                        center = Offset(cx, cy),
                        alpha = (1f - shockP * 0.6f) * masterAlpha,
                        style = Stroke(width = (14.dp.toPx() * (1f - shockP * 0.5f)).coerceAtLeast(2f))
                    )
                    drawCircle(
                        color = Color(0xFF7DD3FC),
                        radius = shockR * 0.82f,
                        center = Offset(cx, cy),
                        alpha = (1f - shockP) * 0.75f * masterAlpha,
                        style = Stroke(width = 4.dp.toPx())
                    )
                }

                // Phase 3 (0.28..1.0): Full Unlimited Void — Rushing 3D Warp Stars + White Singularity Black Hole + Six Eyes
                if (p >= 0.28f) {
                    val voidFade = ((p - 0.28f) / 0.14f).coerceIn(0f, 1f) * masterAlpha
                    val voidC = Offset(cx, h * 0.43f)
                    // 3D hyperspace speed starlines shooting endlessly outward
                    for (i in 0 until 44) {
                        val baseAngle = (i * 137.5f + animAngle * 0.35f) * PI.toFloat() / 180f
                        val travel = ((p * 2.8f + i * 0.17f) % 1f)
                        val rInner = w * (0.12f + travel * 0.72f)
                        val rOuter = rInner + w * (0.06f + travel * 0.14f)
                        val starColor = when (i % 4) {
                            0 -> Color.White
                            1 -> Color(0xFF7DD3FC)
                            2 -> Color(0xFFA855F7)
                            else -> Color(0xFF6366F1)
                        }
                        drawLine(
                            color = starColor,
                            start = Offset(voidC.x + cos(baseAngle) * rInner, voidC.y + sin(baseAngle) * rInner * 1.15f),
                            end = Offset(voidC.x + cos(baseAngle) * rOuter, voidC.y + sin(baseAngle) * rOuter * 1.15f),
                            strokeWidth = (1.2f + travel * 2.4f).dp.toPx(),
                            cap = StrokeCap.Round,
                            alpha = (1f - travel * 0.55f) * voidFade
                        )
                    }

                    // Swirling Void Event Horizon Black Hole (glowing white-blue-violet corona + pure black core)
                    val holeR = w * (0.20f + 0.03f * sin(rad * 2f))
                    drawCircle(
                        brush = Brush.radialGradient(
                            colors = listOf(Color.Black, Color.Black, Color.White, Color(0xFF38BDF8), Color(0xFF7E22CE).copy(alpha = 0.55f), Color.Transparent),
                            center = voidC,
                            radius = holeR * 2.15f
                        ),
                        radius = holeR * 2.15f,
                        center = voidC,
                        alpha = voidFade
                    )
                    drawCircle(Color.Black, holeR * 0.74f, voidC, alpha = voidFade)
                    drawCircle(Color.White, holeR * 0.88f, voidC, alpha = voidFade, style = Stroke(width = 3.2.dp.toPx()))
                    rotate(animAngle * 1.4f, pivot = voidC) {
                        drawOval(
                            brush = Brush.sweepGradient(listOf(Color.White, Color(0xFF38BDF8), Color(0xFFA855F7), Color.White)),
                            topLeft = Offset(voidC.x - holeR * 1.65f, voidC.y - holeR * 0.55f),
                            size = Size(holeR * 3.3f, holeR * 1.1f),
                            alpha = 0.75f * voidFade,
                            style = Stroke(width = 2.4.dp.toPx())
                        )
                    }

                    // Iconic Six Eyes shimmering above the Unlimited Void singularity
                    val eyeY = h * 0.21f
                    val eyeSep = w * 0.16f
                    for (side in listOf(-1f, 1f)) {
                        val ex = cx + side * eyeSep
                        drawCircle(
                            brush = Brush.radialGradient(listOf(Color(0xFFE0F2FE), Color(0xFF38BDF8), Color.Transparent), Offset(ex, eyeY), w * 0.13f),
                            radius = w * 0.13f,
                            center = Offset(ex, eyeY),
                            alpha = 0.90f * voidFade
                        )
                        val eyePath = Path().apply {
                            moveTo(ex - w * 0.095f, eyeY)
                            quadraticTo(ex, eyeY - h * 0.042f, ex + w * 0.095f, eyeY)
                            quadraticTo(ex, eyeY + h * 0.042f, ex - w * 0.095f, eyeY)
                            close()
                        }
                        drawPath(eyePath, color = Color(0xFF7DD3FC), alpha = voidFade)
                        drawCircle(color = Color.White, radius = w * 0.028f, center = Offset(ex, eyeY), alpha = voidFade)
                    }
                }
            }

            "card_dbz_kamehameha" -> {
                drawRect(color = Color(0xFF040B1A).copy(alpha = 0.84f * masterAlpha))
                for (i in 0..7) {
                    val fx = w * (0.06f + i * 0.125f)
                    val fyTop = h * (0.15f + 0.35f * (1f - p))
                    drawLine(
                        brush = Brush.verticalGradient(listOf(Color.Transparent, Color(0xFFFDE047), Color(0xFFF59E0B)), fyTop, h),
                        start = Offset(fx, fyTop),
                        end = Offset(fx, h),
                        strokeWidth = 10.dp.toPx(),
                        cap = StrokeCap.Round,
                        alpha = 0.40f * masterAlpha
                    )
                }
                val chargeCenter = Offset(cx, h * 0.72f)
                val handSpan = w * 0.22f
                val palmL = Path().apply {
                    moveTo(cx - handSpan * 1.1f, h * 0.86f)
                    quadraticTo(cx - handSpan * 0.7f, h * 0.65f, cx - handSpan * 0.25f, h * 0.69f)
                    lineTo(cx - handSpan * 0.18f, h * 0.86f)
                    close()
                }
                val palmR = Path().apply {
                    moveTo(cx + handSpan * 1.1f, h * 0.86f)
                    quadraticTo(cx + handSpan * 0.7f, h * 0.65f, cx + handSpan * 0.25f, h * 0.69f)
                    lineTo(cx + handSpan * 0.18f, h * 0.86f)
                    close()
                }
                drawPath(palmL, color = Color(0xFFE0F2FE), alpha = masterAlpha)
                drawPath(palmR, color = Color(0xFFE0F2FE), alpha = masterAlpha)
                val chargeFraction = (p / 0.44f).coerceIn(0f, 1f)
                val sphereR = w * (0.08f + 0.18f * chargeFraction)
                drawCircle(
                    brush = Brush.radialGradient(listOf(Color.White, Color(0xFF67E8F9), Color(0xFF0284C7), Color.Transparent), chargeCenter, sphereR * 2.1f),
                    radius = sphereR * 2.1f,
                    center = chargeCenter,
                    alpha = masterAlpha
                )
                if (p >= 0.44f) {
                    val beamP = ((p - 0.44f) / 0.35f).coerceIn(0f, 1f)
                    val beamTop = chargeCenter.y - (chargeCenter.y + h * 0.12f) * beamP
                    val halfBeamW = w * (0.14f + 0.18f * sin(beamP * PI.toFloat()).coerceAtLeast(0.4f))
                    val beamRect = Path().apply {
                        moveTo(cx - halfBeamW * 0.55f, chargeCenter.y)
                        lineTo(cx - halfBeamW, beamTop)
                        lineTo(cx + halfBeamW, beamTop)
                        lineTo(cx + halfBeamW * 0.55f, chargeCenter.y)
                        close()
                    }
                    drawPath(
                        beamRect,
                        brush = Brush.horizontalGradient(
                            colors = listOf(Color(0xFF0284C7).copy(alpha = 0.45f), Color(0xFF67E8F9), Color.White, Color(0xFF67E8F9), Color(0xFF0284C7).copy(alpha = 0.45f)),
                            startX = cx - halfBeamW,
                            endX = cx + halfBeamW
                        ),
                        alpha = masterAlpha
                    )
                    for (ringIdx in 0..2) {
                        val ringY = chargeCenter.y - (chargeCenter.y - beamTop) * ((ringIdx * 0.33f + animAngle / 360f) % 1f)
                        drawOval(
                            color = Color.White,
                            topLeft = Offset(cx - halfBeamW * 1.15f, ringY - 8.dp.toPx()),
                            size = Size(halfBeamW * 2.3f, 16.dp.toPx()),
                            alpha = 0.78f * masterAlpha,
                            style = Stroke(width = 3.dp.toPx())
                        )
                    }
                }
            }

            "card_bluelock_striker" -> {
                drawRect(color = Color(0xFF020B1C).copy(alpha = 0.88f * masterAlpha))
                val gridAlpha = 0.30f * masterAlpha
                for (i in 1..5) {
                    drawLine(Color(0xFF22D3EE), Offset(w * i / 6f, 0f), Offset(w * i / 6f, h), 1.2.dp.toPx(), alpha = gridAlpha)
                    drawLine(Color(0xFF1D4ED8), Offset(0f, h * i / 6f), Offset(w, h * i / 6f), 1.2.dp.toPx(), alpha = gridAlpha)
                }
                val ballScale = (p / 0.25f).coerceIn(0.35f, 1f)
                drawBlueLockSoccerBall(
                    center = Offset(cx, h * 0.30f),
                    radius = w * 0.18f * ballScale,
                    spinDeg = animAngle * 1.6f,
                    alpha = masterAlpha
                )
                val snapProgress = ((p - 0.22f) / 0.56f).coerceIn(0f, 1f)
                val puzzleTargets = listOf(
                    Triple(Offset(cx, h * 0.62f), Color(0xFF0284C7), Color(0xFF67E8F9)),
                    Triple(Offset(cx - w * 0.16f, h * 0.62f), Color(0xFF059669), Color(0xFF6EE7B7)),
                    Triple(Offset(cx + w * 0.16f, h * 0.62f), Color(0xFF1D4ED8), Color(0xFF38BDF8)),
                    Triple(Offset(cx - w * 0.08f, h * 0.75f), Color(0xFF0EA5E9), Color(0xFFE0F2FE)),
                    Triple(Offset(cx + w * 0.08f, h * 0.75f), Color(0xFF10B981), Color(0xFFA7F3D0)),
                    Triple(Offset(cx - w * 0.24f, h * 0.49f), Color(0xFF1E40AF), Color(0xFF22D3EE)),
                    Triple(Offset(cx + w * 0.24f, h * 0.49f), Color(0xFF0369A1), Color(0xFF67E8F9))
                )
                puzzleTargets.forEachIndexed { idx, (target, fillC, edgeC) ->
                    val pieceDelay = (idx * 0.08f)
                    val localT = ((snapProgress - pieceDelay) / 0.55f).coerceIn(0f, 1f)
                    if (localT > 0.01f) {
                        val startPos = Offset(
                            target.x + (if (idx % 2 == 0) -1 else 1) * w * 0.42f * (1f - localT),
                            target.y + h * 0.25f * (1f - localT)
                        )
                        drawPuzzleGlyph(
                            center = startPos,
                            pieceSize = w * 0.165f,
                            angleDeg = (1f - localT) * (if (idx % 2 == 0) -65f else 65f),
                            fillColor = fillC.copy(alpha = 0.90f * masterAlpha),
                            edgeColor = edgeC.copy(alpha = masterAlpha)
                        )
                    }
                }
                drawArc(
                    color = Color(0xFF22D3EE),
                    startAngle = 10f,
                    sweepAngle = 160f * snapProgress,
                    useCenter = false,
                    topLeft = Offset(w * 0.10f, h * 0.56f),
                    size = Size(w * 0.80f, h * 0.36f),
                    alpha = 0.78f * masterAlpha,
                    style = Stroke(width = 3.5.dp.toPx(), pathEffect = PathEffect.dashPathEffect(floatArrayOf(18f, 10f), animAngle))
                )
            }

            "card_jjk_sukuna_shrine" -> {
                // Full 17.8s Malevolent Shrine ("Ryoiki Tenkai: Fukuma Mizushi") synchronized visuals
                drawRect(
                    brush = Brush.verticalGradient(
                        colors = listOf(Color(0xFF1A0306), Color(0xFF45060C), Color(0xFF0E0203)),
                        startY = 0f,
                        endY = h
                    ),
                    alpha = 0.92f * masterAlpha
                )
                val shrineReveal = ((p - 0.14f) / 0.34f).coerceIn(0f, 1f)
                drawCircle(
                    brush = Brush.radialGradient(listOf(Color(0xFFEF4444).copy(alpha = 0.80f), Color.Transparent), Offset(cx, h * 0.34f), w * 0.42f),
                    radius = w * 0.42f,
                    center = Offset(cx, h * 0.34f),
                    alpha = shrineReveal * masterAlpha
                )
                if (shrineReveal > 0.05f) {
                    val gateInk = Color(0xFF0A0203)
                    val gateEdge = Color(0xFFEF4444)
                    val gateY = h * (0.48f - 0.12f * shrineReveal)
                    val roof = Path().apply {
                        moveTo(cx - w * 0.36f, gateY)
                        quadraticTo(cx, gateY + h * 0.045f, cx + w * 0.36f, gateY)
                        lineTo(cx + w * 0.32f, gateY + h * 0.06f)
                        quadraticTo(cx, gateY + h * 0.085f, cx - w * 0.32f, gateY + h * 0.06f)
                        close()
                    }
                    drawPath(roof, color = gateInk, alpha = shrineReveal * masterAlpha)
                    drawPath(roof, color = gateEdge, alpha = shrineReveal * masterAlpha, style = Stroke(width = 2.2.dp.toPx()))
                    drawRect(gateInk, Offset(cx - w * 0.19f, gateY + h * 0.05f), Size(w * 0.058f, h * 0.28f), alpha = shrineReveal * masterAlpha)
                    drawRect(gateInk, Offset(cx + w * 0.132f, gateY + h * 0.05f), Size(w * 0.058f, h * 0.28f), alpha = shrineReveal * masterAlpha)
                }
                val fireCount = 16
                for (i in 0 until fireCount) {
                    val fx = w * (i.toFloat() / (fireCount - 1))
                    val fHeight = h * (0.24f + 0.24f * abs(sin(rad * 3.0f + i * 0.9f)) + 0.14f * sin(p * PI.toFloat()))
                    val fPath = Path().apply {
                        moveTo(fx - w * 0.09f, h)
                        lineTo(fx, h - fHeight)
                        lineTo(fx + w * 0.09f, h)
                        close()
                    }
                    drawPath(
                        fPath,
                        brush = Brush.verticalGradient(listOf(Color(0xFFFDE047), Color(0xFFFF4500), Color(0xFF991B1B)), h - fHeight, h),
                        alpha = 0.82f * masterAlpha
                    )
                }
                val handsScale = (p / 0.22f).coerceIn(0.4f, 1f)
                val handsY = h * 0.86f
                val span = w * 0.20f * handsScale
                val spireH = h * 0.24f * handsScale
                val handFill = Color(0xFFFFF5EB)
                val inkColor = Color(0xFF14090A)
                val lHand = Path().apply {
                    moveTo(cx - span * 1.15f, handsY)
                    lineTo(cx - span * 0.56f, handsY - spireH * 0.56f)
                    lineTo(cx - span * 0.04f, handsY - spireH)
                    lineTo(cx + span * 0.02f, handsY - spireH * 0.54f)
                    lineTo(cx - span * 0.28f, handsY - spireH * 0.14f)
                    close()
                }
                val rHand = Path().apply {
                    moveTo(cx + span * 1.15f, handsY)
                    lineTo(cx + span * 0.56f, handsY - spireH * 0.56f)
                    lineTo(cx + span * 0.04f, handsY - spireH)
                    lineTo(cx - span * 0.02f, handsY - spireH * 0.54f)
                    lineTo(cx + span * 0.28f, handsY - spireH * 0.14f)
                    close()
                }
                drawPath(lHand, color = handFill, alpha = masterAlpha)
                drawPath(rHand, color = handFill, alpha = masterAlpha)
                drawPath(lHand, color = inkColor, alpha = masterAlpha, style = Stroke(width = 2.2.dp.toPx(), join = StrokeJoin.Round))
                drawPath(rHand, color = inkColor, alpha = masterAlpha, style = Stroke(width = 2.2.dp.toPx(), join = StrokeJoin.Round))
                if (p >= 0.36f) {
                    val slashMarks = listOf(0.36f, 0.44f, 0.52f, 0.60f, 0.68f, 0.76f, 0.84f, 0.91f)
                    slashMarks.forEachIndexed { idx, startT ->
                        val local = ((p - startT) / 0.10f).coerceIn(0f, 1f)
                        if (local > 0f && local < 1f) {
                            val sx = if (idx % 2 == 0) w * 0.06f else w * 0.94f
                            val ex = if (idx % 2 == 0) w * 0.94f else w * 0.06f
                            val sy = h * (0.14f + (idx % 4) * 0.18f)
                            val ey = sy + h * 0.24f
                            val curX = sx + (ex - sx) * local
                            val curY = sy + (ey - sy) * local
                            drawLine(Color(0xFFEF4444), Offset(sx, sy), Offset(curX, curY), 7.dp.toPx(), StrokeCap.Round, alpha = masterAlpha)
                            drawLine(Color.White, Offset(sx, sy), Offset(curX, curY), 2.5.dp.toPx(), StrokeCap.Round, alpha = masterAlpha)
                        }
                    }
                }
            }
        }
    }
}

private fun DrawScope.drawPuzzleGlyph(
    center: Offset,
    pieceSize: Float,
    angleDeg: Float,
    fillColor: Color,
    edgeColor: Color
) {
    val half = pieceSize * 0.5f
    val tabR = pieceSize * 0.19f
    val px = center.x
    val py = center.y
    val p = Path().apply {
        moveTo(px - half, py - half)
        lineTo(px - tabR, py - half)
        cubicTo(px - tabR * 1.1f, py - half - tabR * 1.35f, px + tabR * 1.1f, py - half - tabR * 1.35f, px + tabR, py - half)
        lineTo(px + half, py - half)
        lineTo(px + half, py - tabR)
        cubicTo(px + half + tabR * 1.35f, py - tabR * 1.1f, px + half + tabR * 1.35f, py + tabR * 1.1f, px + half, py + tabR)
        lineTo(px + half, py + half)
        lineTo(px - half, py + half)
        lineTo(px - half, py + tabR)
        cubicTo(px - half + tabR * 1.1f, py + tabR * 0.9f, px - half + tabR * 1.1f, py - tabR * 0.9f, px - half, py - tabR)
        close()
    }
    rotate(angleDeg, pivot = center) {
        drawPath(p, color = fillColor)
        drawPath(p, color = edgeColor, style = Stroke(width = 1.6.dp.toPx(), join = StrokeJoin.Round))
    }
}

private fun DrawScope.drawBlueLockSoccerBall(
    center: Offset,
    radius: Float,
    spinDeg: Float,
    alpha: Float
) {
    val cx = center.x
    val cy = center.y

    // Blazing cyan/cobalt striker aura trail behind the ball
    val auraFlame = Path().apply {
        moveTo(cx - radius * 1.45f, cy - radius * 0.65f)
        quadraticTo(cx - radius * 0.4f, cy - radius * 1.95f, cx + radius * 1.65f, cy - radius * 1.35f)
        quadraticTo(cx + radius * 1.1f, cy, cx + radius * 1.45f, cy + radius * 0.95f)
        quadraticTo(cx, cy + radius * 1.45f, cx - radius * 1.45f, cy - radius * 0.65f)
        close()
    }
    drawPath(
        auraFlame,
        brush = Brush.radialGradient(
            colors = listOf(Color(0xFF22D3EE).copy(alpha = 0.80f), Color(0xFF1D4ED8).copy(alpha = 0.45f), Color.Transparent),
            center = center,
            radius = radius * 2.1f
        ),
        alpha = alpha
    )
    drawCircle(
        brush = Brush.radialGradient(
            colors = listOf(Color(0xFF38BDF8).copy(alpha = 0.78f), Color.Transparent),
            center = center,
            radius = radius * 1.75f
        ),
        radius = radius * 1.75f,
        center = center,
        alpha = alpha
    )

    // Soccer ball sphere & outer cyan-cobalt neon contour
    drawCircle(color = Color(0xFFF8FAFC), radius = radius, center = center, alpha = alpha)
    drawCircle(
        color = Color(0xFF0284C7),
        radius = radius,
        center = center,
        alpha = alpha,
        style = Stroke(width = (radius * 0.14f).coerceAtLeast(1.6f))
    )

    // Spinning central pentagon + 5 radial seam panels
    rotate(spinDeg, pivot = center) {
        val pentagon = Path().apply {
            val pr = radius * 0.42f
            for (i in 0..4) {
                val a = (i * 72.0 - 90.0) * PI / 180.0
                val px = cx + pr * cos(a).toFloat()
                val py = cy + pr * sin(a).toFloat()
                if (i == 0) moveTo(px, py) else lineTo(px, py)
            }
            close()
        }
        drawPath(pentagon, color = Color(0xFF0F172A), alpha = alpha)
        val seamW = (radius * 0.10f).coerceAtLeast(1.1f)
        for (i in 0..4) {
            val a = (i * 72.0 - 90.0) * PI / 180.0
            drawLine(
                color = Color(0xFF0F172A),
                start = Offset(cx + radius * 0.40f * cos(a).toFloat(), cy + radius * 0.40f * sin(a).toFloat()),
                end = Offset(cx + radius * 0.96f * cos(a).toFloat(), cy + radius * 0.96f * sin(a).toFloat()),
                strokeWidth = seamW,
                alpha = alpha
            )
        }
    }
}
