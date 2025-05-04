package com.monkeyintrouble.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Vector2;
import com.monkeyintrouble.MonkeyInTroubleGame;
import com.monkeyintrouble.entities.Player;
import com.monkeyintrouble.map.GameMap;
import com.monkeyintrouble.map.MapLoader;
import com.monkeyintrouble.ui.GameUI;
import com.badlogic.gdx.utils.Timer;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;

public class GameScreen implements Screen {
    private final MonkeyInTroubleGame game;
    private final SpriteBatch batch;
    private final OrthographicCamera camera;
    private final GameMap gameMap;
    private final Player player;
    private final GameUI ui;
    private final ShapeRenderer shapeRenderer;
    private final float startX = 142;
    private final float startY = 62;

    public GameScreen(MonkeyInTroubleGame game) {
        this.game = game;
        this.batch = new SpriteBatch();
        this.camera = new OrthographicCamera(800, 480);
        this.camera.position.set(400, 240, 0);
        this.camera.update();

        // Load map
        this.gameMap = MapLoader.loadLevel1();

        // Create player at the starting position in the main room
        this.player = new Player(gameMap, startX, startY);
        this.gameMap.setPlayer(player);  // Set player reference in GameMap
        this.ui = new GameUI(this);
        // Register UI as observer for player
        this.player.addObserver(this.ui);

        // Initialize shape renderer for debug
        this.shapeRenderer = new ShapeRenderer();
    }

    @Override
    public void render(float delta) {
        // Handle restart key
        if (Gdx.input.isKeyJustPressed(com.badlogic.gdx.Input.Keys.R)) {
            reset();
            ui.reset();
        }
        // Update game state
        player.update(delta);
        gameMap.update(delta);

        // Clear screen
        Gdx.gl.glClearColor(0, 0, 0, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        // Update camera position to follow player
        camera.position.x = player.getPosition().x;
        camera.position.y = player.getPosition().y;
        camera.update();

        // Set up sprite batch with camera
        batch.setProjectionMatrix(camera.combined);

        // Render game elements
        batch.begin();
        gameMap.render(batch);
        player.render(batch);
        batch.end();

        // Render UI (not affected by camera)
        batch.setProjectionMatrix(ui.getStage().getCamera().combined);
        batch.begin();
        ui.render(batch);
        batch.end();

        // Debug rendering (optional)
        if (MonkeyInTroubleGame.DEBUG) {
            shapeRenderer.setProjectionMatrix(camera.combined);
            shapeRenderer.begin(ShapeRenderer.ShapeType.Line);
            shapeRenderer.setColor(1, 0, 0, 1);
            shapeRenderer.rect(player.getBounds().x, player.getBounds().y, player.getBounds().width, player.getBounds().height);
            shapeRenderer.end();
        }
    }

    @Override
    public void resize(int width, int height) {
        // Keep viewport size fixed at 800x480
        camera.viewportWidth = 800;
        camera.viewportHeight = 480;
        camera.position.set(400, 240, 0);
        camera.update();

        // Update UI viewport
        ui.resize(width, height);
    }

    @Override
    public void show() {
        Gdx.input.setInputProcessor(ui.getStage());
    }

    @Override
    public void hide() {
        Gdx.input.setInputProcessor(null);
    }

    @Override
    public void pause() {
        // Game is paused
    }

    @Override
    public void resume() {
        // Game is resumed
    }

    @Override
    public void dispose() {
        batch.dispose();
        gameMap.dispose();
        player.dispose();
        ui.dispose();
        shapeRenderer.dispose();
    }

    public void reset() {
        // Reset player (which also resets health)
        player.reset();

        // Reset map (which resets doors, buttons, etc)
        gameMap.reset();

        System.out.println("Game Reset - All elements restored to original state!");
    }
}
