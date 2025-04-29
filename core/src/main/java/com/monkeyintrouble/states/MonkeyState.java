package com.monkeyintrouble.states;

import com.monkeyintrouble.entities.Monkey;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;

public interface MonkeyState {
    void update(Monkey monkey, float delta);
    void render(Monkey monkey, SpriteBatch batch);
    void onEnter(Monkey monkey);
}
