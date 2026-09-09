package com.mirvo.tazlen.plushcade.ui.scene

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import com.google.android.filament.Engine
import com.google.android.filament.View
import com.mirvo.tazlen.plushcade.game.levels.Cabinets
import com.mirvo.tazlen.plushcade.game.logic.Cab
import com.mirvo.tazlen.plushcade.game.logic.ClawEngine
import com.mirvo.tazlen.plushcade.game.model.PrizeKind
import io.github.sceneview.SceneView
import io.github.sceneview.SurfaceType
import io.github.sceneview.loaders.ModelLoader
import io.github.sceneview.math.Position
import io.github.sceneview.math.Rotation
import io.github.sceneview.math.Scale
import io.github.sceneview.model.ModelInstance
import io.github.sceneview.node.ModelNode
import io.github.sceneview.rememberCameraNode
import io.github.sceneview.rememberEngine
import io.github.sceneview.rememberMainLightNode
import io.github.sceneview.rememberModelLoader
import io.github.sceneview.rememberView
import kotlin.math.sin

private const val TILE_SIZE = 0.52f
private const val TILE_COLS = 3
private const val TILE_ROWS = 3
private const val TILE_THICK = 0.048f
private const val CLAW_SIZE = 0.66f
private const val CLAW_BASE = CLAW_SIZE / 1.903f
private const val CHUTE_SIZE = 0.78f
private const val RING_SIZE = 0.36f
private const val RAIL_LENGTH = 1.54f
private const val WALL_HEIGHT = 1.98f
private const val PANEL_COUNT = 6
private const val MAX_POSTS = 3
private const val CAM_BACK = 3.45f
private const val CAM_HEIGHT = 2.45f

private class ClawPool {
    val tiles = ArrayList<ModelNode>()
    val trims = ArrayList<ModelNode>()
    val panels = ArrayList<ModelNode>()
    val pillars = ArrayList<ModelNode>()
    val toys = HashMap<PrizeKind, ArrayList<ModelNode>>()
    var claw: ModelNode? = null
    var rail: ModelNode? = null
    var laid = false
    var clock = 0L
    var drift = 0f
}

@Composable
fun CabinetScene(
    engine: ClawEngine,
    isPaused: () -> Boolean,
    onStep: (Float) -> Unit,
    modifier: Modifier = Modifier,
) {
    val filament = rememberEngine(engineCreator = { egl ->
        Engine.Builder()
            .sharedContext(egl)
            .feature("backend.disable_parallel_shader_compile", true)
            .build()
    })
    val modelLoader = rememberModelLoader(filament)
    val pool = remember { ClawPool() }
    val camera = rememberCameraNode(filament)
    val mainLight = rememberMainLightNode(filament) { intensity = 124_000f }
    val bloomOff = remember { View.BloomOptions().apply { enabled = false } }
    val dynamicOff = remember { View.DynamicResolutionOptions().apply { enabled = false } }
    val view = rememberView(filament).apply {
        isPostProcessingEnabled = false
        bloomOptions = bloomOff
        dynamicResolutionOptions = dynamicOff
        setShadowingEnabled(false)
        setScreenSpaceRefractionEnabled(false)
    }

    SceneView(
        modifier = modifier,
        engine = filament,
        modelLoader = modelLoader,
        view = view,
        isOpaque = false,
        surfaceType = SurfaceType.TextureSurface,
        cameraNode = camera,
        mainLightNode = mainLight,
        onFrame = { nanos ->
            view.isPostProcessingEnabled = false
            view.bloomOptions = bloomOff
            view.dynamicResolutionOptions = dynamicOff
            view.setShadowingEnabled(false)
            view.setScreenSpaceRefractionEnabled(false)

            val raw = if (pool.clock == 0L) 0f else (nanos - pool.clock) / 1_000_000_000f
            pool.clock = nanos
            val dt = raw.coerceIn(0f, 0.05f)
            pool.drift += dt
            if (!isPaused()) onStep(dt)
            layout(engine, pool)
            sync(engine, pool)
            val sway = sin(pool.drift * 0.22f) * 0.06f
            camera.position = Position(sway, CAM_HEIGHT, CAM_BACK)
            camera.lookAt(Position(sway * 0.4f, 0.33f, 0f))
        },
        content = {
            build(modelLoader, "models/floor.glb", TILE_COLS * TILE_ROWS, pool.tiles, TILE_SIZE)
            build(modelLoader, "models/panel.glb", PANEL_COUNT, pool.panels, WALL_HEIGHT)
            build(modelLoader, "models/chute.glb", 2, pool.trims, CHUTE_SIZE)
            build(modelLoader, "models/post.glb", MAX_POSTS, pool.pillars, 0.80f)
            PrizeKind.entries.forEach { kind ->
                val slots = pool.toys.getOrPut(kind) { ArrayList() }
                build(modelLoader, kind.asset, Cabinets.poolSize(kind), slots, kind.size)
            }
            val railInstances = remember { modelLoader.createInstancedModel("models/post.glb", 1) }
            railInstances.forEach { instance ->
                ModelNode(
                    modelInstance = instance,
                    scaleToUnits = RAIL_LENGTH,
                    isVisible = false,
                    apply = { pool.rail = this },
                )
            }
            val clawInstances = remember { modelLoader.createInstancedModel("models/claw.glb", 1) }
            clawInstances.forEach { instance ->
                ModelNode(
                    modelInstance = instance,
                    scaleToUnits = CLAW_SIZE,
                    apply = { pool.claw = this },
                )
            }
        },
    )
}

