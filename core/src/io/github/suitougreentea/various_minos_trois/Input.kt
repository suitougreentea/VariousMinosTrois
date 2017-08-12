package io.github.suitougreentea.various_minos_trois

import com.badlogic.gdx.Gdx
import com.beust.klaxon.*

class Input(jsonConfig: JsonObject) {
  val left = InputButton("LEFT")
  val right = InputButton("RIGHT")
  val up = InputButton("UP")
  val down = InputButton("DOWN")
  val a = InputButton("A")
  val b = InputButton("B")
  val c = InputButton("C")
  val d = InputButton("D")
  val e = InputButton("E")
  val f = InputButton("F")

  lateinit var mapping: Map<InputButton, Array<InputType>>/* = mapOf(
          left to arrayOf<InputType>(InputTypeKeyboard(Input.Keys.LEFT)),
          right to arrayOf<InputType>(InputTypeKeyboard(Input.Keys.RIGHT)),
          up to arrayOf<InputType>(InputTypeKeyboard(Input.Keys.UP)),
          down to arrayOf<InputType>(InputTypeKeyboard(Input.Keys.DOWN)),
          a to arrayOf<InputType>(InputTypeKeyboard(Input.Keys.Z)),
          b to arrayOf<InputType>(InputTypeKeyboard(Input.Keys.X)),
          c to arrayOf<InputType>(InputTypeKeyboard(Input.Keys.C)),
          d to arrayOf<InputType>(InputTypeKeyboard(Input.Keys.A)),
          e to arrayOf<InputType>(InputTypeKeyboard(Input.Keys.S)),
          f to arrayOf<InputType>(InputTypeKeyboard(Input.Keys.D))
  )*/
  init {
    jsonToMapping(jsonConfig)
  }

  fun update() {
    mapping.forEach { e ->
      e.key.isPressed = false
      e.key.isReleased = false
      val down = e.value.any { key -> key.isDown() }
      if(!e.key.isDown && down) e.key.isPressed = true
      if(e.key.isDown && !down) e.key.isReleased = true
      e.key.isDown = down
    }
  }

  fun mappingToJson() = json {
    obj(*mapping.map {
      it.key.jsonName to
          array(it.value.map { obj(
              *when(it) {
                is InputTypeKeyboard -> { arrayOf("type" to "keyboard", "code" to it.keyCode)}
                else -> throw IllegalArgumentException()
              }
          ) })
    }.toTypedArray())
  }

  fun jsonToMapping(json: JsonObject) {
    mapping = json.map {
      when(it.key) {
        "left" -> left
        "right" -> right
        "up" -> up
        "down" -> down
        "a" -> a
        "b" -> b
        "c" -> c
        "d" -> d
        "e" -> e
        "f" -> f
        else -> throw IllegalArgumentException()
      } to (it.value as JsonArray<JsonObject>).map {
        when(it.string("type")) {
          "keyboard" -> InputTypeKeyboard(it.int("code") ?: 0)
          else -> throw IllegalArgumentException()
        }
      }.toTypedArray<InputType>()
    }.toMap()
  }
}

class InputButton(val name: String) {
  val jsonName = name.toLowerCase()
  var isPressed: Boolean = false
  var isDown: Boolean = false
  var isReleased: Boolean = false
}

interface InputType {
  fun isDown(): Boolean
}

class InputTypeKeyboard(val keyCode: Int): InputType {
  override fun isDown() = Gdx.input.isKeyPressed(keyCode)
}