package com.monkeyintrouble.entities;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.monkeyintrouble.states.MonkeyState;
import com.monkeyintrouble.states.NormalState;
import com.monkeyintrouble.observers.MonkeyObserver;
import java.util.ArrayList;
import java.util.List;

public class Monkey {
    private static final float GRAVITY = -20f;
    private static final float JUMP_VELOCITY = 10f;
    private static final float MOVEMENT_SPEED = 5f;
    private MonkeyState currentState;
    private Vector2 position;
    private Vector2 velocity;
    private Rectangle bounds;
    private Texture texture;
    private boolean isJumping;
    private boolean isFacingRight;
    private List<MonkeyObserver> observers = new ArrayList<>();
    private int health = 3;
    private int bananas = 0;

    public Monkey(float x, float y) {
        position = new Vector2(x, y);
        velocity = new Vector2();
        bounds = new Rectangle(x, y, 24, 24);
        texture = new Texture(com.badlogic.gdx.Gdx.files.internal("58.png")); // Default to monkey facing right
        isJumping = false;
        isFacingRight = true;
        setState(new NormalState());
    }

    public void update(float deltaTime) {
        if (currentState != null) {
            currentState.update(this, deltaTime);
        }
    }

    public void render(SpriteBatch batch) {
        if (currentState != null) {
            currentState.render(this, batch);
        }
    }

    public void jump() {
        if (!isJumping) {
            velocity.y = JUMP_VELOCITY;
            isJumping = true;
        }
    }

    public void moveLeft() {
        velocity.x = -MOVEMENT_SPEED;
        isFacingRight = false;
    }

    public void moveRight() {
        velocity.x = MOVEMENT_SPEED;
        isFacingRight = true;
    }

    public void moveUp() {
        velocity.y = MOVEMENT_SPEED;
    }

    public void moveDown() {
        velocity.y = -MOVEMENT_SPEED;
    }

    public void stopMoving() {
        velocity.x = 0;
    }

    public Rectangle getBounds() {
        return bounds;
    }

    public void dispose() {
        texture.dispose();
    }

    public void setState(MonkeyState state) {
        this.currentState = state;
        state.onEnter(this);
    }

    public void basicUpdate(float deltaTime) {
        velocity.y += GRAVITY * deltaTime;
        position.add(velocity.x * deltaTime, velocity.y * deltaTime);
        bounds.setPosition(position);

        if (position.y <= 0) {
            position.y = 0;
            velocity.y = 0;
            isJumping = false;
        }
    }

    public void drawMonkey(SpriteBatch batch, boolean ghostMode) {
        String fileName;
        if (ghostMode) {
            fileName = isFacingRight ? "60.png" : "51.png";
        } else {
            fileName = isFacingRight ? "58.png" : "59.png";
        }

        texture.dispose(); // Dispose old texture
        texture = new Texture(com.badlogic.gdx.Gdx.files.internal(fileName));

        batch.draw(texture, position.x, position.y, bounds.width, bounds.height,
            0, 0, texture.getWidth(), texture.getHeight(),
            false, false);
    }

    public void addObserver(MonkeyObserver observer) {
        observers.add(observer);
    }

    private void notifyHealthChanged() {
        for (MonkeyObserver observer : observers) {
            observer.onHealthChanged(health);
        }
    }

    private void notifyBananaCollected() {
        for (MonkeyObserver observer : observers) {
            observer.onBananaCollected(bananas);
        }
    }

    public void takeDamage() {
        health--;
        notifyHealthChanged();
        if (health <= 0) {
            // Handle monkey death logic here
            health = 3; // Reset for now
        }
    }

    public void collectBanana() {
        bananas++;
        notifyBananaCollected();
    }

    public Vector2 getPosition() {
        return position;
    }

    public boolean isFacingRight() {
        return isFacingRight;
    }
}
