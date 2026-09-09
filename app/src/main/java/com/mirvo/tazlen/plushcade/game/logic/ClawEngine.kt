package com.mirvo.tazlen.plushcade.game.logic

import com.mirvo.tazlen.plushcade.game.levels.Boost
import com.mirvo.tazlen.plushcade.game.levels.Cabinet
import com.mirvo.tazlen.plushcade.game.levels.Goal
import com.mirvo.tazlen.plushcade.game.levels.Twist
import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.hypot
import kotlin.math.max
import kotlin.math.sin
import kotlin.math.sqrt
import kotlin.random.Random

enum class ClawPhase { AIMING, DESCENDING, CLOSING, LIFTING, CARRYING, RELEASING, RETURNING, OVER }

enum class ClawEvent { DROP, GRAB, MISS, SLIP, BANK, CLEAR, FAIL, JUDDER }

object Cab {
    const val HALF_X = 0.70f
    const val HALF_Z = 0.60f
    const val TOP_Y = 1.18f
    const val CHUTE_X = -0.40f
    const val CHUTE_Z = 0.28f
    const val CHUTE_R = 0.22f
    const val RIM = 0.09f
    const val POST_R = 0.13f
    const val POST_TOP = 0.80f
}

class Toy(val id: Int, val kind: com.mirvo.tazlen.plushcade.game.model.PrizeKind) {
    var x = 0f
    var y = 0f
    var z = 0f
    var vx = 0f
    var vy = 0f
    var vz = 0f
    var yaw = 0f
    var held = false
    var gone = false
    var falling = false
}

data class Post(val x: Float, val z: Float)

class ClawEngine(val cabinet: Cabinet, seed: Long, boost: Boost = Boost()) {

    private val rng = Random(seed)
    private val queue = ArrayDeque<ClawEvent>()

    val toys = ArrayList<Toy>()
    val posts: List<Post> =
        if (cabinet.twist == Twist.POSTS) {
            listOf(Post(-0.14f, -0.28f), Post(0.28f, 0.16f), Post(0.52f, -0.20f))
        } else {
            emptyList()
        }

    private val startGrip = (cabinet.grip + boost.grip).coerceAtMost(1f)
    private val chuteRadius = Cab.CHUTE_R + boost.chute
    private val winch = boost.speed

    var phase = ClawPhase.AIMING
        private set
    var clawX = 0.4f
        private set
    var clawZ = -0.2f
        private set
    var clawY = Cab.TOP_Y
        private set
    var prongs = 1f
        private set
    var grip = 1f
        private set
    var held: Toy? = null
        private set

    var credits = cabinet.credits + boost.credits
        private set
    var banked = 0
        private set
    var points = 0
        private set
    var slips = 0
        private set
    var misses = 0
        private set
    var flawless = true
        private set
    var won = false
        private set
    var over = false
        private set
    var clock = 0f
        private set

    val bankedKinds = LinkedHashMap<com.mirvo.tazlen.plushcade.game.model.PrizeKind, Int>()

    private var aimX = 0.4f
    private var aimZ = -0.2f
    private var floorY = 0.16f
    private var tapCooldown = 0f
    private var settleClock = 0f
    private var judder = 0f
    private var nextId = 0

    init {
        fill()
    }

    fun timeLeft(): Float =
        if (cabinet.timeLimit <= 0f) 0f else (cabinet.timeLimit - clock).coerceAtLeast(0f)

    fun poll(): ClawEvent? = queue.removeFirstOrNull()

    fun aimAt(x: Float, z: Float) {
        if (phase != ClawPhase.AIMING || over) return
        val margin = 0.16f
        aimX = x.coerceIn(-Cab.HALF_X + margin, Cab.HALF_X - margin)
        aimZ = z.coerceIn(-Cab.HALF_Z + margin, Cab.HALF_Z - margin)
    }

    fun canDrop(): Boolean = phase == ClawPhase.AIMING && credits > 0 && !over

