package com.mirvo.tazlen.plushcade.game.audio

import android.content.Context
import android.media.AudioAttributes
import android.media.SoundPool
import com.mirvo.tazlen.plushcade.R
import com.mirvo.tazlen.plushcade.game.data.GameRepo

enum class Sfx { TAP, DROP, GRAB, SLIP, BANK, WIN, LOSE, UNLOCK }

class SoundManager(context: Context, private val repo: GameRepo) {

    private val pool = SoundPool.Builder()
        .setMaxStreams(5)
        .setAudioAttributes(
            AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_GAME)
                .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                .build()
        )
        .build()

    private val ids = mapOf(
        Sfx.TAP to pool.load(context, R.raw.tap, 1),
        Sfx.DROP to pool.load(context, R.raw.drop, 1),
        Sfx.GRAB to pool.load(context, R.raw.grab, 1),
        Sfx.SLIP to pool.load(context, R.raw.slip, 1),
        Sfx.BANK to pool.load(context, R.raw.bank, 1),
        Sfx.WIN to pool.load(context, R.raw.win, 1),
        Sfx.LOSE to pool.load(context, R.raw.lose, 1),
        Sfx.UNLOCK to pool.load(context, R.raw.unlock, 1),
    )

    fun play(sfx: Sfx, rate: Float = 1f) {
        if (!repo.settings().sound) return
        ids[sfx]?.let { pool.play(it, 0.95f, 0.95f, 1, 0, rate) }
    }

    fun release() = pool.release()
}
