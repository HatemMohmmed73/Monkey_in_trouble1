package com.monkeyintrouble.entities;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;

public class BoxTrap {
    private static final float TILE_SIZE = 32f;

    // Texture indices
    private static final int TRAP_ACTIVE = 31;
    private static final int TRAP_INACTIVE = 30;
    private static final int BOX_NORMAL = 39;
    private static final int BOX_PRESSED = 41;
    private static final int BUTTON = 32;
    private static final int PUSHABLE_BOX = 42;

    private final float trapX;
    private final float trapY;
    private final float boxX;
    private final float boxY;
    private final float buttonX;
    private final float buttonY;
    private boolean isTriggered;
    private final Rectangle trapHitbox;
    private final Rectangle boxHitbox;
    private final Rectangle buttonHitbox;

    public BoxTrap(float trapX, float trapY, float boxX, float boxY, float buttonX, float buttonY) {
        this.trapX = trapX;
        this.trapY = trapY;
        this.boxX = boxX;
        this.boxY = boxY;
        this.buttonX = buttonX;
        this.buttonY = buttonY;
        this.isTriggered = false;

        // Create hitboxes
        this.trapHitbox = new Rectangle(trapX, trapY, TILE_SIZE, TILE_SIZE);
        this.boxHitbox = new Rectangle(boxX, boxY, TILE_SIZE, TILE_SIZE);
        this.buttonHitbox = new Rectangle(buttonX, buttonY, TILE_SIZE, TILE_SIZE);
    }

    public void update(float deltaTime) {
        // Check if box is on button
        if (!isTriggered && boxHitbox.overlaps(buttonHitbox)) {
            isTriggered = true;
        }
    }

    public void render(SpriteBatch batch, Texture[] tileTextures) {
        // Draw trap
        int trapTexture = isTriggered ? TRAP_INACTIVE : TRAP_ACTIVE;
        batch.draw(tileTextures[trapTexture], trapX, trapY, TILE_SIZE, TILE_SIZE);

        // Draw box
        int boxTexture = isTriggered ? BOX_PRESSED : BOX_NORMAL;
        batch.draw(tileTextures[boxTexture], boxX, boxY, TILE_SIZE, TILE_SIZE);

        // Draw button
        batch.draw(tileTextures[BUTTON], buttonX, buttonY, TILE_SIZE, TILE_SIZE);
    }

    public Rectangle getTrapHitbox() {
        return trapHitbox;
    }

    public Rectangle getBoxHitbox() {
        return boxHitbox;
    }

    public boolean isTriggered() {
        return isTriggered;
    }

    public void reset() {
        isTriggered = false;
        boxHitbox.x = boxX;
        boxHitbox.y = boxY;
    }
}
