package com.mirvo.tazlen.plushcade.ui.screens

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mirvo.tazlen.plushcade.game.ui.GameFonts
import com.mirvo.tazlen.plushcade.game.ui.Palette
import com.mirvo.tazlen.plushcade.game.ui.backdropFor
import com.mirvo.tazlen.plushcade.game.ui.drawableId

@Composable
fun ArcadeBackdrop(index: Int, dim: Float = 0.55f) {
    val id = drawableId(backdropFor(index).asset)
    Box(Modifier.fillMaxSize().background(Palette.Ink)) {
        if (id != 0) {
            Image(
                painter = painterResource(id),
                contentDescription = null,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop,
            )
        }
        Box(
            Modifier.fillMaxSize().background(
                Brush.verticalGradient(
                    listOf(
                        Palette.Ink.copy(alpha = dim + 0.18f),
                        Palette.Deep.copy(alpha = dim),
                        Palette.Ink.copy(alpha = dim + 0.26f),
                    )
                )
            )
        )
    }
}

@Composable
fun ScreenTitle(text: String, modifier: Modifier = Modifier, size: Int = 30) {
    Text(
        text = text,
        color = Palette.Cream,
        fontFamily = GameFonts.primary,
        fontWeight = FontWeight.Bold,
        fontSize = size.sp,
        modifier = modifier,
    )
}

@Composable
fun StarStrip(earned: Int, total: Int = 3, star: Int = 18, modifier: Modifier = Modifier) {
    val id = drawableId("ic_star")
    Row(modifier, horizontalArrangement = Arrangement.spacedBy(2.dp)) {
        repeat(total) { index ->
            if (id != 0) {
                Image(
                    painter = painterResource(id),
                    contentDescription = null,
                    modifier = Modifier.size(star.dp),
                    alpha = if (index < earned) 1f else 0.22f,
                )
            }
        }
    }
}

@Composable
fun IconChip(asset: String, label: String, modifier: Modifier = Modifier, tint: Color = Palette.Cream) {
    val id = drawableId(asset)
    Row(
        modifier = modifier
            .clip(RoundedCornerShape(16.dp))
            .background(Palette.Ink.copy(alpha = 0.62f))
            .padding(horizontal = 12.dp, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        if (id != 0) {
            Image(painter = painterResource(id), contentDescription = null, modifier = Modifier.size(20.dp))
        }
        Text(
            text = label,
            color = tint,
            fontFamily = GameFonts.hud,
            fontWeight = FontWeight.Bold,
            fontSize = 15.sp,
        )
    }
}

@Composable
fun PlushToggle(checked: Boolean, onChange: (Boolean) -> Unit, modifier: Modifier = Modifier) {
    val slide by animateFloatAsState(if (checked) 1f else 0f, label = "toggle")
    Canvas(
        modifier = modifier
            .width(64.dp)
            .height(34.dp)
            .clip(RoundedCornerShape(17.dp))
            .clickable { onChange(!checked) }
    ) {
        val track = if (checked) Palette.Mint else Palette.Muted.copy(alpha = 0.45f)
        drawRoundRect(
            color = track,
            size = size,
            cornerRadius = androidx.compose.ui.geometry.CornerRadius(size.height / 2f),
        )
        drawRoundRect(
            color = Palette.Ink.copy(alpha = 0.30f),
            topLeft = Offset(0f, size.height * 0.58f),
            size = Size(size.width, size.height * 0.42f),
            cornerRadius = androidx.compose.ui.geometry.CornerRadius(size.height / 2.6f),
        )
        val knobRadius = size.height * 0.38f
        val travel = size.width - knobRadius * 2f - size.height * 0.16f
        val cx = knobRadius + size.height * 0.08f + travel * slide
        drawCircle(color = Palette.Cream, radius = knobRadius, center = Offset(cx, size.height / 2f))
        drawCircle(
            color = if (checked) Palette.Pink else Palette.Muted,
            radius = knobRadius * 0.42f,
            center = Offset(cx, size.height / 2f),
        )
    }
}

@Composable
fun Slat(content: @Composable () -> Unit) {
    Box(
        Modifier
            .clip(RoundedCornerShape(22.dp))
            .background(Palette.Ink.copy(alpha = 0.70f))
            .padding(horizontal = 16.dp, vertical = 14.dp)
    ) { content() }
}
