package com.mirvo.tazlen.plushcade.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mirvo.tazlen.plushcade.game.data.GameRepo
import com.mirvo.tazlen.plushcade.game.logic.Awards
import com.mirvo.tazlen.plushcade.game.ui.GameFonts
import com.mirvo.tazlen.plushcade.game.ui.OrnateButton
import com.mirvo.tazlen.plushcade.game.ui.Palette
import com.mirvo.tazlen.plushcade.game.ui.drawableId

@Composable
fun AwardsScreen(repo: GameRepo, backdrop: Int, onBack: () -> Unit) {
    BackHandler { onBack() }
    val medal = drawableId("ic_medal")
    val unlockedCount = Awards.all.count { repo.awardUnlocked(it.id) }
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
                ScreenTitle("Awards", size = 26)
                IconChip("ic_medal", "$unlockedCount/${Awards.all.size}")
            }
            LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                items(Awards.all) { award ->
                    val open = repo.awardUnlocked(award.id)
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(20.dp))
                            .background(Palette.Ink.copy(alpha = if (open) 0.78f else 0.55f))
                            .padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        if (medal != 0) {
                            Image(
                                painter = painterResource(medal),
                                contentDescription = null,
                                modifier = Modifier.size(44.dp),
                                alpha = if (open) 1f else 0.22f,
                            )
                        }
                        Column(Modifier.padding(start = 12.dp)) {
                            Text(
                                text = award.title,
                                color = if (open) Palette.Gold else Palette.Muted,
                                fontFamily = GameFonts.primary,
                                fontWeight = FontWeight.Bold,
                                fontSize = 17.sp,
                            )
                            Text(
                                text = award.detail,
                                color = if (open) Palette.Cream else Palette.Muted,
                                fontFamily = GameFonts.hud,
                                fontSize = 13.sp,
                            )
                        }
                    }
                }
            }
        }
    }
}
