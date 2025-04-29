package com.monkeyintrouble;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input.Keys;
import com.monkeyintrouble.screens.GameScreen;

public class MonkeyInTroubleGame extends Game {
    private GameScreen gameScreen;

    @Override
    public void create() {
        gameScreen = new GameScreen(this);
        setScreen(gameScreen);
    }

    @Override
    public void render() {
        super.render();

        // Check for R key press to reset the game
        if (Gdx.input.isKeyJustPressed(Keys.R)) {
            gameScreen.reset();
        }
    }

    @Override
    public void dispose() {
        super.dispose();
        getScreen().dispose();
    }
}
