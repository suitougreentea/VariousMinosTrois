package io.github.suitougreentea.various_minos_trois

import com.badlogic.gdx.graphics.OrthographicCamera
import com.badlogic.gdx.math.Matrix4

object Util {
  val defaultCamera = OrthographicCamera(800f, 600f).apply { setToOrtho(false, 800f, 600f) }
  fun getFrameBufferProjectionMatrix(width: Float, height: Float) = Matrix4().setToOrtho2D(0f, 0f, width, -height).translate(0f, -height, 0f)
  fun easeOutQuad(t: Float) = -(t - 1f) * (t - 1f) + 1f
}