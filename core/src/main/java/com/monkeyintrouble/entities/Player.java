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

public class Player {
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

    public Player(GameMap gameMap, float startX, float startY) {
        this.gameMap = gameMap;
        this.position = new Vector2(startX, startY);
        this.startPosition = new Vector2(startX, startY);
        this.velocity = new Vector2(0, 0);
        this.bounds = new Rectangle(startX, startY, 32, 32);
        this.isFacingRight = true;
        this.currentState = new PlayerNormalState();
        texture = new Texture(com.badlogic.gdx.Gdx.files.internal("58.png")); // Default to player facing right
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
        currentState.render(this, batch);
    }

    public void takeDamage() {
        if (!isInvincible) {
            hearts--;
            isInvincible = true;
            damageTimer = 0;
            System.out.println("Player took damage! Hearts remaining: " + hearts);

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
        currentState.dispose();
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
    }

    public void resetState() {
        setState(new PlayerNormalState());
    }
}
