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
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mirvo.tazlen.plushcade.game.data.GameRepo
import com.mirvo.tazlen.plushcade.game.levels.Arcades
import com.mirvo.tazlen.plushcade.game.levels.Cabinet
import com.mirvo.tazlen.plushcade.game.levels.Cabinets
import com.mirvo.tazlen.plushcade.game.levels.Twist
import com.mirvo.tazlen.plushcade.game.ui.GameFonts
import com.mirvo.tazlen.plushcade.game.ui.OrnateButton
import com.mirvo.tazlen.plushcade.game.ui.Palette
import com.mirvo.tazlen.plushcade.game.ui.drawableId

@Composable
fun LevelSelectScreen(repo: GameRepo, onOpen: (Int) -> Unit, onBack: () -> Unit) {
    val startPage = (repo.highestOpen() / Arcades.PER_ARCADE).coerceIn(0, Arcades.names.lastIndex)
    val pager = rememberPagerState(initialPage = startPage) { Arcades.names.size }
    Box(Modifier.fillMaxSize()) {
        ArcadeBackdrop(pager.currentPage, dim = 0.62f)
        Column(
            modifier = Modifier
                .fillMaxSize()
                .windowInsetsPadding(WindowInsets.safeDrawing)
                .padding(horizontal = 18.dp, vertical = 12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                OrnateButton("Back", onBack, Modifier.height(46.dp), fillColor = Palette.Cyan, fontSize = 15)
                ScreenTitle("Cabinets", size = 26)
                IconChip("ic_star", "${repo.totalStars()}")
            }
            Spacer(Modifier.height(10.dp))
            HorizontalPager(state = pager, modifier = Modifier.weight(1f)) { page ->
                ArcadePage(page, repo, onOpen)
            }
            Row(
                modifier = Modifier.fillMaxWidth().padding(top = 10.dp),
                horizontalArrangement = Arrangement.Center,
            ) {
                repeat(Arcades.names.size) { index ->
                    Box(
                        Modifier
                            .padding(horizontal = 5.dp)
                            .size(if (index == pager.currentPage) 12.dp else 8.dp)
                            .clip(CircleShape)
                            .background(if (index == pager.currentPage) Palette.Pink else Palette.Muted.copy(alpha = 0.45f))
                    )
                }
            }
        }
    }
}

@Composable
private fun ArcadePage(page: Int, repo: GameRepo, onOpen: (Int) -> Unit) {
    val cabinets = Cabinets.byArcade(page)
    val earned = cabinets.sumOf { repo.starsFor(it.index) }
    Column(Modifier.fillMaxSize()) {
        Box(
            Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(22.dp))
                .background(Palette.Ink.copy(alpha = 0.72f))
                .padding(horizontal = 18.dp, vertical = 14.dp)
        ) {
            Column {
                Text(
                    text = Arcades.names[page],
                    color = Palette.Cream,
                    fontFamily = GameFonts.primary,
                    fontWeight = FontWeight.Bold,
                    fontSize = 23.sp,
                )
                Text(
                    text = "$earned of ${cabinets.size * 3} stars collected",
                    color = Palette.Rose,
                    fontFamily = GameFonts.hud,
                    fontSize = 14.sp,
                )
            }
        }
        Spacer(Modifier.height(14.dp))
        for (row in cabinets.chunked(4)) {
            Row(
                modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                row.forEach { cabinet ->
                    CabinetNode(cabinet, repo, Modifier.weight(1f), onOpen)
                }
                repeat(4 - row.size) { Spacer(Modifier.weight(1f)) }
            }
        }
        Spacer(Modifier.height(6.dp))
        val next = cabinets.firstOrNull { repo.unlocked(it.index) && repo.starsFor(it.index) == 0 }
        if (next != null) {
            Box(
                Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(18.dp))
                    .background(Palette.Deep.copy(alpha = 0.78f))
                    .padding(horizontal = 16.dp, vertical = 12.dp)
            ) {
                Column {
                    Text(
                        text = "Next up: ${next.title}",
                        color = Palette.Gold,
                        fontFamily = GameFonts.primary,
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                    )
                    Text(
                        text = next.goal.caption() + " with ${next.credits} credits",
                        color = Palette.Cream,
                        fontFamily = GameFonts.hud,
                        fontSize = 14.sp,
                    )
                    if (next.twist != Twist.STILL) {
                        Text(
                            text = next.twist.label + " — " + next.twist.detail,
                            color = Palette.Cyan,
                            fontFamily = GameFonts.hud,
                            fontSize = 13.sp,
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun CabinetNode(cabinet: Cabinet, repo: GameRepo, modifier: Modifier, onOpen: (Int) -> Unit) {
    val open = repo.unlocked(cabinet.index)
    val stars = repo.starsFor(cabinet.index)
    val node = drawableId("btn_node")
    val lock = drawableId("ic_lock")
    Column(modifier, horizontalAlignment = Alignment.CenterHorizontally) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(1f)
                .clip(CircleShape)
                .clickable(enabled = open) { onOpen(cabinet.index) },
            contentAlignment = Alignment.Center,
        ) {
            if (node != 0) {
                Image(
                    painter = painterResource(node),
                    contentDescription = null,
                    modifier = Modifier.matchParentSize(),
                    contentScale = ContentScale.Fit,
                    alpha = if (open) 1f else 0.35f,
                )
            }
            if (open) {
                Text(
                    text = "${cabinet.index % Arcades.PER_ARCADE + 1}",
                    color = Palette.Cream,
                    fontFamily = GameFonts.primary,
                    fontWeight = FontWeight.Bold,
                    fontSize = 20.sp,
                )
            } else if (lock != 0) {
                Image(
                    painter = painterResource(lock),
                    contentDescription = null,
                    modifier = Modifier.size(22.dp),
                )
            }
        }
        StarStrip(stars, star = 11, modifier = Modifier.padding(top = 3.dp))
        Text(
            text = cabinet.title,
            color = if (open) Palette.Cream else Palette.Muted,
            fontFamily = GameFonts.hud,
            fontSize = 10.sp,
            textAlign = TextAlign.Center,
            maxLines = 2,
            modifier = Modifier.fillMaxWidth(),
        )
    }
}
