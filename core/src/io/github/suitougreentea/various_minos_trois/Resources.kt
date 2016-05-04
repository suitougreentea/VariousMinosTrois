package io.github.suitougreentea.various_minos_trois

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.audio.Sound
import com.badlogic.gdx.files.FileHandle
import com.badlogic.gdx.graphics.Texture
import com.badlogic.gdx.graphics.g2d.BitmapFont
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator

class Resources {
    val design = loadTexture("design.png")
    val frame = loadTexture("frame.png")
    val block = loadTexture("block.png")
    val bomb = loadTexture("bomb.png")

    val debugFont14: BitmapFont
    init {
        val generator = FreeTypeFontGenerator(Gdx.files.internal("Inconsolata.otf"))
        val parameter = FreeTypeFontGenerator.FreeTypeFontParameter()
        parameter.size = 14
        debugFont14 = generator.generateFont(parameter)
        generator.dispose()
    }
    val font12: BitmapFont
    init {
        val generator = FreeTypeFontGenerator(Gdx.files.internal("Koruri-Regular.ttf"))
        val parameter = FreeTypeFontGenerator.FreeTypeFontParameter()
        parameter.size = 12
        font12 = generator.generateFont(parameter)
        generator.dispose()
    }

    val seLanding = loadSE("se/landing.wav")
    val seExplosionSmall = loadSE("se/explosion_small.wav")
    val seExplosionBig = loadSE("se/explosion_big.wav")
    val seHold = loadSE("se/hold.wav")
    val seInitRotation = loadSE("se/init_rotation.wav")
    val seRotation = loadSE("se/rotation.wav")
    val seRotationFail = loadSE("se/rotation_fail.wav")
    val seLock = loadSE("se/lock.wav")
    val seBigBomb = loadSE("se/big_bomb.wav")

    fun loadTexture(path: String): Texture {
        return Texture(path)
    }

    fun loadSE(path: String): Sound {
        return Gdx.audio.newSound(FileHandle(path))
    }
}