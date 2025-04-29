package com.monkeyintrouble.states;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.monkeyintrouble.entities.Player;

public class PlayerNormalState implements PlayerState {
    private Texture normalLeftTexture;
    private Texture normalRightTexture;

    public PlayerNormalState() {
        normalLeftTexture = new Texture("59.png");
        normalRightTexture = new Texture("58.png");
    }

    @Override
    public void update(Player player, float deltaTime) {
        // Normal state doesn't need any special update logic
    }

    @Override
    public void render(Player player, SpriteBatch batch) {
        Texture currentTexture = player.isFacingRight() ? normalRightTexture : normalLeftTexture;
        batch.draw(currentTexture,
            player.getPosition().x,
            player.getPosition().y,
            24, // width
            24  // height
        );
    }

    @Override
    public void onEnter(Player player) {
        // Nothing special to do when entering normal state
    }

    public void dispose() {
        normalLeftTexture.dispose();
        normalRightTexture.dispose();
    }
}
