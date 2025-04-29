package com.monkeyintrouble.entities;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.monkeyintrouble.map.GameMap;
import com.monkeyintrouble.states.PlayerNormalState;
import com.monkeyintrouble.states.PlayerState;
import com.monkeyintrouble.observers.MonkeyObservable;
import com.monkeyintrouble.observers.MonkeyObserver;
import java.util.ArrayList;
import java.util.List;

public class Player implements MonkeyObservable {
    private static final float MOVE_SPEED = 100f;
    private static final float GRAVITY = 0f;
    private static final float DAMAGE_COOLDOWN = 1.0f; // 1 second invincibility after taking damage

    private final Vector2 position;
    private final Vector2 velocity;
    private final Rectangle bounds;
    private final GameMap gameMap;
    private final Vector2 startPosition;
    private PlayerState currentState;
    private boolean isFacingRight;
    private int hearts = 3; // Start with 3 hearts
    private float damageTimer = 0; // Timer for damage cooldown
    private boolean isInvincible = false;
    private Texture texture;
    private boolean isJumping;
    private final List<MonkeyObserver> observers = new ArrayList<>();
    private boolean isGhostMode = false; // New ghost mode state
    private Texture ghostRightTexture; // New ghost textures
    private Texture ghostLeftTexture;

    public Player(GameMap gameMap, float startX, float startY) {
        this.gameMap = gameMap;
        this.position = new Vector2(startX, startY);
        this.startPosition = new Vector2(startX, startY);
        this.velocity = new Vector2(0, 0);
        this.bounds = new Rectangle(startX, startY, 24, 24);
        this.isFacingRight = true;
        this.currentState = new PlayerNormalState();
        texture = new Texture(com.badlogic.gdx.Gdx.files.internal("58.png")); // Default to player facing right
        ghostRightTexture = new Texture(com.badlogic.gdx.Gdx.files.internal("60.png")); // Ghost right texture
        ghostLeftTexture = new Texture(com.badlogic.gdx.Gdx.files.internal("61.png")); // Ghost left texture
        isJumping = false;
    }

    public void update(float deltaTime) {
        // Update damage cooldown
        if (isInvincible) {
            damageTimer += deltaTime;
            if (damageTimer >= DAMAGE_COOLDOWN) {
                isInvincible = false;
                damageTimer = 0;
            }
        }

        // Handle horizontal movement
        float moveX = 0;
        float moveY = 0;

        if (Gdx.input.isKeyPressed(Keys.LEFT)) {
            moveX -= 1;
            isFacingRight = false;
        }
        if (Gdx.input.isKeyPressed(Keys.RIGHT)) {
            moveX += 1;
            isFacingRight = true;
        }
        if (Gdx.input.isKeyPressed(Keys.UP)) {
            moveY += 1;
        }
        if (Gdx.input.isKeyPressed(Keys.DOWN)) {
            moveY -= 1;
        }

        // Apply movement
        velocity.x = moveX * MOVE_SPEED;
        velocity.y = moveY * MOVE_SPEED;

        // Calculate movement for this frame
        float deltaX = velocity.x * deltaTime;
        float deltaY = velocity.y * deltaTime;

        // Try to push boxes first
        if (gameMap.tryPushBox(bounds, deltaX, deltaY)) {
            // If box was pushed, don't move the player
            position.x = bounds.x;
            position.y = bounds.y;
        } else {
            // Update position
            position.x += deltaX;
            position.y += deltaY;

            // Update collision bounds
            bounds.x = position.x;
            bounds.y = position.y;

            // Check collision with map
            if (gameMap.isColliding(bounds)) {
                // Move back if collision occurred
                position.x -= deltaX;
                position.y -= deltaY;
                bounds.x = position.x;
                bounds.y = position.y;
                velocity.x = 0;
                velocity.y = 0;
            }

            // Check collision with saw traps
            if (!isInvincible && gameMap.isCollidingWithSawTrap(bounds)) {
                takeDamage();
            }
        }

        // Update current state
        currentState.update(this, deltaTime);
    }

    public void render(SpriteBatch batch) {
        if (isGhostMode) {
            // Use ghost textures based on facing direction
            Texture currentGhostTexture = isFacingRight ? ghostRightTexture : ghostLeftTexture;
            batch.draw(currentGhostTexture, position.x, position.y, 24, 24);
        } else {
            // Use normal textures
            currentState.render(this, batch);
        }
    }

    @Override
    public void addObserver(MonkeyObserver observer) {
        observers.add(observer);
    }

    @Override
    public void removeObserver(MonkeyObserver observer) {
        observers.remove(observer);
    }

    private void notifyHealthChanged() {
        for (MonkeyObserver observer : observers) {
            observer.onHealthChanged(hearts);
        }
    }

    public void takeDamage() {
        if (!isInvincible) {
            hearts--;
            isInvincible = true;
            damageTimer = 0;
            System.out.println("Player took damage! Hearts remaining: " + hearts);
            notifyHealthChanged();

            // Reset position when hit by saw
            position.set(startPosition);
            bounds.x = position.x;
            bounds.y = position.y;

            if (hearts <= 0) {
                // Game over - reset the game
                reset();
                gameMap.reset();
            }
        }
    }

    public void setState(PlayerState state) {
        this.currentState = state;
        state.onEnter(this);
    }

    public void dispose() {
        texture.dispose();
        ghostRightTexture.dispose();
        ghostLeftTexture.dispose();
    }

    public Vector2 getPosition() {
        return position;
    }

    public Rectangle getBounds() {
        return bounds;
    }

    public int getHearts() {
        return hearts;
    }

    public boolean isFacingRight() {
        return isFacingRight;
    }

    public boolean isInvincible() {
        return isInvincible;
    }

    public float getDamageTimer() {
        return damageTimer;
    }

    public void reset() {
        // Reset position to starting point
        position.set(startPosition);
        // Reset velocity
        velocity.set(0, 0);
        // Reset other states
        isFacingRight = true;
        hearts = 3;
        isInvincible = false;
        damageTimer = 0;
        // Update collision bounds
        bounds.x = position.x;
        bounds.y = position.y;
        // Reset state
        setState(new PlayerNormalState());
        // Notify observers of health reset
        notifyHealthChanged();
    }

    public void resetState() {
        setState(new PlayerNormalState());
    }

    public void teleport(float x, float y) {
        position.set(x, y);
        bounds.x = x;
        bounds.y = y;
        System.out.println("Player teleported to: " + x + "," + y);
    }

    public void setGhostMode(boolean ghostMode) {
        this.isGhostMode = ghostMode;
        // Notify observers about the state change
        for (MonkeyObserver observer : observers) {
            observer.onGhostModeChanged(ghostMode);
        }
    }

    public boolean isGhostMode() {
        return isGhostMode;
    }
}
