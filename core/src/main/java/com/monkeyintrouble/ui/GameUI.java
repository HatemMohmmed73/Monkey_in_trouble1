package com.monkeyintrouble.ui;

import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.monkeyintrouble.observers.MonkeyObserver;
import com.monkeyintrouble.entities.Player;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.monkeyintrouble.screens.GameScreen;

public class GameUI implements MonkeyObserver {

    private static final int MAX_HEARTS = 3;
    private static final int HEART_SIZE = 32;
    private static final int HEART_SPACING = 40;
    private static final int BANANA_SIZE = 32;
    private static final int BANANA_SPACING = 40;
    private int hearts = MAX_HEARTS;
    private int bananaCount = 0;
    private BitmapFont font;
    private Stage stage;
    private Skin skin;
    private TextButton startButton;
    private TextButton restartButton;
    private Player player;
    private Texture fullHeartTexture;  // heart1.png
    private Texture emptyHeartTexture; // heart2.png
    private Texture bananaTexture;     // banana1.png
    private Texture emptyBananaTexture; // banana2.png
    private boolean isGameOver = false;
    private final GameScreen gameScreen;  // Add reference to GameScreen

    public GameUI(Player player, GameScreen gameScreen) {  // Modified constructor
        this.player = player;
        this.gameScreen = gameScreen;  // Store reference to GameScreen
        font = new BitmapFont();
        font.setColor(Color.WHITE);
        font.getData().setScale(2);

        // Load heart textures
        fullHeartTexture = new Texture("heart1.png");
        emptyHeartTexture = new Texture("heart2.png");

        // Load banana textures
        bananaTexture = new Texture("banana1.png");
        emptyBananaTexture = new Texture("banana2.png");

        // Initialize stage and skin
        stage = new Stage();
        skin = new Skin(Gdx.files.internal("ui/uiskin.json"));
        Gdx.input.setInputProcessor(stage);

        // Create buttons
        startButton = new TextButton("Start", skin);
        restartButton = new TextButton("Restart", skin);

        // Position buttons in top left
        Table buttonTable = new Table();
        buttonTable.setPosition(10, Gdx.graphics.getHeight() - 10);
        buttonTable.top().left();
        buttonTable.add(startButton).pad(5);
        buttonTable.row();
        buttonTable.add(restartButton).pad(5);

        // Add button listeners
        startButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                startGame();
            }
        });

        restartButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                restartGame();
            }
        });

        stage.addActor(buttonTable);
    }

    private void startGame() {
        System.out.println("Game Started!");
        isGameOver = false;
    }

    private void restartGame() {
        if (gameScreen != null) {
            gameScreen.reset();  // This will reset everything: player, map, ghost, and game state
            isGameOver = false;
            System.out.println("Game Fully Restarted - All elements restored to original state!");
        }
    }

    @Override
    public void onHealthChanged(int hearts) {
        this.hearts = hearts;
        System.out.println("Health: " + hearts);
        if (hearts <= 0) {
            isGameOver = true;
        }
    }

    @Override
    public void onBananaCollected(int totalBananas) {
        this.bananaCount = totalBananas;
        System.out.println("Bananas: " + bananaCount);
    }

    public void render(SpriteBatch batch) {
        stage.act();
        stage.draw();

        if (isGameOver) {
            // Draw "GAME OVER" text in the center of the screen
            GlyphLayout layout = new GlyphLayout(font, "GAME OVER");
            float x = (Gdx.graphics.getWidth() - layout.width) / 2;
            float y = (Gdx.graphics.getHeight() + layout.height) / 2;
            font.draw(batch, "GAME OVER", x, y);
            return;
        }

        // Draw hearts
        for (int i = 0; i < MAX_HEARTS; i++) {
            Texture heartTexture = (i < hearts) ? fullHeartTexture : emptyHeartTexture;
            batch.draw(heartTexture, 10 + (i * HEART_SPACING), Gdx.graphics.getHeight() - HEART_SIZE - 10, HEART_SIZE, HEART_SIZE);
        }

        // Draw bananas
        for (int i = 0; i < 5; i++) { // Assuming max 5 bananas
            Texture bananaTex = (i < bananaCount) ? bananaTexture : emptyBananaTexture;
            batch.draw(bananaTex, Gdx.graphics.getWidth() - (5 - i) * BANANA_SPACING - 10,
                      Gdx.graphics.getHeight() - BANANA_SIZE - 10, BANANA_SIZE, BANANA_SIZE);
        }
    }

    public void dispose() {
        font.dispose();
        stage.dispose();
        skin.dispose();
        fullHeartTexture.dispose();
        emptyHeartTexture.dispose();
        bananaTexture.dispose();
        emptyBananaTexture.dispose();
    }

    public void resize(int width, int height) {
        stage.getViewport().update(width, height, true);
    }
}
