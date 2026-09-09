package com.mirvo.tazlen.plushcade.game.data

import android.content.Context
import android.content.SharedPreferences
import com.mirvo.tazlen.plushcade.game.levels.Boost
import com.mirvo.tazlen.plushcade.game.levels.Cabinets
import com.mirvo.tazlen.plushcade.game.model.PrizeKind
import com.mirvo.tazlen.plushcade.game.model.Upgrade

data class Settings(
    val sound: Boolean = true,
    val vibration: Boolean = true,
    val background: Int = 0,
)

data class Stats(
    val cleared: Int = 0,
    val stars: Int = 0,
    val prizes: Int = 0,
    val credits: Int = 0,
    val runs: Int = 0,
    val slips: Int = 0,
    val rushBest: Int = 0,
    val tokens: Int = 0,
    val earned: Int = 0,
)

class GameRepo(context: Context) {

    private val prefs: SharedPreferences =
        context.getSharedPreferences("com.mirvo.tazlen.plushcade_game_v1", Context.MODE_PRIVATE)

    fun starsFor(index: Int): Int = prefs.getInt("cab_${index}_stars", 0)

    fun unlocked(index: Int): Boolean = index == 0 || prefs.getBoolean("cab_${index}_open", false)

    fun bestCredits(index: Int): Int = prefs.getInt("cab_${index}_credits", 0)

    fun totalStars(): Int = (0 until Cabinets.count).sumOf { starsFor(it) }

    fun highestOpen(): Int = (0 until Cabinets.count).lastOrNull { unlocked(it) } ?: 0

    fun saveClear(index: Int, stars: Int, creditsLeft: Int) {
        val editor = prefs.edit()
        if (stars > starsFor(index)) editor.putInt("cab_${index}_stars", stars)
        if (creditsLeft > bestCredits(index)) editor.putInt("cab_${index}_credits", creditsLeft)
        if (index + 1 < Cabinets.count) editor.putBoolean("cab_${index + 1}_open", true)
        editor.apply()
    }

    fun addRun(prizes: Int, creditsUsed: Int, slips: Int, cleared: Boolean) {
        val current = stats()
        val editor = prefs.edit()
        editor.putInt("stat_runs", current.runs + 1)
        editor.putInt("stat_prizes", current.prizes + prizes)
        editor.putInt("stat_credits", current.credits + creditsUsed)
        editor.putInt("stat_slips", current.slips + slips)
        if (cleared) editor.putInt("stat_cleared", clearedCount() + 1)
        editor.apply()
    }

    fun clearedCount(): Int = (0 until Cabinets.count).count { starsFor(it) > 0 }

    fun tokens(): Int = prefs.getInt("tokens", 0)

    fun earnTokens(amount: Int) {
        if (amount <= 0) return
        prefs.edit()
            .putInt("tokens", tokens() + amount)
            .putInt("stat_earned", prefs.getInt("stat_earned", 0) + amount)
            .apply()
    }

    fun level(upgrade: Upgrade): Int = prefs.getInt("up_${upgrade.name}", 0)

    fun canBuy(upgrade: Upgrade): Boolean {
        val current = level(upgrade)
        return current < upgrade.steps && tokens() >= upgrade.cost(current)
    }

    fun buy(upgrade: Upgrade): Boolean {
        if (!canBuy(upgrade)) return false
        val current = level(upgrade)
        prefs.edit()
            .putInt("tokens", tokens() - upgrade.cost(current))
            .putInt("up_${upgrade.name}", current + 1)
            .apply()
        return true
    }

    fun maxedAny(): Boolean = Upgrade.entries.any { level(it) >= it.steps }

    fun boost(): Boost = Boost(
        grip = level(Upgrade.GRIP) * 0.04f,
        speed = 1f + level(Upgrade.WINCH) * 0.10f,
        chute = level(Upgrade.MAGNET) * 0.036f,
        credits = level(Upgrade.SPARE),
    )

    fun rushBest(): Int = prefs.getInt("rush_best", 0)

    fun saveRush(points: Int) {
        if (points > rushBest()) prefs.edit().putInt("rush_best", points).apply()
    }

    fun stats(): Stats = Stats(
        cleared = clearedCount(),
        stars = totalStars(),
        prizes = prefs.getInt("stat_prizes", 0),
        credits = prefs.getInt("stat_credits", 0),
        runs = prefs.getInt("stat_runs", 0),
        slips = prefs.getInt("stat_slips", 0),
        rushBest = rushBest(),
        tokens = tokens(),
        earned = prefs.getInt("stat_earned", 0),
    )

    fun shelfCount(kind: PrizeKind): Int = prefs.getInt("shelf_${kind.name}", 0)

    fun addShelf(counts: Map<PrizeKind, Int>) {
        val editor = prefs.edit()
        counts.forEach { (kind, amount) ->
            editor.putInt("shelf_${kind.name}", shelfCount(kind) + amount)
        }
        editor.apply()
    }

    fun shelfComplete(): Boolean = PrizeKind.entries.all { shelfCount(it) > 0 }

    fun awardUnlocked(id: String): Boolean = prefs.getBoolean("award_$id", false)

    fun unlockAward(id: String): Boolean {
        if (awardUnlocked(id)) return false
        prefs.edit().putBoolean("award_$id", true).apply()
        return true
    }

    fun settings(): Settings = Settings(
        sound = prefs.getBoolean("sound", true),
        vibration = prefs.getBoolean("vibration", true),
        background = prefs.getInt("background", 0),
    )

    fun saveSettings(settings: Settings) {
        prefs.edit()
            .putBoolean("sound", settings.sound)
            .putBoolean("vibration", settings.vibration)
            .putInt("background", settings.background)
            .apply()
    }

    fun tutorialSeen(): Boolean = prefs.getBoolean("tutorial_seen", false)

    fun markTutorialSeen() = prefs.edit().putBoolean("tutorial_seen", true).apply()
}
