package com.monkeyintrouble.states;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.monkeyintrouble.entities.Player;

public interface PlayerState {
    void update(Player player, float deltaTime);
    void render(Player player, SpriteBatch batch);
    void onEnter(Player player);
    void dispose();
}
