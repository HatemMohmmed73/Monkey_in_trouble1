package com.monkeyintrouble.entities;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.monkeyintrouble.map.GameMap;
import com.monkeyintrouble.states.MonkeyState;
import com.monkeyintrouble.states.NormalState;
import com.monkeyintrouble.states.GhostState;
import com.monkeyintrouble.observers.MonkeyObservable;
import com.monkeyintrouble.observers.MonkeyObserver;
import java.util.ArrayList;
import java.util.List;

public class Player implements MonkeyObservable {
    private static final float MOVE_SPEED = 150f;
    private static final float GRAVITY = 0f;
    private static final float DAMAGE_COOLDOWN = 1.0f;

    private final float startX;
    private final float startY;
    private final Vector2 position;
    private final Rectangle bounds;
    private final GameMap gameMap;
    private final Texture rightTexture;
    private final Texture leftTexture;
    private final Texture ghostRightTexture;
    private final Texture ghostLeftTexture;
    private boolean isFacingRight = true;
    private float jumpVelocity = 0;
    private boolean isJumping = false;
    private int hearts = 3;
    private int bananas = 0;
    private float damageTimer = 0;
    private boolean isInvincible = false;
    private boolean isGhostMode = false;
    private MonkeyState currentState;
    private MonkeyObserver observer;

    public Player(GameMap gameMap, float x, float y) {
        this.gameMap = gameMap;
        this.position = new Vector2(x, y);
        this.bounds = new Rectangle(x, y, 24, 24);
        this.rightTexture = new Texture(com.badlogic.gdx.Gdx.files.internal("58.png")); // Monkey right texture
        this.leftTexture = new Texture(com.badlogic.gdx.Gdx.files.internal("59.png")); // Monkey left texture
        this.ghostRightTexture = new Texture(com.badlogic.gdx.Gdx.files.internal("60.png")); // Ghost right texture
        this.ghostLeftTexture = new Texture(com.badlogic.gdx.Gdx.files.internal("61.png")); // Ghost left texture
        this.currentState = new NormalState();
        this.startX = x;
        this.startY = y;
    }

    public void update(float deltaTime) {
        // Handle horizontal movement
        float deltaX = 0;
        float deltaY = 0;

        if (Gdx.input.isKeyPressed(Keys.RIGHT)) {
            deltaX += MOVE_SPEED * deltaTime;
            isFacingRight = true;
        }
        if (Gdx.input.isKeyPressed(Keys.LEFT)) {
            deltaX -= MOVE_SPEED * deltaTime;
            isFacingRight = false;
        }
        if (Gdx.input.isKeyPressed(Keys.UP)) {
            deltaY += MOVE_SPEED * deltaTime;
        }
        if (Gdx.input.isKeyPressed(Keys.DOWN)) {
            deltaY -= MOVE_SPEED * deltaTime;
        }

        // Try to push box first
        if (!gameMap.tryPushBox(bounds, deltaX, deltaY)) {
            // If not pushing box, handle normal movement
            if (deltaX != 0) {
                bounds.x += deltaX;
                if (gameMap.isColliding(bounds)) {
                    bounds.x -= deltaX;
                }
            }
            if (deltaY != 0) {
                bounds.y += deltaY;
                if (gameMap.isColliding(bounds)) {
                    bounds.y -= deltaY;
                }
            }
        }

        // Update position
        position.x = bounds.x;
        position.y = bounds.y;

        // Update damage timer
        if (damageTimer > 0) {
            damageTimer -= deltaTime;
            if (damageTimer <= 0) {
                isInvincible = false;
            }
        }

        // Check for saw trap collision
        if (!isGhostMode && gameMap.isCollidingWithSawTrap(bounds)) {
            takeDamage();
        }

        // Handle asset collisions and get teleport destination if any
        Vector2 teleportDestination = gameMap.handleAssetCollision(bounds);
        if (teleportDestination != null) {
            teleport(teleportDestination.x, teleportDestination.y);
        }

        // Update current state
        currentState.update(this, deltaTime);
    }

    public void render(SpriteBatch batch) {
        currentState.render(this, batch);
    }

    public void dispose() {
        rightTexture.dispose();
        leftTexture.dispose();
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

    public void setHearts(int hearts) {
        this.hearts = hearts;
        if (observer != null) {
            observer.onHeartsChanged(hearts);
        }
    }

    public int getBananas() {
        return bananas;
    }

    public void setBananas(int bananas) {
        this.bananas = bananas;
        if (observer != null) {
            observer.onBananasChanged(bananas);
        }
    }

    public boolean isJumping() {
        return isJumping;
    }

    public void setJumping(boolean jumping) {
        isJumping = jumping;
    }

    public float getJumpVelocity() {
        return jumpVelocity;
    }

    public void setJumpVelocity(float jumpVelocity) {
        this.jumpVelocity = jumpVelocity;
    }

    public float getGravity() {
        return GRAVITY;
    }

    public float getJumpForce() {
        return jumpVelocity;
    }

    public void takeDamage() {
        if (!isInvincible) {
            hearts--;
            isInvincible = true;
            damageTimer = DAMAGE_COOLDOWN;

            // Teleport back to starting position
            position.set(startX, startY);
            bounds.x = startX;
            bounds.y = startY;

            if (observer != null) {
                observer.onHeartsChanged(hearts);
            }
        }
    }

    public void teleport(float x, float y) {
        position.set(x, y);
        bounds.x = x;
        bounds.y = y;
    }

    public void reset() {
        position.set(startX, startY);
        bounds.x = startX;
        bounds.y = startY;
        hearts = 3;
        bananas = 0;
        isInvincible = false;
        damageTimer = 0;
        if (observer != null) {
            observer.onHeartsChanged(hearts);
            observer.onBananasChanged(bananas);
        }
    }

    public void addObserver(MonkeyObserver observer) {
        this.observer = observer;
    }

    public void removeObserver(MonkeyObserver observer) {
        if (this.observer == observer) {
            this.observer = null;
        }
    }

    public boolean isInvincible() {
        return isInvincible;
    }

    public float getDamageTimer() {
        return damageTimer;
    }

    public void setState(MonkeyState state) {
        if (this.currentState != null) {
            this.currentState.onExit(this);
        }
        this.currentState = state;
        if (this.currentState != null) {
            this.currentState.onEnter(this);
        }
    }

    public boolean isFacingRight() {
        return isFacingRight;
    }

    public Texture getGhostRightTexture() {
        return ghostRightTexture;
    }

    public Texture getGhostLeftTexture() {
        return ghostLeftTexture;
    }

    public boolean isGhostMode() {
        return isGhostMode;
    }

    public void setGhostMode(boolean ghostMode) {
        this.isGhostMode = ghostMode;
        if (ghostMode) {
            setState(new GhostState());
        } else {
            setState(new NormalState());
        }
        if (observer != null) {
            observer.onGhostModeChanged(ghostMode);
        }
    }
}