    fun drop() {
        if (!canDrop()) return
        credits--
        phase = ClawPhase.DESCENDING
        floorY = descendTarget()
        queue.addLast(ClawEvent.DROP)
    }

    fun tighten() {
        if (held == null || tapCooldown > 0f) return
        if (phase != ClawPhase.LIFTING && phase != ClawPhase.CARRYING) return
        grip = (grip + 0.15f).coerceAtMost(1f)
        tapCooldown = 0.30f
    }

    fun holding(): Boolean = held != null

    fun update(dt: Float) {
        if (over) return
        clock += dt
        if (tapCooldown > 0f) tapCooldown = max(0f, tapCooldown - dt)
        stepTwist(dt)
        stepClaw(dt)
        physics(dt)
        toys.forEach { toy ->
            if (!toy.gone && !toy.held && toy.falling && toy.y < -0.55f) bank(toy)
        }
        if (cabinet.rush) {
            if (toys.none { !it.gone }) fill()
            if (cabinet.timeLimit > 0f && clock >= cabinet.timeLimit) {
                won = banked > 0
                over = true
                phase = ClawPhase.OVER
                queue.addLast(if (won) ClawEvent.CLEAR else ClawEvent.FAIL)
            }
            return
        }
        if (goalMet()) {
            won = true
            over = true
            phase = ClawPhase.OVER
            queue.addLast(ClawEvent.CLEAR)
            return
        }
        if (credits <= 0 && phase == ClawPhase.AIMING) {
            settleClock += dt
            if (settleClock > 1.1f) {
                over = true
                phase = ClawPhase.OVER
                queue.addLast(ClawEvent.FAIL)
            }
        }
    }

    fun stars(): Int {
        if (!won) return 0
        if (cabinet.rush) return 3
        val spent = cabinet.credits - credits
        return when {
            spent <= (cabinet.credits + 2) / 3 -> 3
            spent <= (cabinet.credits * 2 + 2) / 3 -> 2
            else -> 1
        }
    }

    fun goalProgress(): Pair<Int, Int> = when (val goal = cabinet.goal) {
        is Goal.Quota -> banked to goal.count
        is Goal.Hunt -> (bankedKinds[goal.kind] ?: 0) to goal.count
        is Goal.Worth -> points to goal.points
        Goal.Rush -> points to 0
    }

    private fun goalMet(): Boolean {
        if (cabinet.rush) return false
        val (have, need) = goalProgress()
        return have >= need
    }

    private fun fill() {
        toys.removeAll { it.gone }
        cabinet.mix.forEach { (kind, count) ->
            repeat(count) {
                val toy = Toy(nextId++, kind)
                var attempts = 0
                do {
                    toy.x = (rng.nextFloat() - 0.5f) * 2f * (Cab.HALF_X - kind.radius - 0.08f)
                    toy.z = (rng.nextFloat() - 0.5f) * 2f * (Cab.HALF_Z - kind.radius - 0.08f)
                    attempts++
                } while (attempts < 50 && !clearOfFurniture(toy.x, toy.z, kind.radius))
                toy.y = 0.55f + (toy.id % 14) * 0.13f
                toy.yaw = rng.nextFloat() * 360f
                toys.add(toy)
            }
        }
        repeat(200) { physics(0.016f) }
        toys.forEach { toy ->
            toy.vx = 0f
            toy.vy = 0f
            toy.vz = 0f
            toy.falling = false
            if (!toy.gone && toy.y < toy.kind.radius) shoveOutOfChute(toy)
        }
    }

    private fun clearOfFurniture(x: Float, z: Float, radius: Float): Boolean {
        if (hypot(x - Cab.CHUTE_X, z - Cab.CHUTE_Z) < chuteRadius + radius + 0.22f) return false
        return posts.none { hypot(x - it.x, z - it.z) < Cab.POST_R + radius + 0.06f }
    }

