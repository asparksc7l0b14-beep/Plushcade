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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mirvo.tazlen.plushcade.game.data.GameRepo
import com.mirvo.tazlen.plushcade.game.model.Upgrade
import com.mirvo.tazlen.plushcade.game.ui.GameFonts
import com.mirvo.tazlen.plushcade.game.ui.OrnateButton
import com.mirvo.tazlen.plushcade.game.ui.Palette

@Composable
fun WorkshopScreen(
    repo: GameRepo,
    tokens: Int,
    backdrop: Int,
    onBuy: (Upgrade) -> Unit,
    onBack: () -> Unit,
) {
    BackHandler { onBack() }
    Box(Modifier.fillMaxSize()) {
        ArcadeBackdrop(backdrop, dim = 0.68f)
        Column(
            modifier = Modifier
                .fillMaxSize()
                .windowInsetsPadding(WindowInsets.safeDrawing)
                .padding(horizontal = 20.dp, vertical = 12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                OrnateButton("Back", onBack, Modifier.height(46.dp), fillColor = Palette.Cyan, fontSize = 15)
                ScreenTitle("Workshop", size = 26)
                IconChip("ic_token", "$tokens", tint = Palette.Gold)
            }
            Text(
                text = "Every point of prize value you bank becomes a token. Spend them on the machine and they stay bought.",
                color = Palette.Muted,
                fontFamily = GameFonts.hud,
                fontSize = 13.sp,
                modifier = Modifier.padding(bottom = 12.dp),
            )
            LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                items(Upgrade.entries.toList()) { upgrade ->
                    UpgradeCard(upgrade, repo, tokens, onBuy)
                }
                item { Spacer(Modifier.height(10.dp)) }
            }
        }
    }
}

@Composable
private fun UpgradeCard(upgrade: Upgrade, repo: GameRepo, tokens: Int, onBuy: (Upgrade) -> Unit) {
    val level = repo.level(upgrade)
    val maxed = level >= upgrade.steps
    val cost = if (maxed) 0 else upgrade.cost(level)
    val affordable = !maxed && tokens >= cost
    Box(
        Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(22.dp))
            .background(Palette.Ink.copy(alpha = 0.76f))
            .padding(16.dp)
    ) {
        Column {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Column(Modifier.weight(1f)) {
                    Text(
                        text = upgrade.label,
                        color = if (maxed) Palette.Gold else Palette.Cream,
                        fontFamily = GameFonts.primary,
                        fontWeight = FontWeight.Bold,
                        fontSize = 19.sp,
                    )
                    Text(
                        text = upgrade.detail,
                        color = Palette.Muted,
                        fontFamily = GameFonts.hud,
                        fontSize = 13.sp,
                    )
                }
                Text(
                    text = upgrade.readout(level),
                    color = Palette.Mint,
                    fontFamily = GameFonts.hud,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                )
            }
            Spacer(Modifier.height(10.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    repeat(upgrade.steps) { step ->
                        Box(
                            Modifier
                                .size(14.dp)
                                .clip(CircleShape)
                                .background(if (step < level) Palette.Gold else Palette.Muted.copy(alpha = 0.35f))
                        )
                    }
                }
                if (maxed) {
                    Text(
                        text = "Fully fitted",
                        color = Palette.Gold,
                        fontFamily = GameFonts.primary,
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp,
                    )
                } else {
                    OrnateButton(
                        text = "$cost",
                        onClick = { onBuy(upgrade) },
                        modifier = Modifier.width(112.dp).height(46.dp),
                        fillColor = if (affordable) Palette.Mint else Palette.Danger,
                        enabled = affordable,
                        fontSize = 16,
                    )
                }
            }
        }
    }
}
