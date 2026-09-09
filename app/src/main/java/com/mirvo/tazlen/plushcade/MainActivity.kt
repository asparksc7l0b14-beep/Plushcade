package com.mirvo.tazlen.plushcade

import android.app.Activity
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import com.mirvo.tazlen.plushcade.game.ui.Palette
import com.mirvo.tazlen.plushcade.ui.app.AppIntent
import com.mirvo.tazlen.plushcade.ui.app.AppViewModel
import com.mirvo.tazlen.plushcade.ui.app.Screen
import com.mirvo.tazlen.plushcade.ui.screens.AwardsScreen
import com.mirvo.tazlen.plushcade.ui.screens.GameScreen
import com.mirvo.tazlen.plushcade.ui.screens.LevelSelectScreen
import com.mirvo.tazlen.plushcade.ui.screens.MainMenuScreen
import com.mirvo.tazlen.plushcade.ui.screens.ResultsScreen
import com.mirvo.tazlen.plushcade.ui.screens.SettingsScreen
import com.mirvo.tazlen.plushcade.ui.screens.SplashScreen
import com.mirvo.tazlen.plushcade.ui.screens.StatsScreen
import com.mirvo.tazlen.plushcade.ui.screens.TutorialScreen
import com.mirvo.tazlen.plushcade.ui.screens.WorkshopScreen
import com.mirvo.tazlen.plushcade.ui.theme.PlushcadeTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        WindowCompat.setDecorFitsSystemWindows(window, false)
        val controller = WindowInsetsControllerCompat(window, window.decorView)
        controller.hide(WindowInsetsCompat.Type.systemBars())
        controller.systemBarsBehavior =
            WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
        setContent {
            PlushcadeTheme {
                Box(Modifier.fillMaxSize().background(Palette.Ink)) {
                    PlushcadeApp()
                }
            }
        }
    }
}

@Composable
private fun PlushcadeApp(viewModel: AppViewModel = viewModel()) {
    val state by viewModel.state.collectAsState()
    val activity = LocalContext.current as Activity

    when (val screen = state.screen) {
        Screen.Splash -> SplashScreen { viewModel.send(AppIntent.Boot) }

        Screen.Tutorial -> TutorialScreen { viewModel.send(AppIntent.FinishTutorial) }

        Screen.MainMenu -> {
            BackHandler { activity.finish() }
            MainMenuScreen(
                backdrop = state.settings.background,
                stars = state.stars,
                tokens = state.tokens,
                onPlay = { viewModel.send(AppIntent.OpenCabinet(viewModel.repo.highestOpen())) },
                onLevels = { viewModel.send(AppIntent.Navigate(Screen.LevelSelect)) },
                onRush = { viewModel.send(AppIntent.StartRush) },
                onWorkshop = { viewModel.send(AppIntent.Navigate(Screen.Workshop)) },
                onAwards = { viewModel.send(AppIntent.Navigate(Screen.Awards)) },
                onStats = { viewModel.send(AppIntent.Navigate(Screen.Stats)) },
                onSettings = { viewModel.send(AppIntent.Navigate(Screen.Settings)) },
                onExit = { activity.finish() },
            )
        }

        Screen.LevelSelect -> {
            BackHandler { viewModel.send(AppIntent.Back) }
            LevelSelectScreen(
                repo = viewModel.repo,
                onOpen = { viewModel.send(AppIntent.OpenCabinet(it)) },
                onBack = { viewModel.send(AppIntent.Back) },
            )
        }

        is Screen.Game -> {
            val session = state.session
            if (session == null) {
                LaunchedEffect(screen) { viewModel.send(AppIntent.LeaveCabinet) }
            } else {
                GameScreen(
                    cabinet = session.cabinet,
                    engine = session.engine,
                    hud = session.hud,
                    paused = session.paused,
                    backdrop = state.settings.background,
                    onStep = { viewModel.send(AppIntent.Tick(it)) },
                    onAim = { x, z -> viewModel.send(AppIntent.Aim(x, z)) },
                    onDrop = { viewModel.send(AppIntent.Drop) },
                    onTighten = { viewModel.send(AppIntent.Tighten) },
                    onPause = { viewModel.send(AppIntent.TogglePause) },
                    onQuit = { viewModel.send(AppIntent.LeaveCabinet) },
                )
            }
        }

        Screen.Results -> {
            val session = state.session
            val outcome = session?.outcome
            if (session == null || outcome == null) {
                LaunchedEffect(screen) { viewModel.send(AppIntent.LeaveCabinet) }
            } else {
                ResultsScreen(
                    cabinet = session.cabinet,
                    outcome = outcome,
                    backdrop = state.settings.background,
                    onRetry = { viewModel.send(AppIntent.Retry) },
                    onNext = { viewModel.send(AppIntent.NextCabinet) },
                    onMenu = { viewModel.send(AppIntent.LeaveCabinet) },
                )
            }
        }

        Screen.Workshop -> WorkshopScreen(
            repo = viewModel.repo,
            tokens = state.tokens,
            backdrop = state.settings.background,
            onBuy = { viewModel.send(AppIntent.Buy(it)) },
            onBack = { viewModel.send(AppIntent.Back) },
        )

        Screen.Stats -> StatsScreen(
            repo = viewModel.repo,
            backdrop = state.settings.background,
            onBack = { viewModel.send(AppIntent.Back) },
        )

        Screen.Awards -> AwardsScreen(
            repo = viewModel.repo,
            backdrop = state.settings.background,
            onBack = { viewModel.send(AppIntent.Back) },
        )

        Screen.Settings -> SettingsScreen(
            settings = state.settings,
            onChange = { viewModel.send(AppIntent.ApplySettings(it)) },
            onBack = { viewModel.send(AppIntent.Back) },
        )
    }
}