    private fun shoveOutOfChute(toy: Toy) {
        val away = hypot(toy.x - Cab.CHUTE_X, toy.z - Cab.CHUTE_Z).coerceAtLeast(0.001f)
        val reach = chuteRadius + toy.kind.radius + 0.20f
        toy.x = (Cab.CHUTE_X + (toy.x - Cab.CHUTE_X) / away * reach)
            .coerceIn(-Cab.HALF_X + toy.kind.radius, Cab.HALF_X - toy.kind.radius)
        toy.z = (Cab.CHUTE_Z + (toy.z - Cab.CHUTE_Z) / away * reach)
            .coerceIn(-Cab.HALF_Z + toy.kind.radius, Cab.HALF_Z - toy.kind.radius)
        toy.y = toy.kind.radius
    }

    private fun stepTwist(dt: Float) {
        when (cabinet.twist) {
            Twist.DRIFT -> {
                val angle = dt * 0.30f
                val cosine = cos(angle)
                val sine = sin(angle)
                toys.forEach { toy ->
                    if (toy.gone || toy.held) return@forEach
                    val nx = toy.x * cosine - toy.z * sine
                    val nz = toy.x * sine + toy.z * cosine
                    toy.x = nx.coerceIn(-Cab.HALF_X + toy.kind.radius, Cab.HALF_X - toy.kind.radius)
                    toy.z = nz.coerceIn(-Cab.HALF_Z + toy.kind.radius, Cab.HALF_Z - toy.kind.radius)
                    toy.yaw += angle * 57.3f
                }
            }

            Twist.SHAKE -> {
                judder += dt
                if (judder >= 3.4f) {
                    judder = 0f
                    toys.forEach { toy ->
                        if (toy.gone || toy.held) return@forEach
                        toy.vx += (rng.nextFloat() - 0.5f) * 1.5f
                        toy.vz += (rng.nextFloat() - 0.5f) * 1.5f
                        toy.vy += rng.nextFloat() * 0.65f
                    }
                    queue.addLast(ClawEvent.JUDDER)
                }
            }

            Twist.TILT -> {
                toys.forEach { toy ->
                    if (toy.gone || toy.held) return@forEach
                    if (toy.y <= toy.kind.radius + 0.02f) toy.vx -= 0.55f * dt
                }
            }

            Twist.STILL, Twist.POSTS -> Unit
        }
    }

    private fun stepClaw(dt: Float) {
        when (phase) {
            ClawPhase.AIMING -> {
                clawX += (aimX - clawX) * (dt * 9f).coerceAtMost(1f)
                clawZ += (aimZ - clawZ) * (dt * 9f).coerceAtMost(1f)
                clawY += (Cab.TOP_Y - clawY) * (dt * 6f).coerceAtMost(1f)
                prongs = (prongs + dt * 3f).coerceAtMost(1f)
            }

            ClawPhase.DESCENDING -> {
                clawY -= dt * 1.9f * winch
                if (clawY <= floorY) {
                    clawY = floorY
                    phase = ClawPhase.CLOSING
                }
            }

            ClawPhase.CLOSING -> {
                prongs -= dt * 2.6f
                if (prongs <= 0f) {
                    prongs = 0f
                    attemptGrab()
                    phase = ClawPhase.LIFTING
                }
            }

            ClawPhase.LIFTING -> {
                clawY += dt * 1.25f * winch
                drainGrip(dt)
                if (clawY >= Cab.TOP_Y) {
                    clawY = Cab.TOP_Y
                    phase = if (held != null) ClawPhase.CARRYING else ClawPhase.RETURNING
                }
            }

            ClawPhase.CARRYING -> {
                val step = dt * 1.25f * winch
                clawX = approach(clawX, Cab.CHUTE_X, step)
                clawZ = approach(clawZ, Cab.CHUTE_Z, step)
                drainGrip(dt)
                if (abs(clawX - Cab.CHUTE_X) < 0.02f && abs(clawZ - Cab.CHUTE_Z) < 0.02f) {
                    phase = ClawPhase.RELEASING
                }
            }

            ClawPhase.RELEASING -> {
                prongs += dt * 3.2f
                if (prongs >= 0.55f) {
                    releaseHeld(0f)
                    phase = ClawPhase.RETURNING
                }
            }

            ClawPhase.RETURNING -> {
                prongs = (prongs + dt * 3f).coerceAtMost(1f)
                val step = dt * 1.5f * winch
                clawX = approach(clawX, aimX, step)
                clawZ = approach(clawZ, aimZ, step)
                if (abs(clawX - aimX) < 0.03f && abs(clawZ - aimZ) < 0.03f) phase = ClawPhase.AIMING
            }

            ClawPhase.OVER -> Unit
        }

        held?.let { toy ->
            toy.x = clawX
            toy.z = clawZ
            toy.y = clawY - toy.kind.radius * 0.55f
            toy.yaw += dt * 34f
        }
    }

