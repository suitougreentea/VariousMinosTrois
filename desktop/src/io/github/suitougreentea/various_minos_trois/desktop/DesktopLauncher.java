package io.github.suitougreentea.various_minos_trois.desktop;

import com.badlogic.gdx.backends.lwjgl.LwjglApplication;
import com.badlogic.gdx.backends.lwjgl.LwjglApplicationConfiguration;
import io.github.suitougreentea.various_minos_trois.VariousMinosTrois;

public class DesktopLauncher {
	public static void main (String[] arg) {
		LwjglApplicationConfiguration config = new LwjglApplicationConfiguration();
		config.width = 800;
		config.height = 600;
		config.resizable = false;
		config.foregroundFPS = 60;
		new LwjglApplication(new VariousMinosTrois(), config);
	}
}
