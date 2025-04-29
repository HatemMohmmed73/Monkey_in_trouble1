package com.monkeyintrouble.states;

import com.monkeyintrouble.entities.Monkey;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;

public class GhostState implements MonkeyState {

    @Override
    public void update(Monkey monkey, float delta) {
        monkey.basicUpdate(delta);
        // Add logic: now can fight ghosts
    }

    @Override
    public void render(Monkey monkey, SpriteBatch batch) {
        monkey.drawMonkey(batch, true); // true: ghost
    }

    @Override
    public void onEnter(Monkey monkey) {
        System.out.println("Monkey entered Ghost State");
    }
}