    private fun drainGrip(dt: Float) {
        val toy = held ?: return
        grip -= (0.08f + toy.kind.weight * cabinet.slip) * dt
        if (grip <= 0f) {
            grip = 0f
            slips++
            flawless = false
            releaseHeld(0.4f)
            queue.addLast(ClawEvent.SLIP)
        }
    }

    private fun releaseHeld(spread: Float) {
        val toy = held ?: return
        toy.held = false
        toy.vy = -0.4f
        toy.vx = (rng.nextFloat() - 0.5f) * spread
        toy.vz = (rng.nextFloat() - 0.5f) * spread
        held = null
    }

    private fun attemptGrab() {
        var best: Toy? = null
        var bestDistance = Float.MAX_VALUE
        toys.forEach { toy ->
            if (toy.gone || toy.held) return@forEach
            if (abs(toy.y - clawY) > toy.kind.radius + 0.34f) return@forEach
            val distance = hypot(toy.x - clawX, toy.z - clawZ)
            if (distance <= toy.kind.radius + 0.20f && distance < bestDistance) {
                bestDistance = distance
                best = toy
            }
        }
        val target = best
        if (target == null) {
            misses++
            flawless = false
            queue.addLast(ClawEvent.MISS)
            return
        }
        target.held = true
        target.vx = 0f
        target.vy = 0f
        target.vz = 0f
        held = target
        grip = startGrip
        queue.addLast(ClawEvent.GRAB)
    }

    private fun descendTarget(): Float {
        var top = 0.14f
        posts.forEach { post ->
            if (hypot(post.x - clawX, post.z - clawZ) < Cab.POST_R + 0.17f) {
                top = max(top, Cab.POST_TOP)
            }
        }
        toys.forEach { toy ->
            if (toy.gone || toy.held) return@forEach
            if (hypot(toy.x - clawX, toy.z - clawZ) < toy.kind.radius + 0.14f) {
                top = max(top, toy.y + toy.kind.radius * 0.25f)
            }
        }
        return top + 0.10f
    }

    private fun bank(toy: Toy) {
        toy.gone = true
        banked++
        points += toy.kind.value
        bankedKinds[toy.kind] = (bankedKinds[toy.kind] ?: 0) + 1
        queue.addLast(ClawEvent.BANK)
    }

    private fun inChute(toy: Toy): Boolean =
        hypot(toy.x - Cab.CHUTE_X, toy.z - Cab.CHUTE_Z) < chuteRadius

