package com.monkeyintrouble.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Vector2;
import com.monkeyintrouble.MonkeyInTroubleGame;
import com.monkeyintrouble.entities.Player;
import com.monkeyintrouble.entities.Ghost;
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
    private Ghost ghost;
    private boolean gameOver = false;
    private ShapeRenderer shapeRenderer;

    public GameScreen(MonkeyInTroubleGame game) {
        this.game = game;
        this.batch = new SpriteBatch();

        // Create camera with fixed viewport size
        float viewportWidth = 800;
        float viewportHeight = 480;
        this.camera = new OrthographicCamera(viewportWidth, viewportHeight);

        // Load map
        this.gameMap = MapLoader.loadLevel1();

        // Create player at the starting position in the main room
        float startX = 100; // Position in the first room
        float startY = 300 - (9 * 32) + 32; // Start 9 blocks lower (32 pixels per block) and then 1 block up
        this.player = new Player(gameMap, startX, startY);
        this.ui = new GameUI(player, this);
        // Register UI as observer for player
        this.player.addObserver(this.ui);

        // Create ghost at the original position where red square was
        this.ghost = new Ghost(128, 200);

        // Center camera on player
        centerCameraOnPlayer();

        shapeRenderer = new ShapeRenderer();
    }

    private void centerCameraOnPlayer() {
        Vector2 position = player.getPosition();
        camera.position.x = position.x;
        camera.position.y = position.y;
        camera.update();
    }

    @Override
    public void render(float delta) {
        if (gameOver) {
            // Show game over screen
            Gdx.gl.glClearColor(0, 0, 0, 1);
            Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
            batch.begin();
            ui.render(batch);
            batch.end();
            return;
        }

        // Clear screen
        Gdx.gl.glClearColor(0, 0, 0.2f, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        // Update game objects
        player.update(delta);
        ghost.update(delta);
        gameMap.update(delta);

        // Handle collisions
        handleCollisions();

        // Center camera on player
        centerCameraOnPlayer();

        // Update camera
        camera.update();
        batch.setProjectionMatrix(camera.combined);

        // Draw everything
        batch.begin();
        gameMap.render(batch);
        player.render(batch);
        ghost.render(batch);
        ui.render(batch);
        batch.end();

        // Draw debug shapes
        shapeRenderer.setProjectionMatrix(camera.combined);
        shapeRenderer.begin(ShapeRenderer.ShapeType.Line);
        shapeRenderer.setColor(Color.RED);
        shapeRenderer.rect(ghost.getBounds().x, ghost.getBounds().y, ghost.getBounds().width, ghost.getBounds().height);
        shapeRenderer.end();
    }

    private void handleCollisions() {
        // Handle ghost collision
        if (player.getBounds().overlaps(ghost.getBounds())) {
            System.out.println("Ghost collision detected! Player position: " + player.getPosition() + ", Ghost position: (" + ghost.getBounds().x + "," + ghost.getBounds().y + ")");
            player.takeDamage();
            if (player.getHearts() <= 0) {
                gameOver = true;
                // Wait for 2 seconds then reset
                Timer.schedule(new Timer.Task() {
                    @Override
                    public void run() {
                        reset();
                    }
                }, 2); // 2 seconds delay
            }
        }

        // Handle asset collisions
        gameMap.handleAssetCollision(player.getBounds());

        // Handle saw trap collision is handled in Player class
    }

    @Override
    public void resize(int width, int height) {
        camera.setToOrtho(false, width, height);
        ui.resize(width, height);
    }

    @Override
    public void show() {
        // Screen is shown
    }

    @Override
    public void hide() {
        // Screen is hidden
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
        ghost.dispose();
        shapeRenderer.dispose();
    }

    public void reset() {
        // Reset player (which also resets health)
        player.reset();

        // Reset map (which resets doors, buttons, etc)
        gameMap.reset();

        // Reset ghost position
        ghost.reset();

        // Reset game state
        gameOver = false;

        System.out.println("Game Reset - All elements restored to original state!");
    }
}
