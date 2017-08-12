package io.github.suitougreentea.various_minos_trois

import com.badlogic.gdx.graphics.g2d.SpriteBatch
import com.badlogic.gdx.graphics.glutils.ShapeRenderer
import com.badlogic.gdx.math.Matrix4
import io.github.suitougreentea.util.Stack

class RenderTool {
  val spriteBatch = SpriteBatch()
  val shapeRenderer = ShapeRenderer()
  val transformStack = Stack<Matrix4>(mutableListOf())

  var transformMatrix = Matrix4()
  private set

  init {
    spriteBatch.transformMatrix = transformMatrix
    shapeRenderer.transformMatrix = transformMatrix
  }

  fun setTransform(matrix: Matrix4) {
    transformMatrix = matrix.cpy() // TODO: cpy() is needed by somewhat wrong manipulation of matrix
    spriteBatch.transformMatrix = transformMatrix
    shapeRenderer.transformMatrix = transformMatrix
  }

  fun pushTransform() {
    transformStack.push(transformMatrix.cpy())
  }

  fun popTransform() {
    setTransform(transformStack.pop() ?: throw IndexOutOfBoundsException())
  }
}