package com.monkeyintrouble.states;

import com.monkeyintrouble.entities.Player;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.Texture;

public class GhostState implements MonkeyState {

    @Override
    public void update(Player player, float delta) {
        // In ghost state, the player can pass through certain obstacles
        // and interact with ghosts
    }

    @Override
    public void render(Player player, SpriteBatch batch) {
        // Use ghost textures based on facing direction
        Texture currentTexture = player.isFacingRight() ?
            player.getGhostRightTexture() : player.getGhostLeftTexture();

        batch.draw(currentTexture,
            player.getPosition().x,
            player.getPosition().y,
            24, // width
            24  // height
        );
    }

    @Override
    public void onEnter(Player player) {
        System.out.println("Player entered Ghost State");
    }

    @Override
    public void onExit(Player player) {
        System.out.println("Player exited Ghost State");
    }
}
