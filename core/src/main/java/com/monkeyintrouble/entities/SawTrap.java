package com.monkeyintrouble.entities;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Disposable;

public class SawTrap implements Disposable {
    private static final float TILE_SIZE = 32f;
    private static final float POSITION_DURATION = 1.0f; // Time between position changes
    private static final int NUM_POSITIONS = 3; // Number of positions the saw can be in

    // Texture indices for the saw trap
    private static final int TRAP_LEFT = 43;
    private static final int TRAP_MIDDLE = 44;
    private static final int TRAP_RIGHT = 45;
    private static final int SAW_LEFT = 47;
    private static final int SAW_MIDDLE = 48;
    private static final int SAW_RIGHT = 49;

    private final float startX;
    private final float y;
    private float currentX;
    private int currentPosition;
    private boolean movingLeft;
    private float stateTime;
    private final Rectangle hitbox;
    private Texture texture;
    private float rotation;
    private Vector2 startPosition;

    public SawTrap(float x, float y) {
        this.startX = x;
        this.y = y;
        this.currentX = x;
        this.currentPosition = 0;
        this.movingLeft = false;
        this.stateTime = 0;
        this.hitbox = new Rectangle(x, y, TILE_SIZE, TILE_SIZE);
        this.texture = new Texture(com.badlogic.gdx.Gdx.files.internal("43.png"));
        this.rotation = 0;
        this.startPosition = new Vector2(x, y);
    }

    public void update(float deltaTime) {
        stateTime += deltaTime;

        if (stateTime >= POSITION_DURATION) {
            stateTime = 0;

            // Update position with improved logic
            if (movingLeft) {
                if (currentPosition < NUM_POSITIONS - 1) {
                    currentPosition++;
                } else {
                    currentPosition--;
                    movingLeft = false;
                }
            } else {
                if (currentPosition > 0) {
                    currentPosition--;
                } else {
                    currentPosition++;
                    movingLeft = true;
                }
            }

            // Update hitbox position
            currentX = startX + (currentPosition * TILE_SIZE);
            hitbox.x = currentX;
        }
    }

    public void render(SpriteBatch batch, Texture[] tileTextures) {
        // Draw empty trap tiles
        batch.draw(tileTextures[TRAP_LEFT], startX, y, TILE_SIZE, TILE_SIZE);
        batch.draw(tileTextures[TRAP_MIDDLE], startX + TILE_SIZE, y, TILE_SIZE, TILE_SIZE);
        batch.draw(tileTextures[TRAP_RIGHT], startX + (2 * TILE_SIZE), y, TILE_SIZE, TILE_SIZE);

        // Draw the saw at its current position
        int sawTextureIndex = SAW_LEFT + currentPosition;
        batch.draw(tileTextures[sawTextureIndex], currentX, y, TILE_SIZE, TILE_SIZE);
    }

    public Rectangle getHitbox() {
        return hitbox;
    }

    public float getX() {
        return currentX;
    }

    public float getY() {
        return y;
    }

    public void reset() {
        this.currentX = startX;
        this.currentPosition = 0;
        this.movingLeft = false;
        this.stateTime = 0;
        this.rotation = 0;
        this.hitbox.x = startX;
        this.hitbox.y = y;
    }

    @Override
    public void dispose() {
        // No need to dispose textures as they are managed by GameMap
    }
}
