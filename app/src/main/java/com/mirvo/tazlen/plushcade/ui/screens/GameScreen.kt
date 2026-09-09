package com.mirvo.tazlen.plushcade.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mirvo.tazlen.plushcade.game.levels.Cabinet
import com.mirvo.tazlen.plushcade.game.levels.Twist
import com.mirvo.tazlen.plushcade.game.logic.Cab
import com.mirvo.tazlen.plushcade.game.logic.ClawEngine
import com.mirvo.tazlen.plushcade.game.logic.ClawPhase
import com.mirvo.tazlen.plushcade.game.ui.GameFonts
import com.mirvo.tazlen.plushcade.game.ui.OrnateButton
import com.mirvo.tazlen.plushcade.game.ui.Palette
import com.mirvo.tazlen.plushcade.ui.app.Hud
import com.mirvo.tazlen.plushcade.ui.scene.CabinetScene

private const val FLOOR_TOP = 0.505f
private const val FLOOR_BOTTOM = 0.775f
private const val VIEW_HALF_X = 0.80f

@Composable
fun GameScreen(
    cabinet: Cabinet,
    engine: ClawEngine,
    hud: Hud,
    paused: Boolean,
    backdrop: Int,
    onStep: (Float) -> Unit,
    onAim: (Float, Float) -> Unit,
    onDrop: () -> Unit,
    onTighten: () -> Unit,
    onPause: () -> Unit,
    onQuit: () -> Unit,
) {
    var ready by remember { mutableStateOf(false) }
    var boxWidth by remember { mutableStateOf(1) }
    var boxHeight by remember { mutableStateOf(1) }

    BackHandler { onPause() }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Palette.Ink)
            .onSizeChanged {
                boxWidth = it.width.coerceAtLeast(1)
                boxHeight = it.height.coerceAtLeast(1)
            }
    ) {
        ArcadeBackdrop(backdrop, dim = 0.74f)
        CabinetScene(
            engine = engine,
            isPaused = { paused },
            onStep = { dt ->
                if (!ready) ready = true
                onStep(dt)
            },
            modifier = Modifier.fillMaxSize(),
        )
        Box(
            Modifier
                .fillMaxSize()
                .pointerInput(engine) {
                    detectDragGestures { change, _ ->
                        change.consume()
                        emitAim(change.position.x, change.position.y, boxWidth, boxHeight, onAim)
                    }
                }
                .pointerInput(engine) {
                    detectTapGestures { position ->
                        emitAim(position.x, position.y, boxWidth, boxHeight, onAim)
                    }
                }
        )
        Column(
            modifier = Modifier
                .fillMaxSize()
                .windowInsetsPadding(WindowInsets.safeDrawing)
        ) {
            HudBar(cabinet, hud, onPause)
            Spacer(Modifier.weight(1f))
            Controls(hud, onDrop, onTighten)
        }
        if (!ready) {
            Box(
                Modifier.fillMaxSize().background(Palette.Ink.copy(alpha = 0.94f)),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = "Loading cabinet",
                    color = Palette.Rose,
                    fontFamily = GameFonts.primary,
                    fontSize = 20.sp,
                )
            }
        }
        if (paused) {
            PauseCurtain(cabinet, onPause, onQuit)
        }
    }
}

private fun emitAim(px: Float, py: Float, width: Int, height: Int, onAim: (Float, Float) -> Unit) {
    val nx = (px / width).coerceIn(0f, 1f)
    val ny = (py / height).coerceIn(0f, 1f)
    val x = (nx - 0.5f) * 2f * VIEW_HALF_X
    val depth = ((ny - FLOOR_TOP) / (FLOOR_BOTTOM - FLOOR_TOP)).coerceIn(0f, 1f)
    val z = -Cab.HALF_Z + depth * 2f * Cab.HALF_Z
    onAim(x, z)
}

