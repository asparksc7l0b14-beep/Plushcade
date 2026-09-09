package com.mirvo.tazlen.plushcade.game.ui

import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext

enum class Backdrop(val asset: String, val label: String) {
    NEON("bg_menu_neon", "Neon Alley"),
    CANDY("bg_menu_candy", "Candy Court"),
    RETRO("bg_menu_retro", "Boardwalk"),
    MALL("bg_menu_mall", "Skylight Mall"),
    FROST("bg_menu_frost", "Frost Fair"),
    VAULT("bg_menu_vault", "The Vault"),
}

@Composable
fun drawableId(name: String): Int {
    val context = LocalContext.current
    return context.resources.getIdentifier(name, "drawable", context.packageName)
}

fun backdropFor(index: Int): Backdrop = Backdrop.entries[index.coerceIn(0, Backdrop.entries.lastIndex)]
