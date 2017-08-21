package io.github.suitougreentea.various_minos_trois

import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.g2d.SpriteBatch
import io.github.suitougreentea.various_minos_trois.game.BasicMinoGame
import io.github.suitougreentea.various_minos_trois.game.Game
import io.github.suitougreentea.various_minos_trois.game.bomb.GameBombDebug
import io.github.suitougreentea.various_minos_trois.game.bomb.GameBombSurvivalMaster1
import io.github.suitougreentea.various_minos_trois.game.bomb.GameBombSurvivalThanatos1
import io.github.suitougreentea.various_minos_trois.game.magic.GameMagic
import io.github.suitougreentea.various_minos_trois.game.magic.GameMagicDebug
import kotlin.reflect.KClass

object GameTypeList {
  private val bombGameModeList = listOf(
      GameMode("Qualification", "プレイの内容によって段位が認定されるモードです。", FontColor.WHITE,
          listOf(
              GameModeDetailed("bomb.debug", "Debug", "デバッグ用モードです。", FontColor.WHITE, AllowedRuleList(true, true, true, true)),
              GameModeDetailed("bomb.proson1", "Proson-α", "一番難易度の低いモードです。", FontColor.BLUE, AllowedRuleList(true, true, true, false)),
              GameModeDetailed("bomb.thanatos1", "Thanatos-α", "高速落下に耐えるモードです。", FontColor.RED, AllowedRuleList(true, true, true, false))
          )
      )
  )

  private val magicGameModeList = listOf(
      GameMode("Endless", "ミスしない限り、永遠に続くモードです。", FontColor.BLUE,
          listOf(
              GameModeDetailed("magic.debug", "Debug", "デバッグ用モードです。", FontColor.WHITE, AllowedRuleList(true, true, true, true))
          )
      )
  )

  val list = listOf(
      GameType("Bomb", "爆弾ブロックを爆発させてブロックを消すモードです。", FontColor.RED, bombGameModeList,
          { b, r ->
            b.draw(r.tBlockBomb, 0f, 0f, 16, 32, 32, 32)
          }),
      GameType("Magic", "同じ色のブロックでラインを揃えるとボーナスとなるモードです。", FontColor.BLUE, magicGameModeList,
          { b, r ->
            b.draw(r.tBlockMagic, 0f, 16f, 48, 48, 16, 16) // R
            b.draw(r.tBlockMagic, 16f, 16f, 240, 64, 16, 16) // G
            b.draw(r.tBlockMagic, 0f, 0f, 416, 80, 16, 16) // B
            b.draw(r.tBlockMagic, 16f, 0f, 80, 96, 16, 16) // K
          })
  )
  val num = list.size
}

class GameType(val name: String, val description: String, val color: Color, val gameModeList: List<GameMode>,
               val thumbnailRenderFunction: (SpriteBatch, Resources) -> Unit)

class GameMode(val name: String, val description: String, val color: Color, val gameModeDetailedList: List<GameModeDetailed>)

class GameModeDetailed(val internalName: String, val name: String, val description: String, val color: Color, val allowedRuleList: AllowedRuleList)

class AllowedRuleList(val modern: Boolean, val variant: Boolean, val classic: Boolean, val retro: Boolean)
