package com.mirvo.tazlen.plushcade.game.levels

import com.mirvo.tazlen.plushcade.game.model.PrizeKind

sealed interface Goal {
    fun caption(): String

    data class Quota(val count: Int) : Goal {
        override fun caption(): String =
            if (count == 1) "Secure one prize" else "Secure $count prizes"
    }

    data class Hunt(val kind: PrizeKind, val count: Int) : Goal {
        override fun caption(): String =
            if (count == 1) "Secure the ${kind.label}" else "Secure $count of the ${kind.label}"
    }

    data class Worth(val points: Int) : Goal {
        override fun caption(): String = "Bank $points points of prizes"
    }

    data object Rush : Goal {
        override fun caption(): String = "Bank as much value as the clock allows"
    }
}

enum class Twist(val label: String, val detail: String) {
    STILL("Standard", "A plain cabinet with a steady floor."),
    DRIFT("Turntable", "The floor turns slowly — lead your target."),
    SHAKE("Loose Frame", "The machine judders and shifts the whole pile."),
    TILT("Sloped Floor", "Everything creeps toward the low side."),
    POSTS("Glass Posts", "Pillars block the claw where they stand."),
}

data class Cabinet(
    val index: Int,
    val arcade: Int,
    val title: String,
    val credits: Int,
    val grip: Float,
    val slip: Float,
    val goal: Goal,
    val mix: List<Pair<PrizeKind, Int>>,
    val twist: Twist = Twist.STILL,
    val timeLimit: Float = 0f,
    val rush: Boolean = false,
)

data class Boost(
    val grip: Float = 0f,
    val speed: Float = 1f,
    val chute: Float = 0f,
    val credits: Int = 0,
)

object Arcades {
    val names = listOf(
        "Neon Alley",
        "Candy Court",
        "The Boardwalk",
        "Skylight Mall",
        "Frost Fair",
        "The Vault",
    )
    const val PER_ARCADE = 8
}

object Cabinets {

    val rushCabinet = Cabinet(
        index = -1,
        arcade = 0,
        title = "Prize Rush",
        credits = 99,
        grip = 0.86f,
        slip = 0.24f,
        goal = Goal.Rush,
        mix = listOf(
            PrizeKind.CAPSULE to 4,
            PrizeKind.DUCK to 3,
            PrizeKind.CAT to 3,
            PrizeKind.BUNNY to 2,
            PrizeKind.STAR to 1,
        ),
        twist = Twist.STILL,
        timeLimit = 90f,
        rush = true,
    )