@Composable
private fun HudBar(cabinet: Cabinet, hud: Hud, onPause: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                Brush.verticalGradient(
                    listOf(Palette.Ink.copy(alpha = 0.92f), Color.Transparent)
                )
            )
            .padding(start = 16.dp, end = 16.dp, top = 6.dp, bottom = 22.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Column(Modifier.weight(1f)) {
                Text(
                    text = cabinet.title,
                    color = Palette.Cream,
                    fontFamily = GameFonts.primary,
                    fontWeight = FontWeight.Bold,
                    fontSize = 21.sp,
                )
                Text(
                    text = cabinet.goal.caption(),
                    color = Palette.Rose,
                    fontFamily = GameFonts.hud,
                    fontSize = 13.sp,
                )
            }
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                if (cabinet.rush) {
                    IconChip("ic_timer", "${hud.seconds}s")
                } else {
                    IconChip("ic_credit", "${hud.credits}")
                }
                OrnateButton("II", onPause, Modifier.height(42.dp), fillColor = Palette.Cyan, fontSize = 15)
            }
        }
        if (cabinet.twist != Twist.STILL) {
            Spacer(Modifier.height(5.dp))
            Text(
                text = cabinet.twist.label.uppercase() + " — " + cabinet.twist.detail,
                color = Palette.Gold,
                fontFamily = GameFonts.hud,
                fontWeight = FontWeight.Bold,
                fontSize = 12.sp,
            )
        }
        Spacer(Modifier.height(7.dp))
        val fraction = when {
            cabinet.rush -> if (cabinet.timeLimit <= 0f) 1f else (hud.seconds / cabinet.timeLimit).coerceIn(0f, 1f)
            hud.need <= 0 -> 1f
            else -> (hud.have.toFloat() / hud.need).coerceIn(0f, 1f)
        }
        Box(
            Modifier
                .fillMaxWidth()
                .height(20.dp)
                .clip(RoundedCornerShape(10.dp))
                .background(Palette.Ink.copy(alpha = 0.82f))
        ) {
            Box(
                Modifier
                    .fillMaxHeight()
                    .fillMaxWidth(fraction)
                    .background(
                        Brush.horizontalGradient(
                            if (cabinet.rush) listOf(Palette.Gold, Palette.Danger)
                            else listOf(Palette.Mint, Palette.Cyan)
                        )
                    )
            )
            Text(
                text = if (cabinet.rush) "${hud.points} banked" else "${hud.have} / ${hud.need}",
                color = Palette.Cream,
                fontFamily = GameFonts.hud,
                fontWeight = FontWeight.Bold,
                fontSize = 12.sp,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth().padding(top = 2.dp),
            )
        }
    }
}

@Composable
private fun Controls(hud: Hud, onDrop: () -> Unit, onTighten: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                Brush.verticalGradient(
                    listOf(Color.Transparent, Palette.Ink.copy(alpha = 0.94f))
                )
            )
            .padding(start = 18.dp, end = 18.dp, top = 30.dp, bottom = 10.dp)
    ) {
        GripMeter(hud)
        Spacer(Modifier.height(9.dp))
        if (hud.holding) {
            OrnateButton(
                text = "HOLD",
                onClick = onTighten,
                modifier = Modifier.fillMaxWidth().height(66.dp),
                fillColor = Palette.Gold,
                fontSize = 25,
            )
        } else {
            val canDrop = hud.phase == ClawPhase.AIMING && hud.credits > 0
            OrnateButton(
                text = if (canDrop) "DROP" else "…",
                onClick = onDrop,
                modifier = Modifier.fillMaxWidth().height(66.dp),
                fillColor = Palette.Mint,
                enabled = canDrop,
                fontSize = 25,
            )
        }
        Text(
            text = if (hud.holding) "Tap HOLD to tighten the grip before it slips" else "Drag inside the glass to slide the claw",
            color = Palette.Muted,
            fontFamily = GameFonts.hud,
            fontSize = 12.sp,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth().padding(top = 5.dp),
        )
    }
}

@Composable
private fun GripMeter(hud: Hud) {
    Canvas(Modifier.fillMaxWidth().height(16.dp)) {
        drawRoundRect(
            color = Palette.Ink.copy(alpha = 0.85f),
            size = size,
            cornerRadius = CornerRadius(size.height / 2f),
        )
        if (hud.holding) {
            val width = size.width * hud.grip.coerceIn(0f, 1f)
            val tint = when {
                hud.grip > 0.55f -> Palette.Mint
                hud.grip > 0.28f -> Palette.Gold
                else -> Palette.Danger
            }
            drawRoundRect(
                color = tint,
                topLeft = Offset(0f, 0f),
                size = Size(width, size.height),
                cornerRadius = CornerRadius(size.height / 2f),
            )
        }
    }
}

@Composable
private fun PauseCurtain(cabinet: Cabinet, onResume: () -> Unit, onQuit: () -> Unit) {
    Box(
        Modifier.fillMaxSize().background(Palette.Ink.copy(alpha = 0.90f)),
        contentAlignment = Alignment.Center,
    ) {
        Column(
            modifier = Modifier.padding(32.dp).fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            ScreenTitle("Paused", size = 32)
            Text(
                text = cabinet.title,
                color = Palette.Rose,
                fontFamily = GameFonts.hud,
                fontSize = 16.sp,
                modifier = Modifier.padding(bottom = 24.dp),
            )
            OrnateButton(
                text = "Resume",
                onClick = onResume,
                modifier = Modifier.fillMaxWidth().height(60.dp),
                fillColor = Palette.Mint,
            )
            Spacer(Modifier.height(12.dp))
            OrnateButton(
                text = "Leave Cabinet",
                onClick = onQuit,
                modifier = Modifier.fillMaxWidth().height(56.dp),
                fillColor = Palette.Danger,
            )
        }
    }
}
