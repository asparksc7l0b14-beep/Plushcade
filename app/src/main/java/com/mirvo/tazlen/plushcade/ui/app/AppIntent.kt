package com.mirvo.tazlen.plushcade.ui.app

import com.mirvo.tazlen.plushcade.game.data.Settings
import com.mirvo.tazlen.plushcade.game.model.Upgrade

sealed interface AppIntent {
    data object Boot : AppIntent
    data class Navigate(val screen: Screen) : AppIntent
    data object Back : AppIntent
    data class OpenCabinet(val index: Int) : AppIntent
    data object StartRush : AppIntent
    data class Aim(val x: Float, val z: Float) : AppIntent
    data object Drop : AppIntent
    data object Tighten : AppIntent
    data class Tick(val dt: Float) : AppIntent
    data object TogglePause : AppIntent
    data object LeaveCabinet : AppIntent
    data object Retry : AppIntent
    data object NextCabinet : AppIntent
    data class Buy(val upgrade: Upgrade) : AppIntent
    data class ApplySettings(val settings: Settings) : AppIntent
    data object FinishTutorial : AppIntent
    data object DismissBanner : AppIntent
}
