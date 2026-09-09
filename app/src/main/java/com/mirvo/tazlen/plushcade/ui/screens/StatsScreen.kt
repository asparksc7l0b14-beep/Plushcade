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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import com.mirvo.tazlen.plushcade.game.levels.Cabinets
import com.mirvo.tazlen.plushcade.game.model.PrizeKind
import com.mirvo.tazlen.plushcade.game.ui.GameFonts
import com.mirvo.tazlen.plushcade.game.ui.OrnateButton
import com.mirvo.tazlen.plushcade.game.ui.Palette

@Composable
fun StatsScreen(repo: GameRepo, backdrop: Int, onBack: () -> Unit) {
    BackHandler { onBack() }
    val stats = repo.stats()
    Box(Modifier.fillMaxSize()) {
        ArcadeBackdrop(backdrop, dim = 0.68f)
        Column(
            modifier = Modifier
                .fillMaxSize()
                .windowInsetsPadding(WindowInsets.safeDrawing)
                .padding(horizontal = 20.dp, vertical = 12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth().padding(bottom = 14.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                OrnateButton("Back", onBack, Modifier.height(46.dp), fillColor = Palette.Cyan, fontSize = 15)
                ScreenTitle("Shelf", size = 26)
                IconChip("ic_star", "${stats.stars}")
            }
            LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                item {
                    Panel {
                        Column(verticalArrangement = Arrangement.spacedBy(7.dp)) {
                            Line("Cabinets cleared", "${stats.cleared} of ${Cabinets.count}")
                            Line("Stars earned", "${stats.stars} of ${Cabinets.count * 3}")
                            Line("Runs played", "${stats.runs}")
                            Line("Credits spent", "${stats.credits}")
                            Line("Prizes secured", "${stats.prizes}")
                            Line("Grips lost", "${stats.slips}")
                            Line("Prize Rush record", "${stats.rushBest}")
                            Line("Tokens earned", "${stats.earned}")
                            Line("Tokens in hand", "${stats.tokens}")
                        }
                    }
                }
                item {
                    Text(
                        text = "Collection",
                        color = Palette.Gold,
                        fontFamily = GameFonts.primary,
                        fontWeight = FontWeight.Bold,
                        fontSize = 19.sp,
                        modifier = Modifier.padding(top = 6.dp, bottom = 2.dp),
                    )
                }
                items(PrizeKind.entries.toList()) { kind ->
                    val count = repo.shelfCount(kind)
                    Panel {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween,
                        ) {
                            Column {
                                Text(
                                    text = kind.label,
                                    color = if (count > 0) Palette.Cream else Palette.Muted,
                                    fontFamily = GameFonts.primary,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 17.sp,
                                )
                                Text(
                                    text = if (count > 0) "worth ${kind.value} each" else "not on the shelf yet",
                                    color = Palette.Muted,
                                    fontFamily = GameFonts.hud,
                                    fontSize = 13.sp,
                                )
                            }
                            Text(
                                text = "x$count",
                                color = if (count > 0) Palette.Mint else Palette.Muted,
                                fontFamily = GameFonts.hud,
                                fontWeight = FontWeight.Bold,
                                fontSize = 19.sp,
                            )
                        }
                    }
                }
                item { Spacer(Modifier.height(12.dp)) }
            }
        }
    }
}

@Composable
private fun Panel(content: @Composable () -> Unit) {
    Box(
        Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(Palette.Ink.copy(alpha = 0.74f))
            .padding(horizontal = 16.dp, vertical = 13.dp)
    ) { content() }
}

@Composable
private fun Line(label: String, value: String) {
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
