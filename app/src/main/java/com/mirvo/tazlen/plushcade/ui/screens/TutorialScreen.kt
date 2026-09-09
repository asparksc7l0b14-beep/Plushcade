package com.mirvo.tazlen.plushcade.ui.screens

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mirvo.tazlen.plushcade.game.ui.GameFonts
import com.mirvo.tazlen.plushcade.game.ui.OrnateButton
import com.mirvo.tazlen.plushcade.game.ui.Palette
import kotlinx.coroutines.delay

private val tips = listOf(
    "Drag inside the cabinet to slide the claw, then press DROP.",
    "While the claw carries a prize its grip fades — tap HOLD to tighten it.",
    "A prize only counts once it falls through the chute in the corner.",
)

@Composable
fun TutorialScreen(onBegin: () -> Unit) {
    var tip by remember { mutableIntStateOf(0) }
    LaunchedEffect(Unit) {
        while (true) {
            delay(3200)
            tip = (tip + 1) % tips.size
        }
    }
    val motion = rememberInfiniteTransition(label = "demo")
    val cycle by motion.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(5200, easing = LinearEasing), RepeatMode.Restart),
        label = "cycle",
    )

    Box(Modifier.fillMaxSize()) {
        ArcadeBackdrop(0, dim = 0.62f)
        Column(
            modifier = Modifier
                .fillMaxSize()
                .windowInsetsPadding(WindowInsets.safeDrawing)
                .padding(horizontal = 24.dp, vertical = 18.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            ScreenTitle("How A Cabinet Works", size = 27)
            Spacer(Modifier.height(6.dp))
            Text(
                text = "Watch one full grab",
                color = Palette.Rose,
                fontFamily = GameFonts.hud,
                fontSize = 15.sp,
            )
            Spacer(Modifier.weight(1f))
            Box(
                Modifier
                    .fillMaxWidth()
                    .height(320.dp)
                    .clip(RoundedCornerShape(26.dp))
                    .background(Palette.Deep.copy(alpha = 0.82f))
            ) {
                DemoCanvas(cycle)
            }
            Spacer(Modifier.height(20.dp))
            Box(
                Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(20.dp))
                    .background(Palette.Ink.copy(alpha = 0.72f))
                    .padding(horizontal = 18.dp, vertical = 16.dp)
            ) {
                Text(
                    text = tips[tip],
                    color = Palette.Cream,
                    fontFamily = GameFonts.hud,
                    fontSize = 16.sp,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth(),
                )
            }
            Spacer(Modifier.weight(1f))
            OrnateButton(
                text = "Begin!",
                onClick = onBegin,
                modifier = Modifier.fillMaxWidth().height(62.dp),
                fillColor = Palette.Gold,
                fontSize = 22,
            )
        }
    }
}

@Composable
private fun DemoCanvas(progress: Float) {
    Canvas(Modifier.fillMaxSize()) {
        val w = size.width
        val h = size.height
        val floorY = h * 0.78f
        val chuteX = w * 0.20f

        drawRoundRect(
            color = Palette.Ink.copy(alpha = 0.55f),
            topLeft = Offset(w * 0.06f, h * 0.10f),
            size = Size(w * 0.88f, h * 0.78f),
            cornerRadius = CornerRadius(18f),
        )
        drawRoundRect(
            color = Color(0xFF6B3A74),
            topLeft = Offset(w * 0.06f, floorY),
            size = Size(w * 0.88f, h * 0.10f),
            cornerRadius = CornerRadius(10f),
        )
        drawRoundRect(
            color = Palette.Cyan.copy(alpha = 0.75f),
            topLeft = Offset(chuteX - w * 0.09f, floorY - 4f),
            size = Size(w * 0.18f, h * 0.05f),
            cornerRadius = CornerRadius(12f),
        )

        val restingToys = listOf(
            Triple(w * 0.52f, floorY - 18f, Palette.Rose),
            Triple(w * 0.66f, floorY - 16f, Palette.Mint),
            Triple(w * 0.78f, floorY - 20f, Palette.Gold),
            Triple(w * 0.60f, floorY - 46f, Palette.Cyan),
        )
        restingToys.forEach { (x, y, tint) ->
            drawCircle(color = tint, radius = 20f, center = Offset(x, y))
            drawCircle(color = Color.White.copy(alpha = 0.35f), radius = 6f, center = Offset(x - 6f, y - 7f))
        }

        val targetX = w * 0.60f
        val phase = progress
        val clawX: Float
        val clawY: Float
        val open: Float
        val carrying: Boolean
        when {
            phase < 0.20f -> {
                val t = phase / 0.20f
                clawX = w * 0.30f + (targetX - w * 0.30f) * t
                clawY = h * 0.20f
                open = 1f
                carrying = false
            }
            phase < 0.38f -> {
                val t = (phase - 0.20f) / 0.18f
                clawX = targetX
                clawY = h * 0.20f + (floorY - 78f - h * 0.20f) * t
                open = 1f
                carrying = false
            }
            phase < 0.48f -> {
                clawX = targetX
                clawY = floorY - 78f
                open = 1f - (phase - 0.38f) / 0.10f
                carrying = false
            }
            phase < 0.66f -> {
                val t = (phase - 0.48f) / 0.18f
                clawX = targetX
                clawY = floorY - 78f + (h * 0.20f - (floorY - 78f)) * t
                open = 0f
                carrying = true
            }
            phase < 0.86f -> {
                val t = (phase - 0.66f) / 0.20f
                clawX = targetX + (chuteX - targetX) * t
                clawY = h * 0.20f
                open = 0f
                carrying = true
            }
            else -> {
                val t = (phase - 0.86f) / 0.14f
                clawX = chuteX
                clawY = h * 0.20f
                open = t
                carrying = t < 0.4f
            }
        }

        drawLine(
            color = Palette.Muted,
            start = Offset(clawX, h * 0.10f),
            end = Offset(clawX, clawY),
            strokeWidth = 5f,
        )
        val spread = 16f + open * 20f
        listOf(-spread, 0f, spread).forEach { dx ->
            drawLine(
                color = Color(0xFFD8DCE6),
                start = Offset(clawX, clawY),
                end = Offset(clawX + dx, clawY + 34f),
                strokeWidth = 7f,
            )
        }
        drawCircle(color = Color(0xFFAEB6C4), radius = 11f, center = Offset(clawX, clawY))

        if (carrying) {
            drawCircle(color = Palette.Cyan, radius = 20f, center = Offset(clawX, clawY + 42f))
        } else if (phase > 0.90f) {
            val fall = (phase - 0.90f) / 0.10f
            drawCircle(
                color = Palette.Cyan.copy(alpha = 1f - fall),
                radius = 20f,
                center = Offset(chuteX, clawY + 42f + fall * (floorY - clawY - 30f)),
            )
        }

        drawRoundRect(
            color = Palette.Pink.copy(alpha = 0.55f),
            topLeft = Offset(w * 0.06f, h * 0.10f),
            size = Size(w * 0.88f, h * 0.78f),
            cornerRadius = CornerRadius(18f),
            style = androidx.compose.ui.graphics.drawscope.Stroke(width = 6f),
        )
    }
}
