package com.mirvo.tazlen.plushcade.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mirvo.tazlen.plushcade.game.levels.Cabinet
import com.mirvo.tazlen.plushcade.game.levels.Cabinets
import com.mirvo.tazlen.plushcade.game.ui.GameFonts
import com.mirvo.tazlen.plushcade.game.ui.OrnateButton
import com.mirvo.tazlen.plushcade.game.ui.Palette
import com.mirvo.tazlen.plushcade.ui.app.Outcome

@Composable
fun ResultsScreen(
    cabinet: Cabinet,
    outcome: Outcome,
    backdrop: Int,
    onRetry: () -> Unit,
    onNext: () -> Unit,
    onMenu: () -> Unit,
) {
    BackHandler { onMenu() }
    Box(Modifier.fillMaxSize()) {
        ArcadeBackdrop(backdrop, dim = 0.68f)
        Column(
            modifier = Modifier
                .fillMaxSize()
                .windowInsetsPadding(WindowInsets.safeDrawing)
                .padding(horizontal = 26.dp, vertical = 20.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Spacer(Modifier.weight(1f))
            ScreenTitle(
                when {
                    outcome.rush -> "Time!"
                    outcome.won -> "Cabinet Cleared"
                    else -> "Out Of Credits"
                },
                size = 32,
            )
            Text(
                text = cabinet.title,
                color = Palette.Rose,
                fontFamily = GameFonts.hud,
                fontSize = 16.sp,
                modifier = Modifier.padding(top = 4.dp, bottom = 16.dp),
            )
            if (outcome.rush) {
                Text(
                    text = if (outcome.record) "New personal record" else "Run finished",
                    color = if (outcome.record) Palette.Gold else Palette.Muted,
                    fontFamily = GameFonts.primary,
                    fontWeight = FontWeight.Bold,
                    fontSize = 19.sp,
                )
            } else {
                StarStrip(outcome.stars, star = 42)
            }
            Spacer(Modifier.height(20.dp))
            Box(
                Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(22.dp))
                    .background(Palette.Ink.copy(alpha = 0.74f))
                    .padding(18.dp)
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    ResultRow("Prizes secured", "${outcome.prizes}")
                    ResultRow("Prize value", "${outcome.points}")
                    if (!outcome.rush) ResultRow("Credits left", "${outcome.creditsLeft}")
                    ResultRow("Tokens earned", "${outcome.tokens}")
                }
            }
            if (outcome.awards.isNotEmpty()) {
                Spacer(Modifier.height(14.dp))
                Text(
                    text = "New award: " + outcome.awards.joinToString { it.title },
                    color = Palette.Gold,
                    fontFamily = GameFonts.primary,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth(),
                )
            }
            Spacer(Modifier.weight(1f))
            if (outcome.won && !outcome.rush && cabinet.index + 1 < Cabinets.count) {
                OrnateButton(
                    text = "Next Cabinet",
                    onClick = onNext,
                    modifier = Modifier.fillMaxWidth().height(60.dp),
                    fillColor = Palette.Gold,
                    fontSize = 21,
                )
                Spacer(Modifier.height(10.dp))
            }
            OrnateButton(
                text = if (outcome.rush) "Run It Again" else if (outcome.won) "Play Again" else "Try Again",
                onClick = onRetry,
                modifier = Modifier.fillMaxWidth().height(56.dp),
                fillColor = Palette.Mint,
            )
            Spacer(Modifier.height(10.dp))
            OrnateButton(
                text = "Cabinets",
                onClick = onMenu,
                modifier = Modifier.fillMaxWidth().height(52.dp),
                fillColor = Palette.Cyan,
                fontSize = 17,
            )
        }
    }
}

@Composable
private fun ResultRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Text(label, color = Palette.Muted, fontFamily = GameFonts.hud, fontSize = 15.sp)
        Text(
            text = value,
            color = Palette.Cream,
            fontFamily = GameFonts.hud,
            fontWeight = FontWeight.Bold,
            fontSize = 15.sp,
        )
    }
}
