package com.mirvo.tazlen.plushcade.game.model

enum class PrizeKind(
    val label: String,
    val asset: String,
    val size: Float,
    val radius: Float,
    val weight: Float,
    val value: Int,
    val flat: Boolean = false,
) {
    CAPSULE("Gumball Capsule", "models/capsule.glb", 0.40f, 0.19f, 0.34f, 10),
    DUCK("Rubber Duck", "models/duck.glb", 0.52f, 0.22f, 0.58f, 20),
    FROG("Pond Frog", "models/frog.glb", 0.54f, 0.23f, 0.64f, 20),
    CAT("Marmalade Cat", "models/cat.glb", 0.56f, 0.22f, 0.66f, 20),
    BUNNY("Cloud Bunny", "models/bunny.glb", 0.58f, 0.23f, 0.70f, 22),
    PENGUIN("Tuxedo Penguin", "models/penguin.glb", 0.56f, 0.22f, 0.74f, 26),
    DINO("Mint Dino", "models/dino.glb", 0.60f, 0.24f, 0.78f, 28),
    BEAR("Caramel Bear", "models/bear.glb", 0.62f, 0.25f, 0.86f, 30),
    UNICORN("Rainbow Unicorn", "models/unicorn.glb", 0.62f, 0.25f, 0.84f, 45),
    STAR("Golden Star", "models/star.glb", 0.48f, 0.20f, 0.56f, 55, flat = true),
    JUMBO("Jumbo Bear", "models/bear.glb", 0.94f, 0.37f, 1.48f, 120);

    val lift: Float get() = size * 0.5f - radius
}