    private fun physics(dt: Float) {
        toys.forEach { toy ->
            if (toy.gone || toy.held) return@forEach
            toy.vy -= 9.2f * dt
            toy.x += toy.vx * dt
            toy.y += toy.vy * dt
            toy.z += toy.vz * dt
            val radius = toy.kind.radius
            val overChute = inChute(toy)
            if (!overChute) {
                toy.falling = false
            } else if (!toy.falling && toy.y - radius > Cab.RIM) {
                toy.falling = true
            }
            if (overChute && !toy.falling) fendOffRim(toy)
            if (toy.y < radius) {
                if (overChute && toy.falling) {
                    toy.vx *= 0.9f
                    toy.vz *= 0.9f
                } else {
                    toy.y = radius
                    if (toy.vy < 0f) toy.vy = -toy.vy * 0.16f
                    toy.vx *= 0.70f
                    toy.vz *= 0.70f
                }
            }
            if (toy.x < -Cab.HALF_X + radius) {
                toy.x = -Cab.HALF_X + radius
                toy.vx = -toy.vx * 0.28f
            }
            if (toy.x > Cab.HALF_X - radius) {
                toy.x = Cab.HALF_X - radius
                toy.vx = -toy.vx * 0.28f
            }
            if (toy.z < -Cab.HALF_Z + radius) {
                toy.z = -Cab.HALF_Z + radius
                toy.vz = -toy.vz * 0.28f
            }
            if (toy.z > Cab.HALF_Z - radius) {
                toy.z = Cab.HALF_Z - radius
                toy.vz = -toy.vz * 0.28f
            }
            toy.vx *= 0.985f
            toy.vz *= 0.985f
            fendOffPosts(toy)
        }
        separate()
    }

    private fun fendOffRim(toy: Toy) {
        val dx = toy.x - Cab.CHUTE_X
        val dz = toy.z - Cab.CHUTE_Z
        val minimum = chuteRadius + toy.kind.radius * 0.35f
        val distance = hypot(dx, dz)
        if (distance >= minimum) return
        if (distance < 1e-4f) {
            toy.x += minimum
            return
        }
        val push = minimum - distance
        toy.x += dx / distance * push
        toy.z += dz / distance * push
        toy.vx += dx / distance * push * 1.2f
        toy.vz += dz / distance * push * 1.2f
    }

    private fun fendOffPosts(toy: Toy) {
        if (posts.isEmpty() || toy.y > Cab.POST_TOP) return
        posts.forEach { post ->
            val dx = toy.x - post.x
            val dz = toy.z - post.z
            val minimum = Cab.POST_R + toy.kind.radius
            val distance = hypot(dx, dz)
            if (distance >= minimum) return@forEach
            if (distance < 1e-4f) {
                toy.x += minimum
                return@forEach
            }
            val push = minimum - distance
            toy.x += dx / distance * push
            toy.z += dz / distance * push
            toy.vx += dx / distance * push * 1.4f
            toy.vz += dz / distance * push * 1.4f
        }
    }

    private fun separate() {
        for (i in toys.indices) {
            val a = toys[i]
            if (a.gone) continue
            for (j in i + 1 until toys.size) {
                val b = toys[j]
                if (b.gone) continue
                if (a.held && b.held) continue
                val dx = b.x - a.x
                val dy = b.y - a.y
                val dz = b.z - a.z
                val minimum = a.kind.radius + b.kind.radius
                val distance = sqrt(dx * dx + dy * dy + dz * dz)
                if (distance >= minimum || distance < 1e-4f) continue
                val push = (minimum - distance) * 0.5f
                val nx = dx / distance
                val ny = dy / distance
                val nz = dz / distance
                if (!a.held) {
                    a.x -= nx * push
                    a.y -= ny * push
                    a.z -= nz * push
                    a.vx -= nx * push * 1.6f
                    a.vz -= nz * push * 1.6f
                    if (a.y < a.kind.radius && !inChute(a)) a.y = a.kind.radius
                }
                if (!b.held) {
                    b.x += nx * push
                    b.y += ny * push
                    b.z += nz * push
                    b.vx += nx * push * 1.6f
                    b.vz += nz * push * 1.6f
                    if (b.y < b.kind.radius && !inChute(b)) b.y = b.kind.radius
                }
            }
        }
    }

    private fun approach(value: Float, target: Float, step: Float): Float {
        val delta = target - value
        return if (abs(delta) <= step) target else value + step * (if (delta > 0f) 1f else -1f)
    }
}
