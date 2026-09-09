package com.mirvo.tazlen.plushcade.game.logic

data class Award(val id: String, val title: String, val detail: String)

object Awards {
    val all = listOf(
        Award("first_prize", "First Grab", "Secure your first prize from a cabinet."),
        Award("first_clear", "Machine Beaten", "Clear a cabinet's target."),
        Award("five_clears", "Regular Customer", "Clear five cabinets."),
        Award("arcade_done", "Room Emptied", "Clear every cabinet in one arcade."),
        Award("flawless", "Steady Hand", "Clear a cabinet without a single miss or slip."),
        Award("star_catch", "Star Catcher", "Secure a Golden Star."),
        Award("unicorn", "Rainbow Chaser", "Secure a Rainbow Unicorn."),
        Award("jumbo", "Jumbo Haul", "Secure a Jumbo Bear — the heaviest plush in the chain."),
        Award("full_shelf", "Full Shelf", "Secure at least one of every prize."),
        Award("fifty", "Fifty Plush", "Secure fifty prizes in total."),
        Award("thrifty", "Thrifty", "Clear a cabinet on a third of its credits."),
        Award("turntable", "Turntable Tamer", "Clear a cabinet with a turning floor."),
        Award("quake", "Steady In A Quake", "Clear a cabinet in a loose, juddering machine."),
        Award("posts", "Post Runner", "Clear a cabinet guarded by glass posts."),
        Award("slope", "Uphill Work", "Clear a cabinet with a sloped floor."),
        Award("rush", "Rush Hour", "Bank 300 points in a single Prize Rush."),
        Award("kitted", "Kitted Out", "Take any workshop upgrade to its last step."),
        Award("grand", "Grand Collector", "Clear every cabinet in the arcade chain."),
    )

    fun byId(id: String): Award? = all.firstOrNull { it.id == id }
}
