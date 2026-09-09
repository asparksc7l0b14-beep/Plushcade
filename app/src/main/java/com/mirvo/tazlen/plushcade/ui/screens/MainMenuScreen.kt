package com.mirvo.tazlen.plushcade.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mirvo.tazlen.plushcade.game.ui.GameFonts
import com.mirvo.tazlen.plushcade.game.ui.OrnateButton
import com.mirvo.tazlen.plushcade.game.ui.Palette
import com.mirvo.tazlen.plushcade.game.ui.drawableId

private data class MenuTile(val asset: String, val label: String, val action: () -> Unit)

@Composable
fun MainMenuScreen(
    backdrop: Int,
    stars: Int,
    tokens: Int,
    onPlay: () -> Unit,
    onLevels: () -> Unit,
    onRush: () -> Unit,
    onWorkshop: () -> Unit,
    onAwards: () -> Unit,
    onStats: () -> Unit,
    onSettings: () -> Unit,
    onExit: () -> Unit,
) {
    val logo = drawableId("logo")
    val tiles = listOf(
        MenuTile("btn_play", "Play", onPlay),
        MenuTile("btn_levels", "Cabinets", onLevels),
        MenuTile("btn_rush", "Prize Rush", onRush),
        MenuTile("btn_shop", "Workshop", onWorkshop),
        MenuTile("btn_awards", "Awards", onAwards),
        MenuTile("btn_stats", "Shelf", onStats),
    )
    Box(Modifier.fillMaxSize()) {
        ArcadeBackdrop(backdrop, dim = 0.5f)
        Column(
            modifier = Modifier
                .fillMaxSize()
                .windowInsetsPadding(WindowInsets.safeDrawing)
                .padding(horizontal = 18.dp, vertical = 10.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    if (logo != 0) {
                        Image(
                            painter = painterResource(logo),
                            contentDescription = null,
                            modifier = Modifier.size(58.dp),
                        )
                    }
                    Column(Modifier.padding(start = 8.dp)) {
                        Text(
                            text = "Plushcade",
                            color = Palette.Cream,
                            fontFamily = GameFonts.primary,
                            fontWeight = FontWeight.Bold,
                            fontSize = 28.sp,
                        )
                        Text(
                            text = "Prize cabinet chain",
                            color = Palette.Rose,
                            fontFamily = GameFonts.hud,
                            fontSize = 12.sp,
                        )
                    }
                }
                Column(horizontalAlignment = Alignment.End) {
                    IconChip("ic_star", "$stars")
                    Spacer(Modifier.height(5.dp))
                    IconChip("ic_token", "$tokens", tint = Palette.Gold)
                }
            }
            Spacer(Modifier.height(12.dp))
            for (row in tiles.chunked(2)) {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(bottom = 10.dp),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    row.forEach { tile -> MenuSquare(tile, Modifier.weight(1f)) }
                    if (row.size == 1) Spacer(Modifier.weight(1f))
                }
            }
            Spacer(Modifier.weight(1f))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                OrnateButton(
                    text = "Settings",
                    onClick = onSettings,
                    modifier = Modifier.weight(1f).height(50.dp),
                    fillColor = Palette.Cyan,
                    fontSize = 16,
                )
                OrnateButton(
                    text = "Exit",
                    onClick = onExit,
                    modifier = Modifier.weight(1f).height(50.dp),
                    fillColor = Palette.Danger,
                    fontSize = 16,
                )
            }
        }
    }
}

@Composable
private fun MenuSquare(tile: MenuTile, modifier: Modifier) {
    val id = drawableId(tile.asset)
    Box(
        modifier = modifier
            .aspectRatio(1.16f)
            .clip(RoundedCornerShape(22.dp))
            .background(Palette.Deep)
            .clickable { tile.action() },
        contentAlignment = Alignment.BottomCenter,
    ) {
        if (id != 0) {
            Image(
                painter = painterResource(id),
                contentDescription = null,
                modifier = Modifier.matchParentSize(),
                contentScale = ContentScale.Crop,
            )
        }
        Box(
            Modifier
                .fillMaxWidth()
                .height(42.dp)
                .background(
                    Brush.verticalGradient(listOf(Color.Transparent, Palette.Ink.copy(alpha = 0.88f)))
                )
        )
        Text(
            text = tile.label,
            color = Palette.Cream,
            fontFamily = GameFonts.primary,
            fontWeight = FontWeight.Bold,
            fontSize = 16.sp,
            modifier = Modifier.padding(bottom = 7.dp),
        )
    }
}
