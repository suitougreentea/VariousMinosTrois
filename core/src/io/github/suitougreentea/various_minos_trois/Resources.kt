package io.github.suitougreentea.various_minos_trois

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.audio.Music
import com.badlogic.gdx.audio.Sound
import com.badlogic.gdx.files.FileHandle
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.Texture
import com.badlogic.gdx.graphics.g2d.BitmapFont
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator

class Resources {
  val tDesign = loadTexture("design.png")
  val tFrame = loadTexture("frame.png")
  val tBlock = loadTexture("block.png")
  val tBomb = loadTexture("bomb.png")
  val tGrade = loadTexture("grade.png")
  val tNextBackground = loadTexture("next_background.png")

  val tBackgrounds = (0..19).map { loadTexture("bg${it}.png") }

  val fDebug14: BitmapFont
  init {
    val generator = FreeTypeFontGenerator(Gdx.files.internal("Inconsolata.otf"))
    val parameter = FreeTypeFontGenerator.FreeTypeFontParameter()
    parameter.size = 14
    fDebug14 = generator.generateFont(parameter)
    generator.dispose()
  }
  val f12: BitmapFont
  init {
    val generator = FreeTypeFontGenerator(Gdx.files.internal("Koruri-Regular.ttf"))
    val parameter = FreeTypeFontGenerator.FreeTypeFontParameter()
    parameter.size = 12
    f12 = generator.generateFont(parameter)
    generator.dispose()
  }
  val fRoman = BitmapFont(FileHandle("./roman.fnt"))
  val fNum24 = BitmapFont(FileHandle("./num24x32.fnt"))
  val fNum16 = BitmapFont(FileHandle("./num16x24.fnt"))
  val fNum12 = BitmapFont(FileHandle("./num12x16.fnt"))

  val sLanding = loadSE("se/landing.wav")
  val sExplosionSmall = loadSE("se/explosion_small.wav")
  val sExplosionBig = loadSE("se/explosion_big.wav")
  val sHold = loadSE("se/hold.wav")
  val sInitRotation = loadSE("se/init_rotation.wav")
  val sRotation = loadSE("se/rotation.wav")
  val sRotationFail = loadSE("se/rotation_fail.wav")
  val sLock = loadSE("se/lock.wav")
  val sBigBomb = loadSE("se/big_bomb.wav")
  val sCount = loadSE("se/count.wav")
  val sCascade = loadSE("se/cascade.wav")

  val mPlay1 = loadBGM("bgm/play1.ogg")

  fun loadTexture(path: String): Texture {
    return Texture(path)
  }

  fun loadSE(path: String): Sound {
    return Gdx.audio.newSound(FileHandle(path))
  }

  fun loadBGM(path: String): Music {
    return Gdx.audio.newMusic(FileHandle(path))
  }
}