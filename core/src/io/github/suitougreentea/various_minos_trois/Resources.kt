package io.github.suitougreentea.various_minos_trois

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.graphics.Texture
import com.badlogic.gdx.graphics.g2d.BitmapFont
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator

class Resources {
    val design = loadTexture("design.png")
    val frame = loadTexture("frame.png")
    val block = loadTexture("block.png")

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

    fun loadTexture(path: String): Texture {
        return Texture(path)
    }
}