package com.monkeyintrouble.observers;

public interface MonkeyObserver {
    void onHeartsChanged(int hearts);
    void onBananasChanged(int bananas);
    void onGhostModeChanged(boolean isGhostMode);
    void onGameWon();
}
