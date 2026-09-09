package com.mirvo.tazlen.plushcade.ui.app

import com.mirvo.tazlen.plushcade.game.data.Settings
import com.mirvo.tazlen.plushcade.game.levels.Cabinet
import com.mirvo.tazlen.plushcade.game.logic.Award
import com.mirvo.tazlen.plushcade.game.logic.ClawEngine
import com.mirvo.tazlen.plushcade.game.logic.ClawPhase

sealed interface Screen {
    data object Splash : Screen
    data object Tutorial : Screen
    data object MainMenu : Screen
    data object LevelSelect : Screen
    data class Game(val index: Int) : Screen
    data object Results : Screen
    data object Stats : Screen
    data object Awards : Screen
    data object Workshop : Screen
    data object Settings : Screen
}

data class Hud(
    val credits: Int = 0,
    val banked: Int = 0,
    val points: Int = 0,
    val have: Int = 0,
    val need: Int = 1,
    val phase: ClawPhase = ClawPhase.AIMING,
    val grip: Float = 1f,
    val holding: Boolean = false,
    val seconds: Int = 0,
)

data class Outcome(
    val won: Boolean,
    val stars: Int,
    val prizes: Int,
    val points: Int,
    val creditsLeft: Int,
    val tokens: Int,
    val rush: Boolean,
    val record: Boolean,
    val awards: List<Award>,
)

data class Session(
    val cabinet: Cabinet,
    val engine: ClawEngine,
    val hud: Hud,
    val paused: Boolean = false,
    val outcome: Outcome? = null,
)

data class AppState(
    val screen: Screen = Screen.Splash,
    val stack: List<Screen> = emptyList(),
    val settings: Settings = Settings(),
    val stars: Int = 0,
    val tokens: Int = 0,
    val session: Session? = null,
    val banner: Award? = null,
)
