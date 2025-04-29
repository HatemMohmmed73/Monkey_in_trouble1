package com.monkeyintrouble.entities;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Rectangle;

public class Obstacle {
    private Rectangle bounds;
    private Texture texture;
    private float speed;

    public Obstacle(float x, float y, float width, float height, float speed) {
        bounds = new Rectangle(x, y, width, height);
        texture = new Texture("obstacle.png"); // You'll need to add this texture
        this.speed = speed;
    }

    public void update(float deltaTime) {
        bounds.x -= speed * deltaTime;
    }

    public void render(SpriteBatch batch) {
        batch.draw(texture, bounds.x, bounds.y, bounds.width, bounds.height);
    }

    public Rectangle getBounds() {
        return bounds;
    }

    public boolean isOffScreen() {
        return bounds.x + bounds.width < 0;
    }

    public void dispose() {
        texture.dispose();
    }
}