@Composable
private fun io.github.sceneview.SceneScope.build(
    modelLoader: ModelLoader,
    path: String,
    count: Int,
    out: ArrayList<ModelNode>,
    scaleToUnits: Float,
) {
    val instances: List<ModelInstance> = remember(path, count) {
        modelLoader.createInstancedModel(path, count)
    }
    instances.forEach { instance ->
        ModelNode(
            modelInstance = instance,
            scaleToUnits = scaleToUnits,
            isVisible = false,
            apply = { out.add(this) },
        )
    }
}

private fun layout(engine: ClawEngine, pool: ClawPool) {
    if (pool.laid || pool.tiles.size < TILE_COLS * TILE_ROWS || pool.panels.size < PANEL_COUNT) return

    val originX = -(TILE_COLS - 1) * TILE_SIZE / 2f
    val originZ = -(TILE_ROWS - 1) * TILE_SIZE / 2f
    for (index in 0 until TILE_COLS * TILE_ROWS) {
        val node = pool.tiles[index]
        node.position = Position(
            originX + (index % TILE_COLS) * TILE_SIZE,
            -TILE_THICK * 0.5f,
            originZ + (index / TILE_COLS) * TILE_SIZE,
        )
        node.isVisible = true
    }

    val tall = WALL_HEIGHT / 1.903f
    val wide = 1.02f / 1.018f
    val thin = 0.055f / 0.658f
    val midY = WALL_HEIGHT * 0.5f
    listOf(-0.39f, 0.39f).forEachIndexed { index, offset ->
        val node = pool.panels[index]
        node.position = Position(offset, midY, -Cab.HALF_Z - 0.05f)
        node.rotation = Rotation(0f, 0f, 0f)
        node.scale = Scale(wide * 0.82f, tall, thin)
        node.isVisible = true
    }
    var cursor = 2
    listOf(-Cab.HALF_X - 0.05f, Cab.HALF_X + 0.05f).forEach { side ->
        listOf(-0.30f, 0.30f).forEach { depth ->
            if (cursor >= pool.panels.size) return@forEach
            val node = pool.panels[cursor++]
            node.position = Position(side, midY, depth)
            node.rotation = Rotation(0f, 90f, 0f)
            node.scale = Scale(0.66f, tall, thin)
            node.isVisible = true
        }
    }

    pool.rail?.let { node ->
        val base = RAIL_LENGTH / 1.903f
        node.position = Position(0f, Cab.TOP_Y + CLAW_SIZE + 0.06f, 0f)
        node.rotation = Rotation(0f, 0f, 90f)
        node.scale = Scale(base * 0.34f, base, base * 0.34f)
        node.isVisible = true
    }

    if (pool.trims.size >= 2) {
        val chute = pool.trims[0]
        chute.position = Position(Cab.CHUTE_X, 0.03f, Cab.CHUTE_Z)
        chute.isVisible = true
    }
    engine.posts.forEachIndexed { index, post ->
        if (index >= pool.pillars.size) return@forEachIndexed
        val node = pool.pillars[index]
        node.position = Position(post.x, 0.38f, post.z)
        node.isVisible = true
    }
    pool.laid = true
}

private fun sync(engine: ClawEngine, pool: ClawPool) {
    val cursors = HashMap<PrizeKind, Int>()
    engine.toys.forEach { toy ->
        if (toy.gone) return@forEach
        val slots = pool.toys[toy.kind] ?: return@forEach
        val cursor = cursors.getOrDefault(toy.kind, 0)
        if (cursor >= slots.size) return@forEach
        cursors[toy.kind] = cursor + 1
        val node = slots[cursor]
        node.position = Position(toy.x, toy.y + toy.kind.lift, toy.z)
        node.rotation = if (toy.kind.flat) {
            Rotation(-14f, 0f, toy.yaw)
        } else {
            Rotation(0f, toy.yaw, 0f)
        }
        node.isVisible = true
    }
    pool.toys.forEach { (kind, slots) ->
        val used = cursors.getOrDefault(kind, 0)
        for (index in used until slots.size) slots[index].isVisible = false
    }

    pool.claw?.let { node ->
        node.position = Position(engine.clawX, engine.clawY + CLAW_SIZE * 0.5f, engine.clawZ)
        val squeeze = 0.52f + engine.prongs * 0.48f
        node.scale = Scale(CLAW_BASE * squeeze, CLAW_BASE, CLAW_BASE)
    }

    if (pool.trims.size >= 2) {
        val ring = pool.trims[1]
        val show = !engine.over
        ring.isVisible = show
        if (show) {
            ring.position = Position(engine.clawX, 0.022f, engine.clawZ)
            val base = RING_SIZE / 1.901f
            ring.scale = Scale(base, base, base)
        }
    }
}
