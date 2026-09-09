package com.mirvo.tazlen.plushcade.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mirvo.tazlen.plushcade.game.data.Settings
import com.mirvo.tazlen.plushcade.game.ui.Backdrop
import com.mirvo.tazlen.plushcade.game.ui.GameFonts
import com.mirvo.tazlen.plushcade.game.ui.OrnateButton
import com.mirvo.tazlen.plushcade.game.ui.Palette
import com.mirvo.tazlen.plushcade.game.ui.drawableId

@Composable
fun SettingsScreen(settings: Settings, onChange: (Settings) -> Unit, onBack: () -> Unit) {
    BackHandler { onBack() }
    Box(Modifier.fillMaxSize()) {
        ArcadeBackdrop(settings.background, dim = 0.66f)
        Column(
            modifier = Modifier
                .fillMaxSize()
                .windowInsetsPadding(WindowInsets.safeDrawing)
                .padding(horizontal = 20.dp, vertical = 12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                OrnateButton("Back", onBack, Modifier.height(46.dp), fillColor = Palette.Cyan, fontSize = 15)
                ScreenTitle("Settings", size = 26)
                Spacer(Modifier.width(72.dp))
            }
            Text(
                text = "Arcade look",
                color = Palette.Gold,
                fontFamily = GameFonts.primary,
                fontWeight = FontWeight.Bold,
                fontSize = 19.sp,
                modifier = Modifier.padding(bottom = 8.dp),
            )
            LazyRow(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                items(Backdrop.entries.toList()) { entry ->
                    val index = Backdrop.entries.indexOf(entry)
                    val id = drawableId(entry.asset)
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Box(
                            Modifier
                                .width(100.dp)
                                .height(150.dp)
                                .clip(RoundedCornerShape(18.dp))
                                .border(
                                    width = if (index == settings.background) 3.dp else 0.dp,
                                    color = if (index == settings.background) Palette.Pink else Palette.Ink,
                                    shape = RoundedCornerShape(18.dp),
                                )
                                .clickable { onChange(settings.copy(background = index)) }
                        ) {
                            if (id != 0) {
                                Image(
                                    painter = painterResource(id),
                                    contentDescription = null,
                                    modifier = Modifier.matchParentSize(),
                                    contentScale = ContentScale.Crop,
                                )
                            }
                        }
                        Text(
                            text = entry.label,
                            color = if (index == settings.background) Palette.Cream else Palette.Muted,
                            fontFamily = GameFonts.hud,
                            fontSize = 12.sp,
                            modifier = Modifier.padding(top = 5.dp),
                        )
                    }
                }
            }
            Spacer(Modifier.height(22.dp))
            SettingRow("Sound effects", settings.sound) { onChange(settings.copy(sound = it)) }
            Spacer(Modifier.height(12.dp))
            SettingRow("Vibration", settings.vibration) { onChange(settings.copy(vibration = it)) }
            Spacer(Modifier.weight(1f))
            Box(
                Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(20.dp))
                    .background(Palette.Ink.copy(alpha = 0.66f))
                    .padding(16.dp)
            ) {
                Text(
                    text = "Every cabinet has its own grip strength. Weak machines drain your hold fast — tap HOLD to fight it.",
                    color = Palette.Muted,
                    fontFamily = GameFonts.hud,
                    fontSize = 13.sp,
                )
            }
        }
    }
}

@Composable
private fun SettingRow(label: String, checked: Boolean, onChange: (Boolean) -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(Palette.Ink.copy(alpha = 0.74f))
            .padding(horizontal = 18.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Text(
            text = label,
            color = Palette.Cream,
            fontFamily = GameFonts.primary,
            fontWeight = FontWeight.Bold,
            fontSize = 18.sp,
        )
        PlushToggle(checked, onChange)
    }
}
