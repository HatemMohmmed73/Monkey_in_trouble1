package com.monkeyintrouble.states;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.monkeyintrouble.entities.Monkey;

public class NormalState implements MonkeyState {
    private Texture normalLeftTexture;
    private Texture normalRightTexture;

    public NormalState() {
        normalLeftTexture = new Texture("59.png");
        normalRightTexture = new Texture("58.png");
    }

    @Override
    public void update(Monkey monkey, float deltaTime) {
        // Normal state doesn't need any special update logic
    }

    @Override
    public void render(Monkey monkey, SpriteBatch batch) {
        Texture currentTexture = monkey.isFacingRight() ? normalRightTexture : normalLeftTexture;
        batch.draw(currentTexture,
            monkey.getPosition().x,
            monkey.getPosition().y,
            24, // width
            24  // height
        );
    }

    @Override
    public void onEnter(Monkey monkey) {
        // Nothing special to do when entering normal state
    }

    public void dispose() {
        normalLeftTexture.dispose();
        normalRightTexture.dispose();
    }
}
