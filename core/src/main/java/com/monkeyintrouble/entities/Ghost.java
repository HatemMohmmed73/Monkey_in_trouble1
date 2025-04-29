package com.monkeyintrouble.entities;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;

public class Ghost {
    private static final float MOVE_SPEED = 50f;
    private final Texture texture;
    private final Vector2 position;
    private final Rectangle bounds;
    private final Vector2 startPosition;

    public Ghost(float x, float y) {
        this.position = new Vector2(x, y);
        this.startPosition = new Vector2(x, y);
        this.bounds = new Rectangle(x, y, 32, 32);
        this.texture = new Texture("ghost.png");
    }

    public void update(float deltaTime) {
        // Ghost no longer moves horizontally
        bounds.x = position.x;
        bounds.y = position.y;
    }

    public void render(SpriteBatch batch) {
        batch.draw(texture, position.x, position.y, 32, 32);
    }

    public Rectangle getBounds() {
        return bounds;
    }

    public void dispose() {
        texture.dispose();
    }

    public void reset() {
        position.set(startPosition);
        bounds.x = position.x;
        bounds.y = position.y;
    }
}
