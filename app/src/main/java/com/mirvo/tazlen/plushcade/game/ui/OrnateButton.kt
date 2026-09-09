package com.mirvo.tazlen.plushcade.game.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

fun plateFor(fill: Color): String = when (fill) {
    Palette.Cyan -> "btn_plate_blue"
    Palette.Gold -> "btn_plate_gold"
    Palette.Danger -> "btn_plate_red"
    else -> "btn_plate_green"
}

@Composable
fun OrnateButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    fillColor: Color = Palette.Mint,
    textColor: Color? = null,
    enabled: Boolean = true,
    fontSize: Int = 19,
) {
    val context = LocalContext.current
    val plateId = remember(fillColor) {
        context.resources.getIdentifier(plateFor(fillColor), "drawable", context.packageName)
    }
    val alpha = if (enabled) 1f else 0.45f
    val label = (textColor ?: if (fillColor == Palette.Gold) Palette.Ink else Palette.Cream).copy(alpha = alpha)
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(20.dp))
            .clickable(enabled = enabled) { onClick() },
        contentAlignment = Alignment.Center,
    ) {
        if (plateId != 0) {
            Image(
                painter = painterResource(plateId),
                contentDescription = null,
                modifier = Modifier.matchParentSize(),
                contentScale = ContentScale.FillBounds,
                alpha = alpha,
            )
        } else {
            Box(Modifier.matchParentSize().background(fillColor.copy(alpha = alpha)))
        }
        Text(
            text = text,
            color = label,
            fontFamily = GameFonts.primary,
            fontWeight = FontWeight.Bold,
            fontSize = fontSize.sp,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(horizontal = 18.dp, vertical = 10.dp),
        )
    }
}
