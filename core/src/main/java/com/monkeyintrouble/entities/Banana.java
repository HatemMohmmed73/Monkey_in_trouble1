package com.monkeyintrouble.entities;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Rectangle;

public class Banana {
    private Rectangle bounds;
    private Texture texture;
    private boolean collected;

    public Banana(float x, float y) {
        bounds = new Rectangle(x, y, 16, 16);
        texture = new Texture("banana.png"); // You'll need to add this texture
        collected = false;
    }

    public void render(SpriteBatch batch) {
        if (!collected) {
            batch.draw(texture, bounds.x, bounds.y, bounds.width, bounds.height);
        }
    }

    public boolean isCollected() {
        return collected;
    }

    public void collect() {
        collected = true;
    }

    public Rectangle getBounds() {
        return bounds;
    }

    public void dispose() {
        texture.dispose();
    }
}
