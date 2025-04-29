package com.monkeyintrouble.desktop;

import com.badlogic.gdx.backends.lwjgl3.Lwjgl3Application;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3ApplicationConfiguration;
import com.monkeyintrouble.MonkeyInTroubleGame;

public class DesktopLauncher {
    public static void main(String[] arg) {
        Lwjgl3ApplicationConfiguration config = new Lwjgl3ApplicationConfiguration();
        config.setTitle("Monkey in Trouble");
        config.setWindowedMode(800, 600);
        config.useVsync(true);
        new Lwjgl3Application(new MonkeyInTroubleGame(), config);
    }
}
