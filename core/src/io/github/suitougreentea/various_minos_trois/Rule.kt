package io.github.suitougreentea.various_minos_trois

import com.badlogic.gdx.graphics.Color

object RuleList {
  val list = listOf(
      Rule("Modern", "回転の柔軟な、標準的なルールです。", FontColor.YELLOW),
      Rule("Variant", "回転がより厳しいですが、操作しやすいルールです。", FontColor.RED),
      Rule("Classic", "Variantをより厳しくしたルールです。", FontColor.GREEN),
      Rule("Retro", "回転補正のない、昔ながらのルールです。", FontColor.BLUE)
  )
}

class Rule(val name: String, val description: String, val color: Color)