    val all: List<Cabinet> = buildList {
        val rows = listOf(
            Row("Warm Up", 8, 0.98f, 0.14f, Goal.Quota(1), listOf(PrizeKind.CAPSULE to 6, PrizeKind.BUNNY to 3)),
            Row("Loose Change", 8, 0.95f, 0.16f, Goal.Quota(2), listOf(PrizeKind.CAPSULE to 6, PrizeKind.CAT to 4)),
            Row("Bunny Hutch", 7, 0.92f, 0.18f, Goal.Hunt(PrizeKind.BUNNY, 1), listOf(PrizeKind.BUNNY to 4, PrizeKind.CAPSULE to 5)),
            Row("Pocket Money", 7, 0.90f, 0.20f, Goal.Worth(40), listOf(PrizeKind.CAPSULE to 7, PrizeKind.DUCK to 3)),
            Row("Duck Pond", 7, 0.88f, 0.21f, Goal.Hunt(PrizeKind.DUCK, 2), listOf(PrizeKind.DUCK to 5, PrizeKind.CAPSULE to 4), Twist.TILT),
            Row("Three Of A Kind", 6, 0.86f, 0.22f, Goal.Quota(3), listOf(PrizeKind.BUNNY to 4, PrizeKind.CAT to 4, PrizeKind.CAPSULE to 3)),
            Row("First Bear", 6, 0.84f, 0.24f, Goal.Hunt(PrizeKind.BEAR, 1), listOf(PrizeKind.BEAR to 3, PrizeKind.CAPSULE to 5)),
            Row("Neon Finale", 6, 0.82f, 0.26f, Goal.Worth(80), listOf(PrizeKind.BEAR to 3, PrizeKind.BUNNY to 3, PrizeKind.CAPSULE to 4), Twist.SHAKE),

            Row("Sugar Rush", 7, 0.88f, 0.23f, Goal.Quota(3), listOf(PrizeKind.CAPSULE to 6, PrizeKind.FROG to 4)),
            Row("Lily Pad", 6, 0.86f, 0.24f, Goal.Hunt(PrizeKind.FROG, 2), listOf(PrizeKind.FROG to 5, PrizeKind.CAT to 4), Twist.DRIFT),
            Row("Cotton Pile", 6, 0.84f, 0.26f, Goal.Worth(90), listOf(PrizeKind.BUNNY to 5, PrizeKind.CAPSULE to 4)),
            Row("Stiff Spring", 6, 0.76f, 0.28f, Goal.Quota(3), listOf(PrizeKind.BEAR to 4, PrizeKind.CAT to 4)),
            Row("Lucky Star", 5, 0.82f, 0.27f, Goal.Hunt(PrizeKind.STAR, 1), listOf(PrizeKind.STAR to 2, PrizeKind.CAPSULE to 6)),
            Row("Crowded Glass", 6, 0.78f, 0.29f, Goal.Quota(4), listOf(PrizeKind.CAPSULE to 5, PrizeKind.DUCK to 4, PrizeKind.CAT to 4), Twist.POSTS),
            Row("Heavy Sweets", 5, 0.78f, 0.30f, Goal.Hunt(PrizeKind.BEAR, 2), listOf(PrizeKind.BEAR to 5, PrizeKind.CAPSULE to 4)),
            Row("Candy Finale", 5, 0.76f, 0.31f, Goal.Worth(140), listOf(PrizeKind.STAR to 2, PrizeKind.BEAR to 3, PrizeKind.FROG to 3), Twist.DRIFT),

            Row("Salt Air", 6, 0.80f, 0.28f, Goal.Quota(4), listOf(PrizeKind.CAT to 5, PrizeKind.CAPSULE to 5)),
            Row("Cold Water", 6, 0.78f, 0.29f, Goal.Hunt(PrizeKind.PENGUIN, 2), listOf(PrizeKind.PENGUIN to 5, PrizeKind.DUCK to 4)),
            Row("Tight Cable", 5, 0.72f, 0.31f, Goal.Quota(3), listOf(PrizeKind.DINO to 5, PrizeKind.BUNNY to 4), Twist.TILT),
            Row("Pier Bears", 5, 0.74f, 0.32f, Goal.Hunt(PrizeKind.BEAR, 2), listOf(PrizeKind.BEAR to 5, PrizeKind.CAT to 3)),
            Row("Two Stars", 5, 0.76f, 0.31f, Goal.Hunt(PrizeKind.STAR, 2), listOf(PrizeKind.STAR to 3, PrizeKind.CAPSULE to 5), Twist.SHAKE),
            Row("Slippery Grip", 5, 0.68f, 0.34f, Goal.Worth(150), listOf(PrizeKind.DINO to 4, PrizeKind.BEAR to 3, PrizeKind.CAPSULE to 3)),
            Row("Deep Corner", 5, 0.68f, 0.35f, Goal.Hunt(PrizeKind.DINO, 3), listOf(PrizeKind.DINO to 6, PrizeKind.CAPSULE to 3), Twist.POSTS),
            Row("Boardwalk Finale", 4, 0.68f, 0.36f, Goal.Worth(190), listOf(PrizeKind.STAR to 2, PrizeKind.BEAR to 4, PrizeKind.DINO to 3)),

            Row("Skylight Opening", 5, 0.74f, 0.32f, Goal.Quota(4), listOf(PrizeKind.CAT to 5, PrizeKind.FROG to 4, PrizeKind.CAPSULE to 3)),
            Row("First Unicorn", 5, 0.76f, 0.31f, Goal.Hunt(PrizeKind.UNICORN, 1), listOf(PrizeKind.UNICORN to 2, PrizeKind.BUNNY to 5)),
            Row("Glass Tower", 4, 0.68f, 0.34f, Goal.Quota(3), listOf(PrizeKind.BEAR to 5, PrizeKind.BUNNY to 4), Twist.POSTS),
            Row("Carousel", 5, 0.70f, 0.33f, Goal.Worth(160), listOf(PrizeKind.PENGUIN to 4, PrizeKind.DUCK to 4, PrizeKind.CAPSULE to 3), Twist.DRIFT),
            Row("Weak Motor", 4, 0.62f, 0.38f, Goal.Worth(140), listOf(PrizeKind.BEAR to 4, PrizeKind.DINO to 4)),
            Row("Full Cabinet", 5, 0.64f, 0.38f, Goal.Quota(5), listOf(PrizeKind.CAPSULE to 5, PrizeKind.CAT to 4, PrizeKind.FROG to 4)),
            Row("Bear Vault", 4, 0.60f, 0.40f, Goal.Hunt(PrizeKind.BEAR, 3), listOf(PrizeKind.BEAR to 6, PrizeKind.CAPSULE to 3), Twist.TILT),
            Row("Skylight Finale", 4, 0.62f, 0.40f, Goal.Worth(200), listOf(PrizeKind.UNICORN to 2, PrizeKind.STAR to 2, PrizeKind.BEAR to 3), Twist.SHAKE),

            Row("Frozen Latch", 5, 0.68f, 0.34f, Goal.Quota(4), listOf(PrizeKind.PENGUIN to 5, PrizeKind.CAPSULE to 4)),
            Row("Ice Drift", 5, 0.66f, 0.35f, Goal.Hunt(PrizeKind.PENGUIN, 3), listOf(PrizeKind.PENGUIN to 6, PrizeKind.DUCK to 3), Twist.DRIFT),
            Row("Slick Floor", 4, 0.64f, 0.37f, Goal.Quota(3), listOf(PrizeKind.FROG to 5, PrizeKind.CAT to 4), Twist.TILT),
            Row("First Jumbo", 5, 0.72f, 0.33f, Goal.Hunt(PrizeKind.JUMBO, 1), listOf(PrizeKind.JUMBO to 1, PrizeKind.CAPSULE to 6)),
            Row("Frost Posts", 4, 0.62f, 0.38f, Goal.Worth(170), listOf(PrizeKind.PENGUIN to 4, PrizeKind.BEAR to 3, PrizeKind.CAPSULE to 3), Twist.POSTS),
            Row("Unicorn Herd", 4, 0.64f, 0.38f, Goal.Hunt(PrizeKind.UNICORN, 2), listOf(PrizeKind.UNICORN to 4, PrizeKind.BUNNY to 4)),
            Row("Blizzard Box", 4, 0.60f, 0.41f, Goal.Quota(4), listOf(PrizeKind.CAPSULE to 4, PrizeKind.PENGUIN to 4, PrizeKind.FROG to 3), Twist.SHAKE),
            Row("Frost Finale", 4, 0.60f, 0.41f, Goal.Worth(230), listOf(PrizeKind.JUMBO to 1, PrizeKind.STAR to 2, PrizeKind.PENGUIN to 3), Twist.DRIFT),

            Row("Velvet Rope", 4, 0.64f, 0.38f, Goal.Worth(180), listOf(PrizeKind.UNICORN to 3, PrizeKind.STAR to 2, PrizeKind.CAPSULE to 3)),
            Row("Display Case", 4, 0.60f, 0.40f, Goal.Hunt(PrizeKind.STAR, 3), listOf(PrizeKind.STAR to 4, PrizeKind.BEAR to 3), Twist.POSTS),
            Row("Brass Turntable", 4, 0.60f, 0.40f, Goal.Quota(4), listOf(PrizeKind.PENGUIN to 4, PrizeKind.DINO to 4, PrizeKind.DUCK to 3), Twist.DRIFT),
            Row("Heavy Vault", 4, 0.66f, 0.37f, Goal.Hunt(PrizeKind.JUMBO, 2), listOf(PrizeKind.JUMBO to 2, PrizeKind.CAPSULE to 5)),
            Row("Trembling Glass", 3, 0.58f, 0.42f, Goal.Worth(190), listOf(PrizeKind.UNICORN to 3, PrizeKind.BEAR to 3, PrizeKind.CAPSULE to 3), Twist.SHAKE),
            Row("Sloped Vault", 3, 0.56f, 0.43f, Goal.Quota(3), listOf(PrizeKind.BEAR to 5, PrizeKind.DINO to 4), Twist.TILT),
            Row("Thin Margin", 3, 0.58f, 0.43f, Goal.Worth(220), listOf(PrizeKind.STAR to 3, PrizeKind.UNICORN to 2, PrizeKind.DINO to 3), Twist.POSTS),
            Row("The Grand Cabinet", 4, 0.55f, 0.45f, Goal.Worth(340), listOf(PrizeKind.JUMBO to 2, PrizeKind.UNICORN to 2, PrizeKind.STAR to 2), Twist.DRIFT),
        )
        rows.forEachIndexed { index, row ->
            add(
                Cabinet(
                    index = index,
                    arcade = index / Arcades.PER_ARCADE,
                    title = row.title,
                    credits = row.credits,
                    grip = row.grip,
                    slip = row.slip,
                    goal = row.goal,
                    mix = row.mix,
                    twist = row.twist,
                )
            )
        }
    }

    val count: Int get() = all.size

    fun byArcade(arcade: Int): List<Cabinet> = all.filter { it.arcade == arcade }

    fun poolSize(kind: PrizeKind): Int {
        val fromCabinets = all.maxOfOrNull { cabinet ->
            cabinet.mix.filter { it.first == kind }.sumOf { it.second }
        } ?: 0
        val fromRush = rushCabinet.mix.filter { it.first == kind }.sumOf { it.second }
        return maxOf(fromCabinets, fromRush, 1)
    }

    private data class Row(
        val title: String,
        val credits: Int,
        val grip: Float,
        val slip: Float,
        val goal: Goal,
        val mix: List<Pair<PrizeKind, Int>>,
        val twist: Twist = Twist.STILL,
    )
}
