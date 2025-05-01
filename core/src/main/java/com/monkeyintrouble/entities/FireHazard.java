package com.monkeyintrouble.entities;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;

public class FireHazard {
    private static final float TILE_SIZE = 32f;
    private static final float MOVE_SPEED = 100f; // Increased speed
    private static final float MAX_DISTANCE = TILE_SIZE * 3; // 3 blocks down

    private final Vector2 startPosition;
    private final Vector2 position;
    private final Rectangle hitbox;
    private float distanceMoved;
    private boolean isActive = true;

    public FireHazard(float x, float y) {
        this.startPosition = new Vector2(x, y);
        this.position = new Vector2(x, y);
        this.hitbox = new Rectangle(x, y, TILE_SIZE, TILE_SIZE);
        this.distanceMoved = 0;
    }

    public void update(float deltaTime) {
        if (!isActive) return;

        position.y -= MOVE_SPEED * deltaTime;
        distanceMoved += MOVE_SPEED * deltaTime;

        // Update hitbox position
        hitbox.x = position.x;
        hitbox.y = position.y;

        // Deactivate when moved 3 blocks
        if (distanceMoved >= MAX_DISTANCE) {
            isActive = false;
        }
    }

    public void render(SpriteBatch batch, Texture fireTexture) {
        if (!isActive) return;
        batch.draw(fireTexture, position.x, position.y, TILE_SIZE, TILE_SIZE);
    }

    public Rectangle getHitbox() {
        return hitbox;
    }

    public boolean isActive() {
        return isActive;
    }

    public void reset() {
        position.set(startPosition);
        hitbox.x = position.x;
        hitbox.y = position.y;
        distanceMoved = 0;
        isActive = true;
    }
}
