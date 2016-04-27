package io.github.suitougreentea.various_minos_trois

import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.g2d.SpriteBatch
import com.badlogic.gdx.graphics.glutils.ShapeRenderer


class Renderer(val game: VariousMinosTrois) {
    val b = SpriteBatch()
    val s = ShapeRenderer()
    init {
        s.projectionMatrix = b.projectionMatrix
        s.transformMatrix = b.transformMatrix
    }

    val tDesign = game.resources.design
    val tFrame = game.resources.frame
    val tBlock = game.resources.block
    val fDebug14 = game.resources.debugFont14

    val nextPositions = arrayOf(
            Pair(Pair(218, 468), 16),
            Pair(Pair(304, 472), 8),
            Pair(Pair(354, 472), 8),
            Pair(Pair(354, 440), 8),
            Pair(Pair(354, 408), 8),
            Pair(Pair(354, 376), 8)
        )
    val holdPosition = Pair(Pair(168, 472), 8)

    fun render(g: Game) {
        b.begin()

        b.color = Color(1f, 1f, 1f, 0.2f)
        b.draw(tDesign, 0f, 0f)
        b.color = Color.WHITE

        b.draw(tFrame, 152f, 72f)

        g.field.filter { it.key.y < 22 }.forEach {
            val (dx, dy) = getBlockCoord(it.key)
            renderBlock(b, it.value, dx, dy)
        }

        nextPositions.forEachIndexed { i, e ->
            if(g.nextMinos.size > i) {
                val (ox, oy) = e.first
                val size = e.second
                val mino = g.nextMinos.get(i)
                mino.blocks.forEach { f ->
                    val (bx, by) = f.first
                    val block = f.second
                    renderBlock(b, block, (ox + bx * size).toFloat(), (oy + by * size).toFloat(), size)
                }
            }
        }

        val holdMino = g.holdMino
        if(holdMino != null) {
            val (ox, oy) = holdPosition.first
            val size = holdPosition.second
            holdMino.blocks.forEach { f ->
                val (bx, by) = f.first
                val block = f.second
                renderBlock(b, block, (ox + bx * size).toFloat(), (oy + by * size).toFloat(), size)
            }
        }

        g.cascadeList.forEach {
            it.blocks.forEach {
                val (dx, dy) = getBlockCoord(it.first.x, it.first.y)
                renderBlock(b, it.second, dx, dy, t = 0.5f)
            }
        }

        b.end()

        if(g.currentState is Game.StateMoving) {
            b.begin()
            g.currentMino.getRotatedBlocks(g.minoR).forEach { e ->
                val (dx, dy) = getBlockCoord(g.minoX + e.first.x, g.ghostY + e.first.y)
                renderBlock(b, e.second, dx, dy, t = 0.5f)
            }

            g.currentMino.getRotatedBlocks(g.minoR).forEach { e ->
                val (dx, dy) = getBlockCoord(g.minoX + e.first.x, g.minoY + e.first.y)
                renderBlock(b, e.second, dx, dy)
            }

            b.end()

            s.begin(ShapeRenderer.ShapeType.Line)
            run {
                val mino = g.currentMino
                val (dx, dy) = getBlockCoord(g.minoX, g.minoY)
                s.color = Color.WHITE
                s.rect(dx, dy, mino.size * 16f, mino.size * 16f)
            }
            run {
                val mino = g.currentMino
                val rect = mino.getRotatedRectangle(g.minoR)
                val (dx, dy) = getBlockCoord(g.minoX + rect.first.x, g.minoY + rect.first.y)
                val w = rect.second.x - rect.first.x + 1
                val h = rect.second.y - rect.first.y + 1
                s.color = Color.PINK
                s.rect(dx, dy, w * 16f, h * 16f)
            }
            s.end()
        }

        s.begin(ShapeRenderer.ShapeType.Line)
        g.explosionList.forEach {
            val (cx, cy) = it.position
            if(it.size == -1) {
                val (dx, dy) = getBlockCoord(cx - 4, cy - 4)
                val w = 10
                val h = 10
                s.color = Color.RED
                s.rect(dx, dy, w * 16f, h * 16f)
            } else {
                val (bw, bh) = g.bombSize.get(it.size)
                val (dx, dy) = getBlockCoord(cx - bw, cy - bh)
                val w = bw * 2 + 1
                val h = bh * 2 + 1
                s.color = Color.RED
                s.rect(dx, dy, w * 16f, h * 16f)
            }
        }
        s.end()

        b.begin()

        val currentState = g.currentState

        val debugString = buildString {
            appendln(currentState.javaClass.simpleName)
            if(currentState is Game.StateWithTimer) appendln("-> ${currentState.timer} / ${currentState.frames}") else appendln()
            appendln("mino: ${g.minoId}")
            appendln("x: ${g.minoX}")
            appendln("y: ${g.minoY}")
            appendln("r: ${g.minoR}")
            appendln("moveDir: ${g.moveDirection}")
            appendln("moveTimer: ${g.moveTimer}")
            appendln("moveStack: ${g.moveStack}")
            appendln("dropStack: ${g.dropStack}")
            appendln("softStack: ${g.softDropStack}")
            appendln("lock: ${g.lockTimer}")
            appendln("forceLock: ${g.forceLockTimer}")
            appendln("explosion: ${g.explosionTimer}")
            appendln("cascade: ${g.cascadeStack}")
            appendln("chain: ${g.chain}")
            appendln("expSize: ${g.currentExplosionSize}")
        }
        fDebug14.draw(b, debugString, 400f, 584f)

        fun prettifyBoolean(boolean: Boolean) = if(boolean) "*" else "."
        fDebug14.draw(b, g.input.mapping.keys.map { e -> "%5s: %s %s %s".format(e.name, prettifyBoolean(e.isPressed), prettifyBoolean(e.isDown), prettifyBoolean(e.isReleased)) }.joinToString("\n"), 680f, 200f)
        b.end()
    }

    fun renderBlock(batch: SpriteBatch, b: Block, x: Float, y: Float, s: Int = 16, t: Float = 1f) {
        batch.setColor(1f, 1f, 1f, t)
        val (sx, sy) = getBlockSourceCoord(b)
        batch.draw(tBlock, x, y, s.toFloat(), s.toFloat(), sx * 16, sy * 16, 16, 16, false, false)
        batch.color = Color.WHITE
    }

    fun getBlockSourceCoord(b: Block) = when(b) {
        is BlockNormal -> Pair(b.color, 0)
        is BlockBomb -> if(b.ignited) Pair(0, 3) else Pair(0, 2)
        is BlockBigBomb -> when(b.position) {
            BlockBigBomb.Position.BOTTOM_LEFT -> if(b.ignited) Pair(3, 3) else Pair(1, 3)
            BlockBigBomb.Position.BOTTOM_RIGHT -> if(b.ignited) Pair(4, 3) else Pair(2, 3)
            BlockBigBomb.Position.TOP_LEFT -> if(b.ignited) Pair(3, 2) else Pair(1, 2)
            BlockBigBomb.Position.TOP_RIGHT -> if(b.ignited) Pair(4, 2) else Pair(2, 2)
        }
        else -> Pair(0, 0)
    }

    fun getBlockCoord(x: Int, y: Int) = Pair(168f + x * 16f, 88f + y * 16f)

    fun getBlockCoord(c: Pos) = getBlockCoord(c.x, c.y)
}