package com.mirvo.tazlen.plushcade.ui.screens

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mirvo.tazlen.plushcade.game.ui.GameFonts
import com.mirvo.tazlen.plushcade.game.ui.Palette
import com.mirvo.tazlen.plushcade.game.ui.drawableId
import kotlinx.coroutines.delay

@Composable
fun SplashScreen(onDone: () -> Unit) {
    var shown by remember { mutableStateOf(false) }
    val fade by animateFloatAsState(if (shown) 1f else 0f, label = "splash")
    LaunchedEffect(Unit) {
        shown = true
        delay(1500)
        onDone()
    }
    val backdrop = drawableId("bg_splash")
    val logo = drawableId("logo")
    Box(Modifier.fillMaxSize().background(Palette.Ink)) {
        if (backdrop != 0) {
            Image(
                painter = painterResource(backdrop),
                contentDescription = null,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop,
            )
        }
        Box(
            Modifier.fillMaxSize().background(
                Brush.verticalGradient(
                    listOf(Palette.Ink.copy(alpha = 0.30f), Palette.Ink.copy(alpha = 0.82f))
                )
            )
        )
        Column(
            modifier = Modifier.fillMaxSize().padding(32.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            if (logo != 0) {
                Image(
                    painter = painterResource(logo),
                    contentDescription = null,
                    modifier = Modifier.size(190.dp),
                    alpha = fade,
                )
            }
            Text(
                text = "PLUSHCADE",
                color = Palette.Cream.copy(alpha = fade),
                fontFamily = GameFonts.primary,
                fontWeight = FontWeight.Bold,
                fontSize = 42.sp,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth(),
            )
            Text(
                text = "Grab the prize, keep your grip",
                color = Palette.Rose.copy(alpha = fade),
                fontFamily = GameFonts.hud,
                fontSize = 16.sp,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth().padding(top = 6.dp),
            )
        }
    }
}
