package com.monkeyintrouble.ui;

import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.monkeyintrouble.observers.MonkeyObserver;
import com.monkeyintrouble.entities.Player;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.monkeyintrouble.screens.GameScreen;
import com.badlogic.gdx.utils.viewport.ScreenViewport;

public class GameUI implements MonkeyObserver {
    private static final int MAX_HEARTS = 3;
    private static final int HEART_SIZE = 32;
    private static final int HEART_SPACING = 40;
    private static final int BANANA_SIZE = 32;
    private static final int BANANA_SPACING = 40;
    private int hearts = MAX_HEARTS;
    private int bananaCount = 0;
    private BitmapFont font;
    private final Stage stage;
    private final GameScreen gameScreen;
    private boolean isGameOver = false;
    private boolean isGameWon = false;
    private Texture fullHeartTexture;
    private Texture emptyHeartTexture;
    private Texture bananaTexture;
    private Texture emptyBananaTexture;

    public GameUI(GameScreen gameScreen) {
        this.gameScreen = gameScreen;
        this.stage = new Stage(new ScreenViewport());
        Gdx.input.setInputProcessor(stage);

        // Initialize font
        font = new BitmapFont();
        font.setColor(Color.WHITE);
        font.getData().setScale(2);

        // Load textures
        fullHeartTexture = new Texture(Gdx.files.internal("heart1.png"));
        emptyHeartTexture = new Texture(Gdx.files.internal("heart2.png"));
        bananaTexture = new Texture(Gdx.files.internal("banana1.png"));
        emptyBananaTexture = new Texture(Gdx.files.internal("banana2.png"));
    }

    @Override
    public void onHeartsChanged(int hearts) {
        this.hearts = hearts;
        System.out.println("Health: " + hearts);
        if (hearts <= 0) {
            isGameOver = true;
        }
    }

    @Override
    public void onBananasChanged(int bananas) {
        this.bananaCount = bananas;
        System.out.println("Bananas: " + bananaCount);
    }

    @Override
    public void onGhostModeChanged(boolean isGhostMode) {
        System.out.println("Ghost mode: " + isGhostMode);
    }

    @Override
    public void onGameWon() {
        this.isGameWon = true;
        System.out.println("Victory state updated in UI");
    }

    public void render(SpriteBatch batch) {
        stage.act(Gdx.graphics.getDeltaTime());
        stage.draw();

        // Draw hearts
        for (int i = 0; i < MAX_HEARTS; i++) {
            Texture heartTexture = (i < hearts) ? fullHeartTexture : emptyHeartTexture;
            batch.draw(heartTexture, 10 + (i * HEART_SPACING), Gdx.graphics.getHeight() - HEART_SIZE - 10, HEART_SIZE, HEART_SIZE);
        }

        // Draw bananas
        for (int i = 0; i < 3; i++) { // Assuming max 5 bananas
            Texture bananaTex = (i < bananaCount) ? bananaTexture : emptyBananaTexture;
            batch.draw(bananaTex, Gdx.graphics.getWidth() - (5 - i) * BANANA_SPACING - 10,
                      Gdx.graphics.getHeight() - BANANA_SIZE - 10, BANANA_SIZE, BANANA_SIZE);
        }

        // Draw game over or victory text if needed
        if (isGameOver) {
            GlyphLayout layout = new GlyphLayout(font, "GAME OVER");
            float x = (Gdx.graphics.getWidth() - layout.width) / 2;
            float y = (Gdx.graphics.getHeight() + layout.height) / 2;
            font.draw(batch, "GAME OVER", x, y);

            GlyphLayout restartLayout = new GlyphLayout(font, "Press R to restart");
            float restartX = (Gdx.graphics.getWidth() - restartLayout.width) / 2;
            float restartY = y - 40;
            font.draw(batch, "Press R to restart", restartX, restartY);
        } else if (isGameWon) {
            GlyphLayout layout = new GlyphLayout(font, "VICTORY!");
            float x = (Gdx.graphics.getWidth() - layout.width) / 2;
            float y = (Gdx.graphics.getHeight() + layout.height) / 2;
            font.draw(batch, "VICTORY!", x, y);

            GlyphLayout congratsLayout = new GlyphLayout(font, "Congratulations!");
            float congratsX = (Gdx.graphics.getWidth() - congratsLayout.width) / 2;
            float congratsY = y - 40;
            font.draw(batch, "Congratulations!", congratsX, congratsY);

            GlyphLayout restartLayout = new GlyphLayout(font, "Press R to play again");
            float restartX = (Gdx.graphics.getWidth() - restartLayout.width) / 2;
            float restartY = congratsY - 40;
            font.draw(batch, "Press R to play again", restartX, restartY);
        }
    }

    public void dispose() {
        font.dispose();
        stage.dispose();
        fullHeartTexture.dispose();
        emptyHeartTexture.dispose();
        bananaTexture.dispose();
        emptyBananaTexture.dispose();
    }

    public void resize(int width, int height) {
        stage.getViewport().update(width, height, true);
    }

    public Stage getStage() {
        return stage;
    }
}
