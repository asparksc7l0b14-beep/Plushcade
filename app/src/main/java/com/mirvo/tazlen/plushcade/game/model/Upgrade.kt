package com.mirvo.tazlen.plushcade.game.model

enum class Upgrade(
    val label: String,
    val detail: String,
    val steps: Int,
    val baseCost: Int,
) {
    GRIP("Grip Pads", "Every hold starts tighter", 5, 70),
    WINCH("Fast Winch", "The claw drops, lifts and travels quicker", 4, 110),
    MAGNET("Chute Magnet", "Widens the mouth of the chute", 3, 180),
    SPARE("Spare Credit", "One extra credit in every cabinet", 3, 260);

    fun cost(level: Int): Int = baseCost * (level + 1)

    fun readout(level: Int): String = when (this) {
        GRIP -> "+" + (level * 4) + "% starting grip"
        WINCH -> "+" + (level * 10) + "% claw speed"
        MAGNET -> "+" + (level * 14) + "% chute width"
        SPARE -> "+" + level + " credit"
    }
}
