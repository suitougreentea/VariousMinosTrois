package io.github.suitougreentea.various_minos_trois

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.Input

class Input {
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
    val mapping = mapOf(
            Pair(left, arrayOf<InputType>(InputTypeKeyboard(Input.Keys.LEFT))),
            Pair(right, arrayOf<InputType>(InputTypeKeyboard(Input.Keys.RIGHT))),
            Pair(up, arrayOf<InputType>(InputTypeKeyboard(Input.Keys.UP))),
            Pair(down, arrayOf<InputType>(InputTypeKeyboard(Input.Keys.DOWN))),
            Pair(a, arrayOf<InputType>(InputTypeKeyboard(Input.Keys.Z))),
            Pair(b, arrayOf<InputType>(InputTypeKeyboard(Input.Keys.X))),
            Pair(c, arrayOf<InputType>(InputTypeKeyboard(Input.Keys.C))),
            Pair(d, arrayOf<InputType>(InputTypeKeyboard(Input.Keys.A))),
            Pair(e, arrayOf<InputType>(InputTypeKeyboard(Input.Keys.S))),
            Pair(f, arrayOf<InputType>(InputTypeKeyboard(Input.Keys.D)))
    )

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
}

class InputButton(val name: String) {
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