package com.monkeyintrouble.screen;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;
import com.monkeyintrouble.map.GameMap;
import com.monkeyintrouble.entities.Ghost;
import com.monkeyintrouble.entities.SawTrap;

public class GameScreen implements Screen {

    private SpriteBatch batch;
    private Texture background;
    private GameMap gameMap;
    private Ghost ghost;
    private Array<SawTrap> sawTraps;

    @Override
    public void show() {
        batch = new SpriteBatch();
        background = new Texture("background.png");
        gameMap = new GameMap(null, null, null); // You'll need to provide the actual room data
        ghost = new Ghost(128, 200); // Initial position
        sawTraps = new Array<>();
    }

    @Override
    public void render(float delta) {
        // Update game logic
        handleInput(delta);
        ghost.update(delta);
        gameMap.update(delta);

        // Clear screen
        Gdx.gl.glClearColor(0, 0, 0.2f, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        batch.begin();
        gameMap.render(batch);
        ghost.render(batch);
        batch.end();
    }

    private void handleInput(float delta) {
        // Your existing input handling code
    }

    @Override
    public void resize(int width, int height) {
        // Handle resize if needed
    }

    @Override
    public void pause() {
        // Handle pause if needed
    }

    @Override
    public void resume() {
        // Handle resume if needed
    }

    @Override
    public void hide() {
        // Handle hide if needed
    }

    @Override
    public void dispose() {
        batch.dispose();
        background.dispose();
        gameMap.dispose();
        ghost.dispose();
        for (SawTrap sawTrap : sawTraps) {
            sawTrap.dispose();
        }
    }
}
