package io.github.suitougreentea.various_minos_trois

import com.badlogic.gdx.ApplicationAdapter
import com.badlogic.gdx.Gdx
import com.badlogic.gdx.files.FileHandle
import com.badlogic.gdx.graphics.GL20
import com.badlogic.gdx.graphics.g2d.SpriteBatch
import com.badlogic.gdx.graphics.glutils.ShapeRenderer
import com.beust.klaxon.JsonArray
import com.beust.klaxon.JsonObject
import com.beust.klaxon.Parser
import javax.management.relation.RelationNotFoundException

class VariousMinosTrois: ApplicationAdapter() {
  lateinit var resources: Resources
  lateinit var renderTool: RenderTool
  lateinit var screen: GameScreen
  lateinit var keyConfigJson: JsonArray<JsonObject>

  override fun create () {
    println("External Storage: ${Gdx.files.externalStoragePath}")
    println("Local Storage: ${Gdx.files.localStoragePath}")
    resources = Resources()
    renderTool = RenderTool()

    keyConfigJson = parseJsonFileAsArrayOfObject(Gdx.files.internal("keyconfig.json"))

    screen = GameScreen(this)
  }

  override fun render() {
    Gdx.gl.glClearColor(0f, 0f, 0f, 1f)
    Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT)

    screen.update()
  }

  fun loadProfile(name: String): Profile {
    val json = parseJsonFileAsObject(Gdx.files.internal("profile/${name.toLowerCase()}.json"))

    return Profile(name)
  }
}

fun parseJsonFile(file: FileHandle) : Any? {
  return Parser().parse(file.read())
}

fun parseJsonFileAsObject(file: FileHandle): JsonObject {
  val result = parseJsonFile(file)
  if(result is JsonObject) return result
  throw IllegalArgumentException()
}

fun parseJsonFileAsArrayOfObject(file: FileHandle): JsonArray<JsonObject> {
  val result = parseJsonFile(file)
  @Suppress("UNCHECKED_CAST")
  if(result is JsonArray<*> && result.all { it is JsonObject }) return result as JsonArray<JsonObject>
  throw IllegalArgumentException()
}
