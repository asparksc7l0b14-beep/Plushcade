package com.mirvo.tazlen.plushcade.ui.app

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import com.mirvo.tazlen.plushcade.game.audio.Haptics
import com.mirvo.tazlen.plushcade.game.audio.Sfx
import com.mirvo.tazlen.plushcade.game.audio.SoundManager
import com.mirvo.tazlen.plushcade.game.data.GameRepo
import com.mirvo.tazlen.plushcade.game.levels.Arcades
import com.mirvo.tazlen.plushcade.game.levels.Cabinet
import com.mirvo.tazlen.plushcade.game.levels.Cabinets
import com.mirvo.tazlen.plushcade.game.levels.Twist
import com.mirvo.tazlen.plushcade.game.logic.Award
import com.mirvo.tazlen.plushcade.game.logic.Awards
import com.mirvo.tazlen.plushcade.game.logic.ClawEngine
import com.mirvo.tazlen.plushcade.game.logic.ClawEvent
import com.mirvo.tazlen.plushcade.game.model.PrizeKind
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class AppViewModel(application: Application) : AndroidViewModel(application) {

    val repo = GameRepo(application)
    private val sound = SoundManager(application, repo)
    private val haptics = Haptics(application, repo)

    private val _state = MutableStateFlow(
        AppState(settings = repo.settings(), stars = repo.totalStars(), tokens = repo.tokens())
    )
    val state: StateFlow<AppState> = _state.asStateFlow()

    fun send(intent: AppIntent) {
        _state.value = reduce(_state.value, intent)
    }

    private fun reduce(state: AppState, intent: AppIntent): AppState = when (intent) {
        AppIntent.Boot -> {
            val next = if (repo.tutorialSeen()) Screen.MainMenu else Screen.Tutorial
            state.copy(screen = next, stack = emptyList(), stars = repo.totalStars(), tokens = repo.tokens())
        }

        is AppIntent.Navigate -> {
            sound.play(Sfx.TAP)
            haptics.tick()
            state.copy(screen = intent.screen, stack = state.stack + state.screen, tokens = repo.tokens())
        }

        AppIntent.Back -> popped(state)

        is AppIntent.OpenCabinet -> open(state, Cabinets.all[intent.index.coerceIn(0, Cabinets.count - 1)])

        AppIntent.StartRush -> open(state, Cabinets.rushCabinet)

        is AppIntent.Aim -> {
            state.session?.engine?.aimAt(intent.x, intent.z)
            state
        }

        AppIntent.Drop -> {
            val engine = state.session?.engine
            if (engine != null && engine.canDrop()) {
                engine.drop()
                haptics.knock()
            }
            state
        }

        AppIntent.Tighten -> {
            val engine = state.session?.engine
            if (engine != null && engine.holding()) {
                engine.tighten()
                sound.play(Sfx.TAP, 1.35f)
                haptics.tick()
            }
            state
        }

        is AppIntent.Tick -> tick(state, intent.dt)

        AppIntent.TogglePause -> {
            val session = state.session ?: return@reduce state
            sound.play(Sfx.TAP)
            state.copy(session = session.copy(paused = !session.paused))
        }

        AppIntent.LeaveCabinet -> {
            sound.play(Sfx.TAP)
            state.copy(
                screen = Screen.LevelSelect,
                stack = listOf(Screen.MainMenu),
                session = null,
                tokens = repo.tokens(),
            )
        }

        AppIntent.Retry -> {
            val cabinet = state.session?.cabinet ?: Cabinets.all[0]
            open(state.copy(session = null), cabinet)
        }

        AppIntent.NextCabinet -> {
            val cabinet = state.session?.cabinet
            val next = (cabinet?.index ?: 0) + 1
            if (cabinet == null || cabinet.rush || next >= Cabinets.count) {
                state.copy(
                    screen = Screen.LevelSelect,
                    stack = listOf(Screen.MainMenu),
                    session = null,
                    tokens = repo.tokens(),
                )
            } else {
                open(state.copy(session = null), Cabinets.all[next])
            }
        }

        is AppIntent.Buy -> {
            val bought = repo.buy(intent.upgrade)
            if (bought) {
                sound.play(Sfx.UNLOCK)
                haptics.success()
            } else {
                sound.play(Sfx.SLIP)
                haptics.error()
            }
            val earned = if (bought && repo.maxedAny()) award("kitted") else emptyList()
            state.copy(tokens = repo.tokens(), banner = earned.firstOrNull() ?: state.banner)
        }

        is AppIntent.ApplySettings -> {
            repo.saveSettings(intent.settings)
            sound.play(Sfx.TAP)
            haptics.tick()
            state.copy(settings = intent.settings)
        }

        AppIntent.FinishTutorial -> {
            repo.markTutorialSeen()
            state.copy(screen = Screen.MainMenu, stack = emptyList())
        }

        AppIntent.DismissBanner -> state.copy(banner = null)
    }

    private fun popped(state: AppState): AppState {
        if (state.stack.isEmpty()) return state.copy(screen = Screen.MainMenu, session = null)
        val target = state.stack[state.stack.lastIndex]
        return state.copy(
            screen = target,
            stack = state.stack.subList(0, state.stack.lastIndex),
            stars = repo.totalStars(),
            tokens = repo.tokens(),
            session = if (target is Screen.Game) state.session else null,
        )
    }

    private fun open(state: AppState, cabinet: Cabinet): AppState {
        val engine = ClawEngine(cabinet, System.nanoTime(), repo.boost())
        sound.play(Sfx.TAP)
        return state.copy(
            screen = Screen.Game(cabinet.index),
            stack = listOf(Screen.MainMenu, Screen.LevelSelect),
            session = Session(cabinet = cabinet, engine = engine, hud = snapshot(engine)),
        )
    }

    private fun tick(state: AppState, dt: Float): AppState {
        val session = state.session ?: return state
        if (session.paused || session.outcome != null) return state
        val engine = session.engine
        engine.update(dt)
        var event = engine.poll()
        while (event != null) {
            handle(event)
            event = engine.poll()
        }
        val hud = snapshot(engine)
        if (engine.over) return finish(state, session)
        return if (hud == session.hud) state else state.copy(session = session.copy(hud = hud))
    }

    private fun handle(event: ClawEvent) {
        when (event) {
            ClawEvent.DROP -> sound.play(Sfx.DROP)
            ClawEvent.GRAB -> {
                sound.play(Sfx.GRAB)
                haptics.knock()
            }
            ClawEvent.MISS -> sound.play(Sfx.GRAB, 0.7f)
            ClawEvent.SLIP -> {
                sound.play(Sfx.SLIP)
                haptics.error()
            }
            ClawEvent.BANK -> {
                sound.play(Sfx.BANK)
                haptics.success()
            }
            ClawEvent.JUDDER -> {
                sound.play(Sfx.GRAB, 0.55f)
                haptics.knock()
            }
            ClawEvent.CLEAR -> sound.play(Sfx.WIN)
            ClawEvent.FAIL -> sound.play(Sfx.LOSE)
        }
    }

    private fun finish(state: AppState, session: Session): AppState {
        val engine = session.engine
        val cabinet = session.cabinet
        val spent = cabinet.credits - engine.credits
        val record = cabinet.rush && engine.points > repo.rushBest()
        repo.addShelf(engine.bankedKinds)
        repo.addRun(engine.banked, spent.coerceAtLeast(0), engine.slips, engine.won && !cabinet.rush)
        repo.earnTokens(engine.points)
        if (cabinet.rush) {
            repo.saveRush(engine.points)
        } else if (engine.won) {
            repo.saveClear(cabinet.index, engine.stars(), engine.credits)
        }
        val earned = grant(session, spent)
        if (earned.isNotEmpty()) sound.play(Sfx.UNLOCK)
        val outcome = Outcome(
            won = engine.won,
            stars = engine.stars(),
            prizes = engine.banked,
            points = engine.points,
            creditsLeft = engine.credits,
            tokens = engine.points,
            rush = cabinet.rush,
            record = record,
            awards = earned,
        )
        return state.copy(
            screen = Screen.Results,
            stack = listOf(Screen.MainMenu, Screen.LevelSelect),
            stars = repo.totalStars(),
            tokens = repo.tokens(),
            session = session.copy(outcome = outcome),
            banner = earned.firstOrNull(),
        )
    }

    private fun grant(session: Session, spent: Int): List<Award> {
        val engine = session.engine
        val cabinet = session.cabinet
        val hits = ArrayList<String>()
        val cleared = engine.won && !cabinet.rush
        if (engine.banked > 0) hits.add("first_prize")
        if (cleared) hits.add("first_clear")
        if (repo.clearedCount() >= 5) hits.add("five_clears")
        if (!cabinet.rush && Cabinets.byArcade(cabinet.arcade).all { repo.starsFor(it.index) > 0 }) {
            hits.add("arcade_done")
        }
        if (cleared && engine.flawless) hits.add("flawless")
        if (engine.bankedKinds.containsKey(PrizeKind.STAR)) hits.add("star_catch")
        if (engine.bankedKinds.containsKey(PrizeKind.UNICORN)) hits.add("unicorn")
        if (engine.bankedKinds.containsKey(PrizeKind.JUMBO)) hits.add("jumbo")
        if (repo.shelfComplete()) hits.add("full_shelf")
        if (repo.stats().prizes >= 50) hits.add("fifty")
        if (cleared && spent <= (cabinet.credits + 2) / 3) hits.add("thrifty")
        if (cleared) {
            when (cabinet.twist) {
                Twist.DRIFT -> hits.add("turntable")
                Twist.SHAKE -> hits.add("quake")
                Twist.POSTS -> hits.add("posts")
                Twist.TILT -> hits.add("slope")
                Twist.STILL -> Unit
            }
        }
        if (cabinet.rush && engine.points >= 300) hits.add("rush")
        if (repo.maxedAny()) hits.add("kitted")
        if (repo.clearedCount() >= Cabinets.count) hits.add("grand")
        return hits.filter { repo.unlockAward(it) }.mapNotNull { Awards.byId(it) }
    }

    private fun award(id: String): List<Award> =
        if (repo.unlockAward(id)) listOfNotNull(Awards.byId(id)) else emptyList()

    private fun snapshot(engine: ClawEngine): Hud {
        val (have, need) = engine.goalProgress()
        return Hud(
            credits = engine.credits,
            banked = engine.banked,
            points = engine.points,
            have = have,
            need = need,
            phase = engine.phase,
            grip = (engine.grip * 20f).toInt() / 20f,
            holding = engine.holding(),
            seconds = engine.timeLeft().toInt(),
        )
    }

    fun arcadeName(arcade: Int): String = Arcades.names[arcade.coerceIn(0, Arcades.names.lastIndex)]

    override fun onCleared() {
        sound.release()
        super.onCleared()
    }
}
