package io.github.suitougreentea.various_minos_trois

import com.badlogic.gdx.graphics.Color

object RuleList {
  val list = listOf(
      RuleEntry("modern", "Modern", "回転の柔軟な、標準的なルールです。", FontColor.YELLOW),
      RuleEntry("variant", "Variant", "回転がより厳しいですが、操作しやすいルールです。", FontColor.RED),
      RuleEntry("classic", "Classic", "Variantをより厳しくしたルールです。", FontColor.GREEN),
      RuleEntry("retro", "Retro", "回転補正のない、昔ながらのルールです。", FontColor.BLUE)
  )
}

class RuleEntry(val internalName: String, val name: String, val description: String, val color: Color)