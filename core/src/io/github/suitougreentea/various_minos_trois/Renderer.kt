package io.github.suitougreentea.various_minos_trois

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.g2d.SpriteBatch
import com.badlogic.gdx.graphics.glutils.ShapeRenderer
import com.badlogic.gdx.math.MathUtils


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
    val tBomb = game.resources.bomb
    val fDebug14 = game.resources.debugFont14

    val seLanding = game.resources.seLanding
    val seExplosionSmall = game.resources.seExplosionSmall
    val seExplosionBig = game.resources.seExplosionBig
    val seHold = game.resources.seHold
    val seInitRotation = game.resources.seInitRotation
    val seRotation = game.resources.seRotation
    val seRotationFail = game.resources.seRotationFail
    val seLock = game.resources.seLock
    val seBigBomb = game.resources.seBigBomb
    val seCount = game.resources.seCount
    val seCascade = game.resources.seCascade

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

        g.field.map.filter { it.key.y < 22 }.forEach {
            val (dx, dy) = getBlockCoord(it.key)
            val (sx, sy) = getBlockSourceCoord(it.value, g.freezedLines.contains(it.key.y))
            renderBlock(b, sx, sy, dx, dy)
        }

        nextPositions.forEachIndexed { i, e ->
            if(g.minoGenerator.size > i) {
                val (ox, oy) = e.first
                val size = e.second
                val mino = g.minoGenerator[i]
                if(mino != null) {
                    mino.blocks.forEach {
                        val (bx, by) = it.first
                        val block = it.second
                        renderBlock(b, block, (ox + bx * size).toFloat(), (oy + by * size).toFloat(), size)
                    }
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
                renderBlock(b, it.second, dx, dy)
            }
        }

        g.explosionList.forEach {
            val (ox, oy) = if(it.size == -1) getBlockCoord(it.position).let { Pair(it.first + 16f, it.second + 16f) }
            else getBlockCoord(it.position).let { Pair(it.first + 8f, it.second + 8f) }

            val (mw, mh) = when(it.size) {
                -1 -> Pair(10, 10)
                else -> g.bombSize[it.size].let { Pair(it.first * 2 + 1, it.second * 2 + 1)}
            }
            if(g.explosionTimer < g.speed.explosion * 1/3) {
                val t = g.explosionTimer / (g.speed.explosion * 1/3f)
                val w = (1f + (mw - 1f) * bombInterpolationA(t)) * 16f
                val h = (1f + (mh - 1f) * bombInterpolationA(t)) * 16f
                val dx = ox - w / 2
                val dy = oy - h / 2
                b.draw(tBomb, dx, dy, w, h, 0, 0, 512, 512, false, false)
            } else {
                val t = (g.explosionTimer - g.speed.explosion * 1/3f) / (g.speed.explosion * 2/3f)
                val a = bombInterpolationB(t)
                val w = mw * 16f
                val h = mh * 16f
                val dx = ox - w / 2
                val dy = oy - h / 2
                b.color = Color(1f, 1f, 1f, 1f - a)
                b.draw(tBomb, dx, dy, w, h, 0, 0, 512, 512, false, false)
                b.color = Color.WHITE
            }
        }

        g.bigBombList.forEach {
            val t = (g.bigBombTimer / g.speed.bigBomb.toFloat() * 8).toInt().let { if(it >= 8) 7 else it }
            val (dx, dy) = getBlockCoord(it)
            b.draw(tBlock, dx, dy, 32f, 32f, t * 32, 64, 32, 32, false, false)
        }

        b.end()

        if(g.stateManager.currentState is Game.StateMoving) {
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

            /*
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
            */
        }

        s.begin(ShapeRenderer.ShapeType.Filled)
        g.countLines.reversed().forEachIndexed { i, e ->
            if(i <= g.countLinesIndex) {
                val (dx, dy) = getBlockCoord(0, e).let { Pair(it.first, it.second + 8f) }
                s.color = Color.WHITE
                s.rect(dx, dy, 160f, 2f)
            }
        }
        s.end()

        b.begin()

        val currentState = g.stateManager.currentState

        val debugString = buildString {
            appendln(currentState.javaClass.simpleName)
            if(currentState is Game.StateWithTimer) appendln("-> ${currentState.timer} / ${currentState.frames}") else appendln()
            appendln("mino: ${g.currentMino.minoId}")
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
            appendln("bigBomb: ${g.bigBombTimer}")
            appendln("chain: ${g.chain}")
            appendln("count: ${g.countTimer}")
            appendln("countLinesIndex: ${g.countLinesIndex}")
            appendln("countNumber: ${g.countNumber}")
            appendln("expSize: ${g.currentExplosionSize}")
        }
        fDebug14.draw(b, debugString, 400f, 584f)
        fDebug14.draw(b, "${Gdx.graphics.framesPerSecond} FPS", 16f, 584f)

        fun prettifyBoolean(boolean: Boolean) = if(boolean) "*" else "."
        fDebug14.draw(b, g.input.mapping.keys.map { e -> "%5s: %s %s %s".format(e.name, prettifyBoolean(e.isPressed), prettifyBoolean(e.isDown), prettifyBoolean(e.isReleased)) }.joinToString("\n"), 680f, 200f)
        b.end()

        g.seQueue.forEach {
            when(it) {
                "landing" -> seLanding.play()
                "explosion_small" -> seExplosionSmall.play()
                "explosion_big" -> seExplosionBig.play()
                "hold" -> seHold.play()
                "init_rotation" -> seInitRotation.play()
                "rotation" -> seRotation.play()
                "rotation_fail" -> seRotationFail.play()
                "lock" -> seLock.play()
                "big_bomb" -> seBigBomb.play()
                "count" -> {
                    val pitch = Math.pow(2.0, g.countNumber / 12.0).toFloat()
                    seCount.play(1f, pitch, 0f)
                }
                "cascade" -> seCascade.play()
            }
        }
        g.seQueue.clear()
    }

    fun renderBlock(batch: SpriteBatch, b: Block, x: Float, y: Float, s: Int = 16, t: Float = 1f) {
        val (sx, sy) = getBlockSourceCoord(b)
        renderBlock(batch, sx, sy, x, y, s, t)
    }

    fun renderBlock(batch: SpriteBatch, sx: Int, sy: Int, x: Float, y: Float, s: Int = 16, t: Float = 1f) {
        batch.setColor(1f, 1f, 1f, t)
        batch.draw(tBlock, x, y, s.toFloat(), s.toFloat(), sx * 16, sy * 16, 16, 16, false, false)
        batch.color = Color.WHITE
    }

    fun getBlockSourceCoord(b: Block, freeze: Boolean = false) = when(b) {
        is BlockNormal -> Pair(b.color, if(freeze) 1 else 0)
        is BlockBomb -> if(b.ignited) Pair(0, 3) else Pair(0, 2)
        is BlockBigBomb -> when(b.position) {
            BlockBigBomb.Position.BOTTOM_LEFT -> if(b.ignited) Pair(3, 3) else Pair(1, 3)
            BlockBigBomb.Position.BOTTOM_RIGHT -> if(b.ignited) Pair(4, 3) else Pair(2, 3)
            BlockBigBomb.Position.TOP_LEFT -> if(b.ignited) Pair(3, 2) else Pair(1, 2)
            BlockBigBomb.Position.TOP_RIGHT -> if(b.ignited) Pair(4, 2) else Pair(2, 2)
        }
        is BlockWhite -> Pair(5 + b.level, if(freeze) 3 else 2)
        is BlockWhiteUnbreakable -> Pair(10, if(freeze) 3 else 2)
        is BlockBlack -> Pair(11 + b.level, if(freeze) 3 else 2)
        is BlockBlackUnbreakable -> Pair(16, if(freeze) 3 else 2)
        else -> Pair(0, 0)
    }

    fun bombInterpolationA(t: Float) = Math.pow(t.toDouble(), 1/3.0).toFloat()
    fun bombInterpolationB(t: Float) = t * t

    fun getBlockCoord(x: Int, y: Int) = Pair(168f + x * 16f, 88f + y * 16f)

    fun getBlockCoord(c: Pos) = getBlockCoord(c.x, c.y)
}