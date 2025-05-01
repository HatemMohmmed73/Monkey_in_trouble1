package com.monkeyintrouble.states;

import com.monkeyintrouble.entities.Player;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;

public interface MonkeyState {
    void update(Player player, float delta);
    void render(Player player, SpriteBatch batch);
    void onEnter(Player player);
    void onExit(Player player);
}
