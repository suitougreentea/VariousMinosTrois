package io.github.suitougreentea.various_minos_trois.game.bomb

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.g2d.SpriteBatch
import com.badlogic.gdx.graphics.glutils.ShapeRenderer
import io.github.suitougreentea.various_minos_trois.*
import io.github.suitougreentea.various_minos_trois.game.*

class RendererBomb(app: VariousMinosTrois): BasicMinoRenderer(app) {
    override fun render(g: Game) {
        if(g !is GameBomb) return
        b.begin()

        renderBackground()
        renderFrame()
        renderField(g)
        renderNextHold(g)

        renderActiveMino(g)

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
            if(g.explosionTimer < g.speedBomb.explosion * 1/3) {
                val t = g.explosionTimer / (g.speedBomb.explosion * 1/3f)
                val w = (1f + (mw - 1f) * bombInterpolationA(t)) * 16f
                val h = (1f + (mh - 1f) * bombInterpolationA(t)) * 16f
                val dx = ox - w / 2
                val dy = oy - h / 2
                b.draw(r.tBomb, dx, dy, w, h, 0, 0, 512, 512, false, false)
            } else {
                val t = (g.explosionTimer - g.speedBomb.explosion * 1/3f) / (g.speedBomb.explosion * 2/3f)
                val a = bombInterpolationB(t)
                val w = mw * 16f
                val h = mh * 16f
                val dx = ox - w / 2
                val dy = oy - h / 2
                b.color = Color(1f, 1f, 1f, 1f - a)
                b.draw(r.tBomb, dx, dy, w, h, 0, 0, 512, 512, false, false)
                b.color = Color.WHITE
            }
        }

        g.bigBombList.forEach {
            val t = (g.bigBombTimer / g.speedBomb.bigBomb.toFloat() * 8).toInt().let { if(it >= 8) 7 else it }
            val (dx, dy) = getBlockCoord(it)
            b.draw(r.tBlock, dx, dy, 32f, 32f, t * 32, 64, 32, 32, false, false)
        }

        b.end()

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
            if(currentState is StateWithTimer) appendln("-> ${currentState.timer} / ${currentState.frames}") else appendln()
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
            appendln("bombed: ${g.bombedBlocks}")
        }
        r.fDebug14.draw(b, debugString, 400f, 584f)
        r.fDebug14.draw(b, "${Gdx.graphics.framesPerSecond} FPS", 16f, 584f)

        fun prettifyBoolean(boolean: Boolean) = if(boolean) "*" else "."
        r.fDebug14.draw(b, g.input.mapping.keys.map { e -> "%5s: %s %s %s".format(e.name, prettifyBoolean(e.isPressed), prettifyBoolean(e.isDown), prettifyBoolean(e.isReleased)) }.joinToString("\n"), 680f, 200f)

        if(g is GameBombSurvival) {
            fun formatLevel(level: Int) = (level / 100).toString() + "." + "%02d".format(level % 100)
            r.fDebug14.draw(b, "${formatLevel(g.level)}/${g.nextLevel / 100}", 354f, 100f)
        }

        b.end()

        g.seQueue.forEach {
            when(it) {
                "landing" -> r.sLanding.play()
                "explosion_small" -> r.sExplosionSmall.play()
                "explosion_big" -> r.sExplosionBig.play()
                "hold" -> r.sHold.play()
                "init_rotation" -> r.sInitRotation.play()
                "rotation" -> r.sRotation.play()
                "rotation_fail" -> r.sRotationFail.play()
                "lock" -> r.sLock.play()
                "big_bomb" -> r.sBigBomb.play()
                "count" -> {
                    val pitch = Math.pow(2.0, g.countNumber / 12.0).toFloat()
                    r.sCount.play(1f, pitch, 0f)
                }
                "cascade" -> r.sCascade.play()
            }
        }
        g.seQueue.clear()
    }

    override fun getBlockSourceCoord(b: Block) = when(b) {
        is BlockNormal -> Pair(b.color, if(false) 1 else 0)
        is BlockBomb -> if(b.ignited) Pair(0, 3) else Pair(0, 2)
        is BlockBigBomb -> when(b.position) {
            BlockBigBomb.Position.BOTTOM_LEFT -> if(b.ignited) Pair(3, 3) else Pair(1, 3)
            BlockBigBomb.Position.BOTTOM_RIGHT -> if(b.ignited) Pair(4, 3) else Pair(2, 3)
            BlockBigBomb.Position.TOP_LEFT -> if(b.ignited) Pair(3, 2) else Pair(1, 2)
            BlockBigBomb.Position.TOP_RIGHT -> if(b.ignited) Pair(4, 2) else Pair(2, 2)
        }
        is BlockWhite -> Pair(5 + b.level, if(false) 3 else 2)
        is BlockWhiteUnbreakable -> Pair(10, if(false) 3 else 2)
        is BlockBlack -> Pair(11 + b.level, if(false) 3 else 2)
        is BlockBlackUnbreakable -> Pair(16, if(false) 3 else 2)
        else -> Pair(0, 0)
    }

    fun bombInterpolationA(t: Float) = Math.pow(t.toDouble(), 1/3.0).toFloat()
    fun bombInterpolationB(t: Float) = t * t
